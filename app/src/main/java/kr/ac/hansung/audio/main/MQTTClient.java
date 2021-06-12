package kr.ac.hansung.audio.main;

import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kr.ac.hansung.audio.calling.CallingViewModel;
import kr.ac.hansung.audio.calling.PlayThread;
import lombok.Getter;

@Getter
public class MQTTClient implements MqttCallbackExtended {

    private static MQTTClient instance;

    /* MQTT 관련 변수 */
    private MqttClient client;
    private MqttClient client2;
    private String BROKER;
    private int qos = 0;

    private MQTTSettingData settingData = MQTTSettingData.getInstance();
    private MqttConnectOptions connectOptions;

    /* TOPIC */
    private String topic;
    private String topic_audio;
    private String topic_login;
    private String topic_notifyUSer;
    private String topic_logout;

    private String userName;

    private CallingViewModel callingViewModel;

    private List<String> userList = new ArrayList<>(100);
    private List<PlayThread> playThreadList = new ArrayList<>(100);

    public static MQTTClient getInstance() {
        if(instance == null) {
            instance = new MQTTClient();
        }
        return instance;
    }

    public void init(String topic, String userName, String ip, String port, CallingViewModel callingViewModel) {
        connect(ip, port, topic, userName);

        this.topic = topic;
        this.userName = userName;

        userList.clear();
        playThreadList.clear();

        topic_audio = topic + "/audio";
        topic_login = topic + "/login";
        topic_notifyUSer = topic + "/login/notifyUser";
        topic_logout = topic + "/logout";

        this.callingViewModel = callingViewModel;
    }

    public void connect(String ip, String port, String topic, String userName) {
        try {
            BROKER = "tcp://" + ip + ":" + port;

            client = new MqttClient(BROKER, MqttClient.generateClientId(), new MemoryPersistence());
            client2 = new MqttClient(BROKER, MqttClient.generateClientId(), new MemoryPersistence());

            client.setCallback(this);

            connectOptions = new MqttConnectOptions();
            connectOptions.setUserName(userName);
            connectOptions.setMaxInflight(5000);
            connectOptions.setKeepAliveInterval(1000);
            connectOptions.setCleanSession(true);

            connectOptions.setAutomaticReconnect(true);

            client.connect(connectOptions);
            client2.connect(connectOptions);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            client.disconnect();
            client.close();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void subscribe(String topic) {
        try {
            client.subscribe(topic, this.qos);
            Log.i("MQTT", "SUB " + topic);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void subscribeAll() {
        subscribe(topic);
        subscribe(topic_audio);
        subscribe(topic_login);
        subscribe(topic_notifyUSer);
        subscribe(topic_logout);
    }

    public void publish(String topic, String msg) {
        try {
            client.publish(topic, new MqttMessage(msg.getBytes()));
            Log.i("MQTT", "PUB " + topic);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void publish(String topic, byte[] payload) {
        try {
            client.publish(topic, new MqttMessage(payload));
            Log.i("MQTT", "PUB " + topic);
        } catch (MqttException e) {
            e.printStackTrace();
            if(callingViewModel.getRecordThread().isAlive()) {
                callingViewModel.getRecordThread().interrupt();
            }
        }
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {

    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.i("Lost Connection.", String.valueOf(cause.getCause()));
        cause.printStackTrace();
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        Log.i("MQTT", "messageArrived " + topic);

        /* /roomID/login */
        if(topic.equals(topic_login)) {
            String name = new String(message.getPayload(), "UTF-8");
            userList.add(name);
            Log.i("MQTT", "userList add " + name);

            if(!userName.equals(name)) {
                client2.publish(topic_notifyUSer, new MqttMessage(userName.getBytes()));

                PlayThread playThread = new PlayThread();
                playThread.setUserName(name);
                if(callingViewModel.getPlayFlag())
                    playThread.setPlayFlag(true);
                playThread.start();

                playThreadList.add(playThread);
                Log.i("MQTT", "playThreadList add " + name);
            }
        }

        /* /roomID/notifyUser */
        if(topic.equals(topic_notifyUSer)) {
            String name = new String(message.getPayload(), "UTF-8");
            if(!containsUserList(name) && !userName.equals(name)) {
                userList.add(name);
                Log.i("MQTT", "userList add " + name);

                PlayThread playThread = new PlayThread();
                playThread.setUserName(name);
                if(callingViewModel.getPlayFlag())
                    playThread.setPlayFlag(true);
                playThread.start();

                playThreadList.add(playThread);
                Log.i("MQTT", "playThreadList add " + name); // 나오지 않음
            }
        }

        /* /roomID/audio */
        if(topic.equals(topic_audio)) {
            byte[] messageData = message.getPayload();

            byte[] nameData = Arrays.copyOfRange(messageData, 0, messageData.length - 80000);
            String sender = new String(nameData);

            if (sender.equals(userName)) return;
            byte[] audioData = Arrays.copyOfRange(messageData, nameData.length, messageData.length);
            for (PlayThread playThread: playThreadList) {
                if (playThread.getUserName().equals(sender)) {
                    if (playThread.getAudioQueue().size() >= 5) {
                        playThread.getAudioQueue().clear();
                    }
                    playThread.getAudioQueue().add(audioData);
                    break;
                }
            }
        }

        /* roomID/logout */
        if(topic.equals(topic_logout)) {
            String name = new String(message.getPayload(), "UTF-8");

            if(!userName.equals(name)) {

                for(int i=0; i<userList.size(); i++) {
                    if(userList.get(i).equals(name)) {
                        userList.remove(i);
                        Log.i("MQTT", "userList remove " + name);
                        break;
                    }
                }

                for(int i=0; i<playThreadList.size(); i++) {
                    if(playThreadList.get(i).getUserName().equals(name)) {
                        playThreadList.get(i).setPlayFlag(false);
                        playThreadList.get(i).getAudioQueue().clear();
                        playThreadList.get(i).stopPlaying();
                        playThreadList.get(i).interrupt();
                        Log.i("MQTT", "before playThreadList remove " + name + "size(" + playThreadList.size() + ")");
                        playThreadList.remove(i);
                        Log.i("MQTT", "after playThreadList remove " + name + "size(" + playThreadList.size() + ")");
                    }
                }
            }
        }

        /* Other Topic */
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    private boolean containsUserList(String name) {
        for (String user : userList) {
            if (user.equals(name)) {
                return true;
            }
        }

        return false;
    }
}

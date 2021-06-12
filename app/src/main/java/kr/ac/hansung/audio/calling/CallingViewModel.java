package kr.ac.hansung.audio.calling;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import kr.ac.hansung.audio.main.MQTTClient;
import kr.ac.hansung.audio.main.MQTTSettingData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CallingViewModel extends ViewModel {

    /* MQTT 관련 변수 */
    private MQTTClient client = MQTTClient.getInstance();
    private MQTTSettingData settingData = MQTTSettingData.getInstance();
    private String ip;
    private String port;
    private String topic;
    private String userName;

    /* Audio 관련 변수 */
    private Boolean recordFlag = false;
    private Boolean playFlag = true;
    private RecordThread recordThread;

    public CallingViewModel() {
        this.ip = settingData.getIp();
        this.port = settingData.getPort();
        this.topic = settingData.getTopic();
        this.userName = settingData.getUserName();

        client.init(topic, userName, ip, port, this);
        client.subscribeAll();

        recordThread = new RecordThread();
        recordThread.start();

        client.publish(client.getTopic_login(), userName);
    }

    public Boolean clickMic() {
        // recordFlag == false
        if(!recordFlag) {
            recordFlag = true;
            synchronized (recordThread.getAudioRecord()) {
                recordThread.getAudioRecord().notify();
                Log.i("Audio", "Mic On");
            }

            return true;
        }

        // recordFlag == true
        recordFlag = false;
        recordThread.setRecordFlag(recordFlag);
        Log.i("Audio", "Mic Off");

        return false;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }
}

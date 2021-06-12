package kr.ac.hansung.audio.calling;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import kr.ac.hansung.audio.main.MQTTClient;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecordThread extends Thread {

    /* AudioRecord Setting 관련 변수 */
    private int audioSource = MediaRecorder.AudioSource.VOICE_COMMUNICATION;
    private int sampleRate = 16000;
    private int channelCount = AudioFormat.CHANNEL_IN_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int bufferSize = sampleRate*5;
    // todo: bufferSize 고정 및 MQTTClient의 messageArrived(topic_audio)부분 수정, PlayThread의 bufferSize 수정

    private AudioRecord audioRecord;
    private boolean recordFlag = false;
    private byte[] audioData;

    private MQTTClient mqttClient = MQTTClient.getInstance();

    private byte[] nameData = mqttClient.getUserName().getBytes();

    @Override
    public void run() {
        audioData = new byte[bufferSize];

        audioRecord = new AudioRecord.Builder()
                .setAudioSource(audioSource)
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(audioFormat)
                        .setSampleRate(sampleRate)
                        .setChannelMask(channelCount)
                        .build())
                .setBufferSizeInBytes(bufferSize)
                .build();

        audioRecord.startRecording();
        Log.i("Audio", "Start Recording");

        try {
            while(true) {
                // recordFlag == false
                if(!recordFlag) {
                    synchronized (audioRecord) {
                        audioRecord.wait();
                    }
                    recordFlag = true;
                }

                // recordFlag == true
                audioRecord.read(audioData, 0 , bufferSize);

                publishAudioData(audioData);
            }
        } catch (InterruptedException e) {
            Log.i("Audio", "Audio Thread Dead...");
        }
    }

    /* Audio 리소스 해제 */
    public void stopRecording() {
        audioRecord.stop();
        audioRecord.release();
        audioRecord = null;
        Log.i("Audio", "Stop Recording");
    }

    /* nameData + readData */
    public void publishAudioData(byte[] audioData) {
        byte[] messageData = new byte[nameData.length + audioData.length];

        System.arraycopy(nameData, 0, messageData, 0, nameData.length);
        System.arraycopy(audioData, 0, messageData, nameData.length, audioData.length);

         mqttClient.publish(mqttClient.getTopic_audio(), messageData);
    }
}
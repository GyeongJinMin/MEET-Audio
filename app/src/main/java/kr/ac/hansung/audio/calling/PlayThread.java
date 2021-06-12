package kr.ac.hansung.audio.calling;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlayThread extends Thread {

    /* AudioTrack Setting 관련 변수 */
    private int sampleRate = 16000;
    private int channelCount = AudioFormat.CHANNEL_OUT_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int bufferSize = sampleRate * 5;

    private AudioTrack audioTrack;
    private boolean playFlag = true;

    private String userName;

    /* Audio Queue */
    private List<byte[]> audioQueue = Collections.synchronizedList(new ArrayList<>(5));

    @Override
    public void run() {
        super.run();

        audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, sampleRate, channelCount, audioFormat, bufferSize, AudioTrack.MODE_STREAM); // streaming mode => blocking 함수

//        audioTrack = new AudioTrack.Builder()
//                .setAudioAttributes(new AudioAttributes.Builder()
//                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
//                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
//                        .build())
//                .setAudioFormat(new AudioFormat.Builder()
//                        .setEncoding(audioFormat)
//                        .setSampleRate(sampleRate)
//                        .setChannelMask(channelCount)
//                        .build())
//                .setBufferSizeInBytes(bufferSize)
//                .build();


        audioTrack.play();
        Log.i("Audio", "Start Playing");

        while (true) {
            synchronized (audioQueue) {
                if (audioQueue.size() > 0) {
                    Log.i("Audio", "AudioTrack write");
                    audioTrack.write(audioQueue.get(0), 0, bufferSize);
                    audioQueue.remove(0);
                }
            }
        }
    }

    /* AudioTrack 리소스 해제 */
    public void stopPlaying() {
        audioTrack.stop();
        audioTrack.release();
        audioTrack = null;
        Log.i("Audio", "Stop Playing");
    }
}
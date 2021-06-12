package kr.ac.hansung.audio.calling;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import org.eclipse.paho.client.mqttv3.MqttException;

import kr.ac.hansung.audio.databinding.FragmentCallingBinding;
import kr.ac.hansung.audio.main.MQTTClient;

public class CallingFragment extends Fragment {

    private FragmentCallingBinding binding = null;

    private MQTTClient client = MQTTClient.getInstance();

    private CallingViewModel callingViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCallingBinding.inflate(inflater, container, false);

        callingViewModel = new ViewModelProvider(this).get(CallingViewModel.class);

        binding.btnMic.setOnClickListener(v -> {
            if (callingViewModel.clickMic()) {
                binding.btnMic.setText("mic true");
            } else {
                binding.btnMic.setText("mic false");
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        client.getUserList().clear();
        client.getConnectOptions().setAutomaticReconnect(false);

        /* Audio 관련 처리 */
        /* RecordThread interrupt */
        if(callingViewModel.getRecordFlag()) {
            callingViewModel.getRecordThread().setRecordFlag(false);
        }

        callingViewModel.getRecordThread().stopRecording();
        callingViewModel.getRecordThread().interrupt();

        /* topic_audio unsubscribe */
        if(client.getClient().isConnected()) {
            try {
                client.getClient().unsubscribe(client.getTopic_audio());
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

        /* PlayThreadList의 모든 PlayThread interrupt 및 PlayThreadList 초기화*/
        for(PlayThread playThread : client.getPlayThreadList()) {
            if(callingViewModel.getPlayFlag()) {
                playThread.setPlayFlag(false);
            }

            playThread.stopPlaying();
            synchronized (playThread.getAudioQueue()) {
                playThread.getAudioQueue().clear();
            }
            playThread.interrupt();
        }

        client.getPlayThreadList().clear();

        /* MQTTClient 연결 해제 */
        client.disconnect();
    }
}
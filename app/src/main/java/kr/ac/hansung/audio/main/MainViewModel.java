package kr.ac.hansung.audio.main;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {

    /* MQTT 관련 변수 */
    private MQTTSettingData settingData = MQTTSettingData.getInstance();

    private MutableLiveData<String> ip = new MutableLiveData<>();
    private MutableLiveData<String> port = new MutableLiveData<>();
    private MutableLiveData<String> topic = new MutableLiveData<>();
    private MutableLiveData<String> userName = new MutableLiveData<>();

    public MainViewModel() {
        /* 변수 초기화 */
        ip.setValue("192.168.123.109"); // 경진
        port.setValue("1883");
        topic.setValue("");
        userName.setValue("");
    }

    public void clickSubmit() {
        settingData.setIp(ip.getValue());
        settingData.setPort(port.getValue());
        settingData.setTopic(topic.getValue());
        settingData.setUserName(userName.getValue());
    }

    /* Setter */
    public void setTopic(String topic) {
        this.topic.setValue(topic);
    }

    public void setName(String name) {
        this.userName.setValue(name);
    }
}

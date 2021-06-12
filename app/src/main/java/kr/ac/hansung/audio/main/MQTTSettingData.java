package kr.ac.hansung.audio.main;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MQTTSettingData {

    private static final MQTTSettingData data = new MQTTSettingData();

    private String ip;
    private String port;
    private String topic;
    private String userName;

    public static MQTTSettingData getInstance() {
        return data;
    }
}

package io.agora.auikit.model;

import androidx.annotation.NonNull;

public class AUIRoomConfig {

    @NonNull public String channelName = "";     //正常rtm使用的频道
    @NonNull public String rtmToken = "";     //rtm login用，只能007
    @NonNull public String rtcToken = "";     //rtm join用

    @NonNull public String rtcChannelName = "";  //rtc使用的频道
    @NonNull public String rtcRtcToken = "";  //rtc join使用
    @NonNull public String rtcRtmToken = "";  //rtc mcc使用，只能006
    @NonNull public String rtcChorusChannelName = "";  //rtc 合唱使用的频道
    @NonNull public String rtcChorusRtcToken = "";  //rtc 合唱join使用

    public AUIRoomConfig(@NonNull String roomId) {
        channelName = roomId;
        rtcChannelName = roomId + "_rtc";
        rtcChorusChannelName = roomId + "_rtc_ex";
    }
}

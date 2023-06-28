package io.agora.auikit.model;

import android.view.View;

import androidx.annotation.NonNull;

public class AUIRoomConfig {

    @NonNull public String channelName = "";     //正常rtm使用的频道
    @NonNull public String rtmToken007 = "";     //rtm login用，只能007
    @NonNull public String rtcToken007 = "";     //rtm join用

    @NonNull public String rtcChannelName = "";  //rtc使用的频道
    @NonNull public String rtcRtcToken006 = "";  //rtc join使用
    @NonNull public String rtcRtmToken006 = "";  //rtc mcc使用，只能006
    @NonNull public String rtcChorusChannelName = "";  //rtc 合唱使用的频道
    @NonNull public String rtcChorusRtcToken007 = "";  //rtc 合唱join使用

    public int themeId = View.NO_ID;

    public AUIRoomConfig(String roomId) {
        channelName = roomId;
        rtcChannelName = roomId + "_rtc";
        rtcChorusChannelName = roomId + "_rtc_ex";
    }
}

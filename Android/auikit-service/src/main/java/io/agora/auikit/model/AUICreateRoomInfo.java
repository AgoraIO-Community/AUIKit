package io.agora.auikit.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;

public class AUICreateRoomInfo implements Serializable {
    public @NonNull String roomId = "";       //房间Id
    public @NonNull String roomName = "";       //房间名称
    public @NonNull String thumbnail = "";      //房间列表上的缩略图
    public int micSeatCount = 8;                   //麦位个数
    public @Nullable String password;           //房间密码
    public String micSeatStyle = "";            //麦位样式
}

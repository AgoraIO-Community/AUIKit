package io.agora.auikit.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;

public class AUiRoomInfo extends AUiCreateRoomInfo implements Serializable {
    public @NonNull String roomId = ""; // 房间id
    public @Nullable AUiUserThumbnailInfo roomOwner; // 房主信息
    public int onlineUsers = 0; // 房间人数

    // 房间创建时间
    public long createTime = 0;
}

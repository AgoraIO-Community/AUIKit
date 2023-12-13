package io.agora.auikit.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AUIRoomInfo extends AUICreateRoomInfo implements Serializable {
    @SerializedName("roomSeatCount")
    public int micSeatCount = 8; // 麦位个数

    public @NonNull String roomId = ""; // 房间id

    @SerializedName("roomOwner")
    public @Nullable AUIUserThumbnailInfo owner; // 房主信息
    @SerializedName("onlineUsers")
    public int memberCount = 0; // 房间人数
    public long createTime = 0; // 房间创建时间
}

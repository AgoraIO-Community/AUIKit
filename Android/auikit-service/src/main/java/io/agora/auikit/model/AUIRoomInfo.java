package io.agora.auikit.model;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AUIRoomInfo extends AUICreateRoomInfo implements Serializable {

    @SerializedName("roomOwner")
    public @Nullable AUIUserThumbnailInfo owner; // 房主信息
    @SerializedName("onlineUsers")
    public int memberCount = 0; // 房间人数
    public long createTime = 0; // 房间创建时间

}

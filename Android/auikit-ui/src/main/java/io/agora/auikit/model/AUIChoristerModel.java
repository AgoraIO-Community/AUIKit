package io.agora.auikit.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class AUIChoristerModel {
    @SerializedName("userId")
    public @NonNull String userId = "";

    @SerializedName("chorusSongNo")
    public @NonNull String chorusSongNo = "";    //合唱者演唱歌曲

    @SerializedName("owner")
    public @Nullable AUIUserThumbnailInfo owner; //合唱者信息
}

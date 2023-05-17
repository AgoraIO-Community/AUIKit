package io.agora.auikit.model;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class AUiMicSeatInfo {

    @SerializedName("owner")
    public @Nullable AUiUserThumbnailInfo user;

    /** 麦位索引，可以不需要，根据麦位list可以计算出 */
    @SerializedName("micSeatNo")
    public int seatIndex = 0;
    /** 麦位状态 */
    @SerializedName("micSeatStatus")
    public @AUiMicSeatStatus int seatStatus = AUiMicSeatStatus.idle;
    /** 麦位禁用声音 */
    @SerializedName("isMuteAudio")
    public int muteAudio = 0;

    /** 麦位禁用视频 */
    @SerializedName("isMuteVideo")
    public int muteVideo = 0;
}

package io.agora.auikit.service;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import io.agora.auikit.model.AUIMicSeatInfo;
import io.agora.auikit.model.AUIUserThumbnailInfo;
import io.agora.auikit.service.callback.AUICallback;

/**
 * 麦位Service抽象协议，一个房间对应一个MicSeatService
 */
public interface IAUIMicSeatService extends IAUICommonService<IAUIMicSeatService.AUIMicSeatRespObserver> {

    /**
     * 主动上麦（听众端和房主均可调用）
     *
     * @param seatIndex 麦位位置
     * @param callback  成功/失败回调
     */
    void enterSeat(int seatIndex, @Nullable AUICallback callback);

    /**
     * 主动上麦, 获取一个小麦位进行上麦（听众端和房主均可调用）
     *
     * @param callback  成功/失败回调
     */
    void autoEnterSeat(@Nullable AUICallback callback);

    /**
     * 主动下麦（主播调用）
     *
     * @param callback 成功/失败回调
     */
    void leaveSeat(@Nullable AUICallback callback);

    /**
     * 抱人上麦（房主调用）
     *
     * @param seatIndex 麦位位置
     * @param userId    用户id
     * @param callback  成功/失败回调
     */
    void pickSeat(int seatIndex, @NonNull String userId, @Nullable AUICallback callback);

    /**
     * 踢人下麦（房主调用）
     *
     * @param seatIndex 麦位位置
     * @param callback  成功/失败回调
     */
    void kickSeat(int seatIndex, @Nullable AUICallback callback);

    /**
     * 静音/解除静音某个麦位（房主调用）
     *
     * @param seatIndex 麦位位置
     * @param isMute    是否静音
     * @param callback  成功/失败回调
     */
    void muteAudioSeat(int seatIndex, boolean isMute, @Nullable AUICallback callback);

    /**
     * 关闭/打开麦位摄像头
     *
     * @param seatIndex 麦位位置
     * @param isMute    是否关闭摄像头
     * @param callback  成功/失败回调
     */
    void muteVideoSeat(int seatIndex, boolean isMute, @Nullable AUICallback callback);

    /**
     * 封禁/解禁某个麦位（房主调用）
     *
     * @param seatIndex 麦位位置
     * @param isClose   是否封禁
     * @param callback  成功/失败回调
     */
    void closeSeat(int seatIndex, boolean isClose, @Nullable AUICallback callback);

    /**
     * 获取指定麦位信息
     *
     * @return 麦位信息
     */
    @Nullable
    AUIMicSeatInfo getMicSeatInfo(int seatIndex);

    int getMicSeatSize();

    /**
     * 点击邀请
     * @param index 麦位号
     */
    void onClickInvited(int index);

    int getMicSeatIndex(@NotNull String userId);

    interface AUIMicSeatRespObserver {

        /**
         * 有成员上麦（主动上麦/房主抱人上麦）
         *
         * @param seatIndex 麦位位置
         * @param userInfo  麦位上用户信息
         */
        default void onAnchorEnterSeat(int seatIndex, @NonNull AUIUserThumbnailInfo userInfo){}


        /**
         * 有成员下麦（主动下麦/房主踢人下麦）
         *
         * @param seatIndex 麦位位置
         * @param userInfo  麦位上用户信息
         */
        default void onAnchorLeaveSeat(int seatIndex, @NonNull AUIUserThumbnailInfo userInfo) {
        }

        /**
         * 房主禁麦
         *
         * @param seatIndex 麦位位置
         * @param isMute    是否静音
         */
        default void onSeatAudioMute(int seatIndex, boolean isMute) {
        }

        /**
         * 房主禁摄像头
         *
         * @param seatIndex 麦位位置
         * @param isMute    是否禁摄像头
         */
        default void onSeatVideoMute(int seatIndex, boolean isMute) {
        }

        /**
         * 房主封麦
         *
         * @param seatIndex 麦位位置
         * @param isClose   是否封麦
         */
        default void onSeatClose(int seatIndex, boolean isClose) {
        }

        /**
         * 显示邀请dialog
         * @param index
         */
        default void onShowInvited(int index){}
    }
}

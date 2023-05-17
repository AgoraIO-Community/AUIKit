package io.agora.auikit.service;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import io.agora.auikit.model.AUiMicSeatInfo;
import io.agora.auikit.model.AUiUserThumbnailInfo;
import io.agora.auikit.service.callback.AUiCallback;

/**
 * 麦位Service抽象协议，一个房间对应一个MicSeatService
 */
public interface IAUiMicSeatService extends IAUiCommonService<IAUiMicSeatService.AUiMicSeatRespDelegate>{

    /**
     * 主动上麦（听众端和房主均可调用）
     *
     * @param seatIndex 麦位位置
     * @param callback  成功/失败回调
     */
    void enterSeat(int seatIndex, @Nullable AUiCallback callback);

    /**
     * 主动上麦, 获取一个小麦位进行上麦（听众端和房主均可调用）
     *
     * @param callback  成功/失败回调
     */
    void autoEnterSeat(@Nullable AUiCallback callback);

    /**
     * 主动下麦（主播调用）
     *
     * @param callback 成功/失败回调
     */
    void leaveSeat(@Nullable AUiCallback callback);

    /**
     * 抱人上麦（房主调用）
     *
     * @param seatIndex 麦位位置
     * @param userId    用户id
     * @param callback  成功/失败回调
     */
    void pickSeat(int seatIndex, @NonNull String userId, @Nullable AUiCallback callback);

    /**
     * 踢人下麦（房主调用）
     *
     * @param seatIndex 麦位位置
     * @param callback  成功/失败回调
     */
    void kickSeat(int seatIndex, @Nullable AUiCallback callback);

    /**
     * 静音/解除静音某个麦位（房主调用）
     *
     * @param seatIndex 麦位位置
     * @param isMute    是否静音
     * @param callback  成功/失败回调
     */
    void muteAudioSeat(int seatIndex, boolean isMute, @Nullable AUiCallback callback);

    /**
     * 关闭/打开麦位摄像头
     *
     * @param seatIndex 麦位位置
     * @param isMute    是否关闭摄像头
     * @param callback  成功/失败回调
     */
    void muteVideoSeat(int seatIndex, boolean isMute, @Nullable AUiCallback callback);

    /**
     * 封禁/解禁某个麦位（房主调用）
     *
     * @param seatIndex 麦位位置
     * @param isClose   是否封禁
     * @param callback  成功/失败回调
     */
    void closeSeat(int seatIndex, boolean isClose, @Nullable AUiCallback callback);

    /**
     * 获取指定麦位信息
     *
     * @return 麦位信息
     */
    @Nullable
    AUiMicSeatInfo getMicSeatInfo(int seatIndex);

    interface AUiMicSeatRespDelegate {

        /**
         * 全量的麦位列表变化
         *
         * @param seatInfoList 麦位列表
         */
        default void onSeatListChange(List<AUiMicSeatInfo> seatInfoList){}

        /**
         * 有成员上麦（主动上麦/房主抱人上麦）
         *
         * @param seatIndex 麦位位置
         * @param userInfo  麦位上用户信息
         */
        default void onAnchorEnterSeat(int seatIndex, @NonNull AUiUserThumbnailInfo userInfo){}


        /**
         * 有成员下麦（主动下麦/房主踢人下麦）
         *
         * @param seatIndex 麦位位置
         * @param userInfo  麦位上用户信息
         */
        default void onAnchorLeaveSeat(int seatIndex, @NonNull AUiUserThumbnailInfo userInfo) {
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
    }
}

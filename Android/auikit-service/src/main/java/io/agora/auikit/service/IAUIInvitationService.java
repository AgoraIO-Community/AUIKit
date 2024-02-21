package io.agora.auikit.service;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.agora.auikit.model.AUIUserInfo;
import io.agora.auikit.service.callback.AUICallback;
import io.agora.auikit.service.callback.AUIException;

public interface IAUIInvitationService extends IAUICommonService<IAUIInvitationService.AUIInvitationRespObserver> {
    /**
     * 向用户发送邀请
     * @param userId 邀请用户id
     * @param seatIndex 麦位号
     * @param callback 成功/失败回调
     */
    void sendInvitation(@NonNull String userId, int seatIndex, @Nullable AUICallback callback);

    /**
     * 接受邀请
     * @param userId 邀请id
     * @param callback 成功/失败回调
     */
    void acceptInvitation(@NonNull String userId,int seatIndex, @Nullable AUICallback callback);

    /**
     * 拒绝邀请
     * @param userId 邀请id
     * @param callback 成功/失败回调
     */
    void rejectInvitation(@NonNull String userId, @Nullable AUICallback callback);

    /**
     * 取消邀请
     * @param userId 邀请id
     * @param callback 成功/失败回调
     */
    void cancelInvitation(@NonNull String userId, @Nullable AUICallback callback);

    /**
     * 发送申请
     * @param seatIndex 麦位号
     * @param callback 成功/失败回调
     */
    void sendApply(int seatIndex,@NonNull AUICallback callback);

    /**
     * 取消申请
     * @param callback 成功/失败回调
     */
    void cancelApply(@NonNull AUICallback callback);

    /**
     * 接受申请(房主同意)
     * @param userId
     * @param seatIndex
     * @param callback
     */
    void acceptApply(@NonNull String userId,int seatIndex,@NonNull AUICallback callback);

    /**
     * 拒绝申请(房主拒绝)
     * @param userId
     * @param callback
     */
    void rejectApply(@NonNull String userId,@NonNull AUICallback callback);

    interface AUIInvitationRespObserver {

        /**
         * 被邀请者接受邀请
         *
         * @param userId 被邀请者id
         */
        default void onInviteeAccepted(@NonNull String userId, int seatIndex){}

        /**
         * 被邀请者拒绝邀请
         *
         * @param userId 被邀请者id
         */
        default void onInviteeRejected(@NonNull String userId){}

        /**
         * 邀请人取消邀请
         * @param userId 取消邀请者id
         */
        default void onInvitationCancelled(@NonNull String userId){}

        /**
         * 收到新的申请信息
         * @param userId
         * @param seatIndex
         */
        default void onReceiveNewApply(@NonNull String userId, int seatIndex){}

        /**
         * 房主接受申请
         * @param userId 申请者id
         */
        default void onApplyAccepted(@NonNull String userId, int seatIndex){}

        /**
         * 房主拒接申请
         * @param userId 申请者id
         */
        default void onApplyRejected(@NonNull String userId){}

        /**
         * 取消申请
         * @param userId 申请者id
         */
        default void onApplyCanceled(@NonNull String userId){}

        /**
         * 收到上麦邀请
         * @param userId
         * @param micIndex
         */
        default void onReceiveInvitation(String userId,int micIndex){}

        /**
         * 申请列表变更
         * @param userList
         */
        default void onApplyListUpdate(List<AUIUserInfo> userList){}

        default @Nullable AUIException onInviteWillAccept(@NotNull String userId, int seatIndex) {
            return null;
        }

        default @Nullable AUIException onApplyWillAccept(@NotNull String userId, int seatIndex) {
            return null;
        }
    }
}

package io.agora.auikit.service;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.agora.auikit.service.callback.AUICallback;

public interface IAUIInvitationService extends IAUICommonService<IAUIInvitationService.AUIInvitationRespDelegate> {

    /**
     * 向用户发送邀请
     * @param cmd 邀请cmd
     * @param userId 邀请用户id
     * @param content 邀请cmd对应内容
     * @param callback 成功/失败回调
     */
    void sendInvitation(@Nullable String cmd, @NonNull String userId, @Nullable String content, @Nullable AUICallback callback);

    /**
     * 接受邀请
     * @param id 邀请id
     * @param callback 成功/失败回调
     */
    void acceptInvitation(@NonNull String id, @Nullable AUICallback callback);

    /**
     * 拒绝邀请
     * @param id 邀请id
     * @param callback 成功/失败回调
     */
    void rejectInvitation(@NonNull String id, @Nullable AUICallback callback);

    /**
     * 取消邀请
     * @param id 邀请id
     * @param callback 成功/失败回调
     */
    void cancelInvitation(@NonNull String id, @Nullable AUICallback callback);

    interface AUIInvitationRespDelegate {

        /**
         * 收到新的邀请请求
         *
         * @param id 邀请id
         * @param inviter 邀请者
         * @param cmd 邀请cmd
         * @param content 邀请内容
         */
        default void onReceiveNewInvitation(@NonNull String id, @NonNull String inviter, @Nullable String cmd, @Nullable String content){}

        /**
         * 被邀请者接受邀请
         *
         * @param id 邀请id
         * @param inviteeId 被邀请者
         */
        default void onInviteeAccepted(@NonNull String id, @NonNull String inviteeId){}

        /**
         * 被邀请者拒绝邀请
         *
         * @param id 邀请id
         * @param inviteeId 被邀请者
         */
        default void onInviteeRejected(@NonNull String id, @NonNull String inviteeId){}

        /**
         * 邀请人取消邀请
         * @param id 邀请id
         * @param inviteeId 取消邀请者
         */
        default void onInvitationCancelled(@NonNull String id, @NonNull String inviteeId){}
    }
}

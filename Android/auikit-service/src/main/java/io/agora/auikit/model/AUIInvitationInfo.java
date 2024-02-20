package io.agora.auikit.model;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class AUIInvitationInfo {

    private static final long kInvitationTimeoutTs = 10 * 1000;
    private static final long kInvitationInvalidTs = 20 * 1000;

    @IntDef({
            AUIInvitationType.Apply,
            AUIInvitationType.Invite,
    })
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface AUIInvitationType{
        int Apply = 1; // 观众申请
        int Invite = 2; // 主播邀请
    }

    @IntDef({
            AUIInvitationStatus.Waiting,
            AUIInvitationStatus.Accept,
            AUIInvitationStatus.Reject,
            AUIInvitationStatus.Timeout,
            AUIInvitationStatus.Cancel,
    })
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface AUIInvitationStatus{
        int Waiting = 1; // 等待确认
        int Accept = 2; // 同意
        int Reject = 3; // 拒绝
        int Timeout = 4; // 超时

        int Cancel = 5; // 取消
    }

    // 申请观众userId，被邀请观众userId
    public @NonNull String userId = "";

    // 麦位位置
    public int seatNo = 0;

    // 类型，申请 or 邀请
    public @AUIInvitationType int type = AUIInvitationType.Apply;

    public @AUIInvitationStatus int status = AUIInvitationStatus.Waiting;

    public long createTime = System.currentTimeMillis();

    public long editTime = System.currentTimeMillis();

    public long timeoutTs = kInvitationTimeoutTs;

    public long invalidTs = kInvitationInvalidTs;
}

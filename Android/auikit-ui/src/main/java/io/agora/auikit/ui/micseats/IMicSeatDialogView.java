package io.agora.auikit.ui.micseats;


public interface IMicSeatDialogView {

    /**
     * 添加静音/取消静音按钮
     *
     * @param isMute 是否禁音
     */
    void addMuteAudio(boolean isMute);

    /**
     * 添加禁视频/取消禁视频按钮
     *
     * @param isMute 是否禁视频
     */
    void addMuteVideo(boolean isMute);

    /**
     * 添加封麦/取消封麦按钮
     *
     * @param isClosed 是否封麦
     */
    void addCloseSeat(boolean isClosed);

    /**
     * 添加踢人按钮
     *
     */
    void addKickSeat();

    /**
     * 添加下麦按钮
     */
    void addLeaveSeat();

    /**
     * 添加上麦按钮
     */
    void addEnterSeat(boolean isShow);

    /**
     * 设置显示用户信息
     *
     * @param userInfo 用户信息
     */
    void setUserInfo(String userInfo);

    void setUserName(String userName);

    void setUserAvatar(String avatarUrl);

    /**
     * 添加邀请
     */
    void addInvite(boolean isShow);

}

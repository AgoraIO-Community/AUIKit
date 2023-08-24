package io.agora.app.sample.ht.micseats;

import android.graphics.drawable.Drawable;

/**
 * 麦位View
 */
public interface IMicSeatItemView {
    /**
     * 设置是否显示房主标志
     *
     * @param visible {@link android.view.View#VISIBLE} {@link android.view.View#INVISIBLE} {@link android.view.View#GONE}
     */
    void setRoomOwnerVisibility(int visible);

    /**
     * 设置麦位标题
     *
     * @param text 标题
     */
    void setTitleText(String text);

    /**
     * 设置麦位坐标
     *
     * @param index 麦位坐标
     */
    void setIndex(int index);

    /**
     * 获取麦位坐标
     *
     * @return 麦位坐标
     */
    int getIndex();

    /**
     * 设置是否显示静音图标
     *
     * @param visible {@link android.view.View#VISIBLE} {@link android.view.View#INVISIBLE} {@link android.view.View#GONE}
     */
    void setAudioMuteVisibility(int visible);

    /**
     * 设置是否显示禁视频图标
     *
     * @param visible {@link android.view.View#VISIBLE} {@link android.view.View#INVISIBLE} {@link android.view.View#GONE}
     */
    void setVideoMuteVisibility(int visible);

    /**
     * 设置麦位头像
     *
     * @param drawable 头像图片
     */
    void setUserAvatarImageDrawable(Drawable drawable);

    /**
     * 设置麦位状态
     *
     * @param state 麦位状态
     */
    void setMicSeatState(MicSeatState state);

    /**
     * 获取当前麦位状态
     *
     * @return 麦位状态
     */
    MicSeatState getMicSeatState();

    /**
     * 设置麦位头像
     *
     * @param url 头像url
     */
    void setUserAvatarImageUrl(String url);

    /**
     * 设置合唱时麦序类型
     *
     * @param type 类型
     */
    void setChorusMicOwnerType(ChorusType type);

    /**
     * 开启水波纹动画
     */
    void startRippleAnimation();

    /**
     * 结束水波纹动画
     */
    void stopRippleAnimation();

    /**
     * 设置水波幅度
     *
     * @param value 水波幅度
     */
    void setRippleInterpolator(float value);

    /**
     * 合唱相关状态
     */
    enum ChorusType {
        None, // 没参加合唱
        LeadSinger, // 主唱
        SecondarySinger // 副唱
    }

    /**
     * 麦位状态
     */
    enum MicSeatState {
        idle, // 空闲
        used, // 使用中
        locked // 锁定
    }
}

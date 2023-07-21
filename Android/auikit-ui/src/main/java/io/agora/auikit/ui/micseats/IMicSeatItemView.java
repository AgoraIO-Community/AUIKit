package io.agora.auikit.ui.micseats;

import android.graphics.drawable.Drawable;

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
     * 设置麦位空闲时的位置
     *
     * @param index 麦位空闲时的位置
     */
    void setTitleIndex(int index);
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
    void setMicSeatState(int state);

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

    enum ChorusType {
        None, // 没参加合唱
        LeadSinger, // 主唱
        SecondarySinger // 副唱
    }
}

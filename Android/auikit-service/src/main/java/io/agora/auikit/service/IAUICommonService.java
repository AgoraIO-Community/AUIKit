package io.agora.auikit.service;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.agora.auikit.model.AUIRoomContext;

public interface IAUICommonService<Delegate> {

    /**
     * 绑定响应事件回调，可绑定多个
     *
     * @param delegate 响应事件回调
     */
    void bindRespDelegate(@Nullable Delegate delegate);

    /**
     * 解绑响应事件回调
     *
     * @param delegate 响应事件回调
     */
    void unbindRespDelegate(@Nullable Delegate delegate);

    /** 获取当前房间上下文
     *
     * @return
     */
    default @NonNull AUIRoomContext getRoomContext() { return AUIRoomContext.shared(); }

    @NonNull String getChannelName();

}

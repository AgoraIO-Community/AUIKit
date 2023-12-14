package io.agora.auikit.service;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.agora.auikit.model.AUIRoomContext;
import io.agora.auikit.service.callback.AUICallback;

public interface IAUICommonService<Observer> {

    /**
     * 绑定响应事件回调，可绑定多个
     *
     * @param observer 响应事件回调
     */
    void registerRespObserver(@Nullable Observer observer);

    /**
     * 解绑响应事件回调
     *
     * @param observer 响应事件回调
     */
    void unRegisterRespObserver(@Nullable Observer observer);

    default void deInitService(AUICallback completion) {}

    default void initService(AUICallback completion) {}


    /** 获取当前房间上下文
     *
     * @return
     */
    default @NonNull AUIRoomContext getRoomContext() { return AUIRoomContext.shared(); }

    @NonNull String getChannelName();

}

package io.agora.auikit.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AUIRoomContext {
    private static AUIRoomContext instance = null;
    private AUIRoomContext() {
        // 私有构造函数
    }
    public static synchronized AUIRoomContext shared() {
        if (instance == null) {
            instance = new AUIRoomContext();
        }
        return instance;
    }

    public AUIRoomConfig roomConfig = null;

    public @NonNull AUIUserThumbnailInfo currentUserInfo = new AUIUserThumbnailInfo();
    private AUICommonConfig mCommonConfig = new AUICommonConfig();
    private final Map<String, AUIRoomInfo> roomInfoMap = new HashMap<>();

    public void setCommonConfig(@NonNull AUICommonConfig config) {
        mCommonConfig = config;
        currentUserInfo.userId = config.userId;
        currentUserInfo.userName = config.userName;
        currentUserInfo.userAvatar = config.userAvatar;
    }

    public @NonNull AUICommonConfig getCommonConfig() {
        return mCommonConfig;
    }

    public boolean isRoomOwner(String channelName){
        AUIRoomInfo roomInfo = roomInfoMap.get(channelName);
        if(roomInfo == null || roomInfo.roomOwner == null){
            return false;
        }
        return roomInfo.roomOwner.userId.equals(currentUserInfo.userId);
    }

    public void resetRoomMap(@Nullable List<AUIRoomInfo> roomInfoList) {
        roomInfoMap.clear();
        if (roomInfoList == null || roomInfoList.size() == 0) {
            return;
        }
        for (AUIRoomInfo info : roomInfoList) {
            roomInfoMap.put(info.roomId, info);
        }
    }

    public void insertRoomInfo(AUIRoomInfo info) {
        roomInfoMap.put(info.roomId, info);
    }

    public void cleanRoom(String channelName){
        roomInfoMap.remove(channelName);
    }

}

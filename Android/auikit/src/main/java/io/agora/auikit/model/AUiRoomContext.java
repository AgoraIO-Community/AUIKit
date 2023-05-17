package io.agora.auikit.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AUiRoomContext {
    private static AUiRoomContext instance = null;
    private AUiRoomContext() {
        // 私有构造函数
    }
    public static synchronized AUiRoomContext shared() {
        if (instance == null) {
            instance = new AUiRoomContext();
        }
        return instance;
    }

    public AUiRoomConfig roomConfig = null;

    public @NonNull AUiUserThumbnailInfo currentUserInfo = new AUiUserThumbnailInfo();
    private AUiCommonConfig mCommonConfig = new AUiCommonConfig();
    private final Map<String, AUiRoomInfo> roomInfoMap = new HashMap<>();

    public void setCommonConfig(@NonNull AUiCommonConfig config) {
        mCommonConfig = config;
        currentUserInfo.userId = config.userId;
        currentUserInfo.userName = config.userName;
        currentUserInfo.userAvatar = config.userAvatar;
    }

    public @NonNull AUiCommonConfig getCommonConfig() {
        return mCommonConfig;
    }

    public boolean isRoomOwner(String channelName){
        AUiRoomInfo roomInfo = roomInfoMap.get(channelName);
        if(roomInfo == null || roomInfo.roomOwner == null){
            return false;
        }
        return roomInfo.roomOwner.userId.equals(currentUserInfo.userId);
    }

    public void resetRoomMap(@Nullable List<AUiRoomInfo> roomInfoList) {
        roomInfoMap.clear();
        if (roomInfoList == null || roomInfoList.size() == 0) {
            return;
        }
        for (AUiRoomInfo info : roomInfoList) {
            roomInfoMap.put(info.roomId, info);
        }
    }

    public void insertRoomInfo(AUiRoomInfo info) {
        roomInfoMap.put(info.roomId, info);
    }

    public void cleanRoom(String channelName){
        roomInfoMap.remove(channelName);
    }

}

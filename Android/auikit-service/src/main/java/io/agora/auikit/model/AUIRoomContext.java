package io.agora.auikit.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AUIRoomContext {
    private static volatile AUIRoomContext instance = null;
    private AUIRoomContext() {
        // 私有构造函数
    }
    public static AUIRoomContext shared() {
        if (instance == null) {
            synchronized (AUIRoomContext.class){
                if(instance == null){
                    instance = new AUIRoomContext();
                }
            }
        }
        return instance;
    }

    public Map<String, AUIRoomConfig> roomConfigMap = new HashMap<>();

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
        if(roomInfo == null || roomInfo.owner == null){
            return false;
        }
        return roomInfo.owner.userId.equals(currentUserInfo.userId);
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

    public String getRoomOwner(String channelName){
        AUIRoomInfo roomInfo = roomInfoMap.get(channelName);
        if(roomInfo == null || roomInfo.owner == null){
            return "";
        }
        return roomInfo.owner.userId;
    }

    public @Nullable AUIRoomInfo getRoomInfo(String channelName) {
        return roomInfoMap.get(channelName);
    }

}

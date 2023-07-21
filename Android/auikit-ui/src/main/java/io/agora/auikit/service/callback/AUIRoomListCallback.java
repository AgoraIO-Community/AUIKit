package io.agora.auikit.service.callback;

import androidx.annotation.Nullable;

import java.util.List;

import io.agora.auikit.model.AUIRoomInfo;

public interface AUIRoomListCallback {
    void onResult(@Nullable AUIException error, @Nullable List<AUIRoomInfo> roomList);
}


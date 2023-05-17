package io.agora.auikit.service.callback;

import androidx.annotation.Nullable;

import java.util.List;

import io.agora.auikit.model.AUiRoomInfo;

public interface AUiRoomListCallback {
    void onResult(@Nullable AUiException error, @Nullable List<AUiRoomInfo> roomList);
}


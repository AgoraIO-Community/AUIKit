package io.agora.auikit.service.callback;

import androidx.annotation.Nullable;

import io.agora.auikit.model.AUiRoomInfo;

public interface AUiCreateRoomCallback {
    void onResult(@Nullable AUiException error, @Nullable AUiRoomInfo roomInfo);
}

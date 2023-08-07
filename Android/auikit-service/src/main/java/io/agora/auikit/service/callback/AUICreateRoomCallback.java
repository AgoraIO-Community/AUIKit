package io.agora.auikit.service.callback;

import androidx.annotation.Nullable;

import io.agora.auikit.model.AUIRoomInfo;

public interface AUICreateRoomCallback {
    void onResult(@Nullable AUIException error, @Nullable AUIRoomInfo roomInfo);
}

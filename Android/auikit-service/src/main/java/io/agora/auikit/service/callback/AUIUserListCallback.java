package io.agora.auikit.service.callback;

import androidx.annotation.Nullable;

import java.util.List;

import io.agora.auikit.model.AUIUserInfo;

public interface AUIUserListCallback {

    void onResult(@Nullable AUIException error, @Nullable List<AUIUserInfo> userList);
}

package io.agora.auikit.service.callback;

import androidx.annotation.Nullable;

import java.util.List;

import io.agora.auikit.model.AUiUserInfo;

public interface AUiUserListCallback {

    void onResult(@Nullable AUiException error, @Nullable List<AUiUserInfo> userList);
}

package io.agora.auikit.service.callback;

import androidx.annotation.Nullable;

import java.util.List;

import io.agora.auikit.model.AUIChoristerModel;

public interface AUIChoristerListCallback {
    void onResult(@Nullable AUIException error, @Nullable List<AUIChoristerModel> songList);
}

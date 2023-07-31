package io.agora.auikit.service.callback;

import androidx.annotation.Nullable;

import java.util.List;

import io.agora.auikit.model.AUIMusicModel;

public interface AUIMusicListCallback {

    void onResult(@Nullable AUIException error, @Nullable List<AUIMusicModel> songList);
}

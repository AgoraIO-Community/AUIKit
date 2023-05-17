package io.agora.auikit.service.callback;

import androidx.annotation.Nullable;

import java.util.List;

import io.agora.auikit.model.AUiMusicModel;

public interface AUiMusicListCallback {

    void onResult(@Nullable AUiException error, @Nullable List<AUiMusicModel> songList);
}

package io.agora.auikit.service.callback;

import androidx.annotation.Nullable;

import java.util.List;

import io.agora.auikit.model.AUiChooseMusicModel;

public interface AUiChooseSongListCallback {

    void onResult(@Nullable AUiException error, @Nullable List<AUiChooseMusicModel> songList);

}

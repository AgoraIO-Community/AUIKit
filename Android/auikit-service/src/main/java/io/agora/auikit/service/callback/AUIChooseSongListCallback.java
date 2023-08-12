package io.agora.auikit.service.callback;

import androidx.annotation.Nullable;

import java.util.List;

import io.agora.auikit.model.AUIChooseMusicModel;

public interface AUIChooseSongListCallback {

    void onResult(@Nullable AUIException error, @Nullable List<AUIChooseMusicModel> songList);

}

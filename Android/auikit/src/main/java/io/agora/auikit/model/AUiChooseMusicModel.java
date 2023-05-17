package io.agora.auikit.model;

import androidx.annotation.Nullable;

import java.util.Objects;

public class AUiChooseMusicModel extends AUiMusicModel {
    public @Nullable AUiUserThumbnailInfo owner; //点歌用户
    public long pinAt = 0; //置顶歌曲时间，与19700101的时间差，单位ms，为0则无置顶操作
    public long createAt = 0; //点歌时间，与19700101的时间差，单位ms

    public @AUiPlayStatus int status = AUiPlayStatus.idle; //播放状态，0 待播放，1 播放中

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AUiChooseMusicModel)) return false;
        AUiChooseMusicModel that = (AUiChooseMusicModel) o;
        return songCode.equals(that.songCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(songCode);
    }
}

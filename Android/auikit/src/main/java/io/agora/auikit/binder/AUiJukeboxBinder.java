package io.agora.auikit.binder;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.List;

import io.agora.auikit.model.AUiChooseMusicModel;
import io.agora.auikit.model.AUiMusicModel;
import io.agora.auikit.model.AUiRoomContext;
import io.agora.auikit.service.IAUiJukeboxService;
import io.agora.auikit.ui.jukebox.IAUiJukeboxChosenItemView;
import io.agora.auikit.ui.jukebox.IAUiJukeboxView;

public class AUiJukeboxBinder implements IAUiBindable, IAUiJukeboxService.AUiJukeboxRespDelegate, IAUiJukeboxView.ActionDelegate {
    private final IAUiJukeboxView jukeboxView;
    private final IAUiJukeboxService jukeboxService;

    private Handler mMainHandler;

    public AUiJukeboxBinder(IAUiJukeboxView jukeboxView, IAUiJukeboxService jukeboxService) {
        this.jukeboxView = jukeboxView;
        this.jukeboxService = jukeboxService;
    }

    @Override
    public void bind() {
        mMainHandler = new Handler(Looper.getMainLooper());
        jukeboxService.bindRespDelegate(this);
        jukeboxView.setActionDelegate(this);

        jukeboxView.setChooseSongCategories(IAUiJukeboxService.songCategories);
        jukeboxService.getMusicList(IAUiJukeboxService.songCategoryIds.get(0), IAUiJukeboxService.songPageStartIndex, IAUiJukeboxService.songPageSize, (error, songList) -> {
            runOnUiThread(()-> jukeboxView.refreshChooseSongList(IAUiJukeboxService.songCategories.get(0), songList));
        });
        jukeboxService.getAllChooseSongList((error, songList) -> {
            runOnUiThread(()-> jukeboxView.setChosenSongList(songList));
        });
    }

    @Override
    public void unBind() {
        mMainHandler.removeCallbacksAndMessages(null);
        mMainHandler = null;
        jukeboxService.unbindRespDelegate(this);
        jukeboxView.setActionDelegate(null);
    }

    private void runOnUiThread(@NonNull Runnable runnable) {
        if (mMainHandler != null) {
            if (mMainHandler.getLooper().getThread() == Thread.currentThread()) {
                runnable.run();
            } else {
                mMainHandler.post(runnable);
            }
        }
    }

    // IAUiJukeboxView.ActionDelegate implements

    @Override
    public void onChooseSongRefreshing(String category) {
        jukeboxService.getMusicList(IAUiJukeboxService.songCategoryIds.get(IAUiJukeboxService.songCategories.indexOf(category)), IAUiJukeboxService.songPageStartIndex, IAUiJukeboxService.songPageSize, (error, songList) -> {
            runOnUiThread(() -> jukeboxView.refreshChooseSongList(category, songList));
        });
    }

    @Override
    public void onChooseSongLoadMore(String category, int startIndex) {
        jukeboxService.getMusicList(IAUiJukeboxService.songCategoryIds.get(IAUiJukeboxService.songCategories.indexOf(category)), IAUiJukeboxService.songPageStartIndex + startIndex / IAUiJukeboxService.songPageSize, IAUiJukeboxService.songPageSize, (error, songList) -> {
            runOnUiThread(()-> jukeboxView.loadMoreChooseSongList(category, songList));
        });
    }

    @Override
    public void onChosenSongItemUpdating(IAUiJukeboxChosenItemView itemView, int position, @NonNull AUiChooseMusicModel model) {
        // 1. 是房主可以删除歌曲，可以置顶
        // 2. 不是房主只能删除自己的歌曲
        // 3. 第二个歌曲能删除但是不能 切歌/置顶
        AUiRoomContext context = jukeboxService.getRoomContext();
        boolean isRoomOwner = context.isRoomOwner(jukeboxService.getChannelName());
        itemView.setViewStatus(
                (position == 0),
                (isRoomOwner && position != 1),
                isRoomOwner || model.owner.userId.equals(context.currentUserInfo.userId));
    }

    @Override
    public void onSearchSongRefreshing(String condition) {
        jukeboxService.searchMusic(condition, IAUiJukeboxService.songPageStartIndex, IAUiJukeboxService.songPageSize, (error, songList) -> {
            runOnUiThread(()-> jukeboxView.refreshSearchSongList(songList));
        });
    }

    @Override
    public void onSearchSongLoadMore(String condition, int startIndex) {
        jukeboxService.searchMusic(condition, IAUiJukeboxService.songPageStartIndex + startIndex / IAUiJukeboxService.songPageSize, IAUiJukeboxService.songPageSize, (error, songList) -> {
            runOnUiThread(()-> jukeboxView.loadMoreSearchSongList(songList));
        });
    }

    @Override
    public void onSongChosen(AUiMusicModel song) {
        jukeboxService.chooseSong(song, null);
    }

    @Override
    public void onSongPinged(AUiChooseMusicModel song) {
        jukeboxService.pingSong(song.songCode, null);
    }

    @Override
    public void onSongDeleted(AUiChooseMusicModel song) {
        jukeboxService.removeSong(song.songCode, null);
    }

    @Override
    public void onSongSwitched(AUiChooseMusicModel song) {
        jukeboxService.removeSong(song.songCode, null);
    }

    // IAUiJukeboxService.AUiJukeboxRespDelegate implements

    @Override
    public void onUpdateAllChooseSongs(@NonNull List<AUiChooseMusicModel> songs) {
        runOnUiThread(()-> jukeboxView.setChosenSongList(songs));
    }

}

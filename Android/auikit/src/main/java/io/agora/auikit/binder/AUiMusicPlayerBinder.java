package io.agora.auikit.binder;


import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import io.agora.auikit.model.AUiChooseMusicModel;
import io.agora.auikit.model.AUiChoristerModel;
import io.agora.auikit.model.AUiLoadMusicConfiguration;
import io.agora.auikit.model.AUiMicSeatInfo;
import io.agora.auikit.model.AUiMusicSettingInfo;
import io.agora.auikit.model.AUiPlayStatus;
import io.agora.auikit.model.AUiUserThumbnailInfo;
import io.agora.auikit.service.IAUiChorusService;
import io.agora.auikit.service.IAUiJukeboxService;
import io.agora.auikit.service.IAUiMicSeatService;
import io.agora.auikit.service.IAUiMusicPlayerService;
import io.agora.auikit.service.callback.AUiMusicLoadStateCallback;
import io.agora.auikit.service.callback.AUiSwitchSingerRoleCallback;
import io.agora.auikit.ui.musicplayer.IMusicPlayerView;
import io.agora.auikit.ui.musicplayer.impl.AUiMusicPlayerView;

public class AUiMusicPlayerBinder implements IAUiBindable, IAUiMusicPlayerService.AUiPlayerRespDelegate, IAUiJukeboxService.AUiJukeboxRespDelegate, IAUiChorusService.AUiChorusRespDelegate, IAUiMicSeatService.AUiMicSeatRespDelegate, IMusicPlayerView.ActionDelegate {

    private final AUiMusicPlayerView musicPlayerView;
    private final IAUiMusicPlayerService musicPlayerService;
    private final IAUiJukeboxService jukeboxService;
    private final IAUiChorusService chorusService;
    private final IAUiMicSeatService micSeatService;

    private AUiChooseMusicModel songPlayingModel = null;

    enum SingRole {
        SOLO_SINGER,
        CO_SINGER,
        AUDIENCE,
    }
    private SingRole mRole = SingRole.AUDIENCE;

    enum PlayerMusicStatus {
        ON_PLAYING,
        ON_PAUSE
    }
    final MutableLiveData<PlayerMusicStatus> playerMusicStatusLiveData = new MutableLiveData<>();

    private boolean isRoomOwner = false;
    private boolean isOnSeat = false;
    private boolean isOriginal = false;
    private String localUid;

    public AUiMusicPlayerBinder(AUiMusicPlayerView musicPlayerView, IAUiMusicPlayerService musicPlayerService, IAUiJukeboxService jukeboxService, IAUiChorusService chorusService, IAUiMicSeatService micSeatService) {
        this.musicPlayerView = musicPlayerView;
        this.musicPlayerService = musicPlayerService;
        this.jukeboxService = jukeboxService;
        this.chorusService = chorusService;
        this.micSeatService = micSeatService;
    }

    @Override
    public void bind() {
        musicPlayerService.bindRespDelegate(this);
        jukeboxService.bindRespDelegate(this);
        chorusService.bindRespDelegate(this);
        micSeatService.bindRespDelegate(this);

        musicPlayerView.setMusicPlayerActionDelegate(this);
        musicPlayerView.setEffectProperties(musicPlayerService.effectProperties());
        // 音效设置默认值
        AUiMusicSettingInfo musicSettingInfo = new AUiMusicSettingInfo(false,100,50,0,0);
        musicPlayerView.setMusicSettingInfo(musicSettingInfo);

        if (musicPlayerService.getRoomContext().isRoomOwner(musicPlayerService.getChannelName())) {
            isOnSeat = true;
            isRoomOwner = true;
        }
        // TODO musicPlayerService.getChannelName() is always null
        //isRoomOwner = true;
        localUid = musicPlayerService.getRoomContext().currentUserInfo.userId;

        initUI();
        initSong();
    }

    @Override
    public void unBind() {
        musicPlayerView.setMusicPlayerActionDelegate(null);
        musicPlayerService.unbindRespDelegate(this);
        jukeboxService.unbindRespDelegate(this);
        chorusService.unbindRespDelegate(this);
        micSeatService.unbindRespDelegate(this);
    }

    private void initUI() {
        if (isRoomOwner) {
            musicPlayerView.initRoomOwnerUI();
        }
    }

    private void initSong() {
        jukeboxService.getAllChooseSongList((error, songList) -> {
            if (error == null && songList != null && !songList.isEmpty()) {
                AUiChooseMusicModel localSong = songList.get(0);
                songPlayingModel = localSong;

                // 设置身份
                if (localSong.owner != null && localSong.owner.userId.equals(localUid)) {
                    mRole = SingRole.SOLO_SINGER;
                    mRole = SingRole.SOLO_SINGER;
                } else {
                    mRole = SingRole.AUDIENCE;
                }

                // 更新歌曲播放UI、播放歌曲
                isOriginal = false;
                musicPlayerView.onMusicPrepare(localSong, mRole == SingRole.AUDIENCE, isRoomOwner);
                musicStartPlay(localSong);
            }
        });
    }

    // ChorusService delegate implement
    @Override
    public void onChoristerDidEnter(AUiChoristerModel chorister) {

    }

    @Override
    public void onChoristerDidLeave(AUiChoristerModel chorister) {

    }

    @Override
    public void onSingerRoleChanged(int oldRole, int newRole) {

    }

    @Override
    public void onChoristerDidChanged() {
        chorusService.getChoristersList((error, songList) -> {
            if (mRole != SingRole.SOLO_SINGER) return;
            if (songList != null && songList.size() > 0) {
                chorusService.switchSingerRole(2, null);
            } else {
                chorusService.switchSingerRole(0, null);
            }
        });
    }

    // JukeboxService delegate implement
    @Override
    public void onAddChooseSong(@NonNull AUiChooseMusicModel song) {
        IAUiJukeboxService.AUiJukeboxRespDelegate.super.onAddChooseSong(song);
    }

    @Override
    public void onRemoveChooseSong(@NonNull AUiChooseMusicModel song) {
        IAUiJukeboxService.AUiJukeboxRespDelegate.super.onRemoveChooseSong(song);
    }

    @Override
    public void onUpdateChooseSong(@NonNull AUiChooseMusicModel song) {
        IAUiJukeboxService.AUiJukeboxRespDelegate.super.onUpdateChooseSong(song);
    }

    @Override
    public void onUpdateAllChooseSongs(@NonNull List<AUiChooseMusicModel> songs) {
        IAUiJukeboxService.AUiJukeboxRespDelegate.super.onUpdateAllChooseSongs(songs);

        // 当前无已点歌曲
        if (songs.size() == 0) {
            mRole = SingRole.AUDIENCE;
            musicPlayerService.stopSing();
            songPlayingModel = null;
            musicPlayerView.onMusicIdle(isOnSeat);
            return;
        }

        AUiChooseMusicModel newTopSong = songs.get(0);
        if (newTopSong == null) return;
        if (songPlayingModel != null && songPlayingModel.songCode.equals(newTopSong.songCode)) return;

        if (songPlayingModel == null) {
            songPlayingModel = newTopSong;
        } else {
            musicPlayerService.stopSing();
            songPlayingModel = newTopSong;
        }

        // 设置身份
        if (newTopSong.owner != null && newTopSong.owner.userId.equals(localUid)) {
            mRole = SingRole.SOLO_SINGER;
        } else {
            mRole = SingRole.AUDIENCE;
        }

        // 更新歌曲播放UI、播放歌曲
        isOriginal = false;
        musicPlayerView.onMusicPrepare(newTopSong, mRole == SingRole.AUDIENCE, isRoomOwner);
        musicStartPlay(newTopSong);
    }

    // micSeatService delegate implement
    @Override
    public void onSeatListChange(List<AUiMicSeatInfo> seatInfoList) {
        IAUiMicSeatService.AUiMicSeatRespDelegate.super.onSeatListChange(seatInfoList);
    }

    @Override
    public void onAnchorEnterSeat(int seatIndex, @NonNull AUiUserThumbnailInfo userInfo) {
        IAUiMicSeatService.AUiMicSeatRespDelegate.super.onAnchorEnterSeat(seatIndex, userInfo);
        if (userInfo.userId.equals(localUid)) {
            isOnSeat = true;
            musicPlayerView.onSeat();
        }
    }

    @Override
    public void onAnchorLeaveSeat(int seatIndex, @NonNull AUiUserThumbnailInfo userInfo) {
        IAUiMicSeatService.AUiMicSeatRespDelegate.super.onAnchorLeaveSeat(seatIndex, userInfo);
        if (userInfo.userId.equals(localUid)) {
            isOnSeat = false;
            // 离开合唱
            if (mRole == SingRole.CO_SINGER) {
                innerLeaveChorus();
            }
            musicPlayerView.onLeaveSeat();
        }
    }

    @Override
    public void onSeatAudioMute(int seatIndex, boolean isMute) {
        IAUiMicSeatService.AUiMicSeatRespDelegate.super.onSeatAudioMute(seatIndex, isMute);
    }

    @Override
    public void onSeatVideoMute(int seatIndex, boolean isMute) {
        IAUiMicSeatService.AUiMicSeatRespDelegate.super.onSeatVideoMute(seatIndex, isMute);
    }

    @Override
    public void onSeatClose(int seatIndex, boolean isClose) {
        IAUiMicSeatService.AUiMicSeatRespDelegate.super.onSeatClose(seatIndex, isClose);
    }

    // musicPlayerService delegate implement
    @Override
    public void onPreludeDidAppear() {
        IAUiMusicPlayerService.AUiPlayerRespDelegate.super.onPreludeDidAppear();
    }

    @Override
    public void onPreludeDidDisappear() {
        IAUiMusicPlayerService.AUiPlayerRespDelegate.super.onPreludeDidDisappear();
    }

    @Override
    public void onPostludeDidAppear() {
        IAUiMusicPlayerService.AUiPlayerRespDelegate.super.onPostludeDidAppear();
    }

    @Override
    public void onPostludeDidDisappear() {
        IAUiMusicPlayerService.AUiPlayerRespDelegate.super.onPostludeDidDisappear();
    }

    @Override
    public void onPlayerPositionDidChange(Long position) {
        IAUiMusicPlayerService.AUiPlayerRespDelegate.super.onPlayerPositionDidChange(position);
        musicPlayerView.setProgress(position);
    }

    @Override
    public void onPitchDidChange(Float pitch) {
        IAUiMusicPlayerService.AUiPlayerRespDelegate.super.onPitchDidChange(pitch);
        musicPlayerView.setPitch(pitch);
    }

    @Override
    public void onPlayerStateChanged(int state, boolean isLocal) {
        IAUiMusicPlayerService.AUiPlayerRespDelegate.super.onPlayerStateChanged(state, isLocal);

        if (state == 4) { // pause
            musicPlayerView.onPlaying();
            playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PAUSE);
        } else if (state == 3) { // playing
            musicPlayerView.onPause();
            playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PLAYING);
        } else if (state == 6 && songPlayingModel != null && songPlayingModel.owner != null && songPlayingModel.owner.userId.equals(localUid)) { // ALL Loop Completed
            jukeboxService.removeSong(songPlayingModel.songCode, null);
        }
    }

    // actionDelegate
    @Override
    public void onClickJoinChorus() {
        innerJoinChorus();
    }

    @Override
    public void onClickLeaveChorus() {
        innerLeaveChorus();
    }

    @Override
    public void onClickSwitchSong() {
        if (songPlayingModel != null) {
            jukeboxService.removeSong(songPlayingModel.songCode, null);
        }
    }

    @Override
    public void onClickPlaying() {
        if (playerMusicStatusLiveData.getValue() == PlayerMusicStatus.ON_PLAYING) {
            musicPlayerService.pauseSing();
        } else if (playerMusicStatusLiveData.getValue() == PlayerMusicStatus.ON_PAUSE) {
            musicPlayerService.resumeSing();
        }
    }

    @Override
    public void onClickOriginal() {
        if (this.isOriginal) {
            musicPlayerService.selectMusicPlayerTrackMode(1);
            musicPlayerView.onAcc();
        } else {
            musicPlayerService.selectMusicPlayerTrackMode(0);
            musicPlayerView.onOriginal();
        }
        this.isOriginal = !isOriginal;
    }

    @Override
    public void onEarMonitoring(boolean enable) {
        musicPlayerService.enableEarMonitoring(enable);
    }

    @Override
    public void onSignalVolume(int volume) {
        musicPlayerService.adjustRecordingSignal(volume);
    }

    @Override
    public void onMusicVolume(int volume) {
        musicPlayerService.adjustMusicPlayerPlayoutVolume(volume);
        musicPlayerService.adjustMusicPlayerPublishVolume(volume);
    }

    @Override
    public void onMusicPitch(int pitch) {
        musicPlayerService.setAudioPitch(pitch);
    }

    @Override
    public void onAudioEffect(int effectId) {
        musicPlayerService.setAudioEffectPreset(effectId);
    }

    private void musicStartPlay(AUiChooseMusicModel newSong) {
        AUiLoadMusicConfiguration config = new AUiLoadMusicConfiguration();
        if (mRole == SingRole.SOLO_SINGER) {
            // 主唱
            config.autoPlay = true;
            config.mainSingerUid = Integer.parseInt(newSong.owner.userId);
            config.loadMusicMode = 2;
            musicPlayerService.loadMusic(Long.parseLong(newSong.songCode), config, new AUiMusicLoadStateCallback() {
                @Override
                public void onMusicLoadSuccess(Long songCode, String lyricUrl) {
                    if (songPlayingModel == null || !songPlayingModel.songCode.equals(songCode.toString())) {
                        // 当前无歌曲了或不是当前歌曲了
                        return;
                    }
                    musicPlayerView.downloadAndSetLrcData(lyricUrl);
                    playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PLAYING);
                    jukeboxService.updatePlayStatus(songCode.toString(), AUiPlayStatus.playing, null);
                    musicPlayerView.onMusicPlaying();
                }

                @Override
                public void onMusicLoadFail(Long songCode, int reason) {
                    musicPlayerView.onMusicPrepareFailed();
                }

                @Override
                public void onMusicLoadProgress(Long songCode, int percent, int status, String msg, String lyricUrl) {
                    if (songPlayingModel != null && songPlayingModel.songCode.equals(songCode.toString())) {
                        musicPlayerView.setLoadProgress(percent);
                    }
                }
            });
        } else {
            // 观众
            config.autoPlay = false;
            config.mainSingerUid = Integer.parseInt(newSong.owner.userId);
            config.loadMusicMode = 1;
            musicPlayerService.loadMusic(Long.parseLong(newSong.songCode), config, new AUiMusicLoadStateCallback() {
                @Override
                public void onMusicLoadSuccess(Long songCode, String lyricUrl) {
                    if (songPlayingModel == null || !songPlayingModel.songCode.equals(songCode.toString())) {
                        // 当前无歌曲了或不是当前歌曲了
                        return;
                    }

                    musicPlayerView.downloadAndSetLrcData(lyricUrl);
                    playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PLAYING);
                    musicPlayerView.onMusicPlaying();
                }

                @Override
                public void onMusicLoadFail(Long songCode, int reason) {
                    musicPlayerView.onMusicPrepareFailed();
                }

                @Override
                public void onMusicLoadProgress(Long songCode, int percent, int status, String msg, String lyricUrl) {
                    if (songPlayingModel != null && songPlayingModel.songCode.equals(songCode.toString())) {
                        musicPlayerView.setLoadProgress(percent);
                    }
                }
            });
        }
    }

    private void innerJoinChorus() {
        // 不在麦上需要自动上麦
        if (songPlayingModel == null) {
            musicPlayerView.onJoinChorusFailed();
            return;
        }
        if (!isOnSeat) {
            micSeatService.autoEnterSeat(error -> {
                if (error == null) {
                    joinChorusWithSongCode(songPlayingModel.songCode);
                } else {
                    musicPlayerView.onJoinChorusFailed();
                }
            });
        } else  {
            joinChorusWithSongCode(songPlayingModel.songCode);
        }
    }
    private void joinChorusWithSongCode(String code) {
        chorusService.joinChorus(code, chorusService.getRoomContext().currentUserInfo.userId, error -> {
            if (error == null) {
                AUiLoadMusicConfiguration config = new AUiLoadMusicConfiguration();
                config.autoPlay = false;
                config.mainSingerUid = Integer.parseInt(songPlayingModel.owner.userId);
                config.loadMusicMode = 0;  // LOAD Music Only
                musicPlayerService.loadMusic(Long.valueOf(songPlayingModel.songCode), config, new AUiMusicLoadStateCallback() {
                    @Override
                    public void onMusicLoadSuccess(Long songCode, String lyricUrl) {
                        chorusService.switchSingerRole(1, new AUiSwitchSingerRoleCallback() {
                            @Override
                            public void onSwitchRoleSuccess() {
                                mRole = SingRole.CO_SINGER;
                                musicPlayerView.onJoinChorus(isRoomOwner);
                            }

                            @Override
                            public void onSwitchRoleFail(int reason) {

                            }
                        });
                    }

                    @Override
                    public void onMusicLoadFail(Long songCode, int reason) {

                    }

                    @Override
                    public void onMusicLoadProgress(Long songCode, int percent, int status, String msg, String lyricUrl) {

                    }
                });
            }
        });
    }

    private void innerLeaveChorus() {
        if (songPlayingModel == null) return;

        chorusService.leaveChorus(songPlayingModel.songCode, chorusService.getRoomContext().currentUserInfo.userId, error -> {
            if (error == null) {
                chorusService.switchSingerRole(3, new AUiSwitchSingerRoleCallback() {
                    @Override
                    public void onSwitchRoleSuccess() {
                        mRole = SingRole.AUDIENCE;
                        musicPlayerView.onLeaveChorus();
                    }

                    @Override
                    public void onSwitchRoleFail(int reason) {

                    }
                });
            }
        });
    }
}

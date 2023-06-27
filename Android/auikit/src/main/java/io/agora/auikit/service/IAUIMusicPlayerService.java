package io.agora.auikit.service;

import java.util.Map;

import io.agora.auikit.model.AUILoadMusicConfiguration;
import io.agora.auikit.service.callback.AUIMusicLoadStateCallback;

public interface IAUIMusicPlayerService extends IAUICommonService<IAUIMusicPlayerService.AUIPlayerRespDelegate> {
    /**
     * 异步加载歌曲，同时只能为一首歌loadSong，loadSong结果会通过回调通知业务层
     * @param songCode 歌曲唯一编码
     * @param config 加载歌曲配置
     * @param musicLoadStateListener 加载歌曲结果回调
     *
     * 推荐调用：
     * 歌曲开始时：
     * 主唱 loadMusic(KTVLoadMusicConfiguration(autoPlay=true, mode=LOAD_MUSIC_AND_LRC, songCode, mainSingerUid)) switchSingerRole(SoloSinger)
     * 观众 loadMusic(KTVLoadMusicConfiguration(autoPlay=false, mode=LOAD_LRC_ONLY, songCode, mainSingerUid))
     * 加入合唱时：
     * 准备加入合唱者：loadMusic(KTVLoadMusicConfiguration(autoPlay=false, mode=LOAD_MUSIC_ONLY, songCode, mainSingerUid))
     * loadMusic成功后switchSingerRole(CoSinger)
     */
    void loadMusic(Long songCode, AUILoadMusicConfiguration config, AUIMusicLoadStateCallback musicLoadStateListener);

    /**
     * 开始播放歌曲
     * @param songCode 开始播放歌曲
     */
    void startSing(Long songCode);

    /**
     * 停止播放歌曲
     */
    void stopSing();

    /**
     * 恢复播放歌曲
     */
    void resumeSing();

    /**
     * 暂停播放歌曲
     */
    void pauseSing();

    /**
     * 调整播放进度
     * @param time 播放进度
     */
    void seekSing(Long time);

    /**
     * 调整音乐本地播放的声音
     * @param volume 本地播放的声音
     */
    void adjustMusicPlayerPlayoutVolume(int volume);

    /**
     * 调整音乐远端播放的声音
     * @param volume 调整音乐推送到远端的声音大小
     */
    void adjustMusicPlayerPublishVolume(int volume);

    /**
     * 调整音乐本地播放的声音
     * @param volume 调整本地播放远端伴唱人声音量的大小（主唱 && 伴唱都可以调整）
     */
    void adjustRecordingSignal(int volume);

    /**
     * 选择音轨，原唱、伴唱
     * @param mode 原唱0，伴奏1
     */
    void selectMusicPlayerTrackMode(int mode);

    /**
     * 获取播放进度
     */
    Long getPlayerPosition();

    /**
     * 获取播放时长
     */
    Long getPlayerDuration();

    /**
     * 升降调
     * @param pitch 音调
     */
    void setAudioPitch(int pitch);

    /**
     * 音效
     * @param audioEffectId
     */
    void setAudioEffectPreset(int audioEffectId);

    /**
     * 音效映射 key index,value effectId
     */
    Map<Integer,Integer> effectProperties();

    /**
     * 耳返
     * @param inEarMonitoring 是否开启耳返
     */
    void enableEarMonitoring(Boolean inEarMonitoring);

    /**
     * musicPlayer 模块事件
     */
    interface AUIPlayerRespDelegate {
        /**
         * 前奏开始加载
         */
        default void onPreludeDidAppear() {}

        /**
         * 前奏结束加载
         */
        default void onPreludeDidDisappear() {}

        /**
         * 尾奏开始加载
         */
        default void onPostludeDidAppear() {}

        /**
         * 尾奏结束
         */
        default void onPostludeDidDisappear() {}

        /**
         * 获取时间进度回调
         */
        default void onPlayerPositionDidChange(Long position) {}

        /**
         * 获取时间进度回调
         */
        default void onPitchDidChange(Float pitch) {}


        /**
         * 播放状态变化
         */
        default void onPlayerStateChanged(int state, boolean isLocal) {}
    }
}

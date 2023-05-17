package io.agora.auikit.service.imp

import io.agora.auikit.model.AUiLoadMusicConfiguration
import io.agora.auikit.model.AUiRoomContext
import io.agora.auikit.service.IAUiMusicPlayerService
import io.agora.auikit.service.callback.AUiMusicLoadStateCallback
import io.agora.auikit.service.ktv.IKTVApiEventHandler
import io.agora.auikit.service.ktv.IMusicLoadStateListener
import io.agora.auikit.service.ktv.KTVApi
import io.agora.auikit.service.ktv.KTVApiImpl
import io.agora.auikit.service.ktv.KTVLoadMusicConfiguration
import io.agora.auikit.service.ktv.KTVLoadMusicMode
import io.agora.auikit.service.ktv.KTVLoadSongFailReason
import io.agora.auikit.service.ktv.KTVSingRole
import io.agora.auikit.service.ktv.MusicLoadStatus
import io.agora.auikit.utils.DelegateHelper
import io.agora.mediaplayer.Constants
import io.agora.mediaplayer.Constants.MediaPlayerState
import io.agora.rtc2.RtcEngine

class AUiMusicPlayerServiceImpl constructor(
    private val rtcEngine: RtcEngine,
    private val channelName: String,
    private val ktvApi: KTVApi
): IAUiMusicPlayerService, IKTVApiEventHandler() {
    private val delegateHelper = DelegateHelper<IAUiMusicPlayerService.AUiPlayerRespDelegate>()

    private val rtcEffectProperties :MutableMap<Int,Int> by lazy{
        mutableMapOf()
    }

    init {
        ktvApi.addEventHandler(this)
        rtcEffectProperties[0] = io.agora.rtc2.Constants.AUDIO_EFFECT_OFF
        rtcEffectProperties[1] = io.agora.rtc2.Constants.ROOM_ACOUSTICS_KTV
        rtcEffectProperties[2] = io.agora.rtc2.Constants.ROOM_ACOUSTICS_VOCAL_CONCERT
        rtcEffectProperties[3] = io.agora.rtc2.Constants.ROOM_ACOUSTICS_STUDIO
        rtcEffectProperties[4] = io.agora.rtc2.Constants.ROOM_ACOUSTICS_PHONOGRAPH
        rtcEffectProperties[5] = io.agora.rtc2.Constants.ROOM_ACOUSTICS_SPACIAL
        rtcEffectProperties[6] = io.agora.rtc2.Constants.ROOM_ACOUSTICS_ETHEREAL
        rtcEffectProperties[7] = io.agora.rtc2.Constants.STYLE_TRANSFORMATION_POPULAR
        rtcEffectProperties[8] = io.agora.rtc2.Constants.STYLE_TRANSFORMATION_RNB
    }

    // ----------------- IAUiMusicPlayerService -----------------
    override fun bindRespDelegate(delegate: IAUiMusicPlayerService.AUiPlayerRespDelegate?) {
        delegateHelper.bindDelegate(delegate)
    }

    override fun unbindRespDelegate(delegate: IAUiMusicPlayerService.AUiPlayerRespDelegate?) {
        delegateHelper.unBindDelegate(delegate)
    }

    override fun getChannelName() = channelName

    override fun loadMusic(
        songCode: Long,
        config: AUiLoadMusicConfiguration,
        musicLoadStateListener: AUiMusicLoadStateCallback?
    ) {
        val mode = when (config.loadMusicMode) {
            0 -> {
                KTVLoadMusicMode.LOAD_MUSIC_ONLY
            }
            1 -> {
                KTVLoadMusicMode.LOAD_LRC_ONLY
            }
            2 -> {
                KTVLoadMusicMode.LOAD_MUSIC_AND_LRC
            }
            else -> {
                KTVLoadMusicMode.LOAD_NONE
            }
        }

        ktvApi.loadMusic(songCode, KTVLoadMusicConfiguration(songCode.toString(), config.autoPlay, config.mainSingerUid, mode), object: IMusicLoadStateListener {
            override fun onMusicLoadSuccess(songCode: Long, lyricUrl: String) {
                musicLoadStateListener?.onMusicLoadSuccess(songCode, lyricUrl)
            }

            override fun onMusicLoadFail(songCode: Long, reason: KTVLoadSongFailReason) {
                musicLoadStateListener?.onMusicLoadFail(songCode, reason.value)
            }

            override fun onMusicLoadProgress(
                songCode: Long,
                percent: Int,
                status: MusicLoadStatus,
                msg: String?,
                lyricUrl: String?
            ) {
                musicLoadStateListener?.onMusicLoadProgress(songCode, percent, status.value, msg, lyricUrl)
            }
        })
    }

    override fun startSing(songCode: Long?) {
        songCode?.let { ktvApi.startSing(songCode, 0) }
    }

    override fun stopSing() {
        ktvApi.switchSingerRole(KTVSingRole.Audience, null)
        ktvApi.loadMusic(0, KTVLoadMusicConfiguration("", false, 0, KTVLoadMusicMode.LOAD_NONE), null)
    }

    override fun resumeSing() {
        ktvApi.resumeSing()
    }

    override fun pauseSing() {
        ktvApi.pauseSing()
    }

    override fun seekSing(time: Long?) {
        time?.let { ktvApi.seekSing(time) }
    }

    override fun adjustMusicPlayerPlayoutVolume(volume: Int) {
        (ktvApi as KTVApiImpl).mpkPlayoutVolume = volume
        ktvApi.getMediaPlayer().adjustPlayoutVolume(volume)
    }

    override fun adjustMusicPlayerPublishVolume(volume: Int) {
        (ktvApi as KTVApiImpl).mpkPublishVolume = volume
        ktvApi.getMediaPlayer().adjustPublishSignalVolume(volume)
    }

    override fun adjustRecordingSignal(volume: Int) {
        ktvApi.getRtcEngineEx().adjustRecordingSignalVolume(volume)
    }

    override fun selectMusicPlayerTrackMode(mode: Int) {
        ktvApi.getMediaPlayer().selectAudioTrack(mode)
    }

    override fun getPlayerPosition(): Long {
        return ktvApi.getMediaPlayer().playPosition
    }

    override fun getPlayerDuration(): Long {
        return ktvApi.getMediaPlayer().duration
    }

    override fun setAudioPitch(pitch: Int) {
        rtcEngine.setAudioMixingPitch(pitch)
    }

    override fun setAudioEffectPreset(audioEffectId: Int) {
        rtcEngine.setAudioEffectPreset(audioEffectId)
    }

    override fun effectProperties(): MutableMap<Int, Int> {
        return rtcEffectProperties
    }

    override fun enableEarMonitoring(inEarMonitoring: Boolean) {
        rtcEngine.enableInEarMonitoring(inEarMonitoring)
    }

    // ----------------- IKTVApiEventHandler -----------------
    override fun onMusicPlayerStateChanged(
        state: MediaPlayerState, error: Constants.MediaPlayerError, isLocal: Boolean
    ) {
        delegateHelper.notifyDelegate { delegate: IAUiMusicPlayerService.AUiPlayerRespDelegate ->
            delegate.onPlayerStateChanged(MediaPlayerState.getValue(state), isLocal)
        }
    }

    override fun onSingerRoleChanged(oldRole: KTVSingRole, newRole: KTVSingRole) {}

    override fun onChorusChannelTokenPrivilegeWillExpire(token: String?) {}

    override fun onPositionAndPitchChanged(position: Long, pitch: Float) {
        delegateHelper.notifyDelegate { delegate: IAUiMusicPlayerService.AUiPlayerRespDelegate ->
            delegate.onPlayerPositionDidChange(position)
            delegate.onPitchDidChange(pitch)
        }
    }
}
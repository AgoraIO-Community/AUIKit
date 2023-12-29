//
//  AUIPlayerServiceImpl.swift
//  AUIKit
//
//  Created by wushengtao on 2023/3/10.
//

import Foundation
import AgoraRtcKit
import YYModel

open class AUIPlayerServiceImpl: NSObject {
    private var respDelegates: NSHashTable<AnyObject> = NSHashTable<AnyObject>.weakObjects()
    private var ktvApi: KTVApiDelegate!
    private var rtcKit: AgoraRtcEngineKit!
    private var channelName: String!
    private var streamId: Int = 0
    
    deinit {
        aui_info("deinit AUIPlayerServiceImpl", tag: "AUIPlayerServiceImpl")
    }
    
    public init(channelName: String, rtcKit: AgoraRtcEngineKit, ktvApi: KTVApiDelegate, rtmManager: AUIRtmManager) {
        aui_info("init AUIPlayerServiceImpl", tag: "AUIPlayerServiceImpl")
        super.init()
        self.channelName = channelName
        self.rtcKit = rtcKit
        self.ktvApi = ktvApi

        let config = AgoraDataStreamConfig()
        config.ordered = false
        config.syncWithAudio = false
        rtcKit.createDataStream(&streamId, config: config)
    }
}

//MARK: AUIPlayerServiceDelegate
extension AUIPlayerServiceImpl: AUIPlayerServiceDelegate {
    
    public func getRoomContext() -> AUIRoomContext {
        return AUIRoomContext.shared
    }
    
    public func bindRespDelegate(delegate: AUIPlayerRespDelegate) {
        respDelegates.add(delegate)
    }
    
    public func unbindRespDelegate(delegate: AUIPlayerRespDelegate) {
        respDelegates.remove(delegate)
    }
    
    
    public func loadMusic(songCode: Int, config: KTVSongConfiguration, musicLoadStateListener: IMusicLoadStateListener) {
        ktvApi.loadMusic(songCode: songCode, config: config, onMusicLoadStateListener: musicLoadStateListener)
    }
    
    public func switchSingerRole(newRole: KTVSingRole, onSwitchRoleState: @escaping ISwitchRoleStateListener) {
        ktvApi.switchSingerRole(newRole: newRole, onSwitchRoleState: onSwitchRoleState)
    }
    
    public func startSing(songCode: Int) {
        ktvApi.startSing(songCode: songCode, startPos: 0)
    }
    
    public func stopSing() {
        ktvApi.getMusicPlayer()?.stop()
        ktvApi.switchSingerRole(newRole: .audience) { state, reason in
        }
    }
    
    public func getMusicPlayer() -> AgoraRtcMediaPlayerProtocol? {
        return ktvApi.getMusicPlayer()
    }
    
    public func resumeSing() {
//        ktvApi.resumePlay()
        ktvApi.resumeSing()
    }
    
    public func pauseSing() {
        ktvApi.pauseSing()
    }
    
    public func seekSing(time: Int) {
        ktvApi.seekSing(time: time)
    }
    
    //音乐播放音量
    public func adjustMusicPlayerPlayoutVolume(volume: Int) {
        ktvApi.getMusicPlayer()?.adjustPlayoutVolume(Int32(volume))
    }
    
    public func adjustMusicPlayerPublishVolume(volume: Int) {
        ktvApi.getMusicPlayer()?.adjustPublishSignalVolume(Int32(volume))
//        ktvApi.adjustRemoteVolume(volume: Int32(volume))
    }
    
    //人声播放音量
    public func adjustPlaybackVolume(volume: Int) {
        rtcKit.adjustPlaybackSignalVolume(volume)
    }
    
    public func adjustRecordingSignalVolume(volume: Int) {
        rtcKit.adjustRecordingSignalVolume(volume)
    }
    
    public func selectMusicPlayerTrackMode(mode: KTVPlayerTrackMode) {
        ktvApi.getMusicPlayer()?.selectAudioTrack(mode == .origin ? 0 : 1)
    }
    
    public func getPlayerPosition() -> Int {
//        return ktvApi.getMediaPlayer().du
        return 0
    }
    
    public func getPlayerDuration() -> Int {
        return 0
    }
    
    public func getChannelName() -> String {
        return channelName
    }
    
    //升降调
    public func setAudioPitch(pitch: Int) {
        ktvApi.getMusicPlayer()?.setAudioPitch(pitch)
    }
    
    public func setAudioEffectPreset(present: AgoraAudioEffectPreset) {
        rtcKit.setAudioEffectPreset(present)
    }
    
    public func setVoiceConversionPreset(preset: AgoraVoiceConversionPreset) {
        rtcKit.setVoiceConversionPreset(preset)
    }
    
    public func enableEarMonitoring(inEarMonitoring: Bool) {
        rtcKit.enable(inEarMonitoring: inEarMonitoring)
    }
    
    public func setLrcView(delegate: KTVLrcViewDelegate) {
        ktvApi.setLrcView(view: delegate)
    }
    
    public func addEventHandler(ktvApiEventHandler: KTVApiEventHandlerDelegate) {
        ktvApi.addEventHandler(ktvApiEventHandler: ktvApiEventHandler)
    }
    
    public func removeEventHandler(ktvApiEventHandler: KTVApiEventHandlerDelegate) {
        ktvApi.removeEventHandler(ktvApiEventHandler: ktvApiEventHandler)
    }
    
    //开启耳返
    public func enableInEarMonitoring(enabled: Bool) {
        rtcKit.enable(inEarMonitoring: enabled, includeAudioFilters: .none)
    }
    
    public func sendStreamMsg(with dict: [String: Any]) {
        guard let data = compactDictionaryToData(dict) else {return}
        let _ = rtcKit.sendStreamMessage(streamId, data: data)
    }
    
    private func compactDictionaryToData(_ dict: [String: Any]) -> Data? {
        do {
            let jsonData = try JSONSerialization.data(withJSONObject: dict, options: [])
            return jsonData
        }
        catch {
            print("Error encoding data: (error.localizedDescription)")
            return nil
        }
    }
    
//    public func setClientRole(role: AgoraClientRole) {
//        rtcKit.setClientRole(role)
//    }
    
//    public func publishAudioTrack(enable: Bool) {
//        let option = AgoraRtcChannelMediaOptions()
//        option.publishMicrophoneTrack = enable
//        rtcKit.updateChannel(with: option)
//    }
    
//    public func muteAudio(with uid: Int, enable: Bool, isLocal: Bool) {
//        if isLocal {
//            self.rtcKit.muteLocalAudioStream(enable)
//        } else {
//            self.rtcKit.muteRemoteAudioStream(UInt(uid), mute: enable)
//        }
//    }
//
//    public func muteVideo(with uid: Int, enable: Bool, isLocal: Bool) {
//        if isLocal {
//            self.rtcKit.muteLocalVideoStream(enable)
//        } else {
//            self.rtcKit.muteRemoteVideoStream(UInt(uid), mute: enable)
//        }
//    }
    
}

extension AUIPlayerServiceImpl: KTVApiEventHandlerDelegate {
    public func onMusicPlayerProgressChanged(with progress: Int) {
    }
    
    public func onTokenPrivilegeWillExpire() {
        
    }
    
    public func onChorusChannelAudioVolumeIndication(speakers: [AgoraRtcAudioVolumeInfo], totalVolume: Int) {
        
    }
    
    public func onChorusChannelTokenPrivilegeWillExpire(token: String?) {
        
    }
    
    public func onMusicPlayerStateChanged(state: AgoraMediaPlayerState, error: AgoraMediaPlayerError, isLocal: Bool) {
        
    }
    
    public func onSingingScoreResult(score: Float) {
        
    }
    
    public func onSingerRoleChanged(oldRole: KTVSingRole, newRole: KTVSingRole) {
        
    }
}

//MARK: KTVMusicLoadStateListener
extension AUIPlayerServiceImpl: IMusicLoadStateListener {
    
    public func onMusicLoadProgress(songCode: Int, percent: Int, status: AgoraMusicContentCenterPreloadStatus, msg: String?, lyricUrl: String?) {
        
    }
    
    public func onMusicLoadSuccess(songCode: Int, lyricUrl: String) {
        
    }
    
    public func onMusicLoadFail(songCode: Int, reason: KTVLoadSongFailReason) {
        
    }
    
    
    
    
//    public func onPlayerStateChanged(state: AgoraMediaPlayerState, isLocal: Bool) {
//        respDelegates.objectEnumerator().forEach { obj in
//            (obj as? AUIPlayerRespDelegate)?.onPlayerStateChanged(state: state, isLocal: isLocal)
//        }
//    }
//
//    public func onSyncMusicPosition(position: Int, pitch: Float) {
//        respDelegates.objectEnumerator().forEach { obj in
//            (obj as? AUIPlayerRespDelegate)?.onPlayerPositionDidChange(position: position)
//        }
//    }
//
//    public func onMusicLoaded(songCode: NSInteger, lyricUrl: String, role: KTVSingRole, state: KTVLoadSongState) {
//
//    }
//
//    public func onJoinChorusState(reason: KTVJoinChorusState) {
//
//    }
//
//    public func onSingerRoleChanged(oldRole: KTVSingRole, newRole: KTVSingRole) {
//
//    }
//
//    public func didSkipViewShowPreludeEndPosition() {
//        respDelegates.objectEnumerator().forEach { obj in
//            (obj as? AUIPlayerRespDelegate)?.onPreludeDidAppear()
//        }
//    }
//
//    public func didSkipViewShowEndDuration() {
//        respDelegates.objectEnumerator().forEach { obj in
//            (obj as? AUIPlayerRespDelegate)?.onPostludeDidAppear()
//        }
//    }
//
//    public func didlrcViewDidScrolled(with cumulativeScore: Int, totalScore: Int) {
//
//    }
//
//    public func didlrcViewDidScrollFinished(with cumulativeScore: Int, totalScore: Int, lineScore: Int) {
//
//    }
//
//
}

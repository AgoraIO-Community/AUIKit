//
//  AUIPlayerServiceDelegate.swift
//  AUIKit
//
//  Created by wushengtao on 2023/3/8.
//

import Foundation
import AgoraRtcKit

/// 播放器请求协议
public protocol AUIPlayerServiceDelegate: AUICommonServiceDelegate {
    
    /// 绑定响应协议
    /// - Parameter delegate: 需要回调的对象
    func bindRespDelegate(delegate: AUIPlayerRespDelegate)
    
    /// 解绑响应协议
    /// - Parameter delegate: 需要回调的对象
    func unbindRespDelegate(delegate: AUIPlayerRespDelegate)

    /// 加载歌曲
    /// - Parameters:
    ///   - songCode: 歌曲code
    ///   - config: 歌曲配置信息
    ///   - musicLoadStateListener: 歌曲加载回调
    func loadMusic(songCode: Int, config: KTVSongConfiguration, musicLoadStateListener: IMusicLoadStateListener)
    
    /// 切换角色
    /// - Parameters:
    ///   - newRole: 角色
    ///   - onSwitchRoleState: 完成回调
    func switchSingerRole(newRole: KTVSingRole, onSwitchRoleState:@escaping ISwitchRoleStateListener)
    
    /// 播放歌曲
    /// - Parameter songCode: 歌曲code
    func startSing(songCode: Int)
    
    /// 停止播放歌曲
    func stopSing()
    
    /// 恢复播放
    func resumeSing()
     
    /// 暂停播放
    func pauseSing()
    
    /// 调整进度
    /// - Parameter time: 单位ms
    func seekSing(time: Int)
    
    /// 调整音乐本地播放的声音 （主唱&&伴唱都可以调节）
    /// - Parameter volume: <#volume description#>
    func adjustMusicPlayerPlayoutVolume(volume: Int)
    
    //调整采集音量
    func adjustRecordingSignalVolume(volume: Int)
    
    /// 调整音乐推送到远端的声音大小 （主唱调整）
    /// - Parameter volume: <#volume description#>
    func adjustMusicPlayerPublishVolume(volume: Int)
    
    /// 调整本地播放远端伴唱人声音量的大小（主唱 && 伴唱都可以调整）
    ///  观众调整的是远端所有音乐 + 人声的音量大小
    /// - Parameter volume: <#volume description#>
    func adjustPlaybackVolume(volume: Int)
    
    /// 选择音轨，原唱、伴唱
    /// - Parameter mode: mode description
    func selectMusicPlayerTrackMode(mode: KTVPlayerTrackMode)
    
    //TODO:是不是需要回调
    /// 获取播放进度
    /// - Returns: <#description#>
//    func getPlayerPosition() -> Int
    
    /// 获取播放时长
    /// - Returns: <#description#>
    func getPlayerDuration() -> Int
    
    /// 获取播放器实例
    /// - Returns: <#description#>
    func getMusicPlayer() -> AgoraRtcMediaPlayerProtocol?
    
    /// 升降调
    /// - Parameter pitch: <#pitch description#>
    func setAudioPitch(pitch: Int)
    
    /// 音效
    /// - Parameter present: <#present description#>
    func setAudioEffectPreset(present: AgoraAudioEffectPreset)
    
    //变声
    func setVoiceConversionPreset(preset: AgoraVoiceConversionPreset)
    
    //耳返
    func enableEarMonitoring(inEarMonitoring: Bool)
    
    func setLrcView( delegate: KTVLrcViewDelegate)
    
    /// 订阅KTVApi事件
    /// - Parameter ktvApiEventHandler: <#ktvApiEventHandler description#>
    func addEventHandler(ktvApiEventHandler: KTVApiEventHandlerDelegate)
    
    
    /// 取消订阅KTVApi事件
    /// - Parameter ktvApiEventHandler: <#ktvApiEventHandler description#>
    func removeEventHandler(ktvApiEventHandler: KTVApiEventHandlerDelegate)
    
    func sendStreamMsg(with dict:[String: Any])
    
//    func setClientRole(role: AgoraClientRole)
    
//    func publishAudioTrack(enable: Bool)
    
//    func muteAudio(with uid: Int, enable: Bool, isLocal: Bool)
//
//    func muteVideo(with uid: Int, enable: Bool, isLocal: Bool)
}


/// 播放器回调协议
public protocol AUIPlayerRespDelegate: NSObjectProtocol {
    
    /// 前奏开始加载
    func onPreludeDidAppear()
    
    /// 前奏结束加载
    func onPreludeDidDisappear()
    
    /// 尾奏开始加载
    func onPostludeDidAppear()
    
    /// 尾奏结束加载
    func onPostludeDidDisappear()
    
    /// 获取时间进度回调
    /// - Parameter position: <#position description#>
    func onPlayerPositionDidChange(position: Int)
    
    /// 播放状态变化回调
    /// - Parameter status: <#status description#>
    func onPlayerStateChanged(state: AgoraMediaPlayerState, isLocal: Bool ) -> Void
    
    func onDataStreamMsgReceived(with uid: NSInteger, streamId: NSInteger, data: Data)
}

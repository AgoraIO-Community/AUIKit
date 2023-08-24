//
//  RTCManager.swift
//  Game2035OCNew
//
//  Created by FanPengpeng on 2023/4/15.
//

import UIKit
import AgoraRtcKit

protocol RTCManagerDelegate: NSObjectProtocol {
    func onJoinedChannel(_ channel: String)
    func onUserJoined(_ uid: UInt)
    func onReceiveStreamMessageFromUid(_ uid: UInt, streamId: Int, data: Data)
}

class RTCManager: NSObject {
    
    private var agoraKit: AgoraRtcEngineKit!
    @objc var roomId: String?
    
    weak var delegate: RTCManagerDelegate?
    
    private func createEngine(){
        let config = AgoraRtcEngineConfig()
        config.appId = KeyCenter.AppId
        
        let agoraKit = AgoraRtcEngineKit.sharedEngine(with: config, delegate: self)
        self.agoraKit = agoraKit
        // get channel name from configs
        agoraKit.setChannelProfile(.liveBroadcasting)
        agoraKit.enableAudio()
        agoraKit.setDefaultAudioRouteToSpeakerphone(true)
        becomeBroadcaster()
    }

    private func joinChannel(channelName: String, uid: String) {
        let option = AgoraRtcChannelMediaOptions()
        option.autoSubscribeAudio = true
        option.autoSubscribeVideo = true
        let ret = agoraKit.joinChannel(byToken: KeyCenter.Token, channelId: channelName, info: nil, uid: UInt(uid) ?? 0, options: option)
        if ret != 0 {
            print("joinChannel call failed: \(ret), please check your params")
        }
    }
    
    /// make myself a broadcaster
   private func becomeBroadcaster() {
        agoraKit.enableLocalAudio(true)
        agoraKit.setClientRole(.broadcaster, options: nil)
    }
    
    /// make myself an audience
    private func becomeAudience() {
        let options = AgoraClientRoleOptions()
        agoraKit.setClientRole(.audience, options: options)
    }

}

extension RTCManager {
    
    private func enableVideo(videoView:UIView, uid: String){
        let canvas = AgoraRtcVideoCanvas()
        canvas.view = videoView
        canvas.uid = UInt(uid) ?? 0
        agoraKit.setupLocalVideo(canvas)
        agoraKit.enableVideo()
        agoraKit.startPreview()
    }
    
    /// 加入频道
    /// - Parameters:
    ///   - channelName: 频道名称
    ///   - uid: 用户id
    ///   - videoView: 不需要视频功能此处传nil
    ///   - delegate:
    func join(channelName:String, uid: String, videoView:UIView?, delegate: RTCManagerDelegate){
        createEngine()
        if videoView != nil {
            enableVideo(videoView: videoView!, uid: uid)
        }
        joinChannel(channelName: channelName, uid: uid)
        self.delegate = delegate
    }
    
    /// 离开频道
    @objc func leave(){
        agoraKit.leaveChannel()
    }
    
    /// 开启关闭麦克风
    /// - Parameter mute: 是否关闭
    func muteSelf(_ mute: Bool){
        agoraKit.muteLocalAudioStream(mute)
    }
}


extension RTCManager: AgoraRtcEngineDelegate {
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinChannel channel: String, withUid uid: UInt, elapsed: Int) {
        delegate?.onJoinedChannel(channel)
    }

    func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinedOfUid uid: UInt, elapsed: Int) {
        delegate?.onUserJoined(uid)
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, receiveStreamMessageFromUid uid: UInt, streamId: Int, data: Data) {
        delegate?.onReceiveStreamMessageFromUid(uid, streamId: streamId, data: data)
    }
    
    
}


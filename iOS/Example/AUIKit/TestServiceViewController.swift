//
//  TestServiceViewController.swift
//  AUIKit_Example
//
//  Created by FanPengpeng on 2023/8/4.
//  Copyright © 2023 CocoaPods. All rights reserved.
//

import UIKit
import AUIKitCore
import MJRefresh
import AgoraRtcKit

class TestServiceViewController: UIViewController, AgoraRtcEngineDelegate {

   
    
    private var ktvApi: KTVApiDelegate!
    
    private var roomList: [AUIRoomInfo] = []
    
    private var roomManager: AUIRoomManagerImpl?
    
    private var userManager: AUIUserServiceDelegate?
    
    private var micSeatImpl: AUIMicSeatServiceDelegate?
    
    private var chatImplement: AUIMManagerServiceDelegate?
    
    private var musicImpl: AUIMusicServiceDelegate?
    
    private var chorusImpl: AUIChorusServiceDelegate?
    
    private var rtcEngine: AgoraRtcEngineKit!
    

    override func viewDidLoad() {
        super.viewDidLoad()
        initEngine()
        getRoomList()
        

        // Do any additional setup after loading the view.
    }
    
    @IBAction func didClickLoginButton(_ sender: Any) {
        createRoom()
    }
    
    
    @IBAction func didClickGetListButton(_ sender: Any) {
        getRoomList()
    }
    
    
    @IBAction func didClickJoinRoomButton(_ sender: Any) {
        generateToken { config, appid in
            AUIRoomContext.shared.commonConfig?.appId = appid
            self.joinRoom()
        }
    }
    
}


extension TestServiceViewController {
    
    private func initEngine() {
        //设置基础信息到KaraokeUIKit里
        let commonConfig = AUICommonConfig()
        commonConfig.host = KeyCenter.HostUrl
        commonConfig.userId = "123"
        commonConfig.userName = "123"
        commonConfig.userAvatar = "https://t14.baidu.com/it/u=3383681797,1827402820&fm=224&app=112&size=w931&n=0&f=JPEG&fmt=auto?sec=1691254800&t=6d5aaea1b9acc648d394f39a00cca3c9"
        
        AUIRoomContext.shared.commonConfig = commonConfig
        
        self.roomManager = AUIRoomManagerImpl(commonConfig: commonConfig, rtmClient: nil)
        
        self.roomManager?.bindRespDelegate(delegate: self)
    }
    
    private func _rtcEngineConfig(commonConfig: AUICommonConfig) -> AgoraRtcEngineConfig {
       let config = AgoraRtcEngineConfig()
        config.appId = commonConfig.appId
        config.channelProfile = .liveBroadcasting
        config.audioScenario = .gameStreaming
        config.areaCode = .global
        
        if config.appId?.count ?? 0 == 0 {
            aui_error("config.appId is empty, please check 'AUIRoomContext.shared.commonConfig.appId'", tag: "AUIKaraokeRoomService")
            assert(false, "config.appId is empty, please check 'AUIRoomContext.shared.commonConfig.appId'")
        }
        return config
    }
    
    
    private func _createRtcEngine(commonConfig: AUICommonConfig) ->AgoraRtcEngineKit {
        let engine = AgoraRtcEngineKit.sharedEngine(with: _rtcEngineConfig(commonConfig: commonConfig),
                                                    delegate: self)
        engine.delegate = self
        return engine
    }
    
    func getRoomList() {
        self.roomManager?.getRoomInfoList(lastCreateTime: nil, pageSize: 10, callback: { err, lists in
            if let lists = lists {
                self.roomList.append(contentsOf: lists)
            }
            aui_info("roomlist == \(lists?.debugDescription), err = \(err)", tag: "TestServiceViewController")
        })
    }

    
    func createRoom(){
        let room = AUICreateRoomInfo()
        room.roomName = "testService"
        room.thumbnail = "https://img0.baidu.com/it/u=670068607,3062725755&fm=253&app=138&size=w931&n=0&f=JPEG&fmt=auto?sec=1691254800&t=ff17ee347ad4e356d3060b06ba5d4cbb"
        room.micSeatCount = 8
    
        self.roomManager?.createRoom(room: room, callback: { error, info in
            aui_info("createRoom error == \(error), info = \(info)")
        })
    }
    
    func joinRoom(){
        generateToken { config, appid in
            guard let roomInfo = self.roomList.first else { return }
            
            self.chatImplement = AUIIMManagerServiceImplement(channelName: roomInfo.roomId,
                                                              rtmManager: self.roomManager!.rtmManager)
            self.chatImplement?.bindRespDelegate(delegate: self)
            
            self.roomManager?.enterRoom(roomId: roomInfo.roomId, callback: { err in
                aui_info("enterRoom == \(err)")
                
                
                self.userManager = AUIUserServiceImpl(channelName: roomInfo.roomId, rtmManager: self.roomManager!.rtmManager, roomManager: self.roomManager!)
                self.userManager?.bindRespDelegate(delegate: self)
                
                
                self.micSeatImpl = AUIMicSeatServiceImpl(channelName: roomInfo.roomId,
                                                         rtmManager: self.roomManager!.rtmManager,
                                                         roomManager: self.roomManager!)
                self.micSeatImpl?.bindRespDelegate(delegate: self)
                
                
                
                let commonConfig = AUICommonConfig()
                commonConfig.appId = appid
                
                let rtcEngine = self._createRtcEngine(commonConfig: commonConfig)
    
                let userId = Int(self.roomManager?.commonConfig.userId ?? "") ?? 0
                let config = KTVApiConfig(appId: appid,
                                          rtmToken: config.rtcRtmToken,
                                          engine: rtcEngine,
                                          channelName: roomInfo.roomId,
                                          localUid: userId,
                                          chorusChannelName: config.rtcChorusChannelName,
                                          chorusChannelToken: config.rtcChorusRtcToken,
                                          type: .normal,
                                          maxCacheSize: 10)
                self.ktvApi = KTVApiImpl.init(config: config)
                self.musicImpl =  AUIMusicServiceImpl(channelName: roomInfo.roomId,
                                                      rtmManager: self.roomManager!.rtmManager,
                                                      ktvApi: self.ktvApi)
                self.musicImpl?.bindRespDelegate(delegate: self)
                
                
                self.chorusImpl =  AUIChorusServiceImpl(channelName: roomInfo.roomId, rtcKit: rtcEngine, ktvApi: self.ktvApi, rtmManager: self.roomManager!.rtmManager)
                self.chorusImpl?.bindRespDelegate(delegate: self)
                
            })
        }
    }
    
}

extension TestServiceViewController {
    
    private func generateToken(completion:@escaping ((AUIRoomConfig, String)->())) {
        let roomInfo = self.roomList.first
        let uid = "123"
        let channelName = roomInfo?.roomId ?? ""
        let rtcChannelName = "\(channelName)_rtc"
        let rtcChorusChannelName = "\(channelName)_rtc_ex"
        let roomConfig = AUIRoomConfig()
        roomConfig.channelName = channelName
        roomConfig.rtcChannelName = rtcChannelName
        roomConfig.rtcChorusChannelName = rtcChorusChannelName
        print("generateTokens: \(uid)")
        
        var appId = ""
        
        let group = DispatchGroup()
        
        group.enter()
        let tokenModel1 = AUITokenGenerateNetworkModel()
        tokenModel1.channelName = channelName
        tokenModel1.userId = uid
        tokenModel1.request { error, result in
            defer {
                group.leave()
            }
            
            guard let tokenMap = result as? [String: String], tokenMap.count >= 2 else {return}
            
            roomConfig.rtcToken007 = tokenMap["rtcToken"] ?? ""
            roomConfig.rtmToken007 = tokenMap["rtmToken"] ?? ""
            appId = tokenMap["appId"] ?? ""
        }
        
        group.enter()
        let tokenModel2 = AUITokenGenerateNetworkModel()
        tokenModel2.channelName = rtcChannelName
        tokenModel2.userId = uid
        tokenModel2.request { error, result in
            defer {
                group.leave()
            }
            
            guard let tokenMap = result as? [String: String], tokenMap.count >= 2 else {return}
            
            roomConfig.rtcRtcToken = tokenMap["rtcToken"] ?? ""
            roomConfig.rtcRtmToken = tokenMap["rtmToken"] ?? ""
        }
        
        group.enter()
        let tokenModel3 = AUITokenGenerateNetworkModel()
        tokenModel3.channelName = rtcChorusChannelName
        tokenModel3.userId = uid
        tokenModel3.request { error, result in
            defer {
                group.leave()
            }
            
            guard let tokenMap = result as? [String: String], tokenMap.count >= 2 else {return}
            
            roomConfig.rtcChorusRtcToken = tokenMap["rtcToken"] ?? ""
        }
        
        group.notify(queue: DispatchQueue.main) {
            completion(roomConfig, appId)
            AUIRoomContext.shared.roomConfigMap[channelName] = roomConfig
            AUIRoomContext.shared.roomInfoMap[channelName] = roomInfo
        }
    }

}



extension TestServiceViewController: AUIRoomManagerRespDelegate {
    func onRoomDestroy(roomId: String) {
        aui_info(" onRoomDestroy roomId = \(roomId)", tag: "TestServiceViewController")
    }
    
    func onRoomInfoChange(roomId: String, roomInfo: AUIKitCore.AUIRoomInfo) {
        
    }
    
    func onRoomAnnouncementChange(roomId: String, announcement: String) {
        
    }
    
    func onRoomUserBeKicked(roomId: String, userId: String) {
        
    }
    
    
}

extension TestServiceViewController: AUIUserRespDelegate {
    func onRoomUserSnapshot(roomId: String, userList: [AUIKitCore.AUIUserInfo]) {
        aui_info("onRoomUserSnapshot", tag: "TestServiceViewController")
    }
    
    func onRoomUserEnter(roomId: String, userInfo: AUIKitCore.AUIUserInfo) {
        aui_info("onRoomUserEnter roomId = \(roomId)", tag: "TestServiceViewController")
    }
    
    func onRoomUserLeave(roomId: String, userInfo: AUIKitCore.AUIUserInfo) {
        aui_info("onRoomUserLeave roomId = \(roomId)", tag: "TestServiceViewController")

    }
    
    func onRoomUserUpdate(roomId: String, userInfo: AUIKitCore.AUIUserInfo) {
        aui_info("onRoomUserUpdate roomId = \(roomId)", tag: "TestServiceViewController")

    }
    
    func onUserAudioMute(userId: String, mute: Bool) {
        aui_info("onUserAudioMute userID = \(userId)", tag: "TestServiceViewController")

    }
    
    func onUserVideoMute(userId: String, mute: Bool) {
        aui_info("onUserVideoMute userid = \(userId)", tag: "TestServiceViewController")

    }
    
    func onUserBeKicked(roomId: String, userId: String) {
        aui_info("onUserBeKicked roomId = \(roomId), userid = \(userId)", tag: "TestServiceViewController")

    }
    
    
}

extension TestServiceViewController: AUIMicSeatRespDelegate{
    
    func onAnchorEnterSeat(seatIndex: Int, user: AUIKitCore.AUIUserThumbnailInfo) {
        aui_info("onAnchorEnterSeat seatIndex = \(seatIndex), userid = \(user.userId)", tag: "TestServiceViewController")
    }
    
    func onAnchorLeaveSeat(seatIndex: Int, user: AUIKitCore.AUIUserThumbnailInfo) {
        aui_info("onAnchorLeaveSeat seatIndex = \(seatIndex), userid = \(user.userId)", tag: "TestServiceViewController")
    }
    
    func onSeatAudioMute(seatIndex: Int, isMute: Bool) {
        aui_info("onSeatAudioMute seatIndex = \(seatIndex), isMute = \(isMute)", tag: "TestServiceViewController")

    }
    
    func onSeatVideoMute(seatIndex: Int, isMute: Bool) {
        aui_info("onSeatVideoMute seatIndex = \(seatIndex),  isMute = \(isMute)", tag: "TestServiceViewController")
    }
    
    func onSeatClose(seatIndex: Int, isClose: Bool) {
        aui_info("onSeatClose seatIndex = \(seatIndex),  isClose = \(isClose)", tag: "TestServiceViewController")
    }
}

extension TestServiceViewController: AUIMManagerRespDelegate {
    
    func messageDidReceive(roomId: String, message: AUIKitCore.AgoraChatTextMessage) {
        aui_info("messageDidReceive roomId = \(roomId), message = \(message.description)",tag: "TestServiceViewController")
    }
    
    func onUserDidJoinRoom(roomId: String, message: AUIKitCore.AgoraChatTextMessage) {
        aui_info("onUserDidJoinRoom roomId = \(roomId), message = \(message.description)",tag: "TestServiceViewController")
    }
    
  
}

extension TestServiceViewController: AUIMusicRespDelegate {
    func onAddChooseSong(song: AUIKitCore.AUIChooseMusicModel) {
        aui_info("onAddChooseSong song = \(song.songCode)",tag: "TestServiceViewController")
    }
    
    func onRemoveChooseSong(song: AUIKitCore.AUIChooseMusicModel) {
        aui_info("onRemoveChooseSong song = \(song.songCode)",tag: "TestServiceViewController")

    }
    
    func onUpdateChooseSong(song: AUIKitCore.AUIChooseMusicModel) {
        aui_info("onUpdateChooseSong song = \(song.songCode)",tag: "TestServiceViewController")
    }
    
    func onUpdateAllChooseSongs(songs: [AUIKitCore.AUIChooseMusicModel]) {
        aui_info("onUpdateAllChooseSongs song = \(songs.debugDescription)",tag: "TestServiceViewController")

    }
}


extension TestServiceViewController: AUIChorusRespDelegate {
    
    func onChoristerDidEnter(chorister: AUIKitCore.AUIChoristerModel) {
        aui_info("onChoristerDidEnter chorister = \(chorister.userId)",tag: "TestServiceViewController")

    }
    
    func onChoristerDidLeave(chorister: AUIKitCore.AUIChoristerModel) {
        aui_info("onChoristerDidLeave chorister = \(chorister.userId)",tag: "TestServiceViewController")
    }

}

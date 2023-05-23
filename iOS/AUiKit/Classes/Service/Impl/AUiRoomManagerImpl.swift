//
//  AUiRoomManagerImpl.swift
//  AUiKit
//
//  Created by wushengtao on 2023/2/24.
//

import Foundation
import AgoraRtcKit
import YYModel

let kRoomInfoAttrKry = "basic"
let kSeatAttrKry = "micSeat"

let kUserInfoAttrKey = "basic"
let kUserMuteAttrKey = "mute"

//房间Service实现
open class AUiRoomManagerImpl: NSObject {
    private var respDelegates: NSHashTable<AnyObject> = NSHashTable<AnyObject>.weakObjects()
    private lazy var rtmClient: AgoraRtmClientKit = createRtmClient()
    public private(set) var commonConfig: AUiCommonConfig!
    public private(set) lazy var rtmManager: AUiRtmManager = {
        return AUiRtmManager(rtmClient: self.rtmClient)
    }()
    
    deinit {
        //rtmManager.logout()
        aui_info("deinit AUiRoomManagerImpl", tag: "AUiRoomManagerImpl")
    }
    
    public required init(commonConfig: AUiCommonConfig, rtmClient: AgoraRtmClientKit? = nil) {
        super.init()
        self.commonConfig = commonConfig
        if let rtmClient = rtmClient {
            self.rtmClient = rtmClient
        }
        AUiRoomContext.shared.commonConfig = commonConfig
        aui_info("init AUiRoomManagerImpl", tag: "AUiRoomManagerImpl")
    }
    
    private func createRtmClient() -> AgoraRtmClientKit {
        let rtmConfig = AgoraRtmClientConfig()
        rtmConfig.userId = commonConfig.userId
        rtmConfig.appId = commonConfig.appId
        if rtmConfig.userId.count == 0 {
            aui_error("userId is empty")
            assert(false, "userId is empty")
        }
        if rtmConfig.appId.count == 0 {
            aui_error("appId is empty, please check 'AUiRoomContext.shared.commonConfig.appId' ")
            assert(false, "appId is empty, please check 'AUiRoomContext.shared.commonConfig.appId' ")
        }
        
        let rtmClient = AgoraRtmClientKit(config: rtmConfig, delegate: nil)!
        return rtmClient
    }
}

extension AUiRoomManagerImpl: AUiRoomManagerDelegate {
    public func bindRespDelegate(delegate: AUiRoomManagerRespDelegate) {
        respDelegates.add(delegate)
    }
    
    public func unbindRespDelegate(delegate: AUiRoomManagerRespDelegate) {
        respDelegates.remove(delegate)
    }
    
    public func createRoom(room: AUiCreateRoomInfo, callback: @escaping (NSError?, AUiRoomInfo?) -> ()) {
        let model = AUiRoomCreateNetworkModel()
        model.roomName = room.roomName
        model.userId = AUiRoomContext.shared.currentUserInfo.userId
        model.userName = AUiRoomContext.shared.currentUserInfo.userName
        model.userAvatar = AUiRoomContext.shared.currentUserInfo.userAvatar
        model.request {/*[weak self]*/ error, resp in
//            guard let self = self else {return}
//            if let room = resp as? AUiRoomInfo {
//                self.rtmManager.subscribeNSError(channelName: room.roomId, delegate: self)
//            }
            callback(error as? NSError, resp as? AUiRoomInfo)
        }
    }
    
    public func destroyRoom(roomId: String, callback: @escaping (NSError?) -> ()) {
        aui_info("destroyRoom: \(roomId)", tag: "AUiRoomManagerImpl")
        self.rtmManager.unSubscribe(channelName: roomId)
        
        let model = AUiRoomDestoryNetworkModel()
        model.userId = AUiRoomContext.shared.currentUserInfo.userId
        model.roomId = roomId
        model.request { error, _ in
            callback(error as? NSError)
        }
        rtmManager.unsubscribeError(channelName: roomId, delegate: self)
        rtmManager.logout()
    }
    
    public func enterRoom(roomId: String, callback:@escaping (NSError?) -> ()) {
        aui_info("enterRoom: \(roomId) ", tag: "AUiRoomManagerImpl")
        
        let rtmToken = AUiRoomContext.shared.roomConfigMap[roomId]?.rtmToken007 ?? ""
        guard rtmManager.isLogin else {
            rtmManager.login(token: rtmToken) {[weak self] err in
                if let err = err {
                    callback(err as NSError)
                    return
                }
                self?.enterRoom(roomId: roomId, callback: callback)
            }

            return
        }
        
        guard let roomConfig = AUiRoomContext.shared.roomConfigMap[roomId] else {
            assert(false)
            aui_info("enterRoom: \(roomId) fail", tag: "AUiRoomManagerImpl")
            callback(AUiCommonError.missmatchRoomConfig.toNSError())
            return
        }
        aui_info("enterRoom subscribe: \(roomId)", tag: "AUiRoomManagerImpl")
        rtmManager.subscribe(channelName: roomId, rtcToken: roomConfig.rtcToken007) { error in
            aui_info("enterRoom subscribe finished \(roomId) \(error?.localizedDescription ?? "")", tag: "AUiRoomManagerImpl")
            callback(error as? NSError)
        }
        
        self.rtmManager.subscribeError(channelName: roomId, delegate: self)
    }
    
    public func exitRoom(roomId: String, callback: @escaping (NSError?) -> ()) {
        aui_info("exitRoom: \(roomId)", tag: "AUiRoomManagerImpl")
        self.rtmManager.unSubscribe(channelName: roomId)
        rtmManager.logout()
        callback(nil)
    }
    
    public func getRoomInfoList(lastCreateTime: Int64?, pageSize: Int, callback: @escaping AUiRoomListCallback) {
        let model = AUiRoomListNetworkModel()
        model.lastCreateTime = lastCreateTime == nil ? nil : NSNumber(value: Int(lastCreateTime!))
        model.pageSize = pageSize
        model.request { error, list in
            callback(error as NSError?, list as? [AUiRoomInfo])
        }
    }
}

extension AUiRoomManagerImpl: AUiRtmErrorProxyDelegate {
    @objc public func onMsgRecvEmpty(channelName: String) {
        self.respDelegates.allObjects.forEach { obj in
            guard let delegate = obj as? AUiRoomManagerRespDelegate else {return}
            delegate.onRoomDestroy(roomId: channelName)
        }
    }
}

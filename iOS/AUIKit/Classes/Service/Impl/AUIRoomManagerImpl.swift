//
//  AUIRoomManagerImpl.swift
//  AUIKit
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
@objc open class AUIRoomManagerImpl: NSObject {
    
    private var respDelegates: NSHashTable<AnyObject> = NSHashTable<AnyObject>.weakObjects()
    private lazy var rtmClient: AgoraRtmClientKit = createRtmClient()
    public private(set) var commonConfig: AUICommonConfig!
    public private(set) lazy var rtmManager: AUIRtmManager = {
        return AUIRtmManager(rtmClient: self.rtmClient)
    }()
    
    deinit {
        //rtmManager.logout()
        aui_info("deinit AUIRoomManagerImpl", tag: "AUIRoomManagerImpl")
    }
    
    public required init(commonConfig: AUICommonConfig, rtmClient: AgoraRtmClientKit? = nil) {
        super.init()
        self.commonConfig = commonConfig
        if let rtmClient = rtmClient {
            self.rtmClient = rtmClient
        }
        AUIRoomContext.shared.commonConfig = commonConfig
        aui_info("init AUIRoomManagerImpl", tag: "AUIRoomManagerImpl")
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
            aui_error("appId is empty, please check 'AUIRoomContext.shared.commonConfig.appId' ")
            assert(false, "appId is empty, please check 'AUIRoomContext.shared.commonConfig.appId' ")
        }
        
        let rtmClient = AgoraRtmClientKit(config: rtmConfig, delegate: nil)!
        return rtmClient
    }
    
}

extension AUIRoomManagerImpl: AUIRoomManagerDelegate {
    public func bindRespDelegate(delegate: AUIRoomManagerRespDelegate) {
        respDelegates.add(delegate)
    }
    
    public func unbindRespDelegate(delegate: AUIRoomManagerRespDelegate) {
        respDelegates.remove(delegate)
    }
    
    public func createRoom(room: AUICreateRoomInfo, callback: @escaping (NSError?, AUIRoomInfo?) -> ()) {
        let model = AUIRoomCreateNetworkModel()
        model.roomName = room.roomName
        model.userId = AUIRoomContext.shared.currentUserInfo.userId
        model.userName = AUIRoomContext.shared.currentUserInfo.userName
        model.userAvatar = AUIRoomContext.shared.currentUserInfo.userAvatar
        model.micSeatCount = room.seatCount
        model.micSeatStyle = room.seatStyle
        model.request {/*[weak self]*/ error, resp in
//            guard let self = self else {return}
//            if let room = resp as? AUIRoomInfo {
//                self.rtmManager.subscribeError(channelName: room.roomId, delegate: self)
//            }
            callback(error as? NSError, resp as? AUIRoomInfo)
        }
    }
    
    public func destroyRoom(roomId: String, callback: @escaping (NSError?) -> ()) {
        aui_info("destroyRoom: \(roomId)", tag: "AUIRoomManagerImpl")
        self.rtmManager.unSubscribe(channelName: roomId)
        
        let model = AUIRoomDestoryNetworkModel()
        model.userId = AUIRoomContext.shared.currentUserInfo.userId
        model.roomId = roomId
        model.request { error, _ in
            callback(error as? NSError)
        }
        rtmManager.unsubscribeError(channelName: roomId, delegate: self)
        rtmManager.logout()
    }
    
    public func enterRoom(roomId: String, callback:@escaping (NSError?) -> ()) {
        aui_info("enterRoom: \(roomId) ", tag: "AUIRoomManagerImpl")
        
        let rtmToken = AUIRoomContext.shared.roomConfigMap[roomId]?.rtmToken007 ?? ""
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
        
        guard let roomConfig = AUIRoomContext.shared.roomConfigMap[roomId] else {
            assert(false)
            aui_info("enterRoom: \(roomId) fail", tag: "AUIRoomManagerImpl")
            callback(AUICommonError.missmatchRoomConfig.toNSError())
            return
        }
        aui_info("enterRoom subscribe: \(roomId)", tag: "AUIRoomManagerImpl")
        rtmManager.subscribe(channelName: roomId, rtcToken: roomConfig.rtcToken007) { error in
            aui_info("enterRoom subscribe finished \(roomId) \(error?.localizedDescription ?? "")", tag: "AUIRoomManagerImpl")
            callback(error as? NSError)
        }
        
        self.rtmManager.subscribeError(channelName: roomId, delegate: self)
    }
    
    public func exitRoom(roomId: String, callback: @escaping (NSError?) -> ()) {
        aui_info("exitRoom: \(roomId)", tag: "AUIRoomManagerImpl")
        self.rtmManager.unSubscribe(channelName: roomId)
        rtmManager.logout()
        callback(nil)
    }
    
    public func getRoomInfoList(lastCreateTime: Int64?, pageSize: Int, callback: @escaping AUIRoomListCallback) {
        let model = AUIRoomListNetworkModel()
        model.lastCreateTime = lastCreateTime == nil ? nil : NSNumber(value: Int(lastCreateTime!))
        model.pageSize = pageSize
        model.request { error, list in
            callback(error as NSError?, list as? [AUIRoomInfo])
        }
    }
    
    public func changeRoomAnnouncement(roomId: String, announcement: String, callback: @escaping AUICallback) {
        let model = AUIRoomAnnouncementNetworkModel()
        model.notice = announcement
        model.roomId = roomId
        model.userId = AUIRoomContext.shared.currentUserInfo.userId
        model.request { error, _ in
            callback(error as NSError?)
        }
    }
}

extension AUIRoomManagerImpl: AUIRtmErrorProxyDelegate {
    @objc public func onMsgRecvEmpty(channelName: String) {
        self.respDelegates.allObjects.forEach { obj in
            guard let delegate = obj as? AUIRoomManagerRespDelegate else {return}
            delegate.onRoomDestroy(roomId: channelName)
        }
    }
}

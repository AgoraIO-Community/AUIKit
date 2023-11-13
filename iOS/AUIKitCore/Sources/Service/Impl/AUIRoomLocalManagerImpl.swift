//
//  AUIRoomLocalManagerImpl.swift
//  AUIKitCore
//
//  Created by wushengtao on 2023/11/2.
//

import Foundation
import AgoraRtcKit
import YYModel
import AgoraRtmKit

//let kRoomInfoAttrKry = "basic"
//let kSeatAttrKry = "micSeat"
//
//let kUserInfoAttrKey = "basic"
//let kUserMuteAttrKey = "mute"

//房间Service实现
@objc open class AUIRoomLocalManagerImpl: NSObject {
    
//    private var respDelegates: NSHashTable<AnyObject> = NSHashTable<AnyObject>.weakObjects()
    public private(set) var commonConfig: AUICommonConfig!
    
    deinit {
        //rtmManager.logout()
        aui_info("deinit AUIRoomManagerImpl", tag: "AUIRoomManagerImpl")
    }
    
    public required init(commonConfig: AUICommonConfig, rtmClient: AgoraRtmClientKit? = nil) {
        super.init()
        self.commonConfig = commonConfig
        AUIRoomContext.shared.commonConfig = commonConfig
        aui_info("init AUIRoomManagerImpl", tag: "AUIRoomManagerImpl")
    }
    
    
}

extension AUIRoomLocalManagerImpl {
//    public func bindRespDelegate(delegate: AUIRoomManagerRespDelegate) {
//        respDelegates.add(delegate)
//    }
//    
//    public func unbindRespDelegate(delegate: AUIRoomManagerRespDelegate) {
//        respDelegates.remove(delegate)
//    }
    
    public func createRoom(room: AUIRoomInfo, callback: @escaping (NSError?, AUIRoomInfo?) -> ()) {
        aui_info("enterRoom: \(room.roomName) ", tag: "AUIRoomManagerImpl")
        
        let model = AUIRoomCreateNetworkModel()
        model.roomName = room.roomName
        model.userId = AUIRoomContext.shared.currentUserInfo.userId
        model.userName = AUIRoomContext.shared.currentUserInfo.userName
        model.userAvatar = AUIRoomContext.shared.currentUserInfo.userAvatar
        model.micSeatCount = room.micSeatCount
        model.micSeatStyle = "\(room.micSeatStyle)"
        
        var createRoomError: NSError? = nil
        var roomInfo: AUIRoomInfo? = nil
        //create a room from the server
        model.request { error, resp in
            createRoomError = error as? NSError
            roomInfo = resp as? AUIRoomInfo
            callback(createRoomError, roomInfo)
        }
    }
    
    public func destroyRoom(roomId: String, callback: @escaping (NSError?) -> ()) {
//        aui_info("destroyRoom: \(roomId)", tag: "AUIRoomManagerImpl")
//        cleanUserInfo(channelName: roomId)
//        self.rtmManager.unSubscribe(channelName: roomId)
        
        let model = AUIRoomDestroyNetworkModel()
        model.userId = AUIRoomContext.shared.currentUserInfo.userId
        model.roomId = roomId
        model.request { error, _ in
            callback(error as? NSError)
        }
//        rtmManager.unsubscribeError(channelName: roomId, delegate: self)
//        rtmManager.removeLock(channelName: roomId, lockName: kRTM_Referee_LockName) { err in
//        }
//        rtmManager.logout()
    }
    
    public func enterRoom(roomId: String, callback:@escaping (NSError?) -> ()) {
        aui_info("enterRoom: \(roomId) ", tag: "AUIRoomManagerImpl")
        assert(false, "ignore")
//        guard let roomConfig = AUIRoomContext.shared.roomConfigMap[roomId] else {
//            assert(false)
//            aui_info("enterRoom: \(roomId) fail", tag: "AUIRoomManagerImpl")
//            callback(AUICommonError.missmatchRoomConfig.toNSError())
//            return
//        }
//        
//        let rtmToken = roomConfig.rtmToken007
//        assert(!rtmToken.isEmpty, "rtm token invalid")
//        guard rtmManager.isLogin else {
//            rtmManager.login(token: rtmToken) {[weak self] err in
//                if let err = err {
//                    callback(err as NSError)
//                    return
//                }
//                self?.enterRoom(roomId: roomId, callback: callback)
//            }
//            return
//        }
//        
//        aui_info("enterRoom subscribe: \(roomId)", tag: "AUIRoomManagerImpl")
//        rtmManager.subscribe(channelName: roomId, rtcToken: roomConfig.rtcToken007) { error in
//            aui_info("enterRoom subscribe finished \(roomId) \(error?.localizedDescription ?? "")", tag: "AUIRoomManagerImpl")
//            callback(error as? NSError)
//        }
//        rtmManager.setLock(channelName: roomId, lockName: kRTM_Referee_LockName) {[weak self] err in
//            self?.rtmManager.acquireLock(channelName: roomId, lockName: kRTM_Referee_LockName) { err in
//                
//            }
//        }
//        
//        self.rtmManager.subscribeError(channelName: roomId, delegate: self)
    }
    
    public func exitRoom(roomId: String, callback: @escaping (NSError?) -> ()) {
//        aui_info("exitRoom: \(roomId)", tag: "AUIRoomManagerImpl")
//        cleanUserInfo(channelName: roomId)
//        self.rtmManager.unSubscribe(channelName: roomId)
//        
//        self.rtmManager.unsubscribeError(channelName: roomId, delegate: self)
//        rtmManager.logout()
//        callback(nil)
    }
    
    public func getRoomInfoList(lastCreateTime: Int64, pageSize: Int, callback: @escaping AUIRoomListCallback) {
        let model = AUIRoomListNetworkModel()
        model.lastCreateTime = lastCreateTime == 0 ? nil : NSNumber(value: Int(lastCreateTime))
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

extension AUIRoomLocalManagerImpl: AUIRtmErrorProxyDelegate {
//    @objc public func onMsgRecvEmpty(channelName: String) {
//        self.respDelegates.allObjects.forEach { obj in
//            guard let delegate = obj as? AUIRoomManagerRespDelegate else {return}
//            delegate.onRoomDestroy?(roomId: channelName)
//        }
//    }
//    
//    @objc public func onConnectionStateChanged(channelName: String,
//                                               connectionStateChanged state: AgoraRtmClientConnectionState,
//                                               result reason: AgoraRtmClientConnectionChangeReason) {
//        if reason == .changedRejoinSuccess {
//            rtmManager.acquireLock(channelName: channelName, lockName: kRTM_Referee_LockName) { err in
//            }
//        }
//        guard state == .failed, reason == .changedBannedByServer else {
//            return
//        }
//        
//        for obj in self.respDelegates.allObjects {
//            (obj as? AUIRoomManagerRespDelegate)?.onRoomUserBeKicked?(roomId: channelName, userId: AUIRoomContext.shared.currentUserInfo.userId)
//        }
//    }
}

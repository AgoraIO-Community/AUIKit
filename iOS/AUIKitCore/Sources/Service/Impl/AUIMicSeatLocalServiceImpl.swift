//
//  AUIMicSeatLocalServiceImpl.swift
//  AUIKitCore
//
//  Created by wushengtao on 2023/10/27.
//

import Foundation
import AgoraRtmKit

//麦位Service实现(纯端上修改KV)
@objc open class AUIMicSeatLocalServiceImpl: NSObject {
    private var respDelegates: NSHashTable<AnyObject> = NSHashTable<AnyObject>.weakObjects()
    private var channelName: String!
    private let rtmManager: AUIRtmManager!
    private let roomManager: AUIRoomManagerDelegate!
    
    private var lockOwnerId: String?
    
    private var micSeats:[Int: AUIMicSeatInfo] = [:]
    
    deinit {
        self.rtmManager.unsubscribeAttributes(channelName: getChannelName(), itemKey: kSeatAttrKry, delegate: self)
        aui_info("deinit AUIMicSeatServiceImpl", tag: "AUIMicSeatServiceImpl")
    }
    
    public init(channelName: String, rtmManager: AUIRtmManager, roomManager: AUIRoomManagerDelegate) {
        self.rtmManager = rtmManager
        self.channelName = channelName
        self.roomManager = roomManager
        super.init()
        rtmManager.subscribeAttributes(channelName: getChannelName(), itemKey: kSeatAttrKry, delegate: self)
        rtmManager.subscribeMessage(channelName: getChannelName(), delegate: self)
        rtmManager.subscribeLock(channelName: getChannelName(), lockName: kRTM_Referee_LockName, delegate: self)
        aui_info("init AUIMicSeatServiceImpl", tag: "AUIMicSeatServiceImpl")
    }
}

extension AUIMicSeatLocalServiceImpl: AUIRtmAttributesProxyDelegate {
    public func onAttributesDidChanged(channelName: String, key: String, value: Any) {
        if key == kSeatAttrKry {
            aui_info("recv seat attr did changed \(value)", tag: "AUIMicSeatServiceImpl")
            guard let map = value as? [String: [String: Any]] else {return}
            map.values.forEach { element in
                guard let micSeat = AUIMicSeatInfo.yy_model(with: element) else {return}
                aui_info(" micSeat.islock \(micSeat.lockSeat) micSeat.Index = \(micSeat.seatIndex)", tag: "AUIMicSeatServiceImpl")
                let index: Int = Int(micSeat.seatIndex)
                let origMicSeat = self.micSeats[index]
//                if let origMicSeat = origMicSeat {
//                    origMicSeat.user = roomManager.getUserInfo(by: origMicSeat.userId)
//                }
//                micSeat.user = roomManager.getUserInfo(by: micSeat.userId)
                self.micSeats[index] = micSeat
                self.respDelegates.allObjects.forEach { obj in
                    guard let delegate = obj as? AUIMicSeatRespDelegate else {
                        return
                    }
                    
                    if let origUser = origMicSeat?.user, origUser.userId.count > 0, micSeat.user?.userId ?? "" != origUser.userId {
                        delegate.onAnchorLeaveSeat(seatIndex: index, user: origUser)
                    }
                    
                    if let user = micSeat.user, user.userId.count > 0, origMicSeat?.user?.userId ?? "" != user.userId {
                        delegate.onAnchorEnterSeat(seatIndex: index, user: user)
                    }
                    
                    if origMicSeat?.lockSeat ?? .idle != micSeat.lockSeat {
                        delegate.onSeatClose(seatIndex: index, isClose: micSeat.lockSeat == .locked)
                    }
                    
                    if origMicSeat?.muteAudio ?? false != micSeat.muteAudio {
                        delegate.onSeatAudioMute(seatIndex: index, isMute: micSeat.muteAudio)
                    }
                    
                    if origMicSeat?.muteVideo ?? false != micSeat.muteVideo {
                        delegate.onSeatVideoMute(seatIndex: index, isMute: micSeat.muteVideo)
                    }
                    /*
                    if origMicSeat?.muteVideo != micSeat.muteVideo {
                        delegate.onSeatVideoMute(seatIndex: index, isMute: micSeat.muteVideo)
                    }
                    
                    if origMicSeat?.muteAudio != micSeat.muteAudio {
                        delegate.onSeatAudioMute(seatIndex: index, isMute: micSeat.muteAudio)
                    }
                     */
                }
            }
        }
    }
}


extension AUIMicSeatLocalServiceImpl: AUIMicSeatServiceDelegate {
    
    public func getRoomContext() -> AUIRoomContext {
        return AUIRoomContext.shared
    }
    
    public func getChannelName() -> String {
        return channelName
    }
    
    public func bindRespDelegate(delegate: AUIMicSeatRespDelegate) {
        respDelegates.add(delegate)
    }
    
    public func unbindRespDelegate(delegate: AUIMicSeatRespDelegate) {
        respDelegates.remove(delegate)
    }
    
    public func enterSeat(seatIndex: Int, callback: @escaping (NSError?) -> ()) {
        if lockOwnerId == getRoomContext().currentUserInfo.userId {
            rtmEnterSeat(seatIndex: seatIndex, userInfo: getRoomContext().currentUserInfo, callback: callback)
            return
        }
        
        let model = AUISeatEnterNetworkModel()
        model.roomId = channelName
        model.userAvatar = getRoomContext().currentUserInfo.userAvatar
        model.userId = getRoomContext().currentUserInfo.userId
        model.userName = getRoomContext().currentUserInfo.userName
        model.micSeatNo = seatIndex
        
        let message = model.rtmMessage()
        rtmManager.publish(channelName: channelName, message: message, completion: callback)
    }
    
    public func leaveSeat(callback: @escaping (NSError?) -> ()) {
        if lockOwnerId == getRoomContext().currentUserInfo.userId {
            rtmLeaveSeat(userId: getRoomContext().currentUserInfo.userId, callback: callback)
            return
        }
        
        
        let model = AUISeatLeaveNetworkModel()
        model.roomId = channelName
        model.userId = getRoomContext().currentUserInfo.userId
        
        let message = model.rtmMessage()
        rtmManager.publish(channelName: channelName, message: message, completion: callback)
    }
    
    public func pickSeat(seatIndex: Int, userId: String, callback: @escaping (NSError?) -> ()) {
    }
    
    public func kickSeat(seatIndex: Int, callback: @escaping (NSError?) -> ()) {
        if lockOwnerId == getRoomContext().currentUserInfo.userId {
            rtmKickSeat(seatIndex: seatIndex, callback: callback)
            return
        }
        
        let model = AUISeatKickNetworkModel()
        model.roomId = channelName
        model.userId = getRoomContext().currentUserInfo.userId
        model.micSeatNo = seatIndex
        
        let message = model.rtmMessage()
        rtmManager.publish(channelName: channelName, message: message, completion: callback)
    }
    
    public func muteAudioSeat(seatIndex: Int, isMute: Bool, callback: @escaping (NSError?) -> ()) {
        if lockOwnerId == getRoomContext().currentUserInfo.userId {
            rtmMuteAudioSeat(seatIndex: seatIndex, isMute: isMute, callback: callback)
            return
        }
        
        if isMute {
            let model = AUISeatMuteAudioNetworkModel()
            model.roomId = channelName
            model.micSeatNo = seatIndex
            model.userId = getRoomContext().currentUserInfo.userId
            
            let message = model.rtmMessage()
            rtmManager.publish(channelName: channelName, message: message, completion: callback)
        }else {
            let model = AUISeatUnMuteAudioNetworkModel()
            model.roomId = channelName
            model.micSeatNo = seatIndex
            model.userId = getRoomContext().currentUserInfo.userId

            let message = model.rtmMessage()
            rtmManager.publish(channelName: channelName, message: message, completion: callback)
        }
    }
    
    public func muteVideoSeat(seatIndex: Int, isMute: Bool, callback: @escaping AUICallback) {
    }
    
    public func closeSeat(seatIndex: Int, isClose: Bool, callback: @escaping (NSError?) -> ()) {
        if lockOwnerId == getRoomContext().currentUserInfo.userId {
            rtmCloseSeat(seatIndex: seatIndex, isClose: isClose, callback: callback)
            return
        }
        
        if isClose {
            let model = AUISeatLockNetworkModel()
            model.roomId = channelName
            model.micSeatNo = seatIndex
            model.userId = getRoomContext().currentUserInfo.userId
            
            let message = model.rtmMessage()
            rtmManager.publish(channelName: channelName, message: message, completion: callback)
        }else {
            let model = AUISeatUnLockNetworkModel()
            model.roomId = channelName
            model.micSeatNo = seatIndex
            model.userId = getRoomContext().currentUserInfo.userId
            
            let message = model.rtmMessage()
            rtmManager.publish(channelName: channelName, message: message, completion: callback)
        }
    }
}

//MARK: set KV
extension AUIMicSeatLocalServiceImpl {
    private func rtmEnterSeat(seatIndex: Int, userInfo: AUIUserThumbnailInfo, callback: @escaping (NSError?) -> ()) {
        if self.micSeats.values.contains(where: { $0.user?.userId == userInfo.userId }) {
            callback(AUICommonError.micSeatAlreadyEnter.toNSError())
            return
        }
        guard let seat = self.micSeats[seatIndex], seat.lockSeat == .idle, seat.user?.isEmpty() ?? true else {
            callback(AUICommonError.micSeatNotIdle.toNSError())
            return
        }
        
        var seatMap: [String: [String: Any]] = [:]
        
        self.micSeats.forEach { (key: Int, value: AUIMicSeatInfo) in
            var map = value.yy_modelToJSONObject() as? [String: Any] ?? [:]
            if key == seatIndex {
                map["owner"] = userInfo.yy_modelToJSONObject()
                map["micSeatStatus"] = AUILockSeatStatus.user.rawValue
            }
            seatMap["\(key)"] = map
        }
        
        let data = try! JSONSerialization.data(withJSONObject: seatMap, options: .prettyPrinted)
        let str = String(data: data, encoding: .utf8)!
        let metaData = [kSeatAttrKry: str]
        self.rtmManager.setMetadata(channelName: channelName, lockName: kRTM_Referee_LockName, metadata: metaData) { error in
            callback(error)
        }
    }
    
    private func rtmLeaveSeat(userId: String, callback: @escaping (NSError?) -> ()) {
        guard self.micSeats.values.contains(where: { $0.user?.userId == userId }) else {
            callback(AUICommonError.userNoEnterSeat.toNSError())
            return
        }
        
        var seatMap: [String: [String: Any]] = [:]
        
        self.micSeats.forEach { (key: Int, value: AUIMicSeatInfo) in
            var map = value.yy_modelToJSONObject() as? [String: Any] ?? [:]
            if userId == value.user?.userId {
                map.removeValue(forKey: "owner")
                map["micSeatStatus"] = AUILockSeatStatus.idle.rawValue
            }
            seatMap["\(key)"] = map
        }
        
        let data = try! JSONSerialization.data(withJSONObject: seatMap, options: .prettyPrinted)
        let str = String(data: data, encoding: .utf8)!
        let metaData = [kSeatAttrKry: str]
        self.rtmManager.setMetadata(channelName: channelName, lockName: kRTM_Referee_LockName, metadata: metaData) { error in
            callback(error)
        }
    }
    
    private func rtmKickSeat(seatIndex: Int, callback: @escaping (NSError?) -> ()) {
        var seatMap: [String: [String: Any]] = [:]
        
        self.micSeats.forEach { (key: Int, value: AUIMicSeatInfo) in
            var map = value.yy_modelToJSONObject() as? [String: Any] ?? [:]
            if key == seatIndex {
                map.removeValue(forKey: "owner")
                map["micSeatStatus"] = AUILockSeatStatus.idle.rawValue
            }
            seatMap["\(key)"] = map
        }
        
        let data = try! JSONSerialization.data(withJSONObject: seatMap, options: .prettyPrinted)
        let str = String(data: data, encoding: .utf8)!
        let metaData = [kSeatAttrKry: str]
        self.rtmManager.setMetadata(channelName: channelName, lockName: kRTM_Referee_LockName, metadata: metaData) { error in
            callback(error)
        }
    }
    
    private func rtmMuteAudioSeat(seatIndex: Int, isMute: Bool, callback: @escaping (NSError?) -> ()) {
        var seatMap: [String: [String: Any]] = [:]
        self.micSeats.forEach { (key: Int, value: AUIMicSeatInfo) in
            var map = value.yy_modelToJSONObject() as? [String: Any] ?? [:]
            if key == seatIndex {
                map["isMuteAudio"] = isMute
            }
            seatMap["\(key)"] = map
        }
        
        let data = try! JSONSerialization.data(withJSONObject: seatMap, options: .prettyPrinted)
        let str = String(data: data, encoding: .utf8)!
        let metaData = [kSeatAttrKry: str]
        self.rtmManager.setMetadata(channelName: channelName, lockName: kRTM_Referee_LockName, metadata: metaData) { error in
            callback(error)
        }
    }
    
    private func rtmCloseSeat(seatIndex: Int, isClose: Bool, callback: @escaping (NSError?) -> ())  {
        var seatMap: [String: [String: Any]] = [:]
        self.micSeats.forEach { (key: Int, value: AUIMicSeatInfo) in
            var map = value.yy_modelToJSONObject() as? [String: Any] ?? [:]
            if key == seatIndex {
                if isClose {
                    map["micSeatStatus"] = AUILockSeatStatus.locked.rawValue
                } else if value.user?.isEmpty() ?? true {
                    map["micSeatStatus"] = AUILockSeatStatus.idle.rawValue
                } else {
                    map["micSeatStatus"] = AUILockSeatStatus.user.rawValue
                }
            }
            seatMap["\(key)"] = map
        }
        
        let data = try! JSONSerialization.data(withJSONObject: seatMap, options: .prettyPrinted)
        let str = String(data: data, encoding: .utf8)!
        let metaData = [kSeatAttrKry: str]
        self.rtmManager.setMetadata(channelName: channelName, lockName: kRTM_Referee_LockName, metadata: metaData) { error in
            callback(error)
        }
    }
}

extension AUIMicSeatLocalServiceImpl: AUIRtmLockProxyDelegate {
    public func onReceiveLockDetail(channelName: String, lockDetail: AgoraRtmLockDetail) {
        guard channelName == getChannelName() else {return}
        lockOwnerId = lockDetail.owner
    }
    
    public func onReleaseLockDetail(channelName: String, lockDetail: AgoraRtmLockDetail) {
        
    }
}

extension AUIMicSeatLocalServiceImpl: AUIRtmMessageProxyDelegate {
    public func onMessageReceive(channelName: String, message: String) {
        guard channelName == getChannelName(), lockOwnerId == getRoomContext().currentUserInfo.userId else {return}
        
        guard let data = message.data(using: .utf8),
              let map = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
              let interfaceName = map["interfaceName"] as? String else {
            return
        }
        aui_info("onMessageReceive[\(interfaceName)]", tag: "AUIMicSeatServiceImpl")
        if interfaceName == kAUISeatEnterNetworkInterface, let model = AUISeatEnterNetworkModel.model(rtmMessage: message) {
            let user = AUIUserThumbnailInfo()
            user.userId = model.userId ?? ""
            user.userAvatar = model.userAvatar ?? ""
            user.userName = model.userName ?? ""
            rtmEnterSeat(seatIndex: model.micSeatNo, userInfo: user) { err in
            }
        } else if interfaceName == kAUISeatLeaveNetworkInterface, let model = AUISeatLeaveNetworkModel.model(rtmMessage: message) {
            rtmLeaveSeat(userId: model.userId ?? "") { err in
            }
        } else if interfaceName == kAUISeatKickNetworkInterface, let model = AUISeatKickNetworkModel.model(rtmMessage: message) {
            rtmKickSeat(seatIndex: model.micSeatNo) { err in
            }
        } else if interfaceName == kAUISeatMuteAudioNetworkInterface, let model = AUISeatMuteAudioNetworkModel.model(rtmMessage: message) {
            rtmMuteAudioSeat(seatIndex: model.micSeatNo, isMute: true) { err in
            }
        } else if interfaceName == kAUISeatUnmuteAudioNetworkInterface, let model = AUISeatUnMuteAudioNetworkModel.model(rtmMessage: message) {
            rtmMuteAudioSeat(seatIndex: model.micSeatNo, isMute: false) { err in
            }
        } else if interfaceName == kAUISeatLockNetworkInterface, let model = AUISeatLockNetworkModel.model(rtmMessage: message) {
            rtmCloseSeat(seatIndex: model.micSeatNo, isClose: model.isLock == 1) { err in
            }
        }
    }
}

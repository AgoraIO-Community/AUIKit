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
    private var respDelegates: NSHashTable<AUIMicSeatRespDelegate> = NSHashTable<AUIMicSeatRespDelegate>.weakObjects()
    private var channelName: String!
    private let rtmManager: AUIRtmManager!
        
    private var micSeats:[Int: AUIMicSeatInfo] = [:]
    
    private var callbackMap: [String: ((NSError?)-> ())] = [:]
    
    deinit {
        rtmManager.unsubscribeAttributes(channelName: getChannelName(), itemKey: kSeatAttrKry, delegate: self)
        rtmManager.unsubscribeMessage(channelName: getChannelName(), delegate: self)
        aui_info("deinit AUIMicSeatServiceImpl", tag: "AUIMicSeatServiceImpl")
    }
    
    public init(channelName: String, rtmManager: AUIRtmManager) {
        self.rtmManager = rtmManager
        self.channelName = channelName
        super.init()
        rtmManager.subscribeAttributes(channelName: getChannelName(), itemKey: kSeatAttrKry, delegate: self)
        rtmManager.subscribeMessage(channelName: getChannelName(), delegate: self)
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
                
                self.micSeats[index] = micSeat
                self.respDelegates.allObjects.forEach { delegate in
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
        if getRoomContext().getArbiter(channelName: channelName)?.isArbiter() ?? false {
            rtmEnterSeat(seatIndex: seatIndex, userInfo: getRoomContext().currentUserInfo, callback: callback)
            return
        }
        
        let model = AUISeatEnterNetworkModel()
        model.roomId = channelName
        model.userAvatar = getRoomContext().currentUserInfo.userAvatar
        model.userId = getRoomContext().currentUserInfo.userId
        model.userName = getRoomContext().currentUserInfo.userName
        model.micSeatNo = seatIndex
        
        let date = Date()
        let message = model.rtmMessage()
        rtmManager.publish(channelName: channelName, message: message) { err in
        }
        callbackMap[model.uniqueId] = { err in
            aui_benchmark("enterSeat to arbiter", cost: -date.timeIntervalSinceNow, tag: "AUIMicSeatServiceImpl")
            callback(err)
        }
    }
    
    public func leaveSeat(callback: @escaping (NSError?) -> ()) {
        if getRoomContext().getArbiter(channelName: channelName)?.isArbiter() ?? false {
            rtmLeaveSeat(userId: getRoomContext().currentUserInfo.userId) { err in
            }
            return
        }
        
        let model = AUISeatLeaveNetworkModel()
        model.roomId = channelName
        model.userId = getRoomContext().currentUserInfo.userId
        
        let message = model.rtmMessage()
        rtmManager.publish(channelName: channelName, message: message) { err in
        }
        callbackMap[model.uniqueId] = callback
    }
    
    public func pickSeat(seatIndex: Int, userId: String, callback: @escaping (NSError?) -> ()) {
    }
    
    public func kickSeat(seatIndex: Int, callback: @escaping (NSError?) -> ()) {
        if getRoomContext().getArbiter(channelName: channelName)?.isArbiter() ?? false {
            rtmKickSeat(seatIndex: seatIndex, callback: callback)
            return
        }
        
        let model = AUISeatKickNetworkModel()
        model.roomId = channelName
        model.userId = getRoomContext().currentUserInfo.userId
        model.micSeatNo = seatIndex
        
        let message = model.rtmMessage()
        rtmManager.publish(channelName: channelName, message: message) { err in
        }
        callbackMap[model.uniqueId] = callback
    }
    
    public func muteAudioSeat(seatIndex: Int, isMute: Bool, callback: @escaping (NSError?) -> ()) {
        if getRoomContext().getArbiter(channelName: channelName)?.isArbiter() ?? false {
            rtmMuteAudioSeat(seatIndex: seatIndex, isMute: isMute, callback: callback)
            return
        }
        
        if isMute {
            let model = AUISeatMuteAudioNetworkModel()
            model.roomId = channelName
            model.micSeatNo = seatIndex
            model.userId = getRoomContext().currentUserInfo.userId
            
            let message = model.rtmMessage()
            rtmManager.publish(channelName: channelName, message: message) { err in
            }
            callbackMap[model.uniqueId] = callback
        }else {
            let model = AUISeatUnMuteAudioNetworkModel()
            model.roomId = channelName
            model.micSeatNo = seatIndex
            model.userId = getRoomContext().currentUserInfo.userId

            let message = model.rtmMessage()
            rtmManager.publish(channelName: channelName, message: message) { err in
            }
            callbackMap[model.uniqueId] = callback
        }
    }
    
    public func muteVideoSeat(seatIndex: Int, isMute: Bool, callback: @escaping AUICallback) {
    }
    
    public func closeSeat(seatIndex: Int, isClose: Bool, callback: @escaping (NSError?) -> ()) {
        if getRoomContext().getArbiter(channelName: channelName)?.isArbiter() ?? false {
            rtmCloseSeat(seatIndex: seatIndex, isClose: isClose, callback: callback)
            return
        }
        
        if isClose {
            let model = AUISeatLockNetworkModel()
            model.roomId = channelName
            model.micSeatNo = seatIndex
            model.userId = getRoomContext().currentUserInfo.userId
            
            let message = model.rtmMessage()
            rtmManager.publish(channelName: channelName, message: message) { err in
            }
            callbackMap[model.uniqueId] = callback
        }else {
            let model = AUISeatUnLockNetworkModel()
            model.roomId = channelName
            model.micSeatNo = seatIndex
            model.userId = getRoomContext().currentUserInfo.userId
            
            let message = model.rtmMessage()
            rtmManager.publish(channelName: channelName, message: message) { err in
            }
            callbackMap[model.uniqueId] = callback
        }
    }
    
    public func isOnMicSeat(userId: String) -> Bool {
        for (_, seat) in micSeats {
            if seat.user?.userId == userId {
                return true
            }
        }
        
        return false
    }
    
    public func getMicSeatIndex(userId: String) -> Int {
        for (idx, seat) in micSeats {
            if seat.user?.userId == userId {
                return idx
            }
        }
        return -1
    }
}

//MARK: set metadata
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
        self.rtmManager.setBatchMetadata(channelName: channelName,
                                         lockName: kRTM_Referee_LockName,
                                         metadata: metaData) { error in
            callback(error)
        }
    }
    
    private func rtmLeaveSeatMetaData(userId: String) -> NSMutableDictionary {
        guard self.micSeats.values.contains(where: { $0.user?.userId == userId }) else {
            return [:]
        }
        
        var seatMap: [String: [String: Any]] = [:]
        
        self.micSeats.forEach { (key: Int, value: AUIMicSeatInfo) in
            var map = value.yy_modelToJSONObject() as? [String: Any] ?? [:]
            if userId == value.user?.userId {
                map["owner"] = AUIUserThumbnailInfo().yy_modelToJSONObject()
                map["micSeatStatus"] = AUILockSeatStatus.idle.rawValue
            }
            seatMap["\(key)"] = map
        }
        
        let data = try! JSONSerialization.data(withJSONObject: seatMap, options: .prettyPrinted)
        let str = String(data: data, encoding: .utf8)!
        let metaData = NSMutableDictionary()
        metaData[kSeatAttrKry] = str
        return metaData
    }
    
    private func rtmLeaveSeat(userId: String, callback: @escaping (NSError?) -> ()) {
        guard self.micSeats.values.contains(where: { $0.user?.userId == userId }) else {
            callback(AUICommonError.userNoEnterSeat.toNSError())
            return
        }
        var err: NSError?
        let metaData = rtmLeaveSeatMetaData(userId: userId)
        for obj in respDelegates.allObjects {
            err = obj.onSeatWillLeave?(userId: userId, metaData: metaData)
            if let err = err {
                callback(err)
                break
            }
        }
        self.rtmManager.setBatchMetadata(channelName: channelName,
                                         lockName: kRTM_Referee_LockName,
                                         metadata: metaData as! [String : String]) { error in
            callback(error)
        }
    }
    
    private func rtmKickSeat(seatIndex: Int, callback: @escaping (NSError?) -> ()) {
        var seatMap: [String: [String: Any]] = [:]
        
        var userId: String?
        self.micSeats.forEach { (key: Int, value: AUIMicSeatInfo) in
            var map = value.yy_modelToJSONObject() as? [String: Any] ?? [:]
            if key == seatIndex {
                map["owner"] = AUIUserThumbnailInfo().yy_modelToJSONObject()
                map["micSeatStatus"] = AUILockSeatStatus.idle.rawValue
                userId = value.user?.userId
            }
            seatMap["\(key)"] = map
        }
        
        var err: NSError? = nil
        let metaData = NSMutableDictionary()
        for obj in respDelegates.allObjects {
            err = obj.onSeatWillLeave?(userId: userId!, metaData: metaData)
            if let _ = err {
                callback(err)
                break
            }
        }
        let data = try! JSONSerialization.data(withJSONObject: seatMap, options: .prettyPrinted)
        let str = String(data: data, encoding: .utf8)!
        metaData[kSeatAttrKry] = str
        
        self.rtmManager.setBatchMetadata(channelName: channelName,
                                         lockName: kRTM_Referee_LockName,
                                         metadata: metaData as! [String : String]) { error in
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
        self.rtmManager.setBatchMetadata(channelName: channelName,
                                         lockName: kRTM_Referee_LockName,
                                         metadata: metaData) { error in
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
        self.rtmManager.setBatchMetadata(channelName: channelName, 
                                         lockName: kRTM_Referee_LockName,
                                         metadata: metaData) { error in
            callback(error)
        }
    }
}

//MARK: AUIRtmMessageProxyDelegate
extension AUIMicSeatLocalServiceImpl: AUIRtmMessageProxyDelegate {
    public func onMessageReceive(channelName: String, message: String) {
        guard channelName == getChannelName() else {return}
        
        guard let data = message.data(using: .utf8),
              let map = try? JSONSerialization.jsonObject(with: data) as? [String: Any] else {
            return
        }
        let uniqueId = map["uniqueId"] as? String ?? ""
        guard let interfaceName = map["interfaceName"] as? String else {
            if let callback = callbackMap[uniqueId] {
                callbackMap[uniqueId] = nil
                let code = map["code"] as? Int ?? 0
                let reason = map["reason"] as? String ?? "success"
                callback(code == 0 ? nil : NSError(domain: "AUIKit Error", code: Int(code), userInfo: [ NSLocalizedDescriptionKey : reason]))
            }
            return
        }
        guard getRoomContext().getArbiter(channelName: channelName)?.isArbiter() ?? false else { return }
        aui_info("onMessageReceive[\(interfaceName)]", tag: "AUIMicSeatServiceImpl")
        if interfaceName == kAUISeatEnterNetworkInterface, let model = AUISeatEnterNetworkModel.model(rtmMessage: message) {
            let user = AUIUserThumbnailInfo()
            user.userId = model.userId ?? ""
            user.userAvatar = model.userAvatar ?? ""
            user.userName = model.userName ?? ""
            rtmEnterSeat(seatIndex: model.micSeatNo, userInfo: user) {[weak self] err in
                self?.rtmManager.sendReceipt(channelName: channelName, uniqueId: uniqueId, error: err)
            }
        } else if interfaceName == kAUISeatLeaveNetworkInterface, let model = AUISeatLeaveNetworkModel.model(rtmMessage: message) {
            rtmLeaveSeat(userId: model.userId ?? "") {[weak self] err in
                self?.rtmManager.sendReceipt(channelName: channelName, uniqueId: uniqueId, error: err)
            }
        } else if interfaceName == kAUISeatKickNetworkInterface, let model = AUISeatKickNetworkModel.model(rtmMessage: message) {
            rtmKickSeat(seatIndex: model.micSeatNo) {[weak self] err in
                self?.rtmManager.sendReceipt(channelName: channelName, uniqueId: uniqueId, error: err)
            }
        } else if interfaceName == kAUISeatMuteAudioNetworkInterface, let model = AUISeatMuteAudioNetworkModel.model(rtmMessage: message) {
            rtmMuteAudioSeat(seatIndex: model.micSeatNo, isMute: true) {[weak self] err in
                self?.rtmManager.sendReceipt(channelName: channelName, uniqueId: uniqueId, error: err)
            }
        } else if interfaceName == kAUISeatUnmuteAudioNetworkInterface, let model = AUISeatUnMuteAudioNetworkModel.model(rtmMessage: message) {
            rtmMuteAudioSeat(seatIndex: model.micSeatNo, isMute: false) {[weak self] err in
                self?.rtmManager.sendReceipt(channelName: channelName, uniqueId: uniqueId, error: err)
            }
        } else if interfaceName == kAUISeatLockNetworkInterface, let model = AUISeatLockNetworkModel.model(rtmMessage: message) {
            rtmCloseSeat(seatIndex: model.micSeatNo, isClose: true) {[weak self] err in
                self?.rtmManager.sendReceipt(channelName: channelName, uniqueId: uniqueId, error: err)
            }
        } else if interfaceName == kAUISeatUnlockNetworkInterface, let model = AUISeatUnLockNetworkModel.model(rtmMessage: message) {
            rtmCloseSeat(seatIndex: model.micSeatNo, isClose: false) {[weak self] err in
                self?.rtmManager.sendReceipt(channelName: channelName, uniqueId: uniqueId, error: err)
            }
        }
    }

    public func onRoomWillInit(completion: @escaping ((NSError?) -> ())){
        guard let roomInfo = getRoomContext().roomInfoMap[channelName] else {
            completion(AUICommonError.unknown.toNSError())
            return
        }
        var seatMap: [String: [String: Any]] = [:]
        for i in 0...roomInfo.micSeatCount {
            let seat = AUIMicSeatInfo()
            seat.seatIndex = i
            if i == 0 {
                seat.user = getRoomContext().currentUserInfo
                seat.lockSeat = .user
            }
            seatMap["\(i)"] = seat.yy_modelToJSONObject() as? [String : Any]
        }
        
        let data = try! JSONSerialization.data(withJSONObject: seatMap, options: .prettyPrinted)
        let str = String(data: data, encoding: .utf8)!
        var metaData = [String: String]()
        metaData[kSeatAttrKry] = str
        rtmManager.setBatchMetadata(channelName: channelName,
                                    lockName: "",
                                    metadata: metaData,
                                    completion: completion)
    }
    
    public func onUserInfoClean(userId: String, completion: @escaping ((NSError?) -> ())) {
        let micSeatMetaData = rtmLeaveSeatMetaData(userId: userId)
        let str = micSeatMetaData.yy_modelToJSONString() ?? ""
        var metaData = [String: String]()
        metaData[kSeatAttrKry] = str
        
        rtmManager.setBatchMetadata(channelName: channelName,
                                    lockName: kRTM_Referee_LockName,
                                    metadata: metaData,
                                    completion: completion)
    }
    
//    public func onSongWillSelect(channelName: String, userId: String, metaData: NSMutableDictionary) -> NSError? {
//        for (_, seat) in micSeats {
//            if seat.user?.userId == userId {
//                return nil
//            }
//        }
//        return AUICommonError.noPermission.toNSError()
//    }
    
    public func onRoomWillDestroy(removeKeys: NSMutableArray) -> NSError? {
        removeKeys.add(kSeatAttrKry)
        return nil
    }
}

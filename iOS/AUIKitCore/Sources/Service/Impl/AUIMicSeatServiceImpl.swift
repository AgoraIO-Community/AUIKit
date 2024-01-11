//
//  AUIMicSeatServiceImpl.swift
//  Pods
//
//  Created by wushengtao on 2023/2/21.
//


import Foundation
import AgoraRtmKit

let kSeatAttrKry = "micSeat"

enum AUIMicSeatCmd: String {
    case leaveSeatCmd = "leaveSeatCmd"
    case enterSeatCmd = "enterSeatCmd"
    case kickSeatCmd = "kickSeatCmd"
    case muteAudioCmd = "muteAudioCmd"
    case closeSeatCmd = "closeSeatCmd"
}

//麦位Service实现(纯端上修改KV)
@objc open class AUIMicSeatServiceImpl: NSObject {
    private var respDelegates: NSHashTable<AUIMicSeatRespDelegate> = NSHashTable<AUIMicSeatRespDelegate>.weakObjects()
    private var channelName: String!
    private let rtmManager: AUIRtmManager!
    
    private var micSeats:[Int: AUIMicSeatInfo] = [:]
    
    private var mapCollection: AUIMapCollection!
        
    deinit {
        rtmManager.unsubscribeAttributes(channelName: getChannelName(), itemKey: kSeatAttrKry, delegate: self)
//        rtmManager.unsubscribeMessage(channelName: getChannelName(), delegate: self)
        aui_info("deinit AUIMicSeatServiceImpl", tag: "AUIMicSeatServiceImpl")
    }
    
    public init(channelName: String, rtmManager: AUIRtmManager) {
        self.rtmManager = rtmManager
        self.channelName = channelName
        self.mapCollection = AUIMapCollection(channelName: channelName, observeKey: kSeatAttrKry, rtmManager: rtmManager)
        super.init()
        rtmManager.subscribeAttributes(channelName: getChannelName(), itemKey: kSeatAttrKry, delegate: self)
//        rtmManager.subscribeMessage(channelName: getChannelName(), delegate: self)
        mapCollection.subscribeWillSet {[weak self] publisherId, dataCmd, updateMap, currentMap in
            return self?.metadataWillSet(publiserId: publisherId, dataCmd: dataCmd, updateMap: updateMap, currentMap: currentMap)
        }
        aui_info("init AUIMicSeatServiceImpl", tag: "AUIMicSeatServiceImpl")
    }
}

extension AUIMicSeatServiceImpl: AUIRtmAttributesProxyDelegate {
    public func onAttributesDidChanged(channelName: String, key: String, value: Any) {
        mapCollection.onAttributesDidChanged(channelName: channelName, key: key, value: value)
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

extension AUIMicSeatServiceImpl: AUIMicSeatServiceDelegate {
    
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
        let user = AUIUserThumbnailInfo()
        user.userId = getRoomContext().currentUserInfo.userId
        user.userAvatar = getRoomContext().currentUserInfo.userAvatar
        user.userName = getRoomContext().currentUserInfo.userName
        let value = [
            "\(seatIndex)": [
                "owner": user.yy_modelToJSONObject(),
                "micSeatStatus": AUILockSeatStatus.user.rawValue
            ]
        ]
        self.mapCollection.mergeMetaData(valueCmd: AUIMicSeatCmd.enterSeatCmd.rawValue,
                                         value: value,
                                         objectId: "",
                                         callback: callback)
    }
    
    public func leaveSeat(callback: @escaping (NSError?) -> ()) {
        leaveSeat(userId: getRoomContext().currentUserInfo.userId, callback: callback)
    }
    
    public func pickSeat(seatIndex: Int, userId: String, callback: @escaping (NSError?) -> ()) {
    }
    
    public func kickSeat(seatIndex: Int, callback: @escaping (NSError?) -> ()) {
        let value = [
            "\(seatIndex)": [
                "owner": AUIUserThumbnailInfo().yy_modelToJSONObject(),
                "micSeatStatus": AUILockSeatStatus.idle.rawValue
            ]
        ]
        self.mapCollection.mergeMetaData(valueCmd: AUIMicSeatCmd.kickSeatCmd.rawValue,
                                          value: value,
                                          objectId: "",
                                          callback: callback)
    }
    
    public func muteAudioSeat(seatIndex: Int, isMute: Bool, callback: @escaping (NSError?) -> ()) {
        let value = [
            "\(seatIndex)": [
                "isMuteAudio": isMute
            ]
        ]
        self.mapCollection.mergeMetaData(valueCmd: AUIMicSeatCmd.muteAudioCmd.rawValue,
                                          value: value,
                                          objectId: "",
                                          callback: callback)
    }
    
    public func muteVideoSeat(seatIndex: Int, isMute: Bool, callback: @escaping AUICallback) {
    }
    
    public func closeSeat(seatIndex: Int, isClose: Bool, callback: @escaping (NSError?) -> ()) {
        var value: [String: Any] = [:]
        self.micSeats.forEach { (k: Int, v: AUIMicSeatInfo) in
            if k == seatIndex {
                var status: AUILockSeatStatus = .idle
                if isClose {
                    status = .locked
                } else if v.user?.isEmpty() ?? true {
                    status = .idle
                } else {
                    status = .user
                }
                value["\(seatIndex)"] = [
                    "micSeatStatus": status.rawValue
                ]
            }
        }
        
        //TODO: value.isEmpty
        
        self.mapCollection.mergeMetaData(valueCmd: AUIMicSeatCmd.closeSeatCmd.rawValue,
                                          value: value,
                                          objectId: "",
                                          callback: callback)
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
extension AUIMicSeatServiceImpl {
    private func leaveSeat(userId: String, callback: @escaping (NSError?) -> ()) {
        var value: [String: Any] = [:]
        self.micSeats.forEach { (k: Int, v: AUIMicSeatInfo) in
            if userId == v.user?.userId {
                value["\(k)"] = [
                    "owner": AUIUserThumbnailInfo().yy_modelToJSONObject(),
                    "micSeatStatus": AUILockSeatStatus.idle.rawValue
                ]
            }
        }
        
        //TODO: value.isEmpty
        
        self.mapCollection.mergeMetaData(valueCmd: AUIMicSeatCmd.leaveSeatCmd.rawValue,
                                          value: value,
                                          objectId: "",
                                          callback: callback)
    }
    
    private func metadataWillSet(publiserId: String, 
                                 dataCmd: String?,
                                 updateMap: [String: Any],
                                 currentMap: [String: Any]) -> NSError? {
        guard let dataCmd = AUIMicSeatCmd(rawValue: dataCmd ?? ""),
              updateMap.keys.count == 1,
              let seatIndex = updateMap.keys.first,
              let value = updateMap[seatIndex] else {
            return AUICommonError.unknown.toNSError()
        }
        
        switch dataCmd {
        case .enterSeatCmd:
//            func getUserId(_ v: Any) -> String? {
//                let owner = (v as? [String: Any])?["owner"] as? [String: Any]
//                let userId = owner?["userId"] as? String
//                return userId
//            }
//            
//            if self.micSeats.values.contains(where: { getUserId($0) == getUserId(value)  }) {
//                return AUICommonError.micSeatAlreadyEnter.toNSError()
//            }
//            guard let seat = self.micSeats[seatIndex], seat.lockSeat == .idle, seat.user?.isEmpty() ?? true else {
//                return AUICommonError.micSeatNotIdle.toNSError()
//            }
            break
        case .leaveSeatCmd:
            break
        case .kickSeatCmd:
            break
        case .muteAudioCmd:
            break
        case .closeSeatCmd:
            break
        }
        
        return nil
    }
//    private func rtmEnterSeat(seatIndex: Int, userInfo: AUIUserThumbnailInfo, callback: @escaping (NSError?) -> ()) {
//        if self.micSeats.values.contains(where: { $0.user?.userId == userInfo.userId }) {
//            callback(AUICommonError.micSeatAlreadyEnter.toNSError())
//            return
//        }
//        guard let seat = self.micSeats[seatIndex], seat.lockSeat == .idle, seat.user?.isEmpty() ?? true else {
//            callback(AUICommonError.micSeatNotIdle.toNSError())
//            return
//        }
//        
//        var seatMap: [String: [String: Any]] = [:]
//        
//        self.micSeats.forEach { (key: Int, value: AUIMicSeatInfo) in
//            var map = value.yy_modelToJSONObject() as? [String: Any] ?? [:]
//            if key == seatIndex {
//                map["owner"] = userInfo.yy_modelToJSONObject()
//                map["micSeatStatus"] = AUILockSeatStatus.user.rawValue
//            }
//            seatMap["\(key)"] = map
//        }
//        
//        let data = try! JSONSerialization.data(withJSONObject: seatMap, options: .prettyPrinted)
//        let str = String(data: data, encoding: .utf8)!
//        let metaData = [kSeatAttrKry: str]
//        self.rtmManager.setBatchMetadata(channelName: channelName,
//                                         lockName: kRTM_Referee_LockName,
//                                         metadata: metaData) { error in
//            callback(error)
//        }
//    }
    
//    private func rtmLeaveSeatMetaData(userId: String) -> NSMutableDictionary {
//        guard self.micSeats.values.contains(where: { $0.user?.userId == userId }) else {
//            return [:]
//        }
//        
//        var seatMap: [String: [String: Any]] = [:]
//        
//        self.micSeats.forEach { (key: Int, value: AUIMicSeatInfo) in
//            var map = value.yy_modelToJSONObject() as? [String: Any] ?? [:]
//            if userId == value.user?.userId {
//                map["owner"] = AUIUserThumbnailInfo().yy_modelToJSONObject()
//                map["micSeatStatus"] = AUILockSeatStatus.idle.rawValue
//            }
//            seatMap["\(key)"] = map
//        }
//        
//        let data = try! JSONSerialization.data(withJSONObject: seatMap, options: .prettyPrinted)
//        let str = String(data: data, encoding: .utf8)!
//        let metaData = NSMutableDictionary()
//        metaData[kSeatAttrKry] = str
//        return metaData
//    }
    
//    private func rtmLeaveSeat(userId: String, callback: @escaping (NSError?) -> ()) {
//        guard self.micSeats.values.contains(where: { $0.user?.userId == userId }) else {
//            callback(AUICommonError.userNoEnterSeat.toNSError())
//            return
//        }
//        var err: NSError?
//        let metaData = rtmLeaveSeatMetaData(userId: userId)
//        for obj in respDelegates.allObjects {
//            err = obj.onSeatWillLeave?(userId: userId, metaData: metaData)
//            if let err = err {
//                callback(err)
//                break
//            }
//        }
//        self.rtmManager.setBatchMetadata(channelName: channelName,
//                                         lockName: kRTM_Referee_LockName,
//                                         metadata: metaData as! [String : String]) { error in
//            callback(error)
//        }
//    }
    
//    private func rtmKickSeat(seatIndex: Int, callback: @escaping (NSError?) -> ()) {
//        var seatMap: [String: [String: Any]] = [:]
//        
//        var userId: String?
//        self.micSeats.forEach { (key: Int, value: AUIMicSeatInfo) in
//            var map = value.yy_modelToJSONObject() as? [String: Any] ?? [:]
//            if key == seatIndex {
//                map["owner"] = AUIUserThumbnailInfo().yy_modelToJSONObject()
//                map["micSeatStatus"] = AUILockSeatStatus.idle.rawValue
//                userId = value.user?.userId
//            }
//            seatMap["\(key)"] = map
//        }
//        
//        var err: NSError? = nil
//        let metaData = NSMutableDictionary()
//        for obj in respDelegates.allObjects {
//            err = obj.onSeatWillLeave?(userId: userId!, metaData: metaData)
//            if let _ = err {
//                callback(err)
//                break
//            }
//        }
//        let data = try! JSONSerialization.data(withJSONObject: seatMap, options: .prettyPrinted)
//        let str = String(data: data, encoding: .utf8)!
//        metaData[kSeatAttrKry] = str
//        
//        self.rtmManager.setBatchMetadata(channelName: channelName,
//                                         lockName: kRTM_Referee_LockName,
//                                         metadata: metaData as! [String : String]) { error in
//            callback(error)
//        }
//    }
    
//    private func rtmMuteAudioSeat(seatIndex: Int, isMute: Bool, callback: @escaping (NSError?) -> ()) {
//        var seatMap: [String: [String: Any]] = [:]
//        self.micSeats.forEach { (key: Int, value: AUIMicSeatInfo) in
//            var map = value.yy_modelToJSONObject() as? [String: Any] ?? [:]
//            if key == seatIndex {
//                map["isMuteAudio"] = isMute
//            }
//            seatMap["\(key)"] = map
//        }
//        
//        let data = try! JSONSerialization.data(withJSONObject: seatMap, options: .prettyPrinted)
//        let str = String(data: data, encoding: .utf8)!
//        let metaData = [kSeatAttrKry: str]
//        self.rtmManager.setBatchMetadata(channelName: channelName,
//                                         lockName: kRTM_Referee_LockName,
//                                         metadata: metaData) { error in
//            callback(error)
//        }
//    }
    
//    private func rtmCloseSeat(seatIndex: Int, isClose: Bool, callback: @escaping (NSError?) -> ())  {
//        var seatMap: [String: [String: Any]] = [:]
//        self.micSeats.forEach { (key: Int, value: AUIMicSeatInfo) in
//            var map = value.yy_modelToJSONObject() as? [String: Any] ?? [:]
//            if key == seatIndex {
//                if isClose {
//                    map["micSeatStatus"] = AUILockSeatStatus.locked.rawValue
//                } else if value.user?.isEmpty() ?? true {
//                    map["micSeatStatus"] = AUILockSeatStatus.idle.rawValue
//                } else {
//                    map["micSeatStatus"] = AUILockSeatStatus.user.rawValue
//                }
//            }
//            seatMap["\(key)"] = map
//        }
//        
//        let data = try! JSONSerialization.data(withJSONObject: seatMap, options: .prettyPrinted)
//        let str = String(data: data, encoding: .utf8)!
//        let metaData = [kSeatAttrKry: str]
//        self.rtmManager.setBatchMetadata(channelName: channelName,
//                                         lockName: kRTM_Referee_LockName,
//                                         metadata: metaData) { error in
//            callback(error)
//        }
//    }
}

//MARK: AUIRtmMessageProxyDelegate
extension AUIMicSeatServiceImpl {

    public func initService(completion: @escaping ((NSError?) -> ())){
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
    
    public func cleanUserInfo(userId: String, completion: @escaping ((NSError?) -> ())) {
//        let micSeatMetaData = rtmLeaveSeatMetaData(userId: userId)
//        let str = micSeatMetaData.yy_modelToJSONString() ?? ""
//        var metaData = [String: String]()
//        metaData[kSeatAttrKry] = str
//        
//        rtmManager.setBatchMetadata(channelName: channelName,
//                                    lockName: kRTM_Referee_LockName,
//                                    metadata: metaData,
//                                    completion: completion)
        
        leaveSeat(userId: userId, callback: completion)
    }
    
    public func deinitService(completion:  @escaping  ((NSError?) -> ())) {
//        rtmManager.cleanBatchMetadata(channelName: channelName,
//                                      lockName: kRTM_Referee_LockName,
//                                      removeKeys: [kSeatAttrKry],
//                                      completion: completion)
        mapCollection.removeMetaData(valueCmd: nil, objectId: "", callback: completion)
    }
}

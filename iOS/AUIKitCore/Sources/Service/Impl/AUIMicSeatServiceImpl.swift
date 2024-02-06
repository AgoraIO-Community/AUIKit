//
//  AUIMicSeatServiceImpl.swift
//  Pods
//
//  Created by wushengtao on 2023/2/21.
//


import Foundation
import AgoraRtmKit

private let kSeatAttrKey = "micSeat"

private enum AUIMicSeatCmd: String {
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
        aui_info("deinit AUIMicSeatServiceImpl", tag: "AUIMicSeatServiceImpl")
    }
    
    public init(channelName: String, rtmManager: AUIRtmManager) {
        self.rtmManager = rtmManager
        self.channelName = channelName
        self.mapCollection = AUIMapCollection(channelName: channelName, observeKey: kSeatAttrKey, rtmManager: rtmManager)
        super.init()
        mapCollection.subscribeWillMerge {[weak self] publisherId, dataCmd, updateMap, currentMap in
            return self?.metadataWillMerge(publiserId: publisherId, dataCmd: dataCmd, updateMap: updateMap, currentMap: currentMap)
        }
        mapCollection.subscribeAttributesDidChanged {[weak self] channelName, key, value in
            self?.onAttributesDidChanged(channelName: channelName, key: key, value: value)
        }
        aui_info("init AUIMicSeatServiceImpl", tag: "AUIMicSeatServiceImpl")
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
                                         filter: nil,
                                         callback: callback)
    }
    
    public func leaveSeat(callback: @escaping (NSError?) -> ()) {
        leaveSeat(userId: getRoomContext().currentUserInfo.userId, callback: callback)
    }
    
    public func pickSeat(seatIndex: Int, user: AUIUserThumbnailInfo, callback: @escaping (NSError?) -> ()) {
        let value = [
            "\(seatIndex)": [
                "owner": user.yy_modelToJSONObject(),
                "micSeatStatus": AUILockSeatStatus.user.rawValue
            ]
        ]
        self.mapCollection.mergeMetaData(valueCmd: AUIMicSeatCmd.enterSeatCmd.rawValue,
                                         value: value,
                                         filter: nil,
                                         callback: callback)
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
                                         filter: nil,
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
                                         filter: nil,
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
                                         filter: nil,
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
                                         filter: nil,
                                         callback: callback)
    }
    
    private func onAttributesDidChanged(channelName: String, key: String, value: AUIAttributesModel) {
        if key == kSeatAttrKey {
            aui_info("recv seat attr did changed \(value)", tag: "AUIMicSeatServiceImpl")
            guard let map = value.getMap() as? [String: [String: Any]] else {return}
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
    
    private func metadataWillMerge(publiserId: String,
                                   dataCmd: String?,
                                   updateMap: [String: Any],
                                   currentMap: [String: Any]) -> NSError? {
        guard let dataCmd = AUIMicSeatCmd(rawValue: dataCmd ?? ""),
              updateMap.keys.count == 1,
              let seatIndex = Int(updateMap.keys.first ?? ""),
              let value = updateMap["\(seatIndex)"] else {
            return AUICommonError.unknown.toNSError()
        }
        
        let owner = (value as? [String: Any])?["owner"] as? [String: Any]
        var userId: String = owner?["userId"] as? String ?? ""
        switch dataCmd {
        case .enterSeatCmd:
            if self.micSeats.values.contains(where: { $0.user?.userId == userId }) {
                return AUICommonError.micSeatAlreadyEnter.toNSError()
            }
            guard let seat = self.micSeats[seatIndex],
                  seat.lockSeat == .idle,
                  seat.user?.isEmpty() ?? true else {
                return AUICommonError.micSeatNotIdle.toNSError()
            }
            break
        case .leaveSeatCmd:
            if seatIndex == 0 {
                return AUICommonError.noPermission.toNSError()
            }
            guard micSeats[seatIndex]?.user?.userId == publiserId
                    || getRoomContext().isRoomOwner(channelName: channelName, userId: publiserId) else {
                return AUICommonError.userNoEnterSeat.toNSError()
            }
            var err: NSError?
            //TODO: onSeatWillLeave不需要metaData？
            let metaData = NSMutableDictionary()//rtmLeaveSeatMetaData(userId: userId)
            userId = self.micSeats[seatIndex]?.user?.userId ?? ""
            for obj in respDelegates.allObjects {
                err = obj.onSeatWillLeave?(userId: userId, metaData: metaData)
                if let err = err {
                    return err
                }
            }
            break
        case .kickSeatCmd:
            if seatIndex == 0 {
                return AUICommonError.noPermission.toNSError()
            }
            var err: NSError? = nil
            let metaData = NSMutableDictionary()
            userId = self.micSeats[seatIndex]?.user?.userId ?? ""
            for obj in respDelegates.allObjects {
                err = obj.onSeatWillLeave?(userId: userId, metaData: metaData)
                if let _ = err {
                    return err
                }
            }
            break
        case .muteAudioCmd:
            break
        case .closeSeatCmd:
            break
        }
        
        return nil
    }
}

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
        metaData[kSeatAttrKey] = str
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
        mapCollection.cleanMetaData(callback: completion)
    }
}

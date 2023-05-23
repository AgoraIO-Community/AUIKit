//
//  AUiMicSeatServiceImpl.swift
//  Pods
//
//  Created by wushengtao on 2023/2/21.
//

import Foundation
import AgoraRtcKit
import YYModel


//麦位Service实现
open class AUiMicSeatServiceImpl: NSObject {
    private var respDelegates: NSHashTable<AnyObject> = NSHashTable<AnyObject>.weakObjects()
    private var channelName: String!
    private let rtmManager: AUiRtmManager!
    private let roomManager: AUiRoomManagerDelegate!
    
    private var micSeats:[Int: AUiMicSeatInfo] = [:]
    
    deinit {
        self.rtmManager.unsubscribeAttributes(channelName: getChannelName(), itemKey: kSeatAttrKry, delegate: self)
        aui_info("deinit AUiMicSeatServiceImpl", tag: "AUiMicSeatServiceImpl")
    }
    
    public init(channelName: String, rtmManager: AUiRtmManager, roomManager: AUiRoomManagerDelegate) {
        self.rtmManager = rtmManager
        self.channelName = channelName
        self.roomManager = roomManager
        super.init()
        rtmManager.subscribeAttributes(channelName: getChannelName(), itemKey: kSeatAttrKry, delegate: self)
        aui_info("init AUiMicSeatServiceImpl", tag: "AUiMicSeatServiceImpl")
    }
}

extension AUiMicSeatServiceImpl: AUiRtmAttributesProxyDelegate {
    public func onAttributesDidChanged(channelName: String, key: String, value: Any) {
        if key == kSeatAttrKry {
            aui_info("recv seat attr did changed \(value)", tag: "AUiMicSeatServiceImpl")
            guard let map = value as? [String: [String: Any]] else {return}
            map.values.forEach { element in
                guard let micSeat = AUiMicSeatInfo.yy_model(with: element) else {return}
                aui_info(" micSeat.islock \(micSeat.lockSeat) micSeat.Index = \(micSeat.seatIndex)", tag: "AUiMicSeatServiceImpl")
                let index: Int = Int(micSeat.seatIndex)
                let origMicSeat = self.micSeats[index]
//                if let origMicSeat = origMicSeat {
//                    origMicSeat.user = roomManager.getUserInfo(by: origMicSeat.userId)
//                }
//                micSeat.user = roomManager.getUserInfo(by: micSeat.userId)
                self.micSeats[index] = micSeat
                self.respDelegates.allObjects.forEach { obj in
                    guard let delegate = obj as? AUiMicSeatRespDelegate else {
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


extension AUiMicSeatServiceImpl: AUiMicSeatServiceDelegate {
    public func getChannelName() -> String {
        return channelName
    }
    
    public func bindRespDelegate(delegate: AUiMicSeatRespDelegate) {
        respDelegates.add(delegate)
    }
    
    public func unbindRespDelegate(delegate: AUiMicSeatRespDelegate) {
        respDelegates.remove(delegate)
    }
    
    public func enterSeat(seatIndex: Int, callback: @escaping (NSError?) -> ()) {
//        if let _ = self.micSeats.values.filter({ $0.userId == self.getRoomContext().currentUserInfo.userId }).first {
//            callback(nil)
//            return
//        }
        let model = AUiSeatEnterNetworkModel()
        model.roomId = channelName
        model.userAvatar = getRoomContext().currentUserInfo.userAvatar
        model.userId = getRoomContext().currentUserInfo.userId
        model.userName = getRoomContext().currentUserInfo.userName
//        model.user = getRoomContext().currentUserInfo
        model.micSeatNo = seatIndex
        model.request { error, _ in
            callback(error as? NSError)
        }

        return
        //mock
        /*
        if let _ = self.micSeats.values.filter({ $0.userId == self.getRoomContext().currentUserInfo.userId }).first {
            callback(nil)
            return
        }
        
        var seatMap: [String: [String: Any]] = [:]
        
        self.micSeats.forEach { (key: Int, value: AUiMicSeatInfo) in
            var map = value.yy_modelToJSONObject() as? [String: Any] ?? [:]
            if key == seatIndex {
                map["user"] = self.getRoomContext().currentUserInfo.yy_modelToJSONObject()
            }
            seatMap["\(key)"] = map
        }
        
        let data = try! JSONSerialization.data(withJSONObject: seatMap, options: .prettyPrinted)
        let str = String(data: data, encoding: .utf8)!
        let metaData = ["seat": str]
        self.rtmManager.setMetadata(channelName: channelName, metadata: metaData) { error in
            callback(nil)
        }
         */
    }
    
    public func leaveSeat(callback: @escaping (NSError?) -> ()) {
        
        let model = AUiSeatLeaveNetworkModel()
        model.roomId = channelName
        model.userId = getRoomContext().currentUserInfo.userId
//        model.micSeatNo = seatIndex
        model.request { error, _ in
            callback(error as? NSError)
        }
        return
        //mock
        /*
        guard let seat = self.micSeats.values.filter({ $0.userId == self.getRoomContext().currentUserInfo.userId }).first else {
            callback(nil)
            return
        }
        
        var seatMap: [String: [String: Any]] = [:]
        
        self.micSeats.forEach { (key: Int, value: AUiMicSeatInfo) in
            var map = value.yy_modelToJSONObject() as? [String: Any] ?? [:]
            if key == seat.seatIndex {
                map.removeValue(forKey: "user")
            }
            seatMap["\(key)"] = map
        }
        
        let str = (seatMap as AnyObject).yy_modelToJSONString() ?? ""
        let metaData = ["seat": str]
        self.rtmManager.setMetadata(channelName: channelName, metadata: metaData) { error in
            callback(nil)
        }
         */
    }
    
    public func pickSeat(seatIndex: Int, userId: String, callback: @escaping (NSError?) -> ()) {
        //mock
//        guard let seat = self.micSeats[seatIndex], seat.user == nil else {
//            callback(nil)
//            return
//        }
//        
//        var seatMap: [String: [String: Any]] = [:]
//        
//        self.micSeats.forEach { (key: Int, value: AUiMicSeatInfo) in
//            var map = value.yy_modelToJSONObject() as? [String: Any] ?? [:]
//            if key == seatIndex {
//                let user = AUiUserThumbnailInfo()
//                user.userId = userId
//                user.userName = userId
//                map["user"] = user.yy_modelToJSONObject()
//            }
//            seatMap["\(key)"] = map
//        }
//        
//        let str = (seatMap as AnyObject).yy_modelToJSONString() ?? ""
//        let metaData = ["seat": str]
//        self.rtmManager.setMetadata(channelName: channelName, metadata: metaData) { error in
//            callback(nil)
//        }
    }
    
    public func kickSeat(seatIndex: Int, callback: @escaping (NSError?) -> ()) {
        let model = AUiSeatkickNetworkModel()
        model.roomId = channelName
        model.userId = getRoomContext().currentUserInfo.userId
        model.micSeatNo = seatIndex
        model.request { error, _ in
            callback(error as? NSError)
        }
        return
        //mock
        /*
        guard let seat = self.micSeats[seatIndex] else {
            callback(nil)
            return
        }
        
        var seatMap: [String: [String: Any]] = [:]
        
        self.micSeats.forEach { (key: Int, value: AUiMicSeatInfo) in
            var map = value.yy_modelToJSONObject() as? [String: Any] ?? [:]
            if key == seatIndex {
                map = [
                   "seatNo": seat.seatIndex,
               ]
            }
            seatMap["\(key)"] = map
        }
        
        let str = (seatMap as AnyObject).yy_modelToJSONString() ?? ""
        let metaData = ["seat": str]
        self.rtmManager.setMetadata(channelName: channelName, metadata: metaData) { error in
            callback(nil)
        }
         */
    }
    
    public func muteAudioSeat(seatIndex: Int, isMute: Bool, callback: @escaping (NSError?) -> ()) {
        if isMute {
            let model = AUiSeatMuteAudioNetworkModel()
            model.roomId = channelName
            model.micSeatNo = seatIndex
            model.userId = getRoomContext().currentUserInfo.userId
            model.request { error, _ in
                callback(error as? NSError)
            }
        }else {
            let model = AUiSeatUnMuteAudioNetworkModel()
            model.roomId = channelName
            model.micSeatNo = seatIndex
            model.userId = getRoomContext().currentUserInfo.userId
            model.request { error, _ in
                callback(error as? NSError)
            }
        }
        //mock
        /*
        guard let _ = self.micSeats[seatIndex] else {
            //TODO: fatel error
            callback(nil)
            return
        }
        
        var seatMap: [String: [String: Any]] = [:]
        
        self.micSeats.forEach { (key: Int, value: AUiMicSeatInfo) in
            var map = value.yy_modelToJSONObject() as? [String: Any] ?? [:]
            if key == seatIndex {
                map["isMuteAudio"] = isMute
            }
            
            seatMap["\(key)"] = map
        }
        
        let data = try! JSONSerialization.data(withJSONObject: seatMap, options: .prettyPrinted)
        let str = String(data: data, encoding: .utf8)!
        let metaData = ["seat": str]
        self.rtmManager.setMetadata(channelName: channelName, metadata: metaData) { error in
            callback(nil)
        }
         */
    }
    
    public func muteVideoSeat(seatIndex: Int, isMute: Bool, callback: @escaping AUiCallback) {
        if isMute {
            let model = AUiSeatMuteVideoNetworkModel()
            model.roomId = channelName
            model.micSeatNo = seatIndex
            model.userId = getRoomContext().currentUserInfo.userId
            model.request { error, _ in
                callback(error as? NSError)
            }
        }else {
            let model = AUiSeatUnMuteVideoNetworkModel()
            model.roomId = channelName
            model.micSeatNo = seatIndex
            model.userId = getRoomContext().currentUserInfo.userId
            model.request { error, _ in
                callback(error as? NSError)
            }
        }
        //mock
        /*
        guard let _ = self.micSeats[seatIndex] else {
            //TODO: fatel error
            callback(nil)
            return
        }
        
        var seatMap: [String: [String: Any]] = [:]
        
        self.micSeats.forEach { (key: Int, value: AUiMicSeatInfo) in
            var map = value.yy_modelToJSONObject() as? [String: Any] ?? [:]
            if key == seatIndex {
                map["isMuteVideo"] = isMute
            }
            seatMap["\(key)"] = map
        }
        
        let data = try! JSONSerialization.data(withJSONObject: seatMap, options: .prettyPrinted)
        let str = String(data: data, encoding: .utf8)!
        let metaData = ["seat": str]
        self.rtmManager.setMetadata(channelName: channelName, metadata: metaData) { error in
            callback(nil)
        }
         */
    }
    
    public func closeSeat(seatIndex: Int, isClose: Bool, callback: @escaping (NSError?) -> ()) {
        if isClose {
            let model = AUiSeatLockNetworkModel()
            model.roomId = channelName
            model.micSeatNo = seatIndex
            model.userId = getRoomContext().currentUserInfo.userId
            model.request { error, _ in
                callback(error as? NSError)
            }
        }else {
            let model = AUiSeatUnLockNetworkModel()
            model.roomId = channelName
            model.micSeatNo = seatIndex
            model.userId = getRoomContext().currentUserInfo.userId
            model.request { error, _ in
                callback(error as? NSError)
            }
        }
        
        return
        //mock
        /*
        guard let seat = self.micSeats[seatIndex], seat.userId == nil else {
            callback(nil)
            return
        }
        
        var seatMap: [String: [String: Any]] = [:]
        
        self.micSeats.forEach { (key: Int, value: AUiMicSeatInfo) in
            var map = value.yy_modelToJSONObject() as? [String: Any] ?? [:]
            if key == seatIndex {
                map["isLockSeat"] = isClose
            }
            seatMap["\(key)"] = map
        }
        
        let data = try! JSONSerialization.data(withJSONObject: seatMap, options: .prettyPrinted)
        let str = String(data: data, encoding: .utf8)!
        let metaData = ["seat": str]
        self.rtmManager.setMetadata(channelName: channelName, metadata: metaData) { error in
            callback(nil)
        }
         */
    }
}

//
//  AUIKitModel+Coder.swift
//  AUIKitCore
//
//  Created by wushengtao on 2023/11/20.
//

import Foundation
import YYModel

public let kRTM_Referee_LockName = "rtm_referee_lock"

public typealias AUICallback = (NSError?) -> ()

public typealias AUICreateRoomCallback = (NSError?, AUIRoomInfo?) -> ()

public typealias AUIUserListCallback = (NSError?, [AUIUserInfo]?) -> ()

public typealias AUIRoomListCallback = (NSError?, [AUIRoomInfo]?) -> ()

extension AUIRoomInfo {
    class func modelCustomPropertyMapper() -> NSDictionary {
        let superMap = NSMutableDictionary()
        let map = [
            "roomName": "payload.roomName",
            "thumbnail": "payload.roomThumbnail",
            "seatCount": "payload.roomSeatCount",
            "muteAudio": "payload.isMuteAudio",
            "muteVideo": "payload.isMuteVideo",
            "owner": "payload.roomOwner",
            "memberCount": "payload.onlineUsers",
            "customPayload": "payload.customPayload"
        ]
        superMap.addEntries(from: map)
        return superMap
    }
    
    class func modelContainerPropertyGenericClass() -> NSDictionary {
        return [
            "owner": AUIUserThumbnailInfo.self
        ]
    }
}

extension AUIUserThumbnailInfo {    
    public func isEmpty() -> Bool {
        guard userId.count > 0, userName.count > 0 else {return true}
        
        return false
    }
}

extension AUIMicSeatInfo {
    class func modelCustomPropertyMapper()->NSDictionary {
        return [
            "seatIndex": "micSeatNo",
            "muteAudio": "isMuteAudio",
            "muteVideo": "isMuteVideo",
            "lockSeat": "micSeatStatus",
//            "userId": "micSeatUserId"
            "user": "owner"
        ]
    }
    
    class func modelContainerPropertyGenericClass() -> NSDictionary {
        return [
            "user": AUIUserThumbnailInfo.self
        ]
    }
    
    public func seatIndexDesc() -> String {
        if let user = self.user {
            return user.userName
        }
        return  String(format: aui_localized("micSeatDesc1Format"), seatIndex + 1)
    }
    
    public func seatIndexDesc2() -> String {
        return  String(format: aui_localized("micSeatDesc2Format"), seatIndex + 1)
    }
    
    public func seatAndUserDesc() -> String {
        return "\(seatIndexDesc()): \(self.user?.userName ?? "")"
    }
}

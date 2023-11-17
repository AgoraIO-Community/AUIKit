//
//  AUIKitModel.swift
//  AUIKit
//
//  Created by wushengtao on 2023/2/20.
//

import Foundation
import YYModel

public let kRTM_Referee_LockName = "rtm_referee_lock"

public typealias AUICallback = (NSError?) -> ()

public typealias AUICreateRoomCallback = (NSError?, AUIRoomInfo?) -> ()

public typealias AUIUserListCallback = (NSError?, [AUIUserInfo]?) -> ()

public typealias AUIRoomListCallback = (NSError?, [AUIRoomInfo]?) -> ()

@objcMembers
/// 房间列表展示数据
open class AUIRoomInfo: NSObject {
    public var roomName: String = ""    //房间名称
    public var thumbnail: String = ""   //房间列表上的缩略图
    public var micSeatCount: UInt = 8      //麦位个数
    public var micSeatStyle: UInt = 3    //麦位样式 1、6为6麦位环形样式 8麦位为长方形Collection 9为特殊layout的Collection
    
    public var roomId: String = ""            //房间id
    public var owner: AUIUserThumbnailInfo?   //房主信息
    public var memberCount: UInt = 0          //房间人数
    public var createTime: Int64 = 0          //创建时间
    
    class func modelCustomPropertyMapper() -> NSDictionary {
        let superMap = NSMutableDictionary()
        let map = [
            "thumbnail": "roomThumbnail",
            "seatCount": "roomSeatCount",
            "seatIndex": "seatNo",
            "muteAudio": "isMuteAudio",
            "muteVideo": "isMuteVideo",
            "owner": "roomOwner",
            "memberCount": "onlineUsers"
        ]
        superMap.addEntries(from: map)
        return superMap
    }
    
    class func modelContainerPropertyGenericClass() -> NSDictionary {
        return [
            "roomOwner": AUIUserThumbnailInfo.self
        ]
    }
}

@objcMembers
///用户简略信息，用于各个模型传递简单数据
open class AUIUserThumbnailInfo: NSObject,AUIUserCellUserDataProtocol {
    
    public var userId: String = ""      //用户Id
    public var userName: String = ""    //用户名
    public var userAvatar: String = ""  //用户头像
    public var seatIndex: Int = -1 //用户是否在麦上
    public var isOwner: Bool = false //是否是owner

    public func isEmpty() -> Bool {
        guard userId.count > 0, userName.count > 0 else {return true}
        
        return false
    }
    
    //TODO: remove seatIndex & isOwner
    static func modelPropertyBlacklist() -> [Any] {
        return ["seatIndex", "isOwner"]
    }
}

let kUserMuteAudioInitStatus = false
let kUserMuteVideoInitStatus = true

@objcMembers
//用户信息
open class AUIUserInfo: AUIUserThumbnailInfo {
    public var muteAudio: Bool = kUserMuteAudioInitStatus  //是否静音状态
    public var muteVideo: Bool = kUserMuteVideoInitStatus   //是否关闭视频状态
    
}

@objcMembers
open class AUIMicSeatInfo: NSObject {
    public var user: AUIUserThumbnailInfo?            //上麦用户
    public var seatIndex: UInt = 0                    //麦位索引，可以不需要，根据麦位list可以计算出
    public var muteAudio: Bool = false                //麦位禁用声音
    public var muteVideo: Bool = false                //麦位禁用视频
    public var lockSeat: AUILockSeatStatus = .idle
    public var micRole: MicRole = .offlineAudience
    
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


@objc public enum AUILockSeatStatus: Int {
    case idle = 0
    case user = 1
    case locked = 2
}


//
//  AUiKitModel.swift
//  AUiKit
//
//  Created by wushengtao on 2023/2/20.
//

import Foundation
import YYModel

public typealias AUiCallback = (Error?) -> ()

public typealias AUiCreateRoomCallback = (Error?, AUiRoomInfo?) -> ()

public typealias AUiUserListCallback = (Error?, [AUiUserInfo]?) -> ()

public typealias AUiRoomListCallback = (Error?, [AUiRoomInfo]?) -> ()

@objcMembers
/// 创建房间信息对象，用于创建房间时传递
open class AUiCreateRoomInfo: NSObject {
    public var roomName: String = ""    //房间名称
    public var thumbnail: String = ""   //房间列表上的缩略图
    public var seatCount: UInt = 0      //麦位个数
    public var password: String?        //房间密码
    
    class func modelCustomPropertyMapper()->NSDictionary {
        return [
            "thumbnail": "roomThumbnail",
            "seatCount": "roomSeatCount",
            "password": "roomPassword"
        ]
    }
}

@objcMembers
/// 房间列表展示数据
open class AUiRoomInfo: AUiCreateRoomInfo {
    public var roomId: String = ""            //房间id
    public var owner: AUiUserThumbnailInfo?   //房主信息
    public var memberCount: UInt = 0          //房间人数
    public var createTime: Int64 = 0          //创建时间
    
    override class func modelCustomPropertyMapper() -> NSDictionary {
        let superMap = NSMutableDictionary(dictionary: super.modelCustomPropertyMapper())
        let map = [
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
            "roomOwner": AUiUserThumbnailInfo.self
        ]
    }
}

@objcMembers
///用户简略信息，用于各个模型传递简单数据
open class AUiUserThumbnailInfo: NSObject {
    public var userId: String = ""      //用户Id
    public var userName: String = ""    //用户名
    public var userAvatar: String = ""  //用户头像
    
    public func isEmpty() -> Bool {
        guard userId.count > 0, userName.count > 0 else {return true}
        
        return false
    }
}

let kUserMuteAudioInitStatus = false
let kUserMuteVideoInitStatus = true

@objcMembers
//用户信息
open class AUiUserInfo: AUiUserThumbnailInfo {
    public var muteAudio: Bool = kUserMuteAudioInitStatus  //是否静音状态
    public var muteVideo: Bool = kUserMuteVideoInitStatus   //是否关闭视频状态
    
}

@objcMembers
open class AUiMicSeatInfo: NSObject {
    public var user: AUiUserThumbnailInfo?            //上麦用户
    public var seatIndex: UInt = 0                    //麦位索引，可以不需要，根据麦位list可以计算出
    public var muteAudio: Bool = false                //麦位禁用声音
    public var muteVideo: Bool = false                //麦位禁用视频
    public var lockSeat: AUiLockSeatStatus = .idle
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
            "user": AUiUserThumbnailInfo.self
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


@objc public enum AUiLockSeatStatus: Int {
    case idle = 0
    case user = 1
    case locked = 2
}


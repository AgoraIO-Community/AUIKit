//
//  AUIKitModel.swift
//  AUIKit
//
//  Created by wushengtao on 2023/2/20.
//

import Foundation

/// 房间列表展示数据
@objcMembers open class AUIRoomInfo: NSObject {
    public var roomName: String = ""    //房间名称
    public var micSeatCount: UInt = 8      //麦位个数
    public var micSeatStyle: UInt = 3    //麦位样式 1、6为6麦位环形样式 8麦位为长方形Collection 9为特殊layout的Collection
    
    public var roomId: String = ""            //房间id
    public var owner: AUIUserThumbnailInfo?   //房主信息
    
    public var memberCount: UInt = 0
    
    public var customPayload: [String: Any] = [:]   //扩展信息
    
    public var createTime: Int64 = 0
}

///用户简略信息，用于各个模型传递简单数据
@objcMembers open class AUIUserThumbnailInfo: NSObject {
    public var userId: String = ""      //用户Id
    public var userName: String = ""    //用户名
    public var userAvatar: String = ""  //用户头像
}

let kUserMuteAudioInitStatus = false
let kUserMuteVideoInitStatus = true

//用户信息
@objcMembers open class AUIUserInfo: AUIUserThumbnailInfo {
    public var muteAudio: Bool = kUserMuteAudioInitStatus  //是否静音状态
    public var muteVideo: Bool = kUserMuteVideoInitStatus   //是否关闭视频状态
}

@objcMembers open class AUIMicSeatInfo: NSObject {
    public var user: AUIUserThumbnailInfo?            //上麦用户
    public var seatIndex: UInt = 0                    //麦位索引，可以不需要，根据麦位list可以计算出
    public var muteAudio: Bool = false                //麦位禁用声音
    public var muteVideo: Bool = false                //麦位禁用视频
    public var lockSeat: AUILockSeatStatus = .idle
    public var micRole: MicRole = .offlineAudience
}


@objc public enum AUILockSeatStatus: Int {
    case idle = 0
    case user = 1
    case locked = 2
}


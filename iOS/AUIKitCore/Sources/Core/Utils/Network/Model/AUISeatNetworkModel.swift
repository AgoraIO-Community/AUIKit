//
//  AUISeatNetworkModel.swift
//  AUIKit
//
//  Created by FanPengpeng on 2023/3/21.
//

import UIKit
import YYModel

let kAUISeatEnterNetworkInterface = "/v1/seat/enter"
let kAUISeatLeaveNetworkInterface = "/v1/seat/leave"
let kAUISeatKickNetworkInterface = "/v1/seat/kick"
let kAUISeatMuteAudioNetworkInterface = "/v1/seat/audio/mute"
let kAUISeatUnmuteAudioNetworkInterface = "/v1/seat/audio/unmute"
let kAUISeatLockNetworkInterface = "/v1/seat/lock"

@objcMembers
open class AUISeatEnterNetworkModel: AUICommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = kAUISeatEnterNetworkInterface
    }
    
    public var roomId: String?
    public var micSeatUserId: String?
    public var userName: String?
    public var userAvatar: String?
    public var micSeatNo: Int = 0
    
}

open class AUISeatLeaveNetworkModel: AUICommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = kAUISeatLeaveNetworkInterface
    }
    
    public var roomId: String?
//    public var micSeatNo: Int = 0
    
}

open class AUISeatKickNetworkModel: AUICommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = kAUISeatKickNetworkInterface
    }
    
    public var roomId: String?
    public var micSeatNo: Int = 0
}

open class AUISeatMuteAudioNetworkModel: AUICommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = kAUISeatMuteAudioNetworkInterface
    }
    
    public var roomId: String?
    public var micSeatNo: Int = 0
    public var isMuteAudio: Int = 0
}

open class AUISeatUnMuteAudioNetworkModel: AUICommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = kAUISeatUnmuteAudioNetworkInterface
    }
    
    public var roomId: String?
    public var micSeatNo: Int = 0
}

open class AUISeatMuteVideoNetworkModel: AUICommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = "/v1/seat/video/mute"
    }
    
    public var roomId: String?
    public var micSeatNo: Int = 0
    public var isMuteVideo: Int = 0
}

open class AUISeatUnMuteVideoNetworkModel: AUICommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = "/v1/seat/video/unmute"
    }
    
    public var roomId: String?
    public var micSeatNo: Int = 0
}


open class AUISeatLockNetworkModel: AUICommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = kAUISeatLockNetworkInterface
    }
    
    public var roomId: String?
    public var micSeatNo: Int = 0
    public var isLock: Int = 0
}

open class AUISeatUnLockNetworkModel: AUICommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = "/v1/seat/unlock"
    }
    
    public var roomId: String?
    public var micSeatNo: Int = 0
}

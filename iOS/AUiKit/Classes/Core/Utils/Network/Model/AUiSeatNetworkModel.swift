//
//  AUiSeatNetworkModel.swift
//  AUiKit
//
//  Created by FanPengpeng on 2023/3/21.
//

import UIKit
import YYModel

@objcMembers
open class AUiSeatEnterNetworkModel: AUiCommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = "/v1/seat/enter"
    }
    
    public var roomId: String?
    public var micSeatUserId: String?
    public var userName: String?
    public var userAvatar: String?
    public var micSeatNo: Int = 0
    
}

open class AUiSeatLeaveNetworkModel: AUiCommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = "/v1/seat/leave"
    }
    
    public var roomId: String?
//    public var micSeatNo: Int = 0
    
}

open class AUiSeatkickNetworkModel: AUiCommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = "/v1/seat/kick"
    }
    
    public var roomId: String?
    public var micSeatNo: Int = 0
}

open class AUiSeatMuteAudioNetworkModel: AUiCommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = "/v1/seat/audio/mute"
    }
    
    public var roomId: String?
    public var micSeatNo: Int = 0
    public var isMuteAudio: Int = 0
}

open class AUiSeatUnMuteAudioNetworkModel: AUiCommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = "/v1/seat/audio/unmute"
    }
    
    public var roomId: String?
    public var micSeatNo: Int = 0
}

open class AUiSeatMuteVideoNetworkModel: AUiCommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = "/v1/seat/video/mute"
    }
    
    public var roomId: String?
    public var micSeatNo: Int = 0
    public var isMuteVideo: Int = 0
}

open class AUiSeatUnMuteVideoNetworkModel: AUiCommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = "/v1/seat/video/unmute"
    }
    
    public var roomId: String?
    public var micSeatNo: Int = 0
}


open class AUiSeatLockNetworkModel: AUiCommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = "/v1/seat/lock"
    }
    
    public var roomId: String?
    public var micSeatNo: Int = 0
    public var isLock: Int = 0
}

open class AUiSeatUnLockNetworkModel: AUiCommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = "/v1/seat/unlock"
    }
    
    public var roomId: String?
    public var micSeatNo: Int = 0
}

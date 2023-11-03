//
//  AUIPlayerNetworkModel.swift
//  AUIKit
//
//  Created by FanPengpeng on 2023/3/23.
//

import UIKit

let kAUIPlayerJoinInterface = "/v1/chorus/join"
let kAUIPlayerLeaveInterface = "/v1/chorus/leave"

@objcMembers
open class AUIPlayerJoinNetworkModel: AUICommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = kAUIPlayerJoinInterface
    }
    
    public var roomId: String?
    public var songCode: String?

}

open class AUIPlayerLeaveNetworkModel: AUICommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = kAUIPlayerLeaveInterface
    }
    
    public var roomId: String?
    public var songCode: String?

}

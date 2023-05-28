//
//  AUIPlayerNetworkModel.swift
//  AUIKit
//
//  Created by FanPengpeng on 2023/3/23.
//

import UIKit

@objcMembers
open class AUIPlayerJoinNetworkModel: AUICommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = "/v1/chorus/join"
    }
    
    public var roomId: String?
    public var songCode: String?

}

open class AUIPlayerLeaveNetworkModel: AUICommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = "/v1/chorus/leave"
    }
    
    public var roomId: String?
    public var songCode: String?

}

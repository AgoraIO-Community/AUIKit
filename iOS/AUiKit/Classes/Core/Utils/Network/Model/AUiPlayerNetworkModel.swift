//
//  AUiPlayerNetworkModel.swift
//  AUiKit
//
//  Created by FanPengpeng on 2023/3/23.
//

import UIKit

@objcMembers
open class AUiPlayerJoinNetworkModel: AUiCommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = "/v1/chorus/join"
    }
    
    public var roomId: String?
    public var songCode: String?

}

open class AUiPlayerLeaveNetworkModel: AUiCommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = "/v1/chorus/leave"
    }
    
    public var roomId: String?
    public var songCode: String?

}

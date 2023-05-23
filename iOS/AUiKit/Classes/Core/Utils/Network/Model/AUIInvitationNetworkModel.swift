//
//  AUIInvitationNetworkModel.swift
//  AUiKit
//
//  Created by 朱继超 on 2023/5/23.
//

import UIKit

@objcMembers
open class AUIInvitationNetworkModel: AUiCommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = "/v1/invitation/user"
    }
    
    public var roomId: String?
    public var micSeatUserId: String?
    public var userName: String?
    public var userAvatar: String?
    public var micSeatNo: Int? = 0
    
}

@objcMembers
open class AUIApplyNetworkModel: AUiCommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = "/v1/apply/user"
    }
    
    public var roomId: String?
    public var micSeatUserId: String?
    public var userName: String?
    public var userAvatar: String?
    public var micSeatNo: Int? = 0
    
}

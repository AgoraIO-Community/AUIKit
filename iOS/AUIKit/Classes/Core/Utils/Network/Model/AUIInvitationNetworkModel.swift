//
//  AUIInvitationNetworkModel.swift
//  AUIKit
//
//  Created by 朱继超 on 2023/5/23.
//

import UIKit

@objcMembers
open class AUIInvitationNetworkModel: AUICommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = "/v1/invitation/create"
    }
    
    public var roomId: String?
    public var fromUserId: String?
    public var toUserId: String?
    public var payload: AUIPayloadModel?
    
    class func modelContainerPropertyGenericClass() -> Dictionary<String,Any> {
        return ["payload": AUIPayloadModel.self]
    }
    
//    class func modelCustomPropertyMapper() -> Dictionary<String,String> {
//        ["fromUserId":"userId"]
//    }
    
}


@objcMembers
open class AUIInvitationCallbackModel: AUICommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = "/v1/invitation/create"
    }
    
    public var fromUserId: String?

    public var payload: AUIPayloadModel?
    
    class func modelContainerPropertyGenericClass() -> Dictionary<String,Any> {
        return ["payload": AUIPayloadModel.self]
    }
    
}

@objcMembers
open class AUIInvitationAcceptNetworkModel: AUICommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = "/v1/invitation/accept"
    }
    
    public var roomId: String?
    public var fromUserId: String?
    
}

@objcMembers
open class AUIInvitationAcceptRejectNetworkModel: AUICommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = "/v1/invitation/accept/reject"
    }
    
    public var roomId: String?
    public var fromUserId: String?
    
}

@objcMembers
open class AUIInvitationAcceptCancelNetworkModel: AUICommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = "/v1/invitation/accept/cancel"
    }
    
    public var roomId: String?
    public var fromUserId: String?
    
}

@objcMembers
open class AUIApplyNetworkModel: AUICommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = "/v1/application/create"
    }
    
    public var roomId: String?
    public var fromUserId: String?
    public var payload: AUIPayloadModel?
    
    class func modelContainerPropertyGenericClass() -> Dictionary<String,Any> {
        return ["payload": AUIPayloadModel.self]
    }
    
}

@objcMembers
open class AUIApplyAcceptNetworkModel: AUICommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = "/v1/application/accept"
    }
    
    public var roomId: String?
    public var fromUserId: String?
    public var toUserId: String?
    
}

@objcMembers
open class AUIApplyAcceptRejectNetworkModel: AUICommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = "/v1/application/accept/reject"
    }
    
    public var roomId: String?
    public var fromUserId: String?
    
}

@objcMembers
open class AUIApplyAcceptCancelNetworkModel: AUICommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = "/v1/application/accept/cancel"
    }
    
    public var roomId: String?
    public var fromUserId: String?
    
}


@objcMembers public class AUIPayloadModel: NSObject {
    public var desc: String?
    public var seatNo: Int = 1
}
 

//
//  AUIRoomNetworkModel.swift
//  AUIKit
//
//  Created by wushengtao on 2023/3/13.
//

import Foundation
import YYModel

@objcMembers
open class AUIRoomCreateNetworkModel: AUICommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = "/v1/room/create"
    }
    
    public var roomName: String?
    public var userName: String?
    public var userAvatar: String?
    public var micSeatCount: UInt = 8
    public var micSeatStyle: UInt = 8
    
    public override func parse(data: Data?) throws -> Any {
        var dic: Any? = nil
        do {
            try dic = super.parse(data: data)
        } catch let err {
            throw err
        }
        guard let dic = dic as? [String: Any],
              let result = dic["data"],
              let roomInfo = AUIRoomInfo.yy_model(withJSON: result) else {
            throw AUICommonError.networkParseFail.toNSError()
        }
        
        roomInfo.memberCount = micSeatCount
        roomInfo.owner = AUIRoomContext.shared.currentUserInfo
        
        return roomInfo
    }
}

open class AUIRoomDestoryNetworkModel: AUICommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = "/v1/room/destroy"
    }
    
    public var roomId: String?
}


open class AUIRoomAnnouncementNetworkModel: AUICommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = "/v1/room/notice"
    }
    
    public var roomId: String?
        
    public var notice: String?
    
}


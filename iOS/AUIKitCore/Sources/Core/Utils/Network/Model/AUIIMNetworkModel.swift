//
//  AUIIMNetworkModel.swift
//  AUIKit
//
//  Created by 朱继超 on 2023/5/24.
//

import UIKit

class AUIIMUserCreateNetworkModel: AUINetworkModel {

    public override init() {
        super.init()
        interfaceName = "/v1/chatRoom/users/create"
    }
    
    public var userName: String?
    
    public override func parse(data: Data?) throws -> Any {
        var dic: Any? = nil
        do {
            try dic = super.parse(data: data)
        } catch let err {
            throw err
        }
        guard let dic = dic as? [String: Any],
              let result = dic["data"] as? [String: String] else {
            throw AUICommonError.networkParseFail.toNSError()
        }
        
        
        return result
    }
}

class AUIIMChatroomCreateNetworkModel: AUINetworkModel {

    public override init() {
        super.init()
        interfaceName = "/v1/chatRoom/rooms/create"
    }
    
    public var userId: String?
    
    public var roomId: String?
    
    public var userName: String?
    
    public override func parse(data: Data?) throws -> Any {
        var dic: Any? = nil
        do {
            try dic = super.parse(data: data)
        } catch let err {
            throw err
        }
        guard let dic = dic as? [String: Any],
              let result = dic["data"] as? [String: String] else {
            throw AUICommonError.networkParseFail.toNSError()
        }
        
        
        return result
    }
}

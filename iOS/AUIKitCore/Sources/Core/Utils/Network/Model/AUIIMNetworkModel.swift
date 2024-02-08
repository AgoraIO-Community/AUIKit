//
//  AUIIMNetworkModel.swift
//  AUIKit
//
//  Created by 朱继超 on 2023/5/24.
//

import UIKit

@objc public enum AUIChatCreateType: Int {
    case userAndRoom = 0   //创建用户和房间
    case user = 1          //仅创建用户
    case room = 2          //仅创建房间
}

@objcMembers
public class AUIChatRoomConfig: NSObject {
    public var name: String = ""
    public var desc: String = ""
    public var maxUsers: Int = 10000
    public var custom: String = ""
    
    static func modelCustomPropertyMapper()-> [String: Any]? {
        return [
            "desc": "description",
        ]
    }
}

@objcMembers
public class AUIChatConfig: NSObject {
    public var orgName: String = ""
    public var appName: String = ""
    public var appKey: String? {
        didSet {
            self.orgName = appKey?.components(separatedBy: "#").first ?? ""
            self.appName = appKey?.components(separatedBy: "#").last ?? ""
        }
    }
    public var clientId: String = ""
    public var clientSecret: String = ""
    
    static func modelPropertyBlacklist() -> [Any] {
        return ["appKey"]
    }
}

@objcMembers
public class AUIChatUser: NSObject {
    public var username: String = ""
    public var password: String = ""
}

public class AUIChatCreateNetworkModel: AUINetworkModel {
    public override init() {
        super.init()
        interfaceName = "/v2/chatRoom/create"
    }
    
    public var appId: String? = AUIRoomContext.shared.commonConfig?.appId ?? ""
    public var chatRoomConfig: AUIChatRoomConfig?
    public var type: AUIChatCreateType = .userAndRoom
    public var imConfig: AUIChatConfig?
    public var user: AUIChatUser?
    
    public override func parse(data: Data?) throws -> Any {
        var dic: Any? = nil
        do {
            try dic = super.parse(data: data)
        } catch let err {
            throw err
        }
        guard let dic = dic as? [String: Any],
              let result = dic["data"] as? [String: Any] else {
            throw AUICommonError.networkParseFail.toNSError()
        }
        
        
        return result
    }
}

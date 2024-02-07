//
//  AUIRoomNetworkModel.swift
//  AUIKit
//
//  Created by wushengtao on 2023/3/13.
//

import Foundation
import YYModel
import Alamofire

@objcMembers
open class AUIRoomCreateNetworkModel: AUICommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = "/v2/room/create"
    }
    public var appId: String? = AUIRoomContext.shared.commonConfig?.appId
    public var sceneId: String?
    public var roomInfo: AUIRoomInfo?
    
    public override func getParameters() -> Parameters? {
        var payloadParam = roomInfo?.yy_modelToJSONObject() as? Parameters ?? [:]
        payloadParam["appId"] = appId ?? ""
        payloadParam["sceneId"] = sceneId ?? ""
        return payloadParam
    }
    
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
        
        return roomInfo
    }
}

@objcMembers
open class AUIRoomUpdateNetworkModel: AUICommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = "/v2/room/update"
    }
    public var appId: String? = AUIRoomContext.shared.commonConfig?.appId
    public var sceneId: String?
    public var roomInfo: AUIRoomInfo?
    
    public override func getParameters() -> Parameters? {
        var payloadParam = roomInfo?.yy_modelToJSONObject() as? Parameters ?? [:]
        payloadParam["appId"] = appId ?? ""
        payloadParam["sceneId"] = sceneId ?? ""
        return payloadParam
    }
    
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
        
        return roomInfo
    }
}

open class AUIRoomDestroyNetworkModel: AUICommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = "/v2/room/destroy"
    }
    
    public var appId: String? = AUIRoomContext.shared.commonConfig?.appId
    public var sceneId: String?
    public var roomId: String?
}



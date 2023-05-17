//
//  AUiRoomListNetworkModel.swift
//  AUiKit
//
//  Created by wushengtao on 2023/4/18.
//

import UIKit

@objcMembers
class AUiRoomListNetworkModel: AUiNetworkModel {
    var lastCreateTime: NSNumber?
    var pageSize: Int = 10
    public override init() {
        super.init()
        interfaceName = "/v1/room/list"
    }
    
    public override func parse(data: Data?) throws -> Any {
        var dic: Any? = nil
        do {
            try dic = super.parse(data: data)
        } catch let err {
            throw err
        }
        guard let dic = dic as? [String: Any],
              let result = dic["data"] as? [String: Any],
              let list = result["list"],
              let roomInfo = NSArray.yy_modelArray(with: AUiRoomInfo.self, json: list) else {
            throw AUiCommonError.networkParseFail.toNSError()
        }
        
        
        return roomInfo
    }
}


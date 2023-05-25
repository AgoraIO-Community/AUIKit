//
//  AUIGiftNetworkModel.swift
//  AUiKit
//
//  Created by 朱继超 on 2023/5/25.
//

import UIKit

class AUIGiftNetworkModel: AUiNetworkModel {
    
    public override init() {
        super.init()
        interfaceName = "/v1/gifts"
    }
    
    public var appId: String?
        
    public override func parse(data: Data?) throws -> Any {
        var dic: Any? = nil
        do {
            try dic = super.parse(data: data)
        } catch let err {
            throw err
        }
        guard let dic = dic as? [String: Any],
              let result = dic["data"] as? [String: String] else {
            throw AUiCommonError.networkParseFail.toNSError()
        }
        
        
        return result
    }

}

//
//  AUiTokenGenerateNetworkModel.swift
//  AUiKit
//
//  Created by wushengtao on 2023/4/20.
//

import Foundation

public class AUiTokenGenerateNetworkModel: AUiNetworkModel {
    public override init() {
        super.init()
        interfaceName = "/v1/token/generate"
    }
    
    public var channelName: String?
    public var userId: String?
    
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

//public class AUiTokenGenerate006NetworkModel: AUiTokenGenerateNetworkModel {
//    public override init() {
//        super.init()
//        interfaceName = "/v1/token006/generate"
//    }
//    
//}

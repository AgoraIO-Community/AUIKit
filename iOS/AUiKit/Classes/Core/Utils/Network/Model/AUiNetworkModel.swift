//
//  AUiNetworkModel.swift
//  AUiKit
//
//  Created by wushengtao on 2023/3/13.
//

import Foundation
import Alamofire
import YYModel

public enum AUiNetworkMethod: Int {
    case get = 0
    case post
    
    func getAfMethod() -> HTTPMethod {
        switch self {
        case .get:
            return HTTPMethod.get
        case .post:
            return HTTPMethod.post
        }
    }
}

@objcMembers
open class AUiNetworkModel: NSObject {
    public let uniqueId: String = UUID().uuidString
    public var host: String = AUiRoomContext.shared.commonConfig?.host ?? ""
    public var interfaceName: String?
    public var method: AUiNetworkMethod = .post
    
    static func modelPropertyBlacklist() -> [Any] {
        return ["uniqueId", "host", "interfaceName", "method"]
    }
    
    public func getHeaders() -> HTTPHeaders {
        var headers = HTTPHeaders()
        let header = HTTPHeader(name: "Content-Type", value: "application/json")
        headers.add(header)
        return headers
    }
    
    public func getParameters() -> Parameters? {
        let param = self.yy_modelToJSONObject() as? Parameters
        return param
    }
    
    public func request(completion: ((Error?, Any?)->())?) {
        AUiNetworking.shared.request(model: self, completion: completion)
    }
    
    
    public func parse(data: Data?) throws -> Any  {
        guard let data = data,
              let dic = try? JSONSerialization.jsonObject(with: data) else {
            throw AUiCommonError.networkParseFail.toNSError()
        }
        
        if let dic = (dic as? [String: Any]), let code = dic["code"] as? Int, code != 0 {
            let message = dic["message"] as? String ?? ""
            throw AUiCommonError.httpError(code, message).toNSError()
        }
        
        return dic
    }
}


@objcMembers
open class AUiCommonNetworkModel: AUiNetworkModel {
    public var userId: String?
}

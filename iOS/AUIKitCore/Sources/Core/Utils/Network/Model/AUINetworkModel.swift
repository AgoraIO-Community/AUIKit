//
//  AUINetworkModel.swift
//  AUIKit
//
//  Created by wushengtao on 2023/3/13.
//

import Foundation
import Alamofire
import YYModel

public enum AUINetworkMethod: Int {
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
open class AUINetworkModel: NSObject {
    public fileprivate(set) var uniqueId: String = UUID().uuidString
    public var host: String = AUIRoomContext.shared.commonConfig?.host ?? ""
    public var interfaceName: String?
    public var method: AUINetworkMethod = .post
    
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
        AUINetworking.shared.request(model: self, completion: completion)
    }
    
    
    public func parse(data: Data?) throws -> Any  {
        guard let data = data,
              let dic = try? JSONSerialization.jsonObject(with: data) else {
            throw AUICommonError.networkParseFail.toNSError()
        }
        
        if let dic = (dic as? [String: Any]), let code = dic["code"] as? Int, code != 0 {
            let message = dic["message"] as? String ?? ""
            throw AUICommonError.httpError(code, message).toNSError()
        }
        
        return dic
    }
    
    public func rtmMessage(roomId: String) -> String {
        let modelObj = self.yy_modelToJSONObject()
        let jsonObj = [
            "interfaceName": interfaceName,
            "uniqueId": uniqueId,
            "channelName": roomId,
            "data": modelObj
        ]
        assert(roomId.count > 0)
        let data = try! JSONSerialization.data(withJSONObject: jsonObj, options: .prettyPrinted)
        let str = String(data: data, encoding: .utf8)!
        return str
    }
    
    public static func model(rtmMessage: String) -> Self? {
        guard let data = rtmMessage.data(using: .utf8),
              let map = try? JSONSerialization.jsonObject(with: data) as? [AnyHashable: Any],
              let dataMap = map["data"] as? [AnyHashable: Any] else {
            return nil
        }
        let model = self.yy_model(with: dataMap)
        model?.interfaceName = map["interfaceName"] as? String ?? ""
        model?.uniqueId = map["uniqueId"] as? String ?? ""
        return model
    }
}


@objcMembers
open class AUICommonNetworkModel: AUINetworkModel {
    public var userId: String?
}

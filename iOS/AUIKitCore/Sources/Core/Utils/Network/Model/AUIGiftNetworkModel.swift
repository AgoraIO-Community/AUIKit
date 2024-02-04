//
//  AUIGiftNetworkModel.swift
//  AUIKit
//
//  Created by 朱继超 on 2023/5/25.
//

import UIKit

class AUIGiftNetworkModel: AUINetworkModel {
    
    public override init() {
        super.init()
        interfaceName = "/v1/gifts/list"
    }
    
    public override func request(completion: ((Error?, Any?)->())?) {
        DispatchQueue.global().async {
            guard let folderPath = Bundle.main.path(forResource: "Gift", ofType: "bundle"),
                  let data = try? Data(contentsOf: URL(fileURLWithPath: "\(folderPath)/gift.json")) else {
                DispatchQueue.main.async {
                    completion?(AUICommonError.unknown.toNSError(), nil)
                }
                return
            }
            
            var value: Any?
            do {
                value = try JSONSerialization.jsonObject(with: data)
            } catch {
                DispatchQueue.main.async {
                    completion?(error, nil)
                }
                return
            }
            DispatchQueue.main.async {
                completion?(nil, value)
            }
        }                 
    }
            
    public override func parse(data: Data?) throws -> Any {
        var dic: Any? = nil
        do {
            try dic = super.parse(data: data)
        } catch let err {
            throw err
        }
        guard let dic1 = dic as? [String: Any],let result = dic1["data"] as? [[String: Any]] else {
            throw AUICommonError.networkParseFail.toNSError()
        }
        
        
        return result
    }

}

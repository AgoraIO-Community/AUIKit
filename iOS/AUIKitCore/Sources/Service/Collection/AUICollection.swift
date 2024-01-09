//
//  AUICollection.swift
//  AUIKitCore
//
//  Created by wushengtao on 2024/1/4.
//

import Foundation

protocol AUICollection: NSObjectProtocol {
    
    func subscribeWillSet(callback: ((String, [String: Any], [String: Any])-> NSError?)?)
    
    func setMetaData(publisherId: String, value: [String: Any], objectId: String, callback: ((NSError?)->())?)
    
    func updateMetaData(value: [String: Any], objectId: String, callback: ((NSError?)->())?)
}

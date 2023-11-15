//
//  AUIThrottlerMetaDataModel.swift
//  AUIKitCore
//
//  Created by wushengtao on 2023/11/14.
//

import Foundation

class AUIThrottlerMetaDataModel: NSObject {
    var metaData: [String: String] = [:]
    var callbacks: [((NSError?) -> ())] = []
    
    func appendMetaDataInfo(metaData: [String: String], completion:@escaping ((NSError?) -> ())) {
        metaData.forEach { key, value in
            self.metaData[key] = value
        }
        callbacks.append(completion)
    }
    
    func reset() {
        metaData.removeAll()
        callbacks.removeAll()
    }
}

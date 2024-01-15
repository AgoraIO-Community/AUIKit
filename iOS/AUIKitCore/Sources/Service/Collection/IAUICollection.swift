//
//  IAUICollection.swift
//  AUIKitCore
//
//  Created by wushengtao on 2024/1/4.
//

import Foundation

public typealias AUICollectionGetClosure = (NSError?, Any?)-> Void

//(publisher uid, valueCmd, new value)
public typealias AUICollectionAddClosure = (String, String?, [String: Any]) -> NSError?

//(publisher uid, valueCmd, new value, old value of item)
public typealias AUICollectionUpdateClosure = (String, String?, [String: Any], [String: Any]) -> NSError?

//(publisher uid, valueCmd, oldValue)
public typealias AUICollectionRemoveClosure = (String, String?, [String: Any]) -> NSError?

//(channelName, key, value)
public typealias AUICollectionAttributesDidChangedClosure = (String, String, Any) -> Void

@objc public protocol IAUICollection: NSObjectProtocol {
    
    /// 添加新的节点
    /// - Parameter callback: <#callback description#>
    @objc optional func subscribeWillAdd(callback: AUICollectionAddClosure?)
    
    /// 订阅即将替换某个节点的事件
    /// - Parameter callback: <#callback description#>
    @objc optional func subscribeWillUpdate(callback: AUICollectionUpdateClosure?)
    
    /// 订阅即将合并某个节点的事件回调
    /// - Parameter callback: <#callback description#>
    @objc optional func subscribeWillMerge(callback: AUICollectionUpdateClosure?)
    
    /// 订阅即将删除某个节点的事件回调
    /// - Parameter callback: <#callback description#>
    @objc optional func subscribeWillRemove(callback: AUICollectionRemoveClosure?)
    
    /// 收到的meta data变化
    /// - Parameter callback: <#callback description#>
    func subscribeAttributesDidChanged(callback: AUICollectionAttributesDidChangedClosure?)
    
    /// 查询当前scene节点所有内容
    /// - Parameter callback: <#callback description#>
    func getMetaData(callback: AUICollectionGetClosure?)
    
    
    /// 添加节点
    /// - Parameters:
    ///   - valueCmd: <#valueCmd description#>
    ///   - value: <#value description#>
    ///   - filter: 如果原始数据满足该filter，新增失败，为nil则无条件新增
    ///   - callback: <#callback description#>
    func addMetaData(valueCmd: String?,
                     value: [String: Any],
                     filter: [[String: Any]]?,
                     callback: ((NSError?)->())?)
    
    /// 更新节点
    /// - Parameters:
    ///   - valueCmd: 命令类型
    ///   - value: <#value description#>
    ///   - filter: 如果原始数据满足该filter，才会更新成功，为nil则更新全部
    ///   - callback: <#callback description#>
    func updateMetaData(valueCmd: String?,
                        value: [String: Any],
                        filter: [[String: Any]]?,
                        callback: ((NSError?)->())?)
    
    /// 合并节点
    /// - Parameters:
    ///   - valueCmd: <#valueCmd description#>
    ///   - value: <#value description#>
    ///   - filter: 如果原始数据满足该filter，才会合并成功，为nil则合并全部
    ///   - callback: <#callback description#>
    func mergeMetaData(valueCmd: String?,
                       value: [String: Any],
                       filter: [[String: Any]]?,
                       callback: ((NSError?)->())?)
    
    /// 移除
    /// - Parameters:
    ///   - valueCmd: <#value description#>
    ///   - filter: <#value description#>
    ///   - callback: <#callback description#>
    func removeMetaData(valueCmd: String?, 
                        filter: [[String: Any]]?,
                        callback: ((NSError?)->())?)
    
    /// 移除整个collection对应的key
    /// - Parameter callback: <#callback description#>
    func cleanMetaData(callback: ((NSError?)->())?)
}

func mergeMap(origMap: [String: Any], newMap: [String: Any]) -> [String: Any] {
    var _origMap = origMap
    newMap.forEach { (k, v) in
        if let dic = v as? [String: Any] {
            let origDic: [String: Any] = _origMap[k] as? [String: Any] ?? [:]
            let newDic = mergeMap(origMap: origDic, newMap: dic)
            _origMap[k] = newDic
        } else {
            //TODO: array ?
            _origMap[k] = v
        }
    }
    return _origMap
}

func getItemIndexes(array: [[String: Any]], filter: [[String: Any]]?) -> [Int]? {
    guard let filter = filter else {
        let indexes = Array(array.indices)
        return indexes.isEmpty ? nil : indexes
    }
    var indexes: [Int] = []
    for (i, value) in array.enumerated() {
        for filterItem in filter {
            var match = true
            for (k, v) in filterItem {
                //only filter String/Bool/Int
                if let valueV = value[k] as? String, let filterV = v as? String {
                    if valueV != filterV {
                        match = false
                        break
                    }
                } else if let valueV = value[k] as? Bool, let filterV = v as? Bool {
                    if valueV != filterV {
                        match = false
                        break
                    }
                } else if let valueV = value[k] as? Int, let filterV = v as? Int {
                    if valueV != filterV {
                        match = false
                        break
                    }
                }
            }
            if match {
                indexes.append(i)
                break
            }
        }
    }
    return indexes.isEmpty ? nil : indexes
}

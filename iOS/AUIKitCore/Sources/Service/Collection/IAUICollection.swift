//
//  IAUICollection.swift
//  AUIKitCore
//
//  Created by wushengtao on 2024/1/4.
//

import Foundation

public typealias AUICollectionGetClosure = (NSError?, Any?)-> Void

//(publisher uid, valueCmd, added objectId, new value)
public typealias AUICollectionAddClosure = (String, String?, [String: Any])-> NSError?

//(publisher uid, valueCmd, new value, old value of item)
public typealias AUICollectionUpdateClosure = (String, String?, [String: Any], [String: Any])-> NSError?

//(publisher uid, valueCmd, removed objectId, old value of item)
public typealias AUICollectionRemoveClosure = (String, String?, String, [String: Any])-> NSError?

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
    
    
    /// 查询当前scene节点所有内容
    /// - Parameter callback: <#callback description#>
    func getMetaData(callback: AUICollectionGetClosure?)
    
    /// 添加节点
    /// - Parameter value: <#value description#>
    func addMetaData(valueCmd: String?,
                     value: [String: Any],
                     callback: ((NSError?)->())?)
    
    /// 更新节点
    /// - Parameters:
    ///   - valueCmd: 命令类型
    ///   - value: <#value description#>
    ///   - objectId: <#objectId description#>
    ///   - callback: <#callback description#>
    func updateMetaData(valueCmd: String?,
                        value: [String: Any],
                        objectId: String,
                        callback: ((NSError?)->())?)
    
    /// 合并节点
    /// - Parameters:
    ///   - valueCmd: <#valueCmd description#>
    ///   - value: <#value description#>
    ///   - objectId: <#objectId description#>
    ///   - callback: <#callback description#>
    func mergeMetaData(valueCmd: String?,
                       value: [String: Any],
                       objectId: String,
                       callback: ((NSError?)->())?)
    
    /// 移除
    /// - Parameters:
    ///   - valueCmd: <#value description#>
    ///   - value: <#value description#>
    ///   - callback: <#callback description#>
    func removeMetaData(valueCmd: String?, objectId: String, callback: ((NSError?)->())?)
    
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

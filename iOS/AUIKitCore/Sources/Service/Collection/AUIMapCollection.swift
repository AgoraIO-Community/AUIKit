//
//  AUIMapCollection.swift
//  AUIKitCore
//
//  Created by wushengtao on 2024/1/4.
//

import Foundation


public class AUIMapCollection: NSObject {
    private var channelName: String
    private var observeKey: String
    private var rtmManager: AUIRtmManager
    private var currentMap: [String: Any] = [:]
    private var metadataWillSetColsure: ((String, String?, [String: Any], [String: Any])-> NSError?)?
    private var metadataWillMergeColsure: ((String, String?, [String: Any], [String: Any])-> NSError?)?
    private var metadataWillRemoveColsure: ((String, String?, [String: Any])-> NSError?)?
    
    deinit {
        rtmManager.unsubscribeAttributes(channelName: channelName, itemKey: observeKey, delegate: self)
        rtmManager.unsubscribeMessage(channelName: channelName, delegate: self)
        aui_info("deinit AUIMicSeatServiceImpl", tag: "AUIMicSeatServiceImpl")
    }
    
    public required init(channelName: String, observeKey: String, rtmManager: AUIRtmManager) {
        self.rtmManager = rtmManager
        self.observeKey = observeKey
        self.channelName = channelName
        super.init()
        rtmManager.subscribeAttributes(channelName: channelName, itemKey: observeKey, delegate: self)
        rtmManager.subscribeMessage(channelName: channelName, delegate: self)
        aui_info("init AUIMicSeatServiceImpl", tag: "AUIMicSeatServiceImpl")
    }
    
    /*
     上层service调用(例如麦位Service)
     collection.subscribeWillSet { publisher, newValue, oldValue in
        if oldValue["owner"] != nil, newValue["owner"] == nil {
            //踢人，需要判断publisher是否是owner.userId一致
        }
     }
     */
    func subscribeWillSet(callback: ((String, String?, [String: Any], [String: Any])-> NSError?)?) {
        self.metadataWillSetColsure = callback
    }
    
    func subscribeWillMerge(callback: ((String, String?, [String: Any], [String: Any])-> NSError?)?) {
        self.metadataWillMergeColsure = callback
    }
    
    func subscribeWillRemove(callback: ((String, String?, [String: Any])-> NSError?)?) {
        self.metadataWillRemoveColsure = callback
    }
    
    /// 更新，替换根节点
    /// - Parameters:
    ///   - valueCmd: 命令类型
    ///   - value: <#value description#>
    ///   - objectId: <#objectId description#>
    ///   - callback: <#callback description#>
    public func updateMetaData(valueCmd: String?, value: [String: Any], objectId: String, callback: ((NSError?)->())?) {
        if AUIRoomContext.shared.getArbiter(channelName: channelName)?.isArbiter() ?? false {
            let currentUserId = AUIRoomContext.shared.currentUserInfo.userId
            rtmSetMetaData(publisherId: currentUserId, valueCmd: valueCmd, value: value, objectId: objectId, callback: callback)
            return
        }
        
        let payload = AUICollectionMessagePayload(type: .update, dataCmd: valueCmd, data: AUIAnyType(map: value))
        let message = AUICollectionMessage(channelName: channelName,
                                           messageType: AUIMessageType.normal,
                                           uniqueId: UUID().uuidString,
                                           objectId: objectId,
                                           payload: payload)

        guard let jsonStr = encodeModelToJsonStr(message) else {
            callback?(NSError(domain: "AUIKit Error", code: Int(-1), userInfo: [ NSLocalizedDescriptionKey : "updateMetaData fail"]))
            return
        }
        let userId = AUIRoomContext.shared.getArbiter(channelName: channelName)?.lockOwnerId ?? ""
        rtmManager.publishAndWaitReceipt(userId: userId,
                                         channelName: channelName,
                                         message: jsonStr,
                                         uniqueId: message.uniqueId ?? "",
                                         completion: callback)
    }
    
    
    /// 合并，替换所有子节点
    /// - Parameters:
    ///   - valueCmd: <#valueCmd description#>
    ///   - value: <#value description#>
    ///   - objectId: <#objectId description#>
    ///   - callback: <#callback description#>
    public func mergeMetaData(valueCmd: String?, value: [String: Any], objectId: String, callback: ((NSError?)->())?) {
        if AUIRoomContext.shared.getArbiter(channelName: channelName)?.isArbiter() ?? false {
            let currentUserId = AUIRoomContext.shared.currentUserInfo.userId
            rtmMergeMetaData(publisherId: currentUserId, valueCmd: valueCmd, value: value, objectId: objectId, callback: callback)
            return
        }
        
        let payload = AUICollectionMessagePayload(type: .merge, dataCmd: valueCmd, data: AUIAnyType(map: value))
        let message = AUICollectionMessage(channelName: channelName,
                                           messageType: AUIMessageType.normal,
                                           uniqueId: UUID().uuidString,
                                           objectId: objectId,
                                           payload: payload)

        guard let jsonStr = encodeModelToJsonStr(message) else {
            callback?(NSError(domain: "AUIKit Error", code: Int(-1), userInfo: [ NSLocalizedDescriptionKey : "updateMetaData fail"]))
            return
        }
        let userId = AUIRoomContext.shared.getArbiter(channelName: channelName)?.lockOwnerId ?? ""
        rtmManager.publishAndWaitReceipt(userId: userId,
                                         channelName: channelName,
                                         message: jsonStr,
                                         uniqueId: message.uniqueId ?? "",
                                         completion: callback)
    }
    
    
    /// 添加
    /// - Parameter value: <#value description#>
    func addMetaData(valueCmd: String?, value: [String: Any], callback: ((NSError?)->())?) {
        updateMetaData(valueCmd: valueCmd, value: value, objectId: "", callback: callback)
    }
    
    /// 移除
    /// - Parameters:
    ///   - valueCmd: <#value description#>
    ///   - value: <#value description#>
    ///   - callback: <#callback description#>
    func removeMetaData(valueCmd: String?, objectId: String, callback: ((NSError?)->())?) {
        if AUIRoomContext.shared.getArbiter(channelName: channelName)?.isArbiter() ?? false {
            let currentUserId = AUIRoomContext.shared.currentUserInfo.userId
            rtmRemoveMetaData(publisherId: currentUserId, valueCmd: valueCmd, objectId: objectId, callback: callback)
            return
        }
        
        let payload = AUICollectionMessagePayload(type: .remove, data: nil)
        let message = AUICollectionMessage(channelName: channelName,
                                           messageType: AUIMessageType.normal,
                                           uniqueId: UUID().uuidString,
                                           objectId: objectId,
                                           payload: payload)
        
        guard let jsonStr = encodeModelToJsonStr(message) else {
            callback?(NSError(domain: "AUIKit Error", code: Int(-1), userInfo: [ NSLocalizedDescriptionKey : "removeMetaData fail"]))
            return
        }
        let userId = AUIRoomContext.shared.getArbiter(channelName: channelName)?.lockOwnerId ?? ""
        rtmManager.publishAndWaitReceipt(userId: userId,
                                         channelName: channelName,
                                         message: jsonStr,
                                         uniqueId: message.uniqueId ?? "",
                                         completion: callback)
    }
}

//MARK: private
extension AUIMapCollection {
    private func rtmSetMetaData(publisherId: String,
                                valueCmd: String?,
                                value: [String: Any],
                                objectId: String,
                                callback: ((NSError?)->())?) {
        if let err = self.metadataWillSetColsure?(publisherId, valueCmd, value, currentMap) {
            callback?(err)
            return
        }
        
        var map = currentMap
        value.forEach { (key: String, value: Any) in
            map[key] = value
        }
        guard let data = try? JSONSerialization.data(withJSONObject: map, options: .prettyPrinted),
              let metaData = String(data: data, encoding: .utf8) else {
            callback?(NSError(domain: "AUIKit Error", code: Int(-1), userInfo: [ NSLocalizedDescriptionKey : "rtmSetMetaData fail"]))
            return
        }
        self.rtmManager.setBatchMetadata(channelName: channelName,
                                         lockName: kRTM_Referee_LockName,
                                         metadata: [observeKey: metaData]) { error in
            callback?(error)
        }
    }
    
    private func rtmMergeMetaData(publisherId: String,
                                  valueCmd: String?,
                                  value: [String: Any],
                                  objectId: String,
                                  callback: ((NSError?)->())?) {
        if let err = self.metadataWillMergeColsure?(publisherId, valueCmd, value, currentMap) {
            callback?(err)
            return
        }
        
        func replaceMap(origMap: [String: Any], newMap: [String: Any]) -> [String: Any] {
            var _origMap = origMap
            newMap.forEach { (k, v) in
                if let dic = v as? [String: Any] {
                    let origDic: [String: Any] = _origMap[k] as? [String: Any] ?? [:]
                    let newDic = replaceMap(origMap: origDic, newMap: dic)
                    _origMap[k] = newDic
                } else {
                    //TODO: array ?
                    _origMap[k] = v
                }
            }
            return _origMap
        }
        
        let map = replaceMap(origMap: currentMap, newMap: value)
        guard let data = try? JSONSerialization.data(withJSONObject: map, options: .prettyPrinted),
              let metaData = String(data: data, encoding: .utf8) else {
            callback?(NSError(domain: "AUIKit Error", code: Int(-1), userInfo: [ NSLocalizedDescriptionKey : "rtmSetMetaData fail"]))
            return
        }
        self.rtmManager.setBatchMetadata(channelName: channelName,
                                         lockName: kRTM_Referee_LockName,
                                         metadata: [observeKey: metaData]) { error in
            callback?(error)
        }
    }
    
    func rtmRemoveMetaData(publisherId: String,
                           valueCmd: String?,
                           objectId: String,
                           callback: ((NSError?)->())?) {
        if let err = self.metadataWillRemoveColsure?(publisherId, valueCmd, currentMap) {
            callback?(err)
            return
        }
        
        var map = currentMap
        map.removeAll()
        let data = try! JSONSerialization.data(withJSONObject: map, options: .prettyPrinted)
        guard let value = String(data: data, encoding: .utf8) else {
            callback?(NSError(domain: "AUIKit Error", code: Int(-1), userInfo: [ NSLocalizedDescriptionKey : "rtmRemoveMetaData fail"]))
            return
        }
        self.rtmManager.setBatchMetadata(channelName: channelName,
                                         lockName: kRTM_Referee_LockName,
                                         metadata: [observeKey: value]) { error in
            callback?(error)
        }
    }
}

//MARK: AUIRtmAttributesProxyDelegate
extension AUIMapCollection: AUIRtmAttributesProxyDelegate {
    public func onAttributesDidChanged(channelName: String, key: String, value: Any) {
        guard channelName == self.channelName, key == self.observeKey else {return}
        guard let map = value as? [String: [String: Any]] else {return}
        self.currentMap = map
    }
}

//MARK: AUIRtmMessageProxyDelegate
extension AUIMapCollection: AUIRtmMessageProxyDelegate {
    private func sendReceipt(publisher: String, uniqueId: String, error: NSError?) {
        let data: [String: Any] = ["code": error?.code ?? 0,
                                   "reason": error?.localizedDescription ?? ""]
        let payload = AUICollectionMessagePayload(data: AUIAnyType(map: data))
        let message = AUICollectionMessage(channelName: channelName,
                                           messageType: .receipt,
                                           uniqueId: uniqueId,
                                           objectId: "",
                                           payload: payload)
        guard let jsonStr = encodeModelToJsonStr(message) else {
            aui_warn("sendReceipt fail")
            return
        }
        rtmManager.publish(userId: publisher, channelName: channelName, message: jsonStr) { err in
        }
    }
    public func onMessageReceive(publisher: String, message: String) {
        guard let data = message.data(using: .utf8),
              let map = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
              let message: AUICollectionMessage = decodeModel(map) else {
            return
        }
        aui_info("onMessageReceive: \(map)")
        let uniqueId = message.uniqueId ?? ""
        let channelName = message.channelName ?? ""
        guard channelName == self.channelName else {return}
        if message.messageType == .receipt {
            if let callback = rtmManager.receiptCallbackMap[uniqueId]?.closure {
                rtmManager.markReceiptFinished(uniqueId: uniqueId)
                let data = message.payload?.data?.toJsonObject() as? [String : Any]
                let code = data?["code"] as? Int ?? 0
                let reason = data?["reason"] as? String ?? "success"
                callback(code == 0 ? nil : NSError(domain: "AUIKit Error", code: Int(code), userInfo: [ NSLocalizedDescriptionKey : reason]))
            }
            return
        }
        
        guard let updateType = message.payload?.type else {
            sendReceipt(publisher: publisher,
                        uniqueId: uniqueId,
                        error: NSError(domain: "AUIKit Error", code: -1, userInfo: [ NSLocalizedDescriptionKey : "updateType not found"]))
            return
        }
        
        let valueCmd = message.payload?.dataCmd
        var err: NSError? = nil
        switch updateType {
        case .add, .update, .merge:
            if let value = message.payload?.data?.toJsonObject() as? [String : Any] {
                if updateType == .merge {
                    rtmMergeMetaData(publisherId: publisher, valueCmd: valueCmd, value: value, objectId: "") {[weak self] error in
                        self?.sendReceipt(publisher: publisher, uniqueId: uniqueId, error: error)
                    }
                } else {
                    rtmSetMetaData(publisherId: publisher, valueCmd: valueCmd, value: value, objectId: "") {[weak self] error in
                        self?.sendReceipt(publisher: publisher, uniqueId: uniqueId, error: error)
                    }
                }
                return
            }
            err = NSError(domain: "AUIKit Error", code: -1,
                          userInfo: [ NSLocalizedDescriptionKey : "payload is not a map"])
        case .remove:
            rtmRemoveMetaData(publisherId: publisher, valueCmd: valueCmd, objectId: "") {[weak self] error in
                self?.sendReceipt(publisher: publisher, uniqueId: uniqueId, error: error)
            }
        case .increase:
            break
        case .decrease:
            break
        }
        
        guard let err = err else {return}
        sendReceipt(publisher: publisher,
                    uniqueId: uniqueId,
                    error: err)
    }
}

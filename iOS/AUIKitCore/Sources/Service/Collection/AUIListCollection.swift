//
//  AUIListCollection.swift
//  AUIKitCore
//
//  Created by wushengtao on 2024/1/4.
//

import Foundation

private func aui_list_log(_ text: String) {
    aui_info(text, tag: "aui_collection_list")
}

private func aui_list_warn(_ text: String) {
    aui_warn(text, tag: "aui_collection_list")
}
public class AUIListCollection: NSObject {
    private var channelName: String
    private var observeKey: String
    private var rtmManager: AUIRtmManager
    private var currentList: [[String: Any]] = []
    private var metadataWillAddColsure: AUICollectionAddClosure?
    private var metadataWillUpdateColsure: AUICollectionUpdateClosure?
    private var metadataWillMergeColsure: AUICollectionUpdateClosure?
    private var metadataWillRemoveColsure: AUICollectionRemoveClosure?
    
    deinit {
        rtmManager.unsubscribeAttributes(channelName: channelName, itemKey: observeKey, delegate: self)
        rtmManager.unsubscribeMessage(channelName: channelName, delegate: self)
        aui_list_log("deinit AUIListCollection")
    }
    
    public required init(channelName: String, observeKey: String, rtmManager: AUIRtmManager) {
        self.rtmManager = rtmManager
        self.observeKey = observeKey
        self.channelName = channelName
        super.init()
        rtmManager.subscribeAttributes(channelName: channelName, itemKey: observeKey, delegate: self)
        rtmManager.subscribeMessage(channelName: channelName, delegate: self)
        aui_list_log("init AUIListCollection")
    }
}

extension AUIListCollection: IAUICollection {
    
    public func subscribeWillAdd(callback: AUICollectionAddClosure?) {
        self.metadataWillAddColsure = callback
    }
    /*
     上层service调用(例如麦位Service)
     collection.subscribeWillSet { publisher, newValue, oldValue in
        if oldValue["owner"] != nil, newValue["owner"] == nil {
            //踢人，需要判断publisher是否是owner.userId一致
        }
     }
     */
    public func subscribeWillUpdate(callback: AUICollectionUpdateClosure?) {
        self.metadataWillUpdateColsure = callback
    }
    
    public func subscribeWillMerge(callback: AUICollectionUpdateClosure?) {
        self.metadataWillMergeColsure = callback
    }
    
    public func subscribeWillRemove(callback: AUICollectionRemoveClosure?) {
        self.metadataWillRemoveColsure = callback
    }
    
    public func getMetaData(callback: AUICollectionGetClosure?) {
        aui_list_log("getMetaData")
        self.rtmManager.getMetadata(channelName: self.channelName) {[weak self] error, map in
            aui_list_log("getMetaData completion: \(error?.localizedDescription ?? "success")")
            guard let self = self else {return}
            if let error = error {
                //TODO: error
                callback?(error, nil)
                return
            }
            
            guard let jsonStr = map?[self.observeKey],
                  let jsonDict = decodeToJsonObj(jsonStr) as? [[String: Any]] else {
                //TODO: error
                callback?(nil, nil)
                return
            }
            
            callback?(nil, jsonDict)
        }
    }
    
    public func updateMetaData(valueCmd: String?,
                               value: [String : Any],
                               objectId: String,
                               callback: ((NSError?) -> ())?) {
        if AUIRoomContext.shared.getArbiter(channelName: channelName)?.isArbiter() ?? false {
            let currentUserId = AUIRoomContext.shared.currentUserInfo.userId
            rtmSetMetaData(publisherId: currentUserId, 
                           valueCmd: valueCmd,
                           value: value, 
                           objectId: objectId,
                           callback: callback)
            return
        }
        
        let payload = AUICollectionMessagePayload(type: .update, data: AUIAnyType(map: value))
        let message = AUICollectionMessage(channelName: channelName,
                                           messageType: AUIMessageType.normal,
                                           uniqueId: UUID().uuidString,
                                           objectId: objectId,
                                           payload: payload)

        guard let jsonStr = encodeModelToJsonStr(message) else {
            callback?(NSError.auiError("updateMetaData fail"))
            return
        }
        
        let userId = AUIRoomContext.shared.getArbiter(channelName: channelName)?.lockOwnerId ?? ""
        rtmManager.publishAndWaitReceipt(userId: userId,
                                         channelName: channelName,
                                         message: jsonStr,
                                         uniqueId: message.uniqueId ?? "",
                                         completion: callback)
    }
    
    public func mergeMetaData(valueCmd: String?,
                              value: [String : Any],
                              objectId: String,
                              callback: ((NSError?) -> ())?) {
        if AUIRoomContext.shared.getArbiter(channelName: channelName)?.isArbiter() ?? false {
            let currentUserId = AUIRoomContext.shared.currentUserInfo.userId
            rtmMergeMetaData(publisherId: currentUserId,
                             valueCmd: valueCmd,
                             value: value,
                             objectId: objectId,
                             callback: callback)
            return
        }
        
        let payload = AUICollectionMessagePayload(type: .merge, data: AUIAnyType(map: value))
        let message = AUICollectionMessage(channelName: channelName,
                                           messageType: AUIMessageType.normal,
                                           uniqueId: UUID().uuidString,
                                           objectId: objectId,
                                           payload: payload)

        guard let jsonStr = encodeModelToJsonStr(message) else {
            callback?(NSError.auiError("mergeMetaData fail"))
            return
        }
        
        let userId = AUIRoomContext.shared.getArbiter(channelName: channelName)?.lockOwnerId ?? ""
        rtmManager.publishAndWaitReceipt(userId: userId,
                                         channelName: channelName,
                                         message: jsonStr,
                                         uniqueId: message.uniqueId ?? "",
                                         completion: callback)
    }
    
    public func addMetaData(valueCmd: String?,
                            value: [String : Any],
                            callback: ((NSError?) -> ())?) {
        if AUIRoomContext.shared.getArbiter(channelName: channelName)?.isArbiter() ?? false {
            let currentUserId = AUIRoomContext.shared.currentUserInfo.userId
            rtmAddMetaData(publisherId: currentUserId,
                           valueCmd: valueCmd,
                           value: value,
                           callback: callback)
            return
        }
        
        let payload = AUICollectionMessagePayload(type: .add, data: AUIAnyType(map: value))
        let message = AUICollectionMessage(channelName: channelName,
                                           messageType: AUIMessageType.normal,
                                           uniqueId: UUID().uuidString,
                                           payload: payload)

        guard let jsonStr = encodeModelToJsonStr(message) else {
            callback?(NSError.auiError("addMetaData fail"))
            return
        }
        
        let userId = AUIRoomContext.shared.getArbiter(channelName: channelName)?.lockOwnerId ?? ""
        rtmManager.publishAndWaitReceipt(userId: userId,
                                         channelName: channelName,
                                         message: jsonStr,
                                         uniqueId: message.uniqueId ?? "",
                                         completion: callback)
        
    }
    
    public func removeMetaData(valueCmd: String?,
                               objectId: String,
                               callback: ((NSError?) -> ())?) {
        if AUIRoomContext.shared.getArbiter(channelName: channelName)?.isArbiter() ?? false {
            let currentUserId = AUIRoomContext.shared.currentUserInfo.userId
            rtmRemoveMetaData(publisherId: currentUserId, 
                              valueCmd: valueCmd,
                              objectId: objectId, 
                              callback: callback)
            return
        }
        
        
        let payload = AUICollectionMessagePayload(type: .remove)
        let message = AUICollectionMessage(channelName: channelName,
                                           messageType: AUIMessageType.normal,
                                           uniqueId: UUID().uuidString,
                                           objectId: objectId,
                                           payload: payload)

        guard let jsonStr = encodeModelToJsonStr(message) else {
            callback?(NSError.auiError("removeMetaData fail"))
            return
        }
        
        let userId = AUIRoomContext.shared.getArbiter(channelName: channelName)?.lockOwnerId ?? ""
        rtmManager.publishAndWaitReceipt(userId: userId,
                                         channelName: channelName,
                                         message: jsonStr,
                                         uniqueId: message.uniqueId ?? "",
                                         completion: callback)
    }
    
    public func cleanMetaData(callback: ((NSError?) -> ())?) {
        if AUIRoomContext.shared.getArbiter(channelName: channelName)?.isArbiter() ?? false {
            rtmCleanMetaData(callback: callback)
            return
        }
        
        let payload = AUICollectionMessagePayload(type: .remove, data: nil)
        let message = AUICollectionMessage(channelName: channelName,
                                           messageType: AUIMessageType.normal,
                                           uniqueId: UUID().uuidString,
                                           objectId: "",
                                           payload: payload)
        
        guard let jsonStr = encodeModelToJsonStr(message) else {
            callback?(NSError.auiError("removeMetaData fail"))
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

extension AUIListCollection {
    private func getItemIndex(objectId: String) -> Int? {
        let idx =  currentList.firstIndex(where: { $0["objectId"] as? String == objectId })
        return idx
    }
    
    private func rtmAddMetaData(publisherId: String,
                                valueCmd: String?,
                                value: [String: Any],
                                callback: ((NSError?)->())?) {
        if let err = self.metadataWillAddColsure?(publisherId, valueCmd, value) {
            callback?(err)
            return
        }
        var list = currentList
        list.append(value)
        
        let data = try! JSONSerialization.data(withJSONObject: list, options: .prettyPrinted)
        guard let value = String(data: data, encoding: .utf8) else {
            callback?(NSError.auiError("rtmAddMetaData fail"))
            return
        }
        self.rtmManager.setBatchMetadata(channelName: channelName,
                                         lockName: kRTM_Referee_LockName,
                                         metadata: [observeKey: value]) { error in
            callback?(error)
        }
    }
    
    private func rtmSetMetaData(publisherId: String,
                                valueCmd: String?,
                                value: [String: Any],
                                objectId: String,
                                callback: ((NSError?)->())?) {
        guard let itemIdx = getItemIndex(objectId: objectId) else {
            callback?(NSError.auiError("rtmSetMetaData fail, objectId not found"))
            return
        }
        let item = currentList[itemIdx]
        if let err = self.metadataWillUpdateColsure?(publisherId, valueCmd, value, item) {
            callback?(err)
            return
        }
        
        var list = currentList
        var tempItem = item
        value.forEach { (key, value) in
            tempItem[key] = value
        }
        list[itemIdx] = tempItem
        
        let data = try! JSONSerialization.data(withJSONObject: list, options: .prettyPrinted)
        guard let value = String(data: data, encoding: .utf8) else {
            callback?(NSError.auiError("rtmRemoveMetaData fail"))
            return
        }
        self.rtmManager.setBatchMetadata(channelName: channelName,
                                         lockName: kRTM_Referee_LockName,
                                         metadata: [observeKey: value]) { error in
            callback?(error)
        }
    }
    
    func rtmMergeMetaData(publisherId: String,
                          valueCmd: String?,
                          value: [String: Any],
                          objectId: String,
                          callback: ((NSError?)->())?) {
        guard let itemIdx = getItemIndex(objectId: objectId) else {
            callback?(NSError.auiError("rtmRemoveMetaData fail, objectId not found"))
            return
        }
        let item = currentList[itemIdx]
        if let err = self.metadataWillMergeColsure?(publisherId, valueCmd, value, item) {
            callback?(err)
            return
        }
        
        var list = currentList
        let tempItem = mergeMap(origMap: item, newMap: value)
        list[itemIdx] = tempItem
        
        let data = try! JSONSerialization.data(withJSONObject: list, options: .prettyPrinted)
        guard let value = String(data: data, encoding: .utf8) else {
            callback?(NSError.auiError("rtmRemoveMetaData fail"))
            return
        }
        self.rtmManager.setBatchMetadata(channelName: channelName,
                                         lockName: kRTM_Referee_LockName,
                                         metadata: [observeKey: value]) { error in
            callback?(error)
        }
    }
    
    func rtmRemoveMetaData(publisherId: String,
                           valueCmd: String?,
                           objectId: String,
                           callback: ((NSError?)->())?) {
        guard let itemIdx = getItemIndex(objectId: objectId) else {
            callback?(NSError.auiError("rtmRemoveMetaData fail, objectId not found"))
            return
        }
        let item = currentList[itemIdx]
        if let err = self.metadataWillRemoveColsure?(publisherId, valueCmd, objectId, item) {
            callback?(err)
            return
        }
        
        let list = self.currentList.filter { $0["objectId"] as? String != objectId}
        
        let data = try! JSONSerialization.data(withJSONObject: list, options: .prettyPrinted)
        guard let value = String(data: data, encoding: .utf8) else {
            callback?(NSError.auiError("rtmRemoveMetaData fail"))
            return
        }
        self.rtmManager.setBatchMetadata(channelName: channelName,
                                         lockName: kRTM_Referee_LockName,
                                         metadata: [observeKey: value]) { error in
            callback?(error)
        }
    }
    
    func rtmCleanMetaData(callback: ((NSError?)->())?) {
        self.rtmManager.cleanBatchMetadata(channelName: channelName,
                                           lockName: kRTM_Referee_LockName,
                                           removeKeys: [observeKey]) { error in
            callback?(error)
        }
    }
}

extension AUIListCollection: AUIRtmAttributesProxyDelegate {
    public func onAttributesDidChanged(channelName: String, key: String, value: Any) {
        guard channelName == self.channelName, key == self.observeKey else {return}
        guard let list = value as? [[String: Any]] else {return}
        self.currentList = list
    }
}

//MARK: AUIRtmMessageProxyDelegate
extension AUIListCollection: AUIRtmMessageProxyDelegate {
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
            aui_list_warn("sendReceipt fail")
            return
        }
        rtmManager.publish(channelName: publisher, message: jsonStr, completion: { err in
        })
    }
    
    public func onMessageReceive(publisher: String, message: String) {
        guard let data = message.data(using: .utf8),
              let map = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
              let message: AUICollectionMessage = decodeModel(map) else {
            return
        }
        aui_list_log("onMessageReceive: \(map)")
        let uniqueId = message.uniqueId ?? ""
        let channelName = message.channelName ?? ""
        guard channelName == self.channelName else {return}
        if message.messageType == .receipt {
            if let callback = rtmManager.receiptCallbackMap[uniqueId]?.closure {
                rtmManager.markReceiptFinished(uniqueId: uniqueId)
                let data = message.payload?.data?.toJsonObject() as? [String : Any]
                let code = data?["code"] as? Int ?? 0
                let reason = data?["reason"] as? String ?? "success"
                callback(code == 0 ? nil : NSError.auiError(reason))
            }
            return
        }
        
        guard let updateType = message.payload?.type else {
            sendReceipt(publisher: publisher,
                        uniqueId: uniqueId,
                        error: NSError.auiError("updateType not found"))
            return
        }
        
        let objectId = message.objectId ?? ""
        let valueCmd = message.payload?.dataCmd
        var err: NSError? = nil
        switch updateType {
        case .add, .update, .merge:
            if let value = message.payload?.data?.toJsonObject() as? [String : Any] {
                if updateType == .add {
                    rtmAddMetaData(publisherId: publisher,
                                   valueCmd: valueCmd,
                                   value: value) { [weak self] error in
                        self?.sendReceipt(publisher: publisher, uniqueId: uniqueId, error: error)
                    }
                } else if updateType == .merge {
                    rtmMergeMetaData(publisherId: publisher,
                                     valueCmd: valueCmd,
                                     value: value, 
                                     objectId: objectId) {[weak self] error in
                        self?.sendReceipt(publisher: publisher, uniqueId: uniqueId, error: error)
                    }
                } else {
                    rtmSetMetaData(publisherId: publisher, valueCmd: valueCmd, value: value, objectId: objectId) {[weak self] error in
                        self?.sendReceipt(publisher: publisher, uniqueId: uniqueId, error: error)
                    }
                }
                return
            }
            err = NSError.auiError("payload is not a map")
        case .remove:
            cleanMetaData(callback: {[weak self] error in
                self?.sendReceipt(publisher: publisher, uniqueId: uniqueId, error: error)
            })
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

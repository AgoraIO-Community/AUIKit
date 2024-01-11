//
//  AUIListCollection.swift
//  AUIKitCore
//
//  Created by wushengtao on 2024/1/4.
//

import Foundation

public class AUIListCollection: NSObject {
    private var channelName: String
    private var observeKey: String
    private var rtmManager: AUIRtmManager
    private var currentList: [[String: Any]] = []
    private var metadataWillSetColsure: ((String, [String: Any], [String: Any])-> NSError?)?
    private var metadataWillRemoveColsure: ((String, [String: Any])-> NSError?)?
    
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
    func subscribeWillSet(callback: ((String, [String: Any], [String: Any])-> NSError?)?) {
        self.metadataWillSetColsure = callback
    }
    
    func subscribeWillRemove(callback: ((String, [String: Any])-> NSError?)?) {
        self.metadataWillRemoveColsure = callback
    }
    
    
    /// 更新
    /// - Parameters:
    ///   - value: <#value description#>
    ///   - objectId: <#objectId description#>
    public func updateMetaData(value: [String: Any], objectId: String, callback: ((NSError?)->())?) {
        if AUIRoomContext.shared.getArbiter(channelName: channelName)?.isArbiter() ?? false {
            let currentUserId = AUIRoomContext.shared.currentUserInfo.userId
            rtmSetMetaData(publisherId: currentUserId, value: value, objectId: objectId, callback: callback)
            return
        }
        
        let payload = AUICollectionMessagePayload(type: .update, data: AUIAnyType(map: value))
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
    func addMetaData(value: [String: Any], callback: ((NSError?)->())?) {
        updateMetaData(value: value, objectId: "", callback: callback)
    }
    
    /// 移除
    /// - Parameters:
    ///   - value: <#value description#>
    ///   - callback: <#callback description#>
    func removeMetaData(objectId: String, callback: ((NSError?)->())?) {
        if AUIRoomContext.shared.getArbiter(channelName: channelName)?.isArbiter() ?? false {
            let currentUserId = AUIRoomContext.shared.currentUserInfo.userId
            rtmRemoveMetaData(publisherId: currentUserId, objectId: objectId, callback: callback)
            return
        }
        
        
        let payload = AUICollectionMessagePayload(type: .remove)
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

extension AUIListCollection {
    private func getItemIndex(objectId: String) -> Int? {
        let idx =  currentList.firstIndex(where: { $0["objectId"] as? String == objectId })
        return idx
    }
    
    private func rtmSetMetaData(publisherId: String,
                                value: [String: Any],
                                objectId: String,
                                callback: ((NSError?)->())?) {
        guard let itemIdx = getItemIndex(objectId: objectId) else {
            callback?(NSError(domain: "AUIKit Error", code: Int(-1), userInfo: [ NSLocalizedDescriptionKey : "rtmRemoveMetaData fail, objectId not found"]))
            return
        }
        let item = currentList[itemIdx]
        if let err = self.metadataWillSetColsure?(publisherId, value, item) {
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
            callback?(NSError(domain: "AUIKit Error", code: Int(-1), userInfo: [ NSLocalizedDescriptionKey : "rtmRemoveMetaData fail"]))
            return
        }
        self.rtmManager.setBatchMetadata(channelName: channelName,
                                         lockName: kRTM_Referee_LockName,
                                         metadata: [observeKey: value]) { error in
            callback?(error)
        }
    }
    
    func rtmRemoveMetaData(publisherId: String,
                           objectId: String,
                           callback: ((NSError?)->())?) {
        guard let itemIdx = getItemIndex(objectId: objectId) else {
            callback?(NSError(domain: "AUIKit Error", code: Int(-1), userInfo: [ NSLocalizedDescriptionKey : "rtmRemoveMetaData fail, objectId not found"]))
            return
        }
        let item = currentList[itemIdx]
        if let err = self.metadataWillRemoveColsure?(publisherId, item) {
            callback?(err)
            return
        }
        
        let list = self.currentList.filter { $0["objectId"] as? String != objectId}
        
        let data = try! JSONSerialization.data(withJSONObject: list, options: .prettyPrinted)
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
            aui_warn("sendReceipt fail")
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
        
        let uniqueId = message.uniqueId ?? ""
        let channelName = message.channelName ?? ""
        guard channelName == self.channelName else {return}
        if message.messageType == AUIMessageType.receipt {
            if let callback = rtmManager.receiptCallbackMap[uniqueId]?.closure {
                rtmManager.markReceiptFinished(uniqueId: uniqueId)
                let data = message.payload?.data?.toJsonObject() as? [String: Any]
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
        
        guard let objectId = message.objectId else {
            sendReceipt(publisher: publisher,
                        uniqueId: uniqueId,
                        error: NSError(domain: "AUIKit Error", code: -1, userInfo: [ NSLocalizedDescriptionKey : "objectId not found"]))
            return
        }
        
        var err: NSError? = nil
        switch updateType {
        case .add, .update, .merge:
            if let value = message.payload?.data as? [String : Any] {
                rtmSetMetaData(publisherId: publisher, value: value, objectId: objectId) {[weak self] error in
                    self?.sendReceipt(publisher: publisher, uniqueId: uniqueId, error: error)
                }
                return
            }
            err = NSError(domain: "AUIKit Error", code: -1, 
                          userInfo: [ NSLocalizedDescriptionKey : "payload is not a map"])
        case .remove:
            rtmRemoveMetaData(publisherId: publisher, objectId: objectId) {[weak self] error in
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

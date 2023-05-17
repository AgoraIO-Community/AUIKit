//
//  AUiRtmMsgProxy.swift
//  AUiKit
//
//  Created by wushengtao on 2023/2/24.
//

import Foundation
import AgoraRtcKit

@objc public protocol AUiRtmErrorProxyDelegate: NSObjectProtocol {
    
    /// token过期
    /// - Parameter channelName: <#channelName description#>
    @objc optional func onTokenPrivilegeWillExpire(channelName: String?)
    
    /// 网络状态变化
    /// - Parameters:
    ///   - channelName: <#channelName description#>
    ///   - state: <#state description#>
    ///   - reason: <#reason description#>
    @objc optional func onConnectionStateChanged(channelName: String,
                                                 connectionStateChanged state: AgoraRtmClientConnectionState,
                                                 result reason: AgoraRtmClientConnectionChangeReason)
    
    /// 收到的KV为空
    /// - Parameter channelName: <#channelName description#>
    @objc optional func onMsgRecvEmpty(channelName: String)
}

@objc public protocol AUiRtmMsgProxyDelegate: NSObjectProtocol {
    func onMsgDidChanged(channelName: String, key: String, value: Any)
}

public protocol AUiRtmUserProxyDelegate: NSObject {
    func onUserSnapshotRecv(channelName: String, userId:String, userList: [[String: Any]])
    func onUserDidJoined(channelName: String, userId:String, userInfo: [String: Any])
    func onUserDidLeaved(channelName: String, userId:String, userInfo: [String: Any])
    func onUserDidUpdated(channelName: String, userId:String, userInfo: [String: Any])
}

/// RTM消息转发器
open class AUiRtmMsgProxy: NSObject {
    private var msgDelegates:[String: NSHashTable<AnyObject>] = [:]
    private var msgCacheAttr: [String: [String: String]] = [:]
    private var userDelegates: NSHashTable<AnyObject> = NSHashTable<AnyObject>.weakObjects()
    private var errorDelegates: NSHashTable<AnyObject> = NSHashTable<AnyObject>.weakObjects()
    weak var origRtmDelegate: AgoraRtmClientDelegate?  //保存原有的delegate做转发
    
    func cleanCache(channelName: String) {
        msgCacheAttr[channelName] = nil
    }
    
    func subscribeMsg(channelName: String, itemKey: String, delegate: AUiRtmMsgProxyDelegate) {
        let key = "\(channelName)__\(itemKey)"
        if let value = msgDelegates[key] {
            if !value.contains(delegate) {
                value.add(delegate)
            }
            return
        }
        let weakObjects = NSHashTable<AnyObject>.weakObjects()
        weakObjects.add(delegate)
        msgDelegates[key] = weakObjects
    }
    
    func unsubscribeMsg(channelName: String, itemKey: String, delegate: AUiRtmMsgProxyDelegate) {
        let key = "\(channelName)__\(itemKey)"
        guard let value = msgDelegates[key] else {
            return
        }
        value.remove(delegate)
    }
    
    func subscribeUser(channelName: String, delegate: AUiRtmUserProxyDelegate) {
        if userDelegates.contains(delegate) {
            return
        }
        userDelegates.add(delegate)
    }
    
    func unsubscribeUser(channelName: String, delegate: AUiRtmUserProxyDelegate) {
        userDelegates.remove(delegate)
    }
    
    func subscribeError(channelName: String, delegate: AUiRtmErrorProxyDelegate) {
        if errorDelegates.contains(delegate) {
            return
        }
        errorDelegates.add(delegate)
    }
    
    func unsubscribeError(channelName: String, delegate: AUiRtmErrorProxyDelegate) {
        errorDelegates.remove(delegate)
    }
}

//MARK: AgoraRtmClientDelegate
extension AUiRtmMsgProxy: AgoraRtmClientDelegate {
    public func rtmKit(_ rtmKit: AgoraRtmClientKit, onTokenPrivilegeWillExpire channel: String?) {
        aui_info("onTokenPrivilegeWillExpire: \(channel ?? "")", tag: "AUiRtmMsgProxy")
        //TODO: 暂时实现这个
        origRtmDelegate?.rtmKit?(rtmKit, onTokenPrivilegeWillExpire: channel)
        
        errorDelegates.objectEnumerator().forEach { element in
            (element as? AUiRtmErrorProxyDelegate)?.onTokenPrivilegeWillExpire?(channelName: channel)
        }
    }
    
    public func rtmKit(_ kit: AgoraRtmClientKit,
                       channel channelName: String,
                       connectionStateChanged state: AgoraRtmClientConnectionState,
                       result reason: AgoraRtmClientConnectionChangeReason) {
        aui_info("connectionStateChanged: \(state.rawValue)", tag: "AUiRtmMsgProxy")
        origRtmDelegate?.rtmKit?(kit, channel: channelName, connectionStateChanged: state, result: reason)
        
        errorDelegates.objectEnumerator().forEach { element in
            (element as? AUiRtmErrorProxyDelegate)?.onConnectionStateChanged?(channelName: channelName,
                                                                              connectionStateChanged: state,
                                                                              result: reason)
        }
    }
    
    public func rtmKit(_ rtmKit: AgoraRtmClientKit, on event: AgoraRtmStorageEvent) {
        origRtmDelegate?.rtmKit?(rtmKit, on: event)
        
        aui_info("storage event[\(event.target)] channelType: [\(event.channelType.rawValue)] storageType: [\(event.eventType.rawValue)] =======", tag: "AUiRtmMsgProxy")
        //key使用channelType__eventType，保证message channel/stream channel, user storage event/channel storage event共存
        let cacheKey = event.target//"\(event.channelType.rawValue)__\(event.eventType.rawValue)_\(event.target)"
        var cache = self.msgCacheAttr[cacheKey] ?? [:]
        event.data.getItems().forEach { item in
//            aui_info("\(item.key): \(item.value)", tag: "AUiRtmMsgProxy")
            //判断value和缓存里是否一致，这里用string可能会不准，例如不同终端序列化的时候json obj不同kv的位置不一样会造成生成的json string不同
            if cache[item.key] == item.value {
                aui_info("there are no changes of [\(item.key)]", tag: "AUiRtmMsgProxy")
                return
            }
            cache[item.key] = item.value
            guard let itemData = item.value.data(using: .utf8), let itemValue = try? JSONSerialization.jsonObject(with: itemData) else {
                aui_info("parse itemData fail: \(item.key) \(item.value)", tag: "AUiRtmMsgProxy")
                return
            }
            let delegateKey = "\(event.target)__\(item.key)"
            if let value = self.msgDelegates[delegateKey] {
                value.objectEnumerator().forEach { element in
                    if let delegate = element as? AUiRtmMsgProxyDelegate {
                        delegate.onMsgDidChanged(channelName: event.target, key: item.key, value: itemValue)
                    }
                }
            }
        }
        self.msgCacheAttr[cacheKey] = cache
        if event.data.getItems().count > 0 {
            return
        }
        
        errorDelegates.objectEnumerator().forEach { element in
            (element as? AUiRtmErrorProxyDelegate)?.onMsgRecvEmpty?(channelName: event.target)
        }
        aui_info("storage event[\(event.target)] ========", tag: "AUiRtmMsgProxy")
    }
    
    public func rtmKit(_ rtmKit: AgoraRtmClientKit, on event: AgoraRtmPresenceEvent) {
        origRtmDelegate?.rtmKit?(rtmKit, on: event)
        
        aui_info("[\(event.channelName)] presence event type: [\(event.type.rawValue)] channel type: [\(event.channelType.rawValue)]] states: \(event.states.count) =======", tag: "AUiRtmMsgProxy")
//        var map: [[]]
        var map: [String: String] = [:]
        event.states.forEach { item in
            map[item.key] = item.value
        }
        let userId = event.publisher ?? ""
        aui_info("presence userId: \(userId) event_type: \(event.type.rawValue) userInfo: \(map)", tag: "AUiRtmMsgProxy")
        if event.type == .remoteJoinChannel {
            if map.count == 0 {
                aui_warn("join user fail, empty: userId: \(userId) \(map)", tag: "AUiRtmMsgProxy")
                return
            }
            userDelegates.objectEnumerator().forEach{ element in
                (element as? AUiRtmUserProxyDelegate)?.onUserDidJoined(channelName: event.channelName, userId: userId, userInfo: map)
            }
        } else if event.type == .remoteLeaveChannel || event.type == .remoteConnectionTimeout {
            userDelegates.objectEnumerator().forEach { element in
                (element as? AUiRtmUserProxyDelegate)?.onUserDidLeaved(channelName: event.channelName, userId: userId, userInfo: map)
            }
        } else if event.type == .remoteStateChanged {
            if map.count == 0 {
                aui_warn("update user fail, empty: userId: \(userId) \(map)", tag: "AUiRtmMsgProxy")
                return
            }
            userDelegates.objectEnumerator().forEach { element in
                (element as? AUiRtmUserProxyDelegate)?.onUserDidUpdated(channelName: event.channelName, userId: userId, userInfo: map)
            }
        } else if event.type == .snapshot {
            let userList = event.snapshotList()
            userDelegates.objectEnumerator().forEach { element in
                (element as? AUiRtmUserProxyDelegate)?.onUserSnapshotRecv(channelName: event.channelName, userId: userId, userList: userList)
            }
        }
    }
}

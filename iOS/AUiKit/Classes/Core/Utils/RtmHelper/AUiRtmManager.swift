//
//  AUiRtmManager.swift
//  AUiKit
//
//  Created by wushengtao on 2023/3/1.
//

import Foundation
import AgoraRtcKit

let kChannelType = AgoraRtmChannelType.stream


/// 对RTM相关操作的封装类
open class AUiRtmManager: NSObject {
    private var streamChannel: AgoraRtmStreamChannel?
    private let proxy: AUiRtmMsgProxy = AUiRtmMsgProxy()
    
    private var rtmClient: AgoraRtmClientKit!
    private var rtmStreamChannelMap: [String: AgoraRtmStreamChannel] = [:]
    
    public private(set) var isLogin: Bool = false
    
    deinit {
        aui_info("deinit AUiRtmManager", tag: "AUiRtmManager")
    }
    
    init(rtmClient: AgoraRtmClientKit?) {
        self.rtmClient = rtmClient
        super.init()
        //TODO: 如果后续外部再修改rtmClient的delegate会有问题
        proxy.origRtmDelegate = self.rtmClient.agoraRtmDelegate
        self.rtmClient.agoraRtmDelegate = proxy
        aui_info("init AUiRtmManager", tag: "AUiRtmManager")
    }
    
    public func login(token: String, completion: @escaping (Error?)->()) {
        if isLogin {
            completion(nil)
            return
        }
        let ret =
        self.rtmClient.login(byToken: token) {[weak self] resp, error in
            aui_info("login: \(resp) \(error.errorCode.rawValue)", tag: "AUiRtmManager")
            self?.isLogin = error.errorCode == .ok ? true : false
            completion(error.toNSError())
        }
        
        //已经登陆过
        if abs(ret) == AgoraRtmErrorAlreadyLogin.rawValue {
            isLogin = true
            completion(nil)
        }
        
        aui_info("login ret: \(ret)", tag: "AUiRtmManager")
    }
    
    public func logout() {
        aui_info("logout", tag: "AUiRtmManager")
        rtmClient.logout()
        isLogin = false
    }
    
    public func renew(token: String) {
        aui_info("renew: \(token)", tag: "AUiRtmManager")
        rtmClient.renewToken(token)
    }
    
    public func renewChannel(channelName: String, token: String) {
        guard let streamChannel = rtmStreamChannelMap[channelName] else {
            return
        }
        
        aui_info("renewChannel: \(channelName) token: \(token)", tag: "AUiRtmManager")
        streamChannel.renewToken(token)
    }
}

//MARK: user
extension AUiRtmManager {
    public func getUserCount(channelName: String, completion:@escaping (Error?, Int)->()) {
        guard let presence = rtmClient.getPresence() else {
            completion(AUiCommonError.rtmError(-1).toNSError(), 0)
            return
        }
        
        let options = AgoraRtmPresenceOptions()
        options.includeUserId = false
        options.includeState = false
        let ret =
        presence.whoNow(channelName, channelType: kChannelType, options: options, completion: { resp, error in
//            aui_info("presence whoNow '\(channelName)' finished: \(error.errorCode.rawValue) list count: \(resp.userStateList.count) userId: \(AUiRoomContext.shared.commonConfig?.userId ?? "")", tag: "AUiRtmManager")
            aui_info("getUserCount: \(resp.totalOccupancy)", tag: "AUiRtmManager")
            let userList = resp.userList()
            completion(error.toNSError(), userList.count)
        })
        aui_info("presence whoNow '\(channelName)' ret: \(ret)", tag: "AUiRtmManager")
        if ret != 0 {
            completion(AUiCommonError.rtmError(ret).toNSError(), 0)
        }
    }
    
    func whoNow(channelName: String, completion:@escaping (Error?, [[String: String]]?)->()) {
        guard let presence = rtmClient.getPresence() else {
            completion(AUiCommonError.rtmError(-1).toNSError(), nil)
            return
        }
        
        let options = AgoraRtmPresenceOptions()
        options.includeUserId = true
        options.includeState = true
        let ret =
        presence.whoNow(channelName, channelType: kChannelType, options: options, completion: { resp, error in
            aui_info("presence whoNow '\(channelName)' finished: \(error.errorCode.rawValue) list count: \(resp.userStateList.count) userId: \(AUiRoomContext.shared.commonConfig?.userId ?? "")", tag: "AUiRtmManager")
            
            let userList = resp.userList()
            completion(error.toNSError(), userList)
        })
        aui_info("presence whoNow '\(channelName)' ret: \(ret)", tag: "AUiRtmManager")
        if ret != 0 {
            completion(AUiCommonError.rtmError(ret).toNSError(), nil)
        }
    }
    
    public func setPresenceState(channelName: String, attr:[String: Any], completion: @escaping (Error?)->()) {
        guard let presence = rtmClient.getPresence() else {
            completion(AUiCommonError.rtmError(-1).toNSError())
            return
        }
        
        var items: [AgoraRtmStateItem] = []
        attr.forEach { (key: String, value: Any) in
            let item = AgoraRtmStateItem()
            item.key = key
            if let val = value as? String {
                item.value = val
            } else if let val = value as? UInt {
                item.value = "\(val)"
            } else if let val = value as? Double {
                item.value = "\(val)"
            } else {
                aui_error("setPresenceState missmatch item: \(key): \(value)", tag: "AUiRtmManager")
                return
            }
            
            items.append(item)
        }
        let ret =
        presence.setState(channelName, channelType: kChannelType, items: items, completion: { resp, error in
            aui_info("presence setState '\(channelName)' finished: \(error.errorCode.rawValue)", tag: "AUiRtmManager")
            completion(error.toNSError())
        })
        aui_info("presence setState'\(channelName)' ret: \(ret)", tag: "AUiRtmManager")
        if ret != 0 {
            completion(AUiCommonError.rtmError(ret).toNSError())
        }
    }
}

//MARK: Channel Metadata
extension AUiRtmManager {
    public func subscribeMsg(channelName: String, itemKey: String, delegate: AUiRtmMsgProxyDelegate) {
        proxy.subscribeMsg(channelName: channelName, itemKey: itemKey, delegate: delegate)
    }
    
    public func unsubscribeMsg(channelName: String, itemKey: String, delegate: AUiRtmMsgProxyDelegate) {
        proxy.unsubscribeMsg(channelName: channelName, itemKey: itemKey, delegate: delegate)
    }
    
    public func subscribeUser(channelName: String, delegate: AUiRtmUserProxyDelegate) {
        proxy.subscribeUser(channelName: channelName, delegate: delegate)
    }
    
    public func unsubscribeUser(channelName: String, delegate: AUiRtmUserProxyDelegate) {
        proxy.unsubscribeUser(channelName: channelName, delegate: delegate)
    }
    
    public func subscribeError(channelName: String, delegate: AUiRtmErrorProxyDelegate) {
        proxy.subscribeError(channelName: channelName, delegate: delegate)
    }
    
    public func unsubscribeError(channelName: String, delegate: AUiRtmErrorProxyDelegate) {
        proxy.unsubscribeError(channelName: channelName, delegate: delegate)
    }
    
    func subscribe(channelName: String, rtcToken: String, completion:@escaping (Error?)->()) {
        if kChannelType == .message {
            let options = AgoraRtmSubscribeOptions()
            options.withMetadata = true
            options.withPresence = true
            let ret =
            rtmClient.subscribe(withChannel: channelName, option: options) { resp, error in
                aui_info("subscribe '\(channelName)' finished: \(error.errorCode.rawValue)", tag: "AUiRtmManager")
                completion(error.toNSError())
            }
            aui_info("subscribe '\(channelName)' ret: \(ret)", tag: "AUiRtmManager")
            if ret != 0 {
                completion(AUiCommonError.rtmError(ret).toNSError())
            }
        } else if kChannelType == .stream {
            let option = AgoraRtmJoinChannelOption()
            option.token = rtcToken
            option.withMetadata = true
            option.withPresence = true
            if rtmStreamChannelMap[channelName] == nil {
                let streamChannel = rtmClient.createStreamChannel(channelName)
                rtmStreamChannelMap[channelName] = streamChannel
                
            }
            guard let streamChannel = rtmStreamChannelMap[channelName] else {
                assert(false, "streamChannel not found")
                return
            }
            
            let ret = streamChannel.join(with: option) { resp, error in
                aui_info("join '\(channelName)' finished: \(error.errorCode.rawValue)", tag: "AUiRtmManager")
                completion(error.toNSError())
            }
            aui_info("join '\(channelName)' rtcToken: \(rtcToken) ret: \(ret)", tag: "AUiRtmManager")
            if ret != 0 {
                completion(AUiCommonError.rtmError(ret).toNSError())
            }
        } else {
            assert(false, "unknown channel type")
        }
    }
    
    func unSubscribe(channelName: String) {
        proxy.cleanCache(channelName: channelName)
        if kChannelType == .message {
            let ret =
            rtmClient.unsubscribe(withChannel: channelName)
            aui_info("unSubscribe '\(channelName)' ret: \(ret)", tag: "AUiRtmManager")
        } else if kChannelType == .stream {
            guard let streamChannel = rtmStreamChannelMap[channelName] else {
                return
            }
            
            streamChannel.leave()
            rtmStreamChannelMap[channelName] = nil
        }
    }
    
//    func cleanMetadata(channelName: String, completion: @escaping (Error?)->()) {
//        guard let data = rtmClient.getStorage()?.createMetadata(), let storage = rtmClient.getStorage() else {
//            assert(false, "cleanMetadata fail")
//            return
//        }
//        let options = AgoraRtmMetadataOptions()
//        options.recordTs = true
//        options.recordUserId = true
//        let ret =
//        storage.removeChannelMetadata(channelName, channelType: kChannelType, data: data, options: options, lock: "", completion: { resp, error in
//            aui_info("cleanMetadata finished: \(error.errorCode.rawValue)", tag: "AUiRtmManager")
//            completion(nil)
//        })
//        aui_info("cleanMetadata '\(channelName)' ret: \(ret)", tag: "AUiRtmManager")
//
//    }
//
//    func setMetadata(channelName: String, metadata: [String: String], completion: @escaping (Error?)->()) {
//        guard let storage = rtmClient.getStorage(),
//              let data = storage.createMetadata() else {
//            assert(false, "setMetadata fail")
//            return
//        }
//        metadata.forEach { (key: String, value: String) in
//            let item = AgoraRtmMetadataItem()
//            item.key = key
//            item.value = value
//            data.setMetadataItem(item)
//        }
//
//        let options = AgoraRtmMetadataOptions()
//        let ret =
//        storage.setChannelMetadata(channelName, channelType: kChannelType, data: data, options: options, lock: "", completion: { resp, error in
//            aui_info("setMetadata finished: \(error.errorCode.rawValue)", tag: "AUiRtmManager")
//            completion(nil)
//        })
//        aui_info("setMetadata ret: \(ret) \(metadata)", tag: "AUiRtmManager")
//    }
//
//    func updateMetadata(channelName: String, metadata: [String: String], completion: @escaping (Error?)->()) {
//        guard let storage = rtmClient.getStorage(),
//                  let data = storage.createMetadata() else {
//            assert(false, "updateMetadata fail")
//            return
//        }
//        metadata.forEach { (key: String, value: String) in
//            let item = AgoraRtmMetadataItem()
//            item.key = key
//            item.value = value
//            data.setMetadataItem(item)
//        }
//
//        let options = AgoraRtmMetadataOptions()
//        let ret =
//        storage.updateChannelMetadata(channelName, channelType: kChannelType, data: data, options: options, lock: "", completion: { resp, error in
//            aui_info("updateMetadata finished: \(error.errorCode.rawValue)", tag: "AUiRtmManager")
//            completion(nil)
//        })
//        aui_info("updateMetadata ret: \(ret)", tag: "AUiRtmManager")
//    }
    
    func getMetadata(channelName: String, completion: @escaping (Error?, [String: String]?)->()) {
        guard let storage = rtmClient.getStorage() else {
            assert(false, "getMetadata fail")
            return
        }
        let ret =
        storage.getChannelMetadata(channelName, channelType: kChannelType, completion: { resp, error in
            aui_info("getMetadata finished: \(error.errorCode.rawValue) item count: \(resp.data?.getItems().count ?? 0)", tag: "AUiRtmManager")
            var map: [String: String] = [:]
            resp.data?.getItems().forEach({ item in
                map[item.key] = item.value
            })
            completion(nil, map)
        })
        aui_info("getMetadata ret: \(ret)", tag: "AUiRtmManager")
    }
}

//MARK: user metadata
extension AUiRtmManager {
    func subscribeUser(userId: String) {
        guard let storage = rtmClient.getStorage() else {
            assert(false, "subscribeUserMetadata fail")
            return
        }
        let ret =
        storage.subscribeUserMetadata(userId, completion: { resp, error in
            aui_info("subscribeUser finished: \(resp) \(error.errorCode.rawValue)", tag: "AUiRtmManager")
        })
        aui_info("subscribeUserMetadata ret: \(ret)", tag: "AUiRtmManager")
    }
    
    func unSubscribeUser(userId: String) {
        guard let storage = rtmClient.getStorage() else {
            aui_error("subscribeUserMetadata fail", tag: "AUiRtmManager")
            assert(false, "subscribeUserMetadata fail")
            return
        }
        let ret =
        storage.unsubscribeUserMetadata(userId)
        aui_info("subscribeUserMetadata ret: \(ret)", tag: "AUiRtmManager")
    }
    
    func removeUserMetadata(userId: String) {
        guard let storage = rtmClient.getStorage(),
                  let data = storage.createMetadata() else {
            aui_info("removeUserMetadata fail", tag: "AUiRtmManager")
            assert(false, "removeUserMetadata fail")
            return
        }
        let options = AgoraRtmMetadataOptions()
        options.recordTs = true
        options.recordUserId = true
        
        let ret =
        storage.removeUserMetadata(userId, data: data, options: options, completion: { resp, error in
            aui_info("removeUserMetadata finished: \(error.errorCode.rawValue)", tag: "AUiRtmManager")
        })
        aui_info("removeUserMetadata ret: \(ret)", tag: "AUiRtmManager")
    }
    
    func setUserMetadata(userId: String, metadata: [String: String]) {
        guard let storage = rtmClient.getStorage(),
                let data = storage.createMetadata() else {
            assert(false, "setUserMetadata fail")
            return
        }
        let options = AgoraRtmMetadataOptions()
        options.recordTs = true
        options.recordUserId = true
        
        metadata.forEach { (key: String, value: String) in
            let item = AgoraRtmMetadataItem()
            item.key = key
            item.value = value
            data.setMetadataItem(item)
        }
        
        let ret =
        storage.setUserMetadata(userId, data: data, options: options, completion: { resp, error in
            aui_info("setUserMetadata finished: \(error.errorCode.rawValue)", tag: "AUiRtmManager")
        })
        aui_info("setUserMetadata ret: \(ret)", tag: "AUiRtmManager")
    }
    
    func updateUserMetadata(userId: String, metadata: [String: String]) {
        guard let storage = rtmClient.getStorage(),
                let data = storage.createMetadata() else {
            aui_error("updateUserlMetadata fail", tag: "AUiRtmManager")
            assert(false, "updateUserlMetadata fail")
            return
        }
        let options = AgoraRtmMetadataOptions()
        options.recordTs = true
        options.recordUserId = true
        
        metadata.forEach { (key: String, value: String) in
            let item = AgoraRtmMetadataItem()
            item.key = key
            item.value = value
            data.setMetadataItem(item)
        }
        
        let ret =
        storage.updateUserMetadata(userId, data: data, options: options, completion: { resp, error in
            aui_info("updateUserlMetadata finished: \(error.errorCode.rawValue)", tag: "AUiRtmManager")
        })
        aui_info("updateUserlMetadata ret: \(ret)", tag: "AUiRtmManager")
    }
    
    func getUserMetadata(userId: String) {
        guard let storage = rtmClient.getStorage() else {
            aui_error("getUserMetadata fail", tag: "AUiRtmManager")
            return
        }
        
        let ret =
        storage.getUserMetadata(userId) { resp, error in
            aui_info("getUserMetadata: \(resp) \(error.errorCode.rawValue)", tag: "AUiRtmManager")
        }
        aui_info("getUserMetadata ret: \(ret)", tag: "AUiRtmManager")
    }
}

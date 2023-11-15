//
//  AUIChorusLocalServiceImpl.swift
//  AUIKitCore
//
//  Created by wushengtao on 2023/11/3.
//

import Foundation
import AgoraRtcKit
import YYModel


@objc open class AUIChorusLocalServiceImpl: NSObject {
    private var respDelegates: NSHashTable<AUIChorusRespDelegate> = NSHashTable<AUIChorusRespDelegate>.weakObjects()
    private var ktvApi: KTVApiDelegate!
    private var rtcKit: AgoraRtcEngineKit!
    private var channelName: String!
    private var chorusUserList: [AUIChoristerModel] = []
    private var rtmManager: AUIRtmManager!
    
    private var callbackMap: [String: ((NSError?)-> ())] = [:]
    
    deinit {
        aui_info("deinit AUIChorusServiceImpl", tag: "AUIChorusServiceImpl")
        rtmManager.unsubscribeAttributes(channelName: getChannelName(), itemKey: kChorusKey, delegate: self)
        rtmManager.unsubscribeMessage(channelName: getChannelName(), delegate: self)
    }
    
    @objc public init(channelName: String, rtcKit: AgoraRtcEngineKit, ktvApi: KTVApiDelegate, rtmManager: AUIRtmManager) {
        aui_info("init AUIChorusServiceImpl", tag: "AUIChorusServiceImpl")
        super.init()
        self.rtmManager = rtmManager
        self.channelName = channelName
        self.rtcKit = rtcKit
        self.ktvApi = ktvApi
        rtmManager.subscribeAttributes(channelName: getChannelName(), itemKey: kChorusKey, delegate: self)
        rtmManager.subscribeMessage(channelName: getChannelName(), delegate: self)
    }
}

extension AUIChorusLocalServiceImpl: AUIChorusServiceDelegate {
    
    public func getRoomContext() -> AUIRoomContext {
        return AUIRoomContext.shared
    }
    
    public func bindRespDelegate(delegate: AUIChorusRespDelegate) {
        respDelegates.add(delegate)
    }
    
    public func unbindRespDelegate(delegate: AUIChorusRespDelegate) {
        respDelegates.remove(delegate)
    }
    
    public func getChoristersList(completion: (Error?, [AUIChoristerModel]?) -> ()) {
//        rtmManager.rtmClient.
    }
    
    public func joinChorus(songCode: String, userId: String?, completion: @escaping AUICallback) {
        let joinUserId = userId ?? getRoomContext().currentUserInfo.userId
        if getRoomContext().getArbiter(channelName: channelName)?.isArbiter() ?? false {
            rtmJoinChorus(songCode: songCode, userId: joinUserId, completion: completion)
            return
        }
        
        let model = AUIPlayerJoinNetworkModel()
        model.songCode = songCode
        model.userId = joinUserId
        model.roomId = channelName
        
        let message = model.rtmMessage()
        rtmManager.publish(channelName: channelName, message: message) { err in
        }
        callbackMap[model.uniqueId] = completion
    }
    
    public func leaveChorus(songCode: String, userId: String?, completion: @escaping AUICallback) {
        let leaveUserId = userId ?? getRoomContext().currentUserInfo.userId
        if getRoomContext().getArbiter(channelName: channelName)?.isArbiter() ?? false {
            rtmLeaveChorus(songCode: songCode, userId: leaveUserId, completion: completion)
            return
        }
        
        let model = AUIPlayerLeaveNetworkModel()
        model.songCode = songCode
        model.userId = leaveUserId
        model.roomId = channelName
        
        let message = model.rtmMessage()
        rtmManager.publish(channelName: channelName, message: message) { err in
        }
        callbackMap[model.uniqueId] = completion
    }
    
    public func getChannelName() -> String {
        return channelName
    }

}

//MARK: AUIRtmMsgProxyDelegate
extension AUIChorusLocalServiceImpl: AUIRtmAttributesProxyDelegate {
    public func onAttributesDidChanged(channelName: String, key: String, value: Any) {
        if key == kChorusKey {
            aui_info("recv chorus attr did changed \(value)", tag: "AUIPlayerServiceImpl")
            guard let songArray = (value as AnyObject).yy_modelToJSONObject(),
                    let chorusList = NSArray.yy_modelArray(with: AUIChoristerModel.self, json: songArray) as? [AUIChoristerModel] else {
                return
            }
            
            var unChangesOldList = self.chorusUserList
            //TODO: optimize
            let difference = chorusList.difference(from: self.chorusUserList)
            for change in difference {
                switch change {
                case let .remove(offset, oldElement, _):
                    unChangesOldList.remove(at: offset)
                    self.respDelegates.allObjects.forEach { obj in
                        obj.onChoristerDidLeave(chorister: oldElement)
                    }
                case let .insert(_, newElement, _):
                    self.respDelegates.allObjects.forEach { obj in
                        obj.onChoristerDidEnter(chorister: newElement)
                    }
                }
            }
            
            self.chorusUserList = chorusList
        }
    }
}

//MARK: AUIRtmMessageProxyDelegate
extension AUIChorusLocalServiceImpl: AUIRtmMessageProxyDelegate {
    //TODO: using thread queue processing to reduce main thread stuttering
    public func onMessageReceive(channelName: String, message: String) {
        guard channelName == getChannelName() else {return}
        
        guard let data = message.data(using: .utf8),
              let map = try? JSONSerialization.jsonObject(with: data) as? [String: Any] else {
            return
        }
        let uniqueId = map["uniqueId"] as? String ?? ""
        guard let interfaceName = map["interfaceName"] as? String else {
            if let callback = callbackMap[uniqueId] {
                callbackMap[uniqueId] = nil
                let code = map["code"] as? Int ?? 0
                let reason = map["reason"] as? String ?? "success"
                callback(code == 0 ? nil : NSError(domain: "AUIKit Error", code: Int(code), userInfo: [ NSLocalizedDescriptionKey : reason]))
            }
            return
        }
        guard getRoomContext().getArbiter(channelName: channelName)?.isArbiter() ?? false else { return }
        aui_info("onMessageReceive[\(interfaceName)]", tag: "AUIMicSeatServiceImpl")
        if interfaceName == kAUIPlayerJoinInterface, let model = AUIPlayerJoinNetworkModel.model(rtmMessage: message) {
            rtmJoinChorus(songCode: model.songCode ?? "", userId: model.userId ?? "") {[weak self] err in
                self?.rtmManager.sendReceipt(channelName: channelName, uniqueId: uniqueId, error: err)
            }
        } else if interfaceName == kAUIPlayerLeaveInterface, let model = AUIPlayerLeaveNetworkModel.model(rtmMessage: message) {
            rtmLeaveChorus(songCode: model.songCode ?? "", userId: model.userId ?? "") {[weak self] err in
                self?.rtmManager.sendReceipt(channelName: channelName, uniqueId: uniqueId, error: err)
            }
        }
    }
}

//MARK: set meta data
extension AUIChorusLocalServiceImpl {
    private func rtmJoinChorus(songCode: String, userId: String, completion: @escaping AUICallback) {
        if let _ = chorusUserList.firstIndex(where: { $0.userId == userId }) {
            completion(AUICommonError.choristerAlreadyExist.toNSError())
            return
        }
        
        var err: NSError? = nil
        let metaData = NSMutableDictionary()
        for obj in respDelegates.allObjects {
            err = obj.onWillJoinChours?(songCode: songCode, userId: userId, metaData: metaData)
            if let err = err {
                completion(err)
                return
            }
        }
        
        let metaDataList = NSMutableArray(array: chorusUserList)
        let model = AUIChoristerModel()
        model.chorusSongNo = songCode
        model.userId = userId
        //TODO: get owner from song service
//        let user = AUIUserThumbnailInfo()
//        model.owner = user
        metaDataList.add(model)
        let str = metaDataList.yy_modelToJSONString() ?? ""
        metaData[kChorusKey] = str
        self.rtmManager.setBatchMetadata(channelName: channelName,
                                         lockName: kRTM_Referee_LockName,
                                         metadata: metaData as! [String : String]) { error in
            completion(error)
        }
    }
    
    private func rtmLeaveChorus(songCode: String, userId: String, completion: @escaping AUICallback)  {
        let userList = chorusUserList.filter({ $0.userId != userId })
        if userList.count == chorusUserList.count {
            completion(AUICommonError.choristerNotExist.toNSError())
            return
        }
        
        let metaDataList = NSMutableArray(array: userList)
        let str = metaDataList.yy_modelToJSONString() ?? ""
        let metaData = [kChorusKey: str]
        self.rtmManager.setBatchMetadata(channelName: channelName,
                                         lockName: kRTM_Referee_LockName,
                                         metadata: metaData) { error in
            completion(error)
        }
    }
    
    public func onUserInfoClean(userId: String, completion: @escaping ((NSError?) -> ())){
        var metaData = [String: String]()
        let filterList = chorusUserList.filter({ $0.userId != userId })
        if filterList.count != chorusUserList.count {
            let metaDataList = NSMutableArray(array: filterList)
            let str = metaDataList.yy_modelToJSONString() ?? ""
            metaData[kChorusKey] = str
        }
        self.rtmManager.setBatchMetadata(channelName: channelName, 
                                         lockName: kRTM_Referee_LockName, 
                                         metadata: metaData,
                                         completion: completion)
    }
    
//    public func onSongDidRemove(channelName: String, songCode: String, metaData: NSMutableDictionary) -> NSError? {
//        guard songCode == self.chorusUserList.first?.chorusSongNo else { return nil }
//        metaData[kChorusKey] = NSArray().yy_modelToJSONString() ?? ""
//        return nil
//    }
    
    public func onRoomWillDestroy(removeKeys: NSMutableArray) -> NSError? {
        removeKeys.add(kChorusKey)
        return nil
    }
}

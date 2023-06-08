//
//  AUIChorusServiceImpl.swift
//  AUIKit
//
//  Created by wushengtao on 2023/4/7.
//

import Foundation
import AgoraRtcKit
import YYModel

private let kChorusKey = "chorus"

@objc open class AUIChorusServiceImpl: NSObject {
    private var respDelegates: NSHashTable<AnyObject> = NSHashTable<AnyObject>.weakObjects()
    private var ktvApi: KTVApiDelegate!
    private var rtcKit: AgoraRtcEngineKit!
    private var channelName: String!
    private var chorusUserList: [AUIChoristerModel] = []
    private var rtmManager: AUIRtmManager!
    
    deinit {
        aui_info("deinit AUIChorusServiceImpl", tag: "AUIChorusServiceImpl")
        rtmManager.unsubscribeAttributes(channelName: getChannelName(), itemKey: kChorusKey, delegate: self)
    }
    
    @objc public init(channelName: String, rtcKit: AgoraRtcEngineKit, ktvApi: KTVApiDelegate, rtmManager: AUIRtmManager) {
        aui_info("init AUIChorusServiceImpl", tag: "AUIChorusServiceImpl")
        super.init()
        self.rtmManager = rtmManager
        self.channelName = channelName
        self.rtcKit = rtcKit
        self.ktvApi = ktvApi
        rtmManager.subscribeAttributes(channelName: getChannelName(), itemKey: kChorusKey, delegate: self)
    }
}

extension AUIChorusServiceImpl: AUIChorusServiceDelegate {
    
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
        let model = AUIPlayerJoinNetworkModel()
        model.songCode = songCode
        model.userId = userId ?? getRoomContext().currentUserInfo.userId
        model.roomId = channelName
        model.request { err, _ in
            completion(err as? NSError)
        }
    }
    
    public func leaveChorus(songCode: String, userId: String?, completion: @escaping AUICallback) {
        let model = AUIPlayerLeaveNetworkModel()
        model.songCode = songCode
        model.userId = userId ?? getRoomContext().currentUserInfo.userId
        model.roomId = channelName
        model.request { err, _ in
            completion(err as? NSError)
        }
    }
    
    public func getChannelName() -> String {
        return channelName
    }

}

//MARK: AUIRtmMsgProxyDelegate
extension AUIChorusServiceImpl: AUIRtmAttributesProxyDelegate {
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
                        guard let delegate = obj as? AUIChorusRespDelegate else {return}
                        delegate.onChoristerDidLeave(chorister: oldElement)
                    }
                case let .insert(_, newElement, _):
                    self.respDelegates.allObjects.forEach { obj in
                        guard let delegate = obj as? AUIChorusRespDelegate else {return}
                        delegate.onChoristerDidEnter(chorister: newElement)
                    }
                }
            }
            
            self.chorusUserList = chorusList
        }
    }
    
    
}

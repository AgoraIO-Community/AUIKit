//
//  AUiChorusServiceImpl.swift
//  AUiKit
//
//  Created by wushengtao on 2023/4/7.
//

import Foundation
import AgoraRtcKit
import YYModel

private let kChorusKey = "chorus"

open class AUiChorusServiceImpl: NSObject {
    private var respDelegates: NSHashTable<AnyObject> = NSHashTable<AnyObject>.weakObjects()
    private var ktvApi: KTVApiDelegate!
    private var rtcKit: AgoraRtcEngineKit!
    private var channelName: String!
    private var chorusUserList: [AUiChoristerModel] = []
    private var rtmManager: AUiRtmManager!
    
    deinit {
        aui_info("deinit AUiChorusServiceImpl", tag: "AUiChorusServiceImpl")
        rtmManager.unsubscribeMsg(channelName: getChannelName(), itemKey: kChorusKey, delegate: self)
    }
    
    public init(channelName: String, rtcKit: AgoraRtcEngineKit, ktvApi: KTVApiDelegate, rtmManager: AUiRtmManager) {
        aui_info("init AUiChorusServiceImpl", tag: "AUiChorusServiceImpl")
        super.init()
        self.rtmManager = rtmManager
        self.channelName = channelName
        self.rtcKit = rtcKit
        self.ktvApi = ktvApi
        rtmManager.subscribeMsg(channelName: getChannelName(), itemKey: kChorusKey, delegate: self)
    }
}

extension AUiChorusServiceImpl: AUiChorusServiceDelegate {
    public func bindRespDelegate(delegate: AUiChorusRespDelegate) {
        respDelegates.add(delegate)
    }
    
    public func unbindRespDelegate(delegate: AUiChorusRespDelegate) {
        respDelegates.remove(delegate)
    }
    
    public func getChoristersList(completion: (Error?, [AUiChoristerModel]?) -> ()) {
//        rtmManager.rtmClient.
    }
    
    public func joinChorus(songCode: String, userId: String?, completion: @escaping AUiCallback) {
        let model = AUiPlayerJoinNetworkModel()
        model.songCode = songCode
        model.userId = userId ?? getRoomContext().currentUserInfo.userId
        model.roomId = channelName
        model.request { err, _ in
            completion(err)
        }
    }
    
    public func leaveChorus(songCode: String, userId: String?, completion: @escaping AUiCallback) {
        let model = AUiPlayerLeaveNetworkModel()
        model.songCode = songCode
        model.userId = userId ?? getRoomContext().currentUserInfo.userId
        model.roomId = channelName
        model.request { err, _ in
            completion(err)
        }
    }
    
    public func getChannelName() -> String {
        return channelName
    }

}

//MARK: AUiRtmMsgProxyDelegate
extension AUiChorusServiceImpl: AUiRtmMsgProxyDelegate {
    public func onMsgDidChanged(channelName: String, key: String, value: Any) {
        if key == kChorusKey {
            aui_info("recv chorus attr did changed \(value)", tag: "AUiPlayerServiceImpl")
            guard let songArray = (value as AnyObject).yy_modelToJSONObject(),
                    let chorusList = NSArray.yy_modelArray(with: AUiChoristerModel.self, json: songArray) as? [AUiChoristerModel] else {
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
                        guard let delegate = obj as? AUiChorusRespDelegate else {return}
                        delegate.onChoristerDidLeave(chorister: oldElement)
                    }
                case let .insert(_, newElement, _):
                    self.respDelegates.allObjects.forEach { obj in
                        guard let delegate = obj as? AUiChorusRespDelegate else {return}
                        delegate.onChoristerDidEnter(chorister: newElement)
                    }
                }
            }
            
            self.chorusUserList = chorusList
        }
    }
    
    
}

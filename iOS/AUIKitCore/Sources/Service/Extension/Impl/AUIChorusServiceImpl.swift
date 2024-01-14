//
//  AUIChorusServiceImpl.swift
//  AUIKit
//
//  Created by wushengtao on 2023/4/7.
//

import Foundation
import AgoraRtcKit
import YYModel

let kChorusKey = "chorus"

enum AUIChorusCMd: String {
    case joinCmd = "joinChorusCmd"
    case leaveCmd = "leaveChorusCmd"
}

@objc open class AUIChorusServiceImpl: NSObject {
    private var respDelegates: NSHashTable<AUIChorusRespDelegate> = NSHashTable<AUIChorusRespDelegate>.weakObjects()
    private var ktvApi: KTVApiDelegate!
    private var rtcKit: AgoraRtcEngineKit!
    private var channelName: String!
    private var chorusUserList: [AUIChoristerModel] = []
    private var rtmManager: AUIRtmManager!
    
    private var listCollection: AUIListCollection!
        
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
        
        self.listCollection = AUIListCollection(channelName: channelName, observeKey: kChorusKey, rtmManager: rtmManager)
        listCollection.subscribeWillAdd {[weak self] publisherId, dataCmd, newItem in
            return self?.metadataWillAdd(publiserId: publisherId,
                                         dataCmd: dataCmd,
                                         newItem: newItem)
        }
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
    
    public func getChoristersList(completion:@escaping (Error?, [AUIChoristerModel]?) -> ()) {
        aui_info("getChoristersList with", tag: "AUIChorusServiceImpl")
        listCollection.getMetaData {[weak self] error, obj in
            guard let self = self else {return}
            aui_info("getAllChooseSongList error: \(error?.localizedDescription ?? "success")", tag: "AUIMusicServiceImpl")
            if let error = error {
                //TODO: error
                completion(error, nil)
                return
            }
            guard let obj = obj as? [[String: Any]] else {
                completion(NSError.auiError("getChoristersList fail! not a array"), nil)
                return
            }
            self.chorusUserList = NSArray.yy_modelArray(with: AUIChoristerModel.self, json: obj) as? [AUIChoristerModel] ?? []
            completion(nil, self.chorusUserList)
        }
    }
    
    public func joinChorus(songCode: String, userId: String?, completion: @escaping AUICallback) {
        aui_info("joinChorus: \(songCode)", tag: "AUIChorusServiceImpl")
        let model = AUIPlayerJoinNetworkModel()
        model.songCode = songCode
        model.userId = userId
        
        guard let value = model.yy_modelToJSONObject() as? [String: Any] else {
            completion(AUICommonError.chooseSongIsFail.toNSError())
            return
        }

        listCollection.addMetaData(valueCmd: AUIChorusCMd.joinCmd.rawValue,
                                   value: value,
                                   filter: [["userId": userId ?? ""]],
                                   callback: completion)
    }
    
    public func leaveChorus(songCode: String, userId: String?, completion: @escaping AUICallback) {
        aui_info("leaveChorus: \(songCode)", tag: "AUIChorusServiceImpl")

        listCollection.removeMetaData(valueCmd: AUIMusicCmd.removeSongCmd.rawValue,
                                      filter: [["userId": userId ?? ""]],
                                      callback: completion)
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

//MARK: set meta data
extension AUIChorusServiceImpl {
    
    private func metadataWillAdd(publiserId: String,
                                 dataCmd: String?,
                                 newItem: [String: Any]) -> NSError? {
        guard let dataCmd = AUIChorusCMd(rawValue: dataCmd ?? "") else {
            return AUICommonError.unknown.toNSError()
        }
        
        let owner = newItem["owner"] as? [String: Any]
        let userId = owner?["userId"] as? String ?? ""
        switch dataCmd {
        case .joinCmd:
//            if self.chooseSongList.contains(where: { $0.songCode == songCode }) {
//                return AUICommonError.chooseSongAlreadyExist.toNSError()
//            }
            //过滤条件在filter里包含
            return nil
        default:
            break
        }
        
        return NSError.auiError("add music cmd incorrect")
    }
    
    public func cleanUserInfo(userId: String, completion: @escaping ((NSError?) -> ())){
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
    
    public func deinitService(completion:  @escaping  ((NSError?) -> ())) {
//        rtmManager.cleanBatchMetadata(channelName: channelName,
//                                      lockName: kRTM_Referee_LockName,
//                                      removeKeys: [kChorusKey], 
//                                      completion: completion)
        listCollection.cleanMetaData(callback: completion)
    }
}

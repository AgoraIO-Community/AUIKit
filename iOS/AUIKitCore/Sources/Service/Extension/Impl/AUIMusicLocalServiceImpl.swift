//
//  AUIMusicLocalServiceImpl.swift
//  AUIKitCore
//
//  Created by wushengtao on 2023/11/2.
//


import Foundation
import AgoraRtcKit
import YYModel

private let kChooseSongKey = "song"

//class AUIMusicLoadingInfo: NSObject {
//    var songCode: String?
//    var lrcMsgId: String?
//    var preloadStatus: AgoraMusicContentCenterPreloadStatus?
//    var lrcUrl: String?
//    var callback: AUILoadSongCompletion?
//    
//    func makeCallbackIfNeed() -> Bool {
//        
//        if let lrcUrl = lrcUrl, lrcUrl.count == 0 {
//            //TODO: error
//            //callback?()
//            return true
//        }
//        if let preloadStatus = preloadStatus, preloadStatus != .preloading {
//            //TODO: error / ok
//            //callback?()
//            return true
//        }
//        
//        return false
//    }
//}

open class AUIMusicLocalServiceImpl: NSObject {
    //选歌列表
    private var chooseSongList: [AUIChooseMusicModel] = []
    private var respDelegates: NSHashTable<AUIMusicRespDelegate> = NSHashTable<AUIMusicRespDelegate>.weakObjects()
    private var rtmManager: AUIRtmManager!
    private var channelName: String!
    private var ktvApi: KTVApiDelegate!
    
    private var callbackMap: [String: ((NSError?)-> ())] = [:]
    
    deinit {
        rtmManager.unsubscribeAttributes(channelName: getChannelName(), itemKey: kChooseSongKey, delegate: self)
        rtmManager.unsubscribeMessage(channelName: getChannelName(), delegate: self)
        aui_info("deinit AUIMusicServiceImpl", tag: "AUIMusicServiceImpl")
    }
    
    public init(channelName: String, rtmManager: AUIRtmManager, ktvApi: KTVApiDelegate) {
        aui_info("init AUIMusicServiceImpl", tag: "AUIMusicServiceImpl")
        super.init()
        self.rtmManager = rtmManager
        self.channelName = channelName
        self.ktvApi = ktvApi
        rtmManager.subscribeAttributes(channelName: getChannelName(), itemKey: kChooseSongKey, delegate: self)
        rtmManager.subscribeMessage(channelName: getChannelName(), delegate: self)
    }
}

//MARK: AUIRtmMsgProxyDelegate
extension AUIMusicLocalServiceImpl: AUIRtmAttributesProxyDelegate {
    public func onAttributesDidChanged(channelName: String, key: String, value: Any) {
        if key == kChooseSongKey {
            aui_info("recv choose song attr did changed \(value)", tag: "AUIMusicServiceImpl")
            guard let songArray = (value as AnyObject).yy_modelToJSONObject(),
                  let chooseSongList = NSArray.yy_modelArray(with: AUIChooseMusicModel.self, json: songArray) as? [AUIChooseMusicModel] else {
                return
            }
            
            aui_info("update \(chooseSongList.count)", tag: "AUIMusicServiceImpl")
            self.chooseSongList = chooseSongList
            self.respDelegates.allObjects.forEach { obj in
                obj.onUpdateAllChooseSongs(songs: chooseSongList)
            }
            
        }
    }
}

//let jsonOption = "{\"needLyric\":true,\"pitchType\":1}"
//MARK: AUIMusicServiceDelegate
extension AUIMusicLocalServiceImpl: AUIMusicServiceDelegate {
    
    public func getRoomContext() -> AUIRoomContext {
        return AUIRoomContext.shared
    }
    
    public func bindRespDelegate(delegate: AUIMusicRespDelegate) {
        respDelegates.add(delegate)
    }
    
    public func unbindRespDelegate(delegate: AUIMusicRespDelegate) {
        respDelegates.remove(delegate)
    }
    
    public func getChannelName() -> String {
        return channelName
    }
    
    public func getMusicList(chartId: Int,
                             page: Int,
                             pageSize: Int,
                             completion: @escaping AUIMusicListCompletion) {
        aui_info("getMusicList with chartId: \(chartId)", tag: "AUIMusicServiceImpl")
        self.ktvApi.searchMusic(musicChartId: chartId,
                                page: page,
                                pageSize: pageSize,
                                jsonOption: jsonOption) { requestId, status, collection in
            aui_info("getMusicList with chartId: \(chartId) status: \(status.rawValue) count: \(collection.count)", tag: "AUIMusicServiceImpl")
            guard status == .OK else {
                //TODO:
                DispatchQueue.main.async {
                    completion(nil, nil)
                }
                return
            }
            
            var musicList: [AUIMusicModel] = []
            collection.musicList.forEach { music in
                let model = AUIMusicModel()
                model.songCode = "\(music.songCode)"
                model.name = music.name
                model.singer = music.singer
                model.poster = music.poster
//                model.releaseTime = music.releaseTime
                model.duration = music.durationS
                musicList.append(model)
            }
            
            DispatchQueue.main.async {
                completion(nil, musicList)
            }
        }
    }
    
    public func searchMusic(keyword: String,
                            page: Int,
                            pageSize: Int,
                            completion: @escaping AUIMusicListCompletion) {
        aui_info("searchMusic with keyword: \(keyword)", tag: "AUIMusicServiceImpl")
        self.ktvApi.searchMusic(keyword: keyword,
                                page: page,
                                pageSize: pageSize,
                                jsonOption: jsonOption) { requestId, status, collection in
            aui_info("searchMusic with keyword: \(keyword) status: \(status.rawValue) count: \(collection.count)", tag: "AUIMusicServiceImpl")
            guard status == .OK else {
                //TODO:
                DispatchQueue.main.async {
                    completion(nil, nil)
                }
                return
            }
            
            var musicList: [AUIMusicModel] = []
            collection.musicList.forEach { music in
                let model = AUIMusicModel()
                model.songCode = "\(music.songCode)"
                model.name = music.name
                model.singer = music.singer
                model.poster = music.poster
//                model.releaseTime = music.releaseTime
                model.duration = music.durationS
                musicList.append(model)
            }
            
            DispatchQueue.main.async {
                completion(nil, musicList)
            }
        }
    }
    
    public func getAllChooseSongList(completion: AUIChooseSongListCompletion?) {
        aui_info("getAllChooseSongList", tag: "AUIMusicServiceImpl")
        self.rtmManager.getMetadata(channelName: self.channelName) { error, map in
            aui_info("getAllChooseSongList error: \(error?.localizedDescription ?? "success")", tag: "AUIMusicServiceImpl")
            if let error = error {
                //TODO: error
                completion?(error, nil)
                return
            }
            
            guard let jsonStr = map?[kChooseSongKey] else {
                //TODO: error
                completion?(nil, nil)
                return
            }
            
            self.chooseSongList = NSArray.yy_modelArray(with: AUIChooseMusicModel.self, json: jsonStr) as? [AUIChooseMusicModel] ?? []
            completion?(nil, self.chooseSongList)
        }
    }
    
    public func chooseSong(songModel:AUIMusicModel, completion: AUICallback?) {
        aui_info("chooseSong: \(songModel.songCode)", tag: "AUIMusicServiceImpl")
        
        guard let dic = songModel.yy_modelToJSONObject() as? [String: Any] else {
            completion?(AUICommonError.chooseSongIsFail.toNSError())
            return
        }
        
        if getRoomContext().getArbiter(channelName: channelName)?.isArbiter() ?? false {
            let songModel = AUIChooseMusicModel.yy_model(with: dic)!
            songModel.owner = getRoomContext().currentUserInfo
            rtmChooseSong(songModel: songModel) { err in
                completion?(err)
            }
            return
        }
        
        let model = AUISongAddNetworkModel.yy_model(with: dic)!
        model.userId = getRoomContext().currentUserInfo.userId
        model.roomId = channelName
        let owner = getRoomContext().currentUserInfo
        model.owner = owner
        
        let message = model.rtmMessage()
        rtmManager.publish(channelName: channelName, message: message) { err in
        }
        callbackMap[model.uniqueId] = completion
    }
    
    public func removeSong(songCode: String, completion: AUICallback?) {
        aui_info("removeSong: \(songCode)", tag: "AUIMusicServiceImpl")
        
        let removeUserId = getRoomContext().currentUserInfo.userId
        if getRoomContext().getArbiter(channelName: channelName)?.isArbiter() ?? false {
            rtmRemoveSong(songCode: songCode, removeUserId: removeUserId) { err in
                completion?(err)
            }
            return
        }
        
        let model = AUISongRemoveNetworkModel()
        model.userId = removeUserId
        model.songCode = songCode
        model.roomId = channelName
        
        let message = model.rtmMessage()
        rtmManager.publish(channelName: channelName, message: message) { err in
        }
        callbackMap[model.uniqueId] = completion
    }
    
    public func pinSong(songCode: String, completion: AUICallback?) {
        aui_info("pinSong: \(songCode)", tag: "AUIMusicServiceImpl")
        let updateUserId = getRoomContext().currentUserInfo.userId
        if getRoomContext().getArbiter(channelName: channelName)?.isArbiter() ?? false {
            rtmPinSong(songCode: songCode, updateUserId: updateUserId) { err in
                completion?(err)
            }
            return
        }
        
        let model = AUISongPinNetworkModel()
        model.userId = updateUserId
        model.songCode = songCode
        model.roomId = channelName
        
        let message = model.rtmMessage()
        rtmManager.publish(channelName: channelName, message: message) { err in
        }
        callbackMap[model.uniqueId] = completion
    }
    
    public func updatePlayStatus(songCode: String, playStatus: AUIPlayStatus, completion: AUICallback?) {
        aui_info("updatePlayStatus: \(songCode)", tag: "AUIMusicServiceImpl")
        
        let updateUserId = getRoomContext().currentUserInfo.userId
        if getRoomContext().getArbiter(channelName: channelName)?.isArbiter() ?? false {
            rtmUpdatePlayStatus(songCode: songCode, playStatus: playStatus, updateUserId: updateUserId) { err in
                completion?(err)
            }
            return
        }
        
        if playStatus == .playing {
            let model = AUISongPlayNetworkModel()
            model.userId = updateUserId
            model.songCode = songCode
            model.roomId = channelName
            
            let message = model.rtmMessage()
            rtmManager.publish(channelName: channelName, message: message) { err in
            }
            callbackMap[model.uniqueId] = completion
        } else {
            let model = AUISongStopNetworkModel()
            model.userId = updateUserId
            model.songCode = songCode
            model.roomId = channelName
            
            let message = model.rtmMessage()
            rtmManager.publish(channelName: channelName, message: message) { err in
            }
            callbackMap[model.uniqueId] = completion
        }
    }
}

//MARK: AUIRtmMessageProxyDelegate
extension AUIMusicLocalServiceImpl: AUIRtmMessageProxyDelegate {
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
        if interfaceName == kAUISongAddNetworkInterface, let model = AUISongAddNetworkModel.model(rtmMessage: message) {
            let dic = model.yy_modelToJSONObject() as? [String : Any] ?? [:]
            let songModel = AUIChooseMusicModel.yy_model(with: dic)!
            //TODO: use ntp time
            songModel.createAt = Int64(Date().timeIntervalSince1970 * 1000)
            rtmChooseSong(songModel: songModel) {[weak self] err in
                self?.rtmManager.sendReceipt(channelName: channelName, uniqueId: uniqueId, error: err)
            }
        } else if interfaceName == kAUISongPinNetworkInterface, let model = AUISongPinNetworkModel.model(rtmMessage: message) {
            rtmPinSong(songCode: model.songCode ?? "", updateUserId: model.userId ?? "") {[weak self] err in
                self?.rtmManager.sendReceipt(channelName: channelName, uniqueId: uniqueId, error: err)
            }
        } else if interfaceName == kAUISongRemoveNetworkInterface, let model = AUISongRemoveNetworkModel.model(rtmMessage: message) {
            rtmRemoveSong(songCode: model.songCode ?? "", removeUserId: model.userId ?? ""){[weak self] err in
                self?.rtmManager.sendReceipt(channelName: channelName, uniqueId: uniqueId, error: err)
            }
        } else if interfaceName == kAUISongPlayNetworkInterface, let model = AUISongPlayNetworkModel.model(rtmMessage: message) {
            rtmUpdatePlayStatus(songCode: model.songCode ?? "", playStatus: .playing, updateUserId: model.userId ?? "") {[weak self] err in
                self?.rtmManager.sendReceipt(channelName: channelName, uniqueId: uniqueId, error: err)
            }
        } else if interfaceName == kAUISongStopNetworkInterface, let model = AUISongStopNetworkModel.model(rtmMessage: message) {
            rtmUpdatePlayStatus(songCode: model.songCode ?? "", playStatus: .idle, updateUserId: model.userId ?? "") {[weak self] err in
                self?.rtmManager.sendReceipt(channelName: channelName, uniqueId: uniqueId, error: err)
            }
        }
    }
}

//MARK: set meta data
extension AUIMusicLocalServiceImpl {
    private func _sortChooseSongList() -> [AUIChooseMusicModel] {
        let songList = chooseSongList.sorted(by: { model1, model2 in
            //歌曲播放中优先（只会有一个，多个目前没有，如果有需要修改排序策略）
            if model1.playStatus == .playing {
                return true
            }
            if model2.playStatus == .playing {
                return false
            }
                  
            //都没有置顶时间，比较创建时间，创建时间小的在前（即创建早的在前）
            if model1.pinAt < 1,  model2.pinAt < 1 {
                return model1.createAt - model2.createAt < 0 ? true : false
            }
                  
            //有一个有置顶时间，置顶时间大的在前（即后置顶的在前）
            return model1.pinAt - model2.pinAt > 0 ? true : false
        })
        
        return songList
    }
    
    private func rtmChooseSong(songModel:AUIChooseMusicModel, callback: @escaping AUICallback) {
        //TODO: check song owner is on micseat
        
        if self.chooseSongList.contains(where: { $0.songCode == songModel.songCode }) {
            callback(AUICommonError.chooseSongAlreadyExist.toNSError())
            return
        }
        
        
        let metaData = NSMutableDictionary()
        var err: NSError? = nil
        for obj in self.respDelegates.allObjects {
            err = obj.onSongWillAdd?(userId: songModel.userId ?? "", metaData: metaData)
            if let err = err {
                callback(err)
                return
            }
        }
        
        let metaDataSongList = NSMutableArray(array: chooseSongList)
        metaDataSongList.add(songModel)
        let str = metaDataSongList.yy_modelToJSONString() ?? ""
        metaData[kChooseSongKey] = str
        self.rtmManager.setBatchMetadata(channelName: channelName,
                                         lockName: kRTM_Referee_LockName,
                                         metadata: metaData as! [String : String]) { error in
            callback(error)
        }
    }
    
    private func rtmRemoveSong(songCode: String, removeUserId: String, callback: @escaping AUICallback) {
        //TODO: check is song owner or is room owner
        guard let idx = chooseSongList.firstIndex(where: { $0.songCode == songCode }) else {
            callback(AUICommonError.chooseSongNotExist.toNSError())
            return
        }
        let song = chooseSongList[idx]
        guard song.owner?.userId == removeUserId || getRoomContext().isRoomOwner(channelName: channelName, userId: removeUserId) else {
            callback(AUICommonError.noPermission.toNSError())
            return
        }
        
        var err: NSError? = nil
        let metaData = NSMutableDictionary()
        for obj in respDelegates.allObjects {
            err = obj.onSongWillRemove?(songCode: songCode, metaData: metaData)
            if let err = err {
                callback(err)
                return
            }
        }
        
        let metaDataSongList = NSMutableArray(array: chooseSongList)
        metaDataSongList.removeObject(at: idx)
        let str = metaDataSongList.yy_modelToJSONString() ?? ""
        metaData[kChooseSongKey] = str
        self.rtmManager.setBatchMetadata(channelName: channelName,
                                         lockName: kRTM_Referee_LockName,
                                         metadata: metaData as! [String : String]) { error in
            callback(error)
        }
        
        //TODO: remove chorus list if need
    }
    
    private func rtmUpdatePlayStatus(songCode: String, playStatus: AUIPlayStatus, updateUserId: String, callback: @escaping AUICallback) {
        guard let idx = chooseSongList.firstIndex(where: { $0.songCode == songCode }) else {
            callback(AUICommonError.chooseSongNotExist.toNSError())
            return
        }
        
        let song = chooseSongList[idx]
        guard song.owner?.userId == updateUserId else {
            callback(AUICommonError.noPermission.toNSError())
            return
        }
        let origStatus = song.status
        song.status = playStatus.rawValue
        let metaDataSongList = NSMutableArray(array: chooseSongList)
        let str = metaDataSongList.yy_modelToJSONString() ?? ""
        let metaData = [kChooseSongKey: str]
        self.rtmManager.setBatchMetadata(channelName: channelName,
                                         lockName: kRTM_Referee_LockName,
                                         metadata: metaData) { error in
            callback(error)
        }
        song.status = origStatus
    }
    
    private func rtmPinSong(songCode: String, updateUserId: String, callback: @escaping AUICallback) {
        aui_info("pinSong: \(songCode)", tag: "AUIMusicServiceImpl")
        guard let idx = chooseSongList.firstIndex(where: { $0.songCode == songCode }) else {
            callback(AUICommonError.chooseSongNotExist.toNSError())
            return
        }
        
        let song = chooseSongList[idx]
        guard song.owner?.userId == updateUserId else {
            callback(AUICommonError.noPermission.toNSError())
            return
        }
        let origPinAt = song.pinAt
        song.pinAt = Int64(Date().timeIntervalSince1970 * 1000)
        let sortSongList = _sortChooseSongList()
        let metaDataSongList = NSMutableArray(array: sortSongList)
        let str = metaDataSongList.yy_modelToJSONString() ?? ""
        let metaData = [kChooseSongKey: str]
        self.rtmManager.setBatchMetadata(channelName: channelName,
                                         lockName: kRTM_Referee_LockName,
                                         metadata: metaData) { error in
            callback(error)
        }
        song.pinAt = origPinAt
    }
    
    public func onUserInfoClean(userId: String, completion: @escaping ((NSError?) -> ())) {
        var metaData = [String: String]()
        let filterSongList = chooseSongList.filter({ $0.userId != userId })
        if filterSongList.count != chooseSongList.count {
            let metaDataSongList = NSMutableArray(array: filterSongList)
            let str = metaDataSongList.yy_modelToJSONString() ?? ""
            metaData[kChooseSongKey] = str
        }
        
        self.rtmManager.setBatchMetadata(channelName: channelName,
                                         lockName: kRTM_Referee_LockName,
                                         metadata: metaData,
                                         completion: completion)
    }
    
    public func onRoomWillDestroy(removeKeys: NSMutableArray) -> NSError? {
        removeKeys.add(kChooseSongKey)
        return nil
    }
}

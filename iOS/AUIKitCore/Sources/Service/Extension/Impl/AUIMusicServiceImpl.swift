//
//  AUIMusicServiceImpl.swift
//  AUIKit
//
//  Created by wushengtao on 2023/3/7.
//

import Foundation
import AgoraRtcKit
import YYModel

private let kChooseSongKey = "song"

private enum AUIMusicCmd: String {
    case chooseSongCmd = "chooseSongCmd"
    case removeSongCmd = "removeSongCmd"
    case pingSongCmd = "pingSongCmd"
    case updatePlayStatusCmd = "updatePlayStatusCmd"
}

class AUIMusicLoadingInfo: NSObject {
    var songCode: String?
    var lrcMsgId: String?
    var preloadStatus: AgoraMusicContentCenterPreloadStatus?
    var lrcUrl: String?
    var callback: AUILoadSongCompletion?

    func makeCallbackIfNeed() -> Bool {
        if let lrcUrl = lrcUrl, lrcUrl.count == 0 {
            //TODO: error
            //callback?()
            return true
        }
        if let preloadStatus = preloadStatus, preloadStatus != .preloading {
            //TODO: error / ok
            //callback?()
            return true
        }

        return false
    }
}

open class AUIMusicServiceImpl: NSObject {
    //选歌列表
    private var chooseSongList: [AUIChooseMusicModel] = []
    private var respDelegates: NSHashTable<AUIMusicRespDelegate> = NSHashTable<AUIMusicRespDelegate>.weakObjects()
    private var rtmManager: AUIRtmManager!
    private var channelName: String!
    private var ktvApi: KTVApiDelegate!
    
    private var listCollection: AUIListCollection!
        
    deinit {
        aui_info("deinit AUIMusicServiceImpl", tag: "AUIMusicServiceImpl")
    }
    
    public init(channelName: String, rtmManager: AUIRtmManager, ktvApi: KTVApiDelegate) {
        aui_info("init AUIMusicServiceImpl", tag: "AUIMusicServiceImpl")
        super.init()
        self.rtmManager = rtmManager
        self.channelName = channelName
        self.ktvApi = ktvApi
        self.listCollection = AUIListCollection(channelName: channelName, observeKey: kChooseSongKey, rtmManager: rtmManager)
        
        listCollection.subscribeWillAdd {[weak self] publisherId, dataCmd, newItem, attr in
            return self?.metadataWillAdd(publiserId: publisherId, 
                                         dataCmd: dataCmd,
                                         newItem: newItem)
        }
        listCollection.subscribeWillMerge {[weak self] publisherId, dataCmd, updateMap, currentMap in
            return self?.metadataWillMerge(publiserId: publisherId, 
                                           dataCmd: dataCmd,
                                           updateMap: updateMap, 
                                           currentMap: currentMap)
        }
        
        listCollection.subscribeWillRemove {[weak self] publisherId, dataCmd, item in
            return self?.metadataWillRemove(publiserId: publisherId,
                                            dataCmd: dataCmd,
                                            currentMap: item)
        }
        
        listCollection.subscribeAttributesDidChanged {[weak self] channelName, key, value in
            self?.onAttributesDidChanged(channelName: channelName, key: key, value: value)
        }
        
        listCollection.subscribeAttributesWillSet { channelName, key, valueCmd, attr in
            guard valueCmd == AUIMusicCmd.pingSongCmd.rawValue else { return }
            guard let value = attr.getList() else { return }
            
            let sortList = self._sortChooseSongList(chooseSongList: value)
            attr.setList(sortList)
        }
    }
}

let jsonOption = "{\"needLyric\":true,\"pitchType\":1}"
//MARK: AUIMusicServiceDelegate
extension AUIMusicServiceImpl: AUIMusicServiceDelegate {
    
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
        
        listCollection.getMetaData {[weak self] error, obj in
            guard let self = self else {return}
            aui_info("getAllChooseSongList error: \(error?.localizedDescription ?? "success")", tag: "AUIMusicServiceImpl")
            if let error = error {
                //TODO: error
                completion?(error, nil)
                return
            }
            guard let obj = obj as? [[String: Any]] else {
                completion?(NSError.auiError("getAllChooseSongList fail not a array"), nil)
                return
            }
            self.chooseSongList = NSArray.yy_modelArray(with: AUIChooseMusicModel.self, json: obj) as? [AUIChooseMusicModel] ?? []
            completion?(nil, self.chooseSongList)
        }
    }
    
    public func chooseSong(songModel:AUIMusicModel, completion: AUICallback?) {
        aui_info("chooseSong: \(songModel.songCode)", tag: "AUIMusicServiceImpl")
        guard let dic = songModel.yy_modelToJSONObject() as? [String: Any] else {
            completion?(AUICommonError.chooseSongIsFail.toNSError())
            return
        }
        let model = AUIChooseMusicModel.yy_model(with: dic)!
        model.owner = getRoomContext().currentUserInfo
        guard let value = model.yy_modelToJSONObject() as? [String: Any] else {
            completion?(NSError.auiError("convert to json fail"))
            return
        }
        listCollection.addMetaData(valueCmd: AUIMusicCmd.chooseSongCmd.rawValue,
                                   value: value,
                                   filter: [["songCode": model.songCode]],
                                   callback: completion)
    }
    
    public func removeSong(songCode: String, completion: AUICallback?) {
        aui_info("removeSong: \(songCode)", tag: "AUIMusicServiceImpl")
        listCollection.removeMetaData(valueCmd: AUIMusicCmd.removeSongCmd.rawValue,
                                      filter: [["songCode": songCode]],
                                      callback: completion)
    }
    
    public func pinSong(songCode: String, completion: AUICallback?) {
        aui_info("pinSong: \(songCode)", tag: "AUIMusicServiceImpl")
        let value = ["pinAt": Int64(Date().timeIntervalSince1970 * 1000)]
        listCollection.mergeMetaData(valueCmd: AUIMusicCmd.pingSongCmd.rawValue,
                                     value: value,
                                     filter: [["songCode": songCode]],
                                     callback: completion)
    }
    
    public func updatePlayStatus(songCode: String, 
                                 playStatus: AUIPlayStatus,
                                 completion: AUICallback?) {
        aui_info("updatePlayStatus: \(songCode)", tag: "AUIMusicServiceImpl")
        let value = ["status": playStatus.rawValue]
        listCollection.mergeMetaData(valueCmd: AUIMusicCmd.updatePlayStatusCmd.rawValue,
                                      value: value,
                                      filter: [["songCode": songCode]],
                                      callback: completion)
    }
}

//MARK: set meta data
extension AUIMusicServiceImpl {
    private func _sortChooseSongList(chooseSongList: [[String: Any]]) -> [[String: Any]] {
        let songList = chooseSongList.sorted(by: { model1, model2 in
            //歌曲播放中优先（只会有一个，多个目前没有，如果有需要修改排序策略）
            if model1["status"] as? Int == AUIPlayStatus.playing.rawValue {
                return true
            }
            if model2["status"] as? Int == AUIPlayStatus.playing.rawValue {
                return false
            }
            
            let pinAt1 = model1["pinAt"] as? Int64 ?? 0
            let pinAt2 = model2["pinAt"] as? Int64 ?? 0
            let createAt1 = model1["createAt"] as? Int64 ?? 0
            let createAt2 = model2["createAt"] as? Int64 ?? 0
            //都没有置顶时间，比较创建时间，创建时间小的在前（即创建早的在前）
            if pinAt1 < 1,  pinAt2 < 1 {
                return createAt1 - createAt2 < 0 ? true : false
            }
            
            //有一个有置顶时间，置顶时间大的在前（即后置顶的在前）
            return pinAt1 - pinAt2 > 0 ? true : false
        })
        
        return songList
    }
    
    private func onAttributesDidChanged(channelName: String, key: String, value: AUIAttributesModel) {
        if key == kChooseSongKey {
            aui_info("recv choose song attr did changed \(value)", tag: "AUIMusicServiceImpl")
            guard let songArray = (value.getList() as? AnyObject)?.yy_modelToJSONObject(),
                  let chooseSongList = NSArray.yy_modelArray(with: AUIChooseMusicModel.self, json: songArray) as? [AUIChooseMusicModel] else {
                return
            }
            
            aui_info("update \(chooseSongList.count)", tag: "AUIMusicServiceImpl")
            self.chooseSongList = chooseSongList
            self.respDelegates.allObjects.forEach { obj in
                obj.onUpdateAllChooseSongs(songs: self.chooseSongList)
            }
        }
    }
    
    private func metadataWillAdd(publiserId: String,
                                 dataCmd: String?,
                                 newItem: [String: Any]) -> NSError? {
        guard let dataCmd = AUIMusicCmd(rawValue: dataCmd ?? "") else {
            return AUICommonError.unknown.toNSError()
        }
        
        let owner = newItem["owner"] as? [String: Any]
        let userId = owner?["userId"] as? String ?? ""
        switch dataCmd {
        case .chooseSongCmd:
//            if self.chooseSongList.contains(where: { $0.songCode == songCode }) {
//                return AUICommonError.chooseSongAlreadyExist.toNSError()
//            }
            //过滤条件在filter里包含
    
            let metaData = NSMutableDictionary(dictionary: newItem)
            var err: NSError? = nil
            for obj in self.respDelegates.allObjects {
                err = obj.onSongWillAdd?(userId: userId, metaData: metaData)
                if let err = err {
                    return err
                }
            }
            return nil
        default:
            break
        }
        
        return NSError.auiError("add music cmd incorrect")
    }
            
    private func metadataWillMerge(publiserId: String,
                                   dataCmd: String?,
                                   updateMap: [String: Any],
                                   currentMap: [String: Any]) -> NSError? {
        guard let dataCmd = AUIMusicCmd(rawValue: dataCmd ?? "") else {
            return AUICommonError.unknown.toNSError()
        }
        
        switch dataCmd {
        case .pingSongCmd:
            //过滤条件在filter里包含
            return nil
        case .updatePlayStatusCmd:
            //过滤条件在filter里包含
            return nil
        default:
            break
        }
        
        return NSError.auiError("merge music cmd incorrect")
    }
            
    private func metadataWillRemove(publiserId: String,
                                    dataCmd: String?,
                                    currentMap: [String: Any]) -> NSError? {
        guard let dataCmd = AUIMusicCmd(rawValue: dataCmd ?? "") else {
            return AUICommonError.unknown.toNSError()
        }
        
        let owner = currentMap["owner"] as? [String: Any]
        let userId = owner?["userId"] as? String ?? ""
        let songCode = currentMap["songCode"] as? String ?? ""
        switch dataCmd {
        case .removeSongCmd:
            //点歌本人/房主可操作
            guard publiserId == userId || getRoomContext().isRoomOwner(channelName: channelName, userId: publiserId) else {
                return AUICommonError.noPermission.toNSError()
            }
            let metaData = NSMutableDictionary()
            for obj in respDelegates.allObjects {
                let err = obj.onSongWillRemove?(songCode: songCode, metaData: metaData)
                if let err = err {
                    return err
                }
            }
            return nil
        default:
            break
        }
        
        return NSError.auiError("remove music cmd incorrect")
    }
    
    public func cleanUserInfo(userId: String, completion: @escaping ((NSError?) -> ())) {
        listCollection.removeMetaData(valueCmd: AUIMusicCmd.removeSongCmd.rawValue,
                                      filter: [["owner": ["userId": userId]]],
                                      callback: completion)
    }
    
    public func deinitService(completion:  @escaping  ((NSError?) -> ())) {
//        rtmManager.cleanBatchMetadata(channelName: channelName,
//                                      lockName: kRTM_Referee_LockName,
//                                      removeKeys: [kChooseSongKey],
//                                      completion: completion)
        listCollection.cleanMetaData(callback: completion)
    }
}

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
    private var respDelegates: NSHashTable<AnyObject> = NSHashTable<AnyObject>.weakObjects()
    private var rtmManager: AUIRtmManager!
    private var channelName: String!
    private var ktvApi: KTVApiDelegate!
    
    deinit {
        rtmManager.unsubscribeAttributes(channelName: getChannelName(), itemKey: kChooseSongKey, delegate: self)
        aui_info("deinit AUIMusicServiceImpl", tag: "AUIMusicServiceImpl")
    }
    
    public init(channelName: String, rtmManager: AUIRtmManager, ktvApi: KTVApiDelegate) {
        aui_info("init AUIMusicServiceImpl", tag: "AUIMusicServiceImpl")
        super.init()
        self.rtmManager = rtmManager
        self.channelName = channelName
        self.ktvApi = ktvApi
        rtmManager.subscribeAttributes(channelName: getChannelName(), itemKey: kChooseSongKey, delegate: self)
    }
}

//MARK: AUIRtmMsgProxyDelegate
extension AUIMusicServiceImpl: AUIRtmAttributesProxyDelegate {
    public func onAttributesDidChanged(channelName: String, key: String, value: Any) {
        if key == kChooseSongKey {
            aui_info("recv choose song attr did changed \(value)", tag: "AUIMusicServiceImpl")
            guard let songArray = (value as AnyObject).yy_modelToJSONObject(),
                    let chooseSongList = NSArray.yy_modelArray(with: AUIChooseMusicModel.self, json: songArray) as? [AUIChooseMusicModel] else {
                return
            }
            
            //TODO: optimize
            if #available(iOS 13.0, *) {
                let difference =
                chooseSongList.difference(from: self.chooseSongList) { song1, song2 in
                    return song1 == song2
                }
            }else{
                
            }
            var ifDiff = false
//            if difference.count == 1 {
//                for change in difference {
//                    switch change {
//                    case let .remove(offset, oldElement, _):
//                        aui_info("remove \(oldElement.name) idx: \(offset)", tag: "AUIMusicServiceImpl")
//                        self.respDelegates.allObjects.forEach { obj in
//                            guard let delegate = obj as? AUIMusicRespDelegate else {return}
//                            delegate.onRemoveChooseSong(song: oldElement)
//                        }
//                        ifDiff = true
//                    case let .insert(offset, newElement, _):
//                        aui_info("insert \(newElement.name) idx: \(offset)", tag: "AUIMusicServiceImpl")
//                        self.respDelegates.allObjects.forEach { obj in
//                            guard let delegate = obj as? AUIMusicRespDelegate else {return}
//                            delegate.onAddChooseSong(song: newElement)
//                        }
//                        ifDiff = true
//                    }
//                }
//            } else if difference.removals.count == 1, difference.insertions.count == 1 {
//                if let remove =  difference.removals.first,
//                    let insert = difference.insertions.first {
//                    switch remove {
//                    case let .remove(oldOffset, oldElement, _):
//                        switch insert {
//                        case let .insert(newOffset, newElement, _):
//                            if oldOffset == newOffset, oldElement.songCode == newElement.songCode {
//                                aui_info("update \(newElement.name) idx: \(newOffset)", tag: "AUIMusicServiceImpl")
//                                self.respDelegates.allObjects.forEach { obj in
//                                    guard let delegate = obj as? AUIMusicRespDelegate else {return}
//                                    delegate.onUpdateChooseSong(song:newElement)
//                                }
//                                ifDiff = true
//                            }
//                        default:
//                            break
//                        }
//                    default:
//                        break
//                    }
//                }
//            }
            
            if ifDiff == false {
                aui_info("update \(chooseSongList.count)", tag: "AUIMusicServiceImpl")
                self.respDelegates.allObjects.forEach { obj in
                    guard let delegate = obj as? AUIMusicRespDelegate else {return}
                    delegate.onUpdateAllChooseSongs(songs: chooseSongList)
                }
            }
            
            aui_info("song update: \(self.chooseSongList.count)->\(chooseSongList.count)", tag: "AUIMusicServiceImpl")
            self.chooseSongList = chooseSongList
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
                model.releaseTime = music.releaseTime
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
                model.releaseTime = music.releaseTime
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
            //TODO: error
            completion?(nil)
            return
        }
        
        let networkModel = AUISongAddNetworkModel.yy_model(with: dic)!
        networkModel.userId = getRoomContext().currentUserInfo.userId
        let chooseModel = AUIChooseMusicModel.yy_model(with: dic)!
        networkModel.roomId = channelName
        let owner = getRoomContext().currentUserInfo
        networkModel.owner = owner
        chooseModel.owner = owner
        networkModel.request(completion: { err, _ in
            completion?(err as? NSError)
        })
    }
    
    public func removeSong(songCode: String, completion: AUICallback?) {
        aui_info("removeSong: \(songCode)", tag: "AUIMusicServiceImpl")
        let model = AUISongRemoveNetworkModel()
        model.userId = getRoomContext().currentUserInfo.userId
        model.songCode = songCode
        model.roomId = channelName
        model.request { err, _ in
            completion?(err as? NSError)
        }
    }
    
    public func pinSong(songCode: String, completion: AUICallback?) {
        aui_info("pinSong: \(songCode)", tag: "AUIMusicServiceImpl")
        let model = AUISongPinNetworkModel()
        model.userId = getRoomContext().currentUserInfo.userId
        model.songCode = songCode
        model.roomId = channelName
        model.request { err, _ in
            completion?(err as? NSError)
        }
    }
    
    public func updatePlayStatus(songCode: String, playStatus: AUIPlayStatus, completion: AUICallback?) {
        aui_info("updatePlayStatus: \(songCode)", tag: "AUIMusicServiceImpl")
        if playStatus == .playing {
            let model = AUISongPlayNetworkModel()
            model.userId = getRoomContext().currentUserInfo.userId
            model.songCode = songCode
            model.roomId = channelName
            model.request { err, _ in
                completion?(err as? NSError)
            }
        } else {
            let model = AUISongStopNetworkModel()
            model.userId = getRoomContext().currentUserInfo.userId
            model.songCode = songCode
            model.roomId = channelName
            model.request { err, _ in
                completion?(err as? NSError)
            }
        }
    }
}

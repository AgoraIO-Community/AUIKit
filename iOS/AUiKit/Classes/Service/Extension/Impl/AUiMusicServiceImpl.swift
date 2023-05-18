//
//  AUiMusicServiceImpl.swift
//  AUiKit
//
//  Created by wushengtao on 2023/3/7.
//

import Foundation
import AgoraRtcKit
import YYModel

private let kChooseSongKey = "song"

class AUiMusicLoadingInfo: NSObject {
    var songCode: String?
    var lrcMsgId: String?
    var preloadStatus: AgoraMusicContentCenterPreloadStatus?
    var lrcUrl: String?
    var callback: AUiLoadSongCompletion?
    
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

open class AUiMusicServiceImpl: NSObject {
    //选歌列表
    private var chooseSongList: [AUiChooseMusicModel] = []
    private var respDelegates: NSHashTable<AnyObject> = NSHashTable<AnyObject>.weakObjects()
    private var rtmManager: AUiRtmManager!
    private var channelName: String!
    private var ktvApi: KTVApiDelegate!
    
    deinit {
        rtmManager.unsubscribeAttributes(channelName: getChannelName(), itemKey: kChooseSongKey, delegate: self)
        aui_info("deinit AUiMusicServiceImpl", tag: "AUiMusicServiceImpl")
    }
    
    public init(channelName: String, rtmManager: AUiRtmManager, ktvApi: KTVApiDelegate) {
        aui_info("init AUiMusicServiceImpl", tag: "AUiMusicServiceImpl")
        super.init()
        self.rtmManager = rtmManager
        self.channelName = channelName
        self.ktvApi = ktvApi
        rtmManager.subscribeAttributes(channelName: getChannelName(), itemKey: kChooseSongKey, delegate: self)
    }
}


//MARK: AUiRtmMsgProxyDelegate
extension AUiMusicServiceImpl: AUiRtmAttributesProxyDelegate {
    public func onAttributesDidChanged(channelName: String, key: String, value: Any) {
        if key == kChooseSongKey {
            aui_info("recv choose song attr did changed \(value)", tag: "AUiMusicServiceImpl")
            guard let songArray = (value as AnyObject).yy_modelToJSONObject(),
                    let chooseSongList = NSArray.yy_modelArray(with: AUiChooseMusicModel.self, json: songArray) as? [AUiChooseMusicModel] else {
                return
            }
            
            //TODO: optimize
            let difference =
            chooseSongList.difference(from: self.chooseSongList) { song1, song2 in
                return song1 == song2
            }
            var ifDiff = false
//            if difference.count == 1 {
//                for change in difference {
//                    switch change {
//                    case let .remove(offset, oldElement, _):
//                        aui_info("remove \(oldElement.name) idx: \(offset)", tag: "AUiMusicServiceImpl")
//                        self.respDelegates.allObjects.forEach { obj in
//                            guard let delegate = obj as? AUiMusicRespDelegate else {return}
//                            delegate.onRemoveChooseSong(song: oldElement)
//                        }
//                        ifDiff = true
//                    case let .insert(offset, newElement, _):
//                        aui_info("insert \(newElement.name) idx: \(offset)", tag: "AUiMusicServiceImpl")
//                        self.respDelegates.allObjects.forEach { obj in
//                            guard let delegate = obj as? AUiMusicRespDelegate else {return}
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
//                                aui_info("update \(newElement.name) idx: \(newOffset)", tag: "AUiMusicServiceImpl")
//                                self.respDelegates.allObjects.forEach { obj in
//                                    guard let delegate = obj as? AUiMusicRespDelegate else {return}
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
                aui_info("update \(chooseSongList.count)", tag: "AUiMusicServiceImpl")
                self.respDelegates.allObjects.forEach { obj in
                    guard let delegate = obj as? AUiMusicRespDelegate else {return}
                    delegate.onUpdateAllChooseSongs(songs: chooseSongList)
                }
            }
            
            aui_info("song update: \(self.chooseSongList.count)->\(chooseSongList.count)", tag: "AUiMusicServiceImpl")
            self.chooseSongList = chooseSongList
        }
    }
    
    
}

let jsonOption = "{\"needLyric\":true,\"pitchType\":1}"
//MARK: AUiMusicServiceDelegate
extension AUiMusicServiceImpl: AUiMusicServiceDelegate {
    public func bindRespDelegate(delegate: AUiMusicRespDelegate) {
        respDelegates.add(delegate)
    }
    
    public func unbindRespDelegate(delegate: AUiMusicRespDelegate) {
        respDelegates.remove(delegate)
    }
    
    public func getChannelName() -> String {
        return channelName
    }
    
    public func getMusicList(chartId: Int,
                             page: Int,
                             pageSize: Int,
                             completion: @escaping AUiMusicListCompletion) {
        aui_info("getMusicList with chartId: \(chartId)", tag: "AUiMusicServiceImpl")
        self.ktvApi.searchMusic(musicChartId: chartId,
                                page: page,
                                pageSize: pageSize,
                                jsonOption: jsonOption) { requestId, status, collection in
            aui_info("getMusicList with chartId: \(chartId) status: \(status.rawValue) count: \(collection.count)", tag: "AUiMusicServiceImpl")
            guard status == .OK else {
                //TODO:
                DispatchQueue.main.async {
                    completion(nil, nil)
                }
                return
            }
            
            var musicList: [AUiMusicModel] = []
            collection.musicList.forEach { music in
                let model = AUiMusicModel()
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
                            completion: @escaping AUiMusicListCompletion) {
        aui_info("searchMusic with keyword: \(keyword)", tag: "AUiMusicServiceImpl")
        self.ktvApi.searchMusic(keyword: keyword,
                                page: page,
                                pageSize: pageSize,
                                jsonOption: jsonOption) { requestId, status, collection in
            aui_info("searchMusic with keyword: \(keyword) status: \(status.rawValue) count: \(collection.count)", tag: "AUiMusicServiceImpl")
            guard status == .OK else {
                //TODO:
                DispatchQueue.main.async {
                    completion(nil, nil)
                }
                return
            }
            
            var musicList: [AUiMusicModel] = []
            collection.musicList.forEach { music in
                let model = AUiMusicModel()
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
    
    public func getAllChooseSongList(completion: AUiChooseSongListCompletion?) {
        aui_info("getAllChooseSongList", tag: "AUiMusicServiceImpl")
        self.rtmManager.getMetadata(channelName: self.channelName) { error, map in
            aui_info("getAllChooseSongList error: \(error?.localizedDescription ?? "success")", tag: "AUiMusicServiceImpl")
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
            
            self.chooseSongList = NSArray.yy_modelArray(with: AUiChooseMusicModel.self, json: jsonStr) as? [AUiChooseMusicModel] ?? []
            completion?(nil, self.chooseSongList)
        }
    }
    
    public func chooseSong(songModel:AUiMusicModel, completion: AUiCallback?) {
        aui_info("chooseSong: \(songModel.songCode)", tag: "AUiMusicServiceImpl")
        guard let dic = songModel.yy_modelToJSONObject() as? [String: Any] else {
            //TODO: error
            completion?(nil)
            return
        }
        
        let networkModel = AUiSongAddNetworkModel.yy_model(with: dic)!
        networkModel.userId = getRoomContext().currentUserInfo.userId
        let chooseModel = AUiChooseMusicModel.yy_model(with: dic)!
        
        networkModel.roomId = channelName
        let owner = getRoomContext().currentUserInfo
        networkModel.owner = owner
        chooseModel.owner = owner
        networkModel.request(completion: { err, _ in
            completion?(err)
        })
    }
    
    public func removeSong(songCode: String, completion: AUiCallback?) {
        aui_info("removeSong: \(songCode)", tag: "AUiMusicServiceImpl")
        let model = AUiSongRemoveNetworkModel()
        model.userId = getRoomContext().currentUserInfo.userId
        model.songCode = songCode
        model.roomId = channelName
        model.request { err, _ in
            completion?(err)
        }
    }
    
    public func pinSong(songCode: String, completion: AUiCallback?) {
        aui_info("pinSong: \(songCode)", tag: "AUiMusicServiceImpl")
        let model = AUiSongPinNetworkModel()
        model.userId = getRoomContext().currentUserInfo.userId
        model.songCode = songCode
        model.roomId = channelName
        model.request { err, _ in
            completion?(err)
        }
    }
    
    public func updatePlayStatus(songCode: String, playStatus: AUiPlayStatus, completion: AUiCallback?) {
        aui_info("updatePlayStatus: \(songCode)", tag: "AUiMusicServiceImpl")
        if playStatus == .playing {
            let model = AUiSongPlayNetworkModel()
            model.userId = getRoomContext().currentUserInfo.userId
            model.songCode = songCode
            model.roomId = channelName
            model.request { err, _ in
                completion?(err)
            }
        } else {
            let model = AUiSongStopNetworkModel()
            model.userId = getRoomContext().currentUserInfo.userId
            model.songCode = songCode
            model.roomId = channelName
            model.request { err, _ in
                completion?(err)
            }
        }
    }
}

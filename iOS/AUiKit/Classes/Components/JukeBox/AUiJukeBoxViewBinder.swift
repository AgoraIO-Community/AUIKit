//
//  AUiJukeBoxViewBinder.swift
//  AUiKit
//
//  Created by wushengtao on 2023/4/10.
//

import Foundation

private let kChartIds = [3, 4, 2, 6]
let kListPageCount: Int = 10
open class AUiJukeBoxViewBinder: NSObject {
    private weak var jukeBoxView: AUiJukeBoxView? {
        didSet {
            jukeBoxView?.uiDelegate = self
        }
    }
    private weak var serviceDelegate: AUiMusicServiceDelegate? {
        didSet {
            oldValue?.unbindRespDelegate(delegate: self)
            serviceDelegate?.bindRespDelegate(delegate: self)
        }
    }
    
    //歌曲查询列表
    private var searchMusicList: [AUiMusicModel]? 
    //点歌列表
    private var musicListMap: [Int: [AUiMusicModel]] = [:]
    //已点列表
    private var addedMusicList: [AUiChooseMusicModel] = [] {
        didSet {
            if let topSong = addedMusicList.first,
                topSong.userId == serviceDelegate?.getRoomContext().currentUserInfo.userId,
                !topSong.isPlaying {
                self.serviceDelegate?.updatePlayStatus(songCode: topSong.songCode, playStatus: .playing) { error in
                    
                }
            }
            self.jukeBoxView?.selectedSongCount = addedMusicList.count
        }
    }
    /// 已点歌曲key的map
    private var addedMusicSet: NSMutableSet = NSMutableSet()
    
    /// 可置顶歌曲key
    private var pinEnableSet: NSMutableSet = NSMutableSet()
    
    /// 可删除歌曲key
    private var deleteEnableSet: NSMutableSet = NSMutableSet()
    
    deinit {
        aui_info("AUiJukeBoxViewBinder deinit", tag: "AUiJukeBoxViewBinder")
    }
    
    public override init() {
        super.init()
        aui_info("AUiJukeBoxViewBinder init", tag: "AUiJukeBoxViewBinder")
    }
    
    public func bind(jukeBoxView: AUiJukeBoxView, service: AUiMusicServiceDelegate) {
        self.jukeBoxView = jukeBoxView
        self.serviceDelegate = service
    }
}

extension AUiJukeBoxViewBinder: AUiJukeBoxViewDelegate {

    public func cleanSearchText(view: AUiJukeBoxView) {
        self.searchMusicList = nil
    }
    
    public func search(view: AUiJukeBoxView, text: String, completion: @escaping ([AUiJukeBoxItemDataProtocol]?)->()) {
        serviceDelegate?.searchMusic(keyword: text, page: 1, pageSize: kListPageCount, completion: {[weak self] error, list in
            guard let self = self else {return}
            if let err = error {
                AUiToast.show(text:err.localizedDescription)
                return
            }
            self.searchMusicList = list ?? []
            completion(list)
        })
    }
    
    public func onSegmentedChanged(view: AUiJukeBoxView, segmentIndex: Int) -> Bool {
        return false
    }
    
    public func onTabsDidChanged(view: AUiJukeBoxView, tabIndex: Int) -> Bool {
        return false
    }
    
    public func onSelectSong(view: AUiJukeBoxView, tabIndex: Int, index: Int) {
        guard let model = searchMusicList == nil ? musicListMap[tabIndex]?[index] : searchMusicList?[index] else {return}
        self.serviceDelegate?.chooseSong(songModel: model, completion: { error in
            guard let err = error else {return}
            AUiToast.show(text:err.localizedDescription)
        })
    }
    
    public func onRemoveSong(view: AUiJukeBoxView, index: Int) {
        let song = self.addedMusicList[index]
        self.serviceDelegate?.removeSong(songCode: song.songCode, completion: { error in
            guard let err = error else {return}
            AUiToast.show(text:err.localizedDescription)
        })
    }
    
    public func onNextSong(view: AUiJukeBoxView, index: Int) {
        AUiAlertView.theme_defaultAlert()
            .isShowCloseButton(isShow: false)
            .title(title: aui_localized("switchToNextSong"))
            .rightButton(title: "确认")
            .rightButtonTapClosure(onTap: {[weak self] text in
                guard let self = self else { return }
                self.onRemoveSong(view: view, index: index)
            })
            .leftButton(title: "取消")
            .show()
    }
    
    public func onPinSong(view: AUiJukeBoxView, index: Int) {
        let song = self.addedMusicList[index]
        self.serviceDelegate?.pinSong(songCode: song.songCode, completion: { error in
            guard let err = error else {return}
            AUiToast.show(text:err.localizedDescription)
        })
    }
    
    public func songIsSelected(view: AUiJukeBoxView, songCode: String) -> Bool {
        return addedMusicSet.contains(songCode)
    }
    
    public func pingEnable(view: AUiJukeBoxView, songCode: String) -> Bool {
        return pinEnableSet.contains(songCode)
    }
    
    public func deleteEnable(view: AUiJukeBoxView, songCode: String) -> Bool {
        return deleteEnableSet.contains(songCode)
    }
    
    public func getSearchMusicList(view: AUiJukeBoxView) -> [AUiJukeBoxItemDataProtocol]? {
        return self.searchMusicList
    }
    
    public func getMusicList(view: AUiJukeBoxView, tabIndex: Int) -> [AUiJukeBoxItemDataProtocol] {
        return self.musicListMap[tabIndex] ?? []
    }
    
    public func getSelectedSongList(view: AUiJukeBoxView) -> [AUiJukeBoxItemSelectedDataProtocol] {
        return self.addedMusicList
    }
    
    public func onRefreshMusicList(view: AUiJukeBoxView, tabIndex: Int, completion: @escaping ([AUiJukeBoxItemDataProtocol]?)->()) {
        let idx = tabIndex
        aui_info("onRefreshMusicList tabIndex: \(idx)", tag: "AUiJukeBoxViewBinder")
        self.serviceDelegate?.getMusicList(chartId: kChartIds[idx],
                                           page: 1,
                                           pageSize: kListPageCount,
                                           completion: {[weak self] error, list in
            guard let self = self else {return}
            if let err = error {
                AUiToast.show(text:err.localizedDescription)
                return
            }
            self.musicListMap[idx] = list ?? []
            completion(list)
        })
    }
    
    public func onLoadMoreMusicList(view: AUiJukeBoxView, tabIndex: Int, completion: @escaping ([AUiJukeBoxItemDataProtocol]?)->()) {
        let idx = tabIndex
        let musicListCount = musicListMap[idx]?.count ?? 0
        let page = 1 + musicListCount / kListPageCount
        if musicListCount % kListPageCount > 0 {
            //no more data
            completion(nil)
            return
        }
        aui_info("onLoadMoreMusicList tabIndex: \(idx) page: \(page)", tag: "AUiJukeBoxViewBinder")
        self.serviceDelegate?.getMusicList(chartId: kChartIds[idx],
                                           page: page,
                                           pageSize: kListPageCount,
                                           completion: {[weak self] error, list in
            guard let self = self else {return}
            if let err = error {
                AUiToast.show(text:err.localizedDescription)
                return
            }
            if let list = list {
                let musicList = self.musicListMap[idx] ?? []
                self.musicListMap[idx] = musicList + list
            }
            completion(list)
        })
    }
    
    public func onRefreshAddedMusicList(view: AUiJukeBoxView, completion: @escaping ([AUiJukeBoxItemSelectedDataProtocol]?) -> ()) {
        aui_info("onRefreshAddedMusicList", tag: "AUiJukeBoxViewBinder")
        self.serviceDelegate?.getAllChooseSongList(completion: { [weak self] error, list in
            guard let self = self else {return}
            if let err = error {
                AUiToast.show(text:err.localizedDescription)
                return
            }
            self.addedMusicList = list ?? []
            completion(list)
        })
    }
}


extension AUiJukeBoxViewBinder: AUiMusicRespDelegate {
    
    public func _notifySongDidAdded(song: AUiChooseMusicModel) {
        addedMusicSet.add(song.songCode)
        if serviceDelegate?.currentUserIsRoomOwner() ?? false == true {
            deleteEnableSet.add(song.songCode)
            pinEnableSet.add(song.songCode)
        } else if song.owner?.userId == serviceDelegate?.getRoomContext().commonConfig?.userId {
            deleteEnableSet.add(song.songCode)
        }
    }
    public func _notifySongDidRemove(song: AUiChooseMusicModel) {
        addedMusicSet.remove(song.songCode)
        deleteEnableSet.remove(song.songCode)
        pinEnableSet.remove(song.songCode)
    }
    
    public func onAddChooseSong(song: AUiChooseMusicModel) {
        aui_info("onAddChooseSong \(song.name)", tag: "AUiJukeBoxViewBinder")
        addedMusicList.append(song)
        _notifySongDidAdded(song: song)
        jukeBoxView?.addedMusicTableView.reloadData()
        jukeBoxView?.allMusicTableView.reloadData()
    }
    
    public func onRemoveChooseSong(song: AUiChooseMusicModel) {
        aui_info("onRemoveChooseSong \(song.name)", tag: "AUiJukeBoxViewBinder")
        self.addedMusicList.removeAll(where: {$0.songCode == song.songCode})
        _notifySongDidRemove(song: song)
        self.jukeBoxView?.addedMusicTableView.reloadData()
        self.jukeBoxView?.allMusicTableView.reloadData()
    }
    
    public func onUpdateChooseSong(song: AUiChooseMusicModel) {
        aui_info("onUpdateChooseSong \(song.name)", tag: "AUiJukeBoxViewBinder")
        if let index = self.addedMusicList.firstIndex(where: {$0.songCode == song.songCode}) {
            self.addedMusicList.remove(at: index)
            self.addedMusicList.insert(song, at: index)
        } else {
            self.addedMusicList.append(song)
            _notifySongDidAdded(song: song)
        }
        self.jukeBoxView?.addedMusicTableView.reloadData()
        self.jukeBoxView?.allMusicTableView.reloadData()
    }
    
    public func onUpdateAllChooseSongs(songs: [AUiChooseMusicModel]) {
        aui_info("onUpdateAllChooseSongs", tag: "AUiJukeBoxViewBinder")
        self.addedMusicList = songs
        addedMusicSet.removeAllObjects()
        pinEnableSet.removeAllObjects()
        deleteEnableSet.removeAllObjects()
        addedMusicList.forEach { song in
            self._notifySongDidAdded(song: song)
        }
        self.jukeBoxView?.addedMusicTableView.reloadData()
        self.jukeBoxView?.allMusicTableView.reloadData()
    }
}

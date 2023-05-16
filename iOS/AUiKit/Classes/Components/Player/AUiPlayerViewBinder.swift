//
//  AUiPlayerViewBinder.swift
//  AUiKit
//
//  Created by wushengtao on 2023/4/11.
//

import Foundation
import AgoraRtcKit

open class AUiPlayerViewBinder: NSObject {
    
    var singerRole: KTVSingRole = .audience {
        didSet {
            print("current:Role: \(singerRole.rawValue)")
            //根据用户角色更新UI按钮显示
            updateBtnsWithRole()
        }
    }
    
    var isMainSinger: Bool = false
    var isOnSeat: Bool = false
    
    var currentSong: AUiChooseMusicModel?
    var preparedToCosinger: Bool = false
    var coSingerCount: Int = 0 {
        didSet {
            print("current:\(coSingerCount)")
        }
    }
    
    private var micSeatArray: [AUiMicSeatInfo] = []
    
    private var cosingerDegree: Int = 0
    
    private var playerVol: CGFloat = 0.5
    private var audioPitch: CGFloat = 0
    private var recordVol: CGFloat = 1
    private var enableEarMonitoring: Bool = false
    
    private weak var playerView: AUiPlayerView? {
        didSet {
//            playerView?.uiDelegate = self
        }
    }
    private weak var musicServiceDelegate: AUiMusicServiceDelegate? {
        didSet {
            oldValue?.unbindRespDelegate(delegate: self)
            musicServiceDelegate?.bindRespDelegate(delegate: self)
        }
    }
    
    private weak var micSeatServiceDelegate: AUiMicSeatServiceDelegate? {
        didSet {
            oldValue?.unbindRespDelegate(delegate: self)
            micSeatServiceDelegate?.bindRespDelegate(delegate: self)
        }
    }
    
    private weak var playerServiceDelegate: AUiPlayerServiceDelegate? {
        didSet {
            oldValue?.unbindRespDelegate(delegate: self)
            playerServiceDelegate?.bindRespDelegate(delegate: self)
        }
    }
    
    private weak var chorusServiceDelegate: AUiChorusServiceDelegate? {
        didSet {
            oldValue?.unbindRespDelegate(delegate: self)
            chorusServiceDelegate?.bindRespDelegate(delegate: self)
        }
    }
    
     public override init() {
        super.init()
        for i in 0...(kMicSeatCount - 1) {
            let seatInfo = AUiMicSeatInfo()
            seatInfo.seatIndex = UInt(i)
            micSeatArray.append(seatInfo)
        }
    }
    
    public func bind(playerView: AUiPlayerView,
              playerService: AUiPlayerServiceDelegate,
              micSeatService: AUiMicSeatServiceDelegate,
              musicService: AUiMusicServiceDelegate,
              chorusService: AUiChorusServiceDelegate) {
        self.playerView = playerView
        self.musicServiceDelegate = musicService
        self.micSeatServiceDelegate = micSeatService
        self.playerServiceDelegate = playerService
        self.chorusServiceDelegate = chorusService
        self.playerView?.addActionHandler(playerViewActionHandler: self)
        
        playerService.setLrcView(delegate: playerView.karaokeLrcView)
        playerService.addEventHandler(ktvApiEventHandler: self)
        playerView.delegate = self
        playerView.karaokeLrcView.delegate = self
        
        playerView.karaokeLrcView.skipCallBack = {[weak self] time, flag in
            guard let self = self,
                  let mpk = self.playerServiceDelegate?.getMusicPlayer() else {return}
            
            let seekTime = flag ? mpk.getDuration() - 800 : time
            mpk.seek(toPosition: seekTime)
        }
        playerView.karaokeLrcView.showSkipCallBack = {[weak self] type in
            if type == .epilogue {
                self?.playerView?.karaokeLrcView.showEpilogue(enable: true)
            } else {
                self?.playerView?.karaokeLrcView.showPreludeEnd(enable: false)
            }
        }
    }
    
    private func updateBtnsWithRole() {
        playerView?.updateBtns(with: singerRole, isMainSinger: isMainSinger, isOnSeat: isOnSeat)
    }
}

extension AUiPlayerViewBinder: AUiPlayerViewDelegate {
    public func onButtonTapAction(playerView: AUiPlayerView, actionType: AUiPlayerViewButtonType) {
        if actionType == .acc {
            playerServiceDelegate?.selectMusicPlayerTrackMode(mode: .acc)
        } else if actionType == .original {
            playerServiceDelegate?.selectMusicPlayerTrackMode(mode: .origin)
        } else if actionType == .play {
            playerServiceDelegate?.resumeSing()
        } else if actionType == .pause {
            playerServiceDelegate?.pauseSing()
            
        } else if actionType == .nextSong {
            musicServiceDelegate?.removeSong(songCode: currentSong?.songCode ?? "0", completion: { error in
                
            })
        }
    }
    
    
    private func effectItemClickAction(index: Int) {
        let effects: [AgoraAudioEffectPreset] = [.off,.roomAcousticsKTV, .roomAcousVocalConcer, .roomAcousStudio, .roomAcousPhonograph, .roomAcousSpatial, .roomAcousEthereal, .styleTransformationPopular, .styleTransformationRnb]
        playerServiceDelegate?.setAudioEffectPreset(present: effects[index])
    }

    public func onVoiceConversionDidChanged( index: Int) {
        var effect: AgoraAudioEffectPreset = .off
        switch index {
            case 0:
                effect = .off
            case 1:
                effect = .voiceChangerEffectBoy
            case 2:
                effect = .voiceChangerEffectGirl
            case 3:
                effect = .voiceChangerEffectUncle
            case 4:
                effect = .roomAcousEthereal
        default:
            effect = .off
        }
        playerServiceDelegate?.setAudioEffectPreset(present: effect)
        playerView?.voiceConversionIdx = index
    }
    
    //音乐 人声 升降调
    public func onSliderValueDidChanged(value: CGFloat, item: AUiPlayerAudioSettingItem) {
        let type = item.uniqueId
        switch type {
        case 1://音乐音量
            playerServiceDelegate?.adjustMusicPlayerPlayoutVolume(volume:  Int(value * 100))
            playerServiceDelegate?.adjustMusicPlayerPublishVolume(volume: Int(value * 100))
            playerVol = value
        case 2://人声音量
            playerServiceDelegate?.adjustRecordingSignalVolume(volume: Int(value * 100))
            recordVol = value
        case 3://升降调
            playerServiceDelegate?.setAudioPitch(pitch: Int(value * 100))
            audioPitch = value
        default:
            break
        }
    }
    
    //耳返
    public func onSwitchValueDidChanged(isSwitch: Bool, item: AUiPlayerAudioSettingItem) {
        playerServiceDelegate?.enableEarMonitoring(inEarMonitoring: isSwitch)
        enableEarMonitoring = isSwitch
    }
    
    //混音
    public func onAudioMixDidChanged(audioMixIndex: Int) {
        effectItemClickAction(index: audioMixIndex)
        playerView?.audioMixinIdx = audioMixIndex
    }
    
    public func onSliderCellWillLoad(playerView: AUiPlayerAudioSettingView, item: AUiPlayerAudioSettingItem) {
        if item.uniqueId == 1 {//伴奏音量
            item.sliderCurrentValue = playerVol
        } else if item.uniqueId == 2 {//人声
            item.sliderCurrentValue = recordVol
        } else if item.uniqueId == 3 {//升降调
            item.sliderCurrentValue = audioPitch
        }
    }
    
    public func onSwitchCellWillLoad(playerView: AUiPlayerAudioSettingView, item: AUiPlayerAudioSettingItem) {
        item.aui_isSwitchOn = enableEarMonitoring
    }
}

//MARK: AUiMusicServiceDelegate
extension AUiPlayerViewBinder: AUiMusicRespDelegate {
    public func onAddChooseSong(song: AUiChooseMusicModel) {
       
    }
    
    public func onRemoveChooseSong(song: AUiChooseMusicModel) {
    }
    
    public func onUpdateChooseSong(song: AUiChooseMusicModel) {
        aui_info("onUpdateChooseSong \(song.name) isPlayer:\(song.isPlaying)", tag: "AUiPlayerViewBinder")
        
    }
    
    public func onUpdateAllChooseSongs(songs: [AUiChooseMusicModel]) {
        playerView?.originalButton.isSelected = false
        playerView?.playOrPauseButton.isSelected = false
        playerView?.karaokeLrcView.resetShowOnce()
        guard let topSong = songs.first else {
            playerView?.musicInfo = nil
            currentSong = nil
            playerView?.karaokeLrcView.resetLrc()
            playerServiceDelegate?.stopSing()
            return
        }
        if currentSong?.songCode != topSong.songCode {
            cosingerDegree = 0
            playerView?.karaokeLrcView.resetScore()
            playerView?.karaokeLrcView.resetLrc()
            //切歌之后先把自己变成观众
            singerRole = .audience
            playerServiceDelegate?.switchSingerRole(newRole: .audience, onSwitchRoleState: { state, reason in
                
            })
            playerView?.musicInfo = topSong
            currentSong = topSong
            handleKtvLogic(with: topSong)
        }
    }
    
    //处理各种身份的加入合唱逻辑
    private func handleKtvLogic(with song: AUiChooseMusicModel) {
        //判断当前用户是否是点歌者
        let songOwnerUid = currentSong?.owner?.userId
        guard let commonConfig = AUiRoomContext.shared.commonConfig else {return}
        let local = commonConfig.userId
        let isSongOwner = local == songOwnerUid
        self.singerRole = isSongOwner ? .soloSinger : .audience
        playerView?.joinState = isSongOwner ? .none : .before
        
        if isSongOwner {
            mainSingerJoinKtv(with: song)
        } else {
            audienceJoinKtv(with: song)
        }
    }
    
    private func mainSingerJoinKtv(with song: AUiChooseMusicModel) {
        let config = KTVSongConfiguration()
        config.autoPlay = true
        config.mainSingerUid = Int(song.owner?.userId ?? "0") ?? 0
        config.mode = .loadMusicAndLrc
        config.songIdentifier = song.songCode
        playerServiceDelegate?.loadMusic(songCode: Int(song.songCode) ?? 0, config: config, musicLoadStateListener: self)
        singerRole = .soloSinger
        playerServiceDelegate?.switchSingerRole(newRole: singerRole, onSwitchRoleState: { state, error in
            
        })
        
    }
    
    private func audienceJoinKtv(with song: AUiChooseMusicModel) {
        let config = KTVSongConfiguration()
        config.autoPlay = false
        config.mainSingerUid = Int(song.owner?.userId ?? "0") ?? 0
        config.mode = .loadLrcOnly
        config.songIdentifier = song.songCode
        playerServiceDelegate?.loadMusic(songCode: Int(song.songCode) ?? 0, config: config, musicLoadStateListener: self)
        singerRole = .audience
    }
    
    private func audienceToCosinger(with song: AUiChooseMusicModel) {
        let config = KTVSongConfiguration()
        config.autoPlay = false
        config.mainSingerUid = Int(song.owner?.userId ?? "0") ?? 0
        config.mode = .loadMusicOnly
        config.songIdentifier = song.songCode
        playerServiceDelegate?.loadMusic(songCode: Int(song.songCode) ?? 0, config: config, musicLoadStateListener: self)
    }

}

extension AUiPlayerViewBinder: AUiLrcViewDelegate {
    func onKaraokeView( didDragTo position: Int) {
        //歌词滚动
        playerServiceDelegate?.seekSing(time: position)
    }
    
    func onKaraokeView(score: Int, totalScore: Int, lineScore: Int, lineIndex: Int) {
        //更新分数
        if singerRole == .audience {return}
        let realScore = singerRole == .coSinger ? cosingerDegree + lineScore : score
        playerView?.karaokeLrcView.updateScore(with: lineScore, cumulativeScore: realScore, totalScore: totalScore)
        if singerRole == .leadSinger || singerRole == .soloSinger {
           //主唱发送分数给观众同步
            let dict: [String: Any] = [ "cmd": "singleLineScore",
                                       "score": lineScore,
                                       "index": lineIndex,
                                       "cumulativeScore": score,
                                       "total": totalScore]
            playerServiceDelegate?.sendStreamMsg(with: dict)
        } else {
            cosingerDegree += lineScore
        }
    }
}

extension AUiPlayerViewBinder: AUiMicSeatRespDelegate {
    public func onAnchorEnterSeat(seatIndex: Int, user: AUiUserThumbnailInfo) {
        aui_info("onAnchorEnterSeat: \(seatIndex)", tag: "AUiPlayerViewBinder")
        let micSeat = micSeatArray[seatIndex]
        micSeat.user = user
        micSeatArray[seatIndex] = micSeat
        
        
        if seatIndex == 0 {
            isMainSinger = user.userId == getLocalUserId() ?? ""
        }
        
        if user.userId == getLocalUserId() ?? "" {
            isOnSeat = true
        }
        
        playerView?.selectSongBtnNeedHidden = !isOnSeat
        playerView?.musicInfo = currentSong
    }
    
    public func onAnchorLeaveSeat(seatIndex: Int, user: AUiUserThumbnailInfo) {
        aui_info("onAnchorLeaveSeat: \(seatIndex)", tag: "AUiPlayerViewBinder")
        let micSeat = micSeatArray[seatIndex]
        micSeat.user = nil
        micSeatArray[seatIndex] = micSeat

        guard let userId = getLocalUserId() else {
            return
        }
        if userId == user.userId {
            isOnSeat = false
            //下麦需要设置成观众
            singerRole = .audience
            playerView?.joinState = currentSong == nil ? .none : .before
            playerView?.selectSongBtnNeedHidden = !isOnSeat
            playerView?.musicInfo = currentSong
            playerServiceDelegate?.switchSingerRole(newRole: .audience, onSwitchRoleState: { state, reason in
            })
        }
    }
    
    public func onSeatAudioMute(seatIndex: Int, isMute: Bool) {

    }
    
    public func onSeatVideoMute(seatIndex: Int, isMute: Bool) {
  
    }
    
    public func onSeatClose(seatIndex: Int, isClose: Bool) {
        aui_info("onSeatClose: \(seatIndex)", tag: "AUiPlayerViewBinder")
        let micSeat = micSeatArray[seatIndex]
        micSeat.lockSeat = isClose ? AUiLockSeatStatus.locked : AUiLockSeatStatus.idle
        micSeatArray[seatIndex] = micSeat
    }
}

//MARK: AUiPlayerServiceDelegate
extension AUiPlayerViewBinder: AUiPlayerRespDelegate {
    public func onPreludeDidAppear() {
        
    }
    
    public func onPreludeDidDisappear() {
        
    }
    
    public func onPostludeDidAppear() {
        
    }
    
    public func onPostludeDidDisappear() {
        
    }
    
    public func onPlayerPositionDidChange(position: Int) {
        
    }
    
    public func onPlayerStateChanged(state: AgoraMediaPlayerState, isLocal: Bool) {
        
    }
    
    public func onDataStreamMsgReceived(with uid: NSInteger, streamId: NSInteger, data: Data) {
        guard let dict: [String: Any] = dataToDictionary(data: data) else {return}
        if let cmd = dict["cmd"] as? String,
            cmd == "singleLineScore",
            let index = dict["index"] as? Int,
            let score = dict["score"] as? Int,
            let cumulativeScore = dict["cumulativeScore"] as? Int,
            let total = dict["total"] as? Int {
            guard singerRole == .audience else { return }
            DispatchQueue.main.async {
                self.playerView?.karaokeLrcView.updateScore(with: score, cumulativeScore: cumulativeScore, totalScore: total)
            }
        }
    }
    
    private func dataToDictionary(data: Data) -> [String: Any]? {
        do {
            let json = try JSONSerialization.jsonObject(with: data, options: [])
            return json as? [String: Any]
        } catch {
            aui_info("Error decoding data: \(error.localizedDescription)", tag: "AUiPlayerViewBinder")
            return nil
        }
    }
}

//MARK: AUiChorusRespDelegate
extension AUiPlayerViewBinder: AUiChorusRespDelegate {
    public func onChoristerDidEnter(chorister: AUiChoristerModel) {
        coSingerCount += 1
        if singerRole == .soloSinger && coSingerCount == 1 {
            playerServiceDelegate?.switchSingerRole(newRole: .leadSinger, onSwitchRoleState: {[weak self] state, reason in
                self?.singerRole = .leadSinger
            })
        }
    }
    
    public func onChoristerDidLeave(chorister: AUiChoristerModel) {
        coSingerCount -= 1
        if coSingerCount == 0 && singerRole == .leadSinger {
            playerServiceDelegate?.switchSingerRole(newRole: .soloSinger, onSwitchRoleState: {[weak self] state, reason in
                self?.singerRole = .soloSinger
            })
        } else if chorister.userId == getLocalUserId() {
            playerServiceDelegate?.switchSingerRole(newRole: .audience, onSwitchRoleState: { state, reason in
            })
        }
    }
}

extension AUiPlayerViewBinder: KTVApiEventHandlerDelegate {
    public func onMusicPlayerStateChanged(state: AgoraMediaPlayerState, error: AgoraMediaPlayerError, isLocal: Bool) {
        if state == .playBackCompleted || state == .playBackAllLoopsCompleted {
            if isLocal {
                musicServiceDelegate?.removeSong(songCode: currentSong?.songCode ?? "", completion: { error in
                    //自动切歌成功
                })
            }
        } else if state == .playing && (singerRole == .soloSinger || singerRole == .leadSinger) {
            DispatchQueue.main.async {
                self.playerView?.karaokeLrcView.showPreludeEnd(enable: true)
            }
        } else if state == .paused && (singerRole == .soloSinger || singerRole == .leadSinger)  {
            DispatchQueue.main.async {
                self.playerView?.karaokeLrcView.hideSkipView(flag: true)
            }
        }
        
    }
    
    public func onSingingScoreResult(score: Float) {
        
    }
    
    public func onSingerRoleChanged(oldRole: KTVSingRole, newRole: KTVSingRole) {
        
    }
    
    public func onChorusChannelTokenPrivilegeWillExpire(token: String?) {
        
    }
}

extension AUiPlayerViewBinder: IMusicLoadStateListener {
    public func onMusicLoadProgress(songCode: Int, percent: Int, status: AgoraMusicContentCenterPreloadStatus, msg: String?, lyricUrl: String?) {
        if singerRole == .soloSinger {
            playerView?.updateLoadingView(with: status == .OK ? 100 : percent)
        }
    }
    
    public func onMusicLoadSuccess(songCode: Int, lyricUrl: String) {
        aui_info("load music success", tag: "AUiPlayerViewBinder")
        //想办法去掉preparedToCosinger
        if preparedToCosinger {
            playerServiceDelegate?.switchSingerRole(newRole: .coSinger, onSwitchRoleState: {[weak self] state, reason in
                aui_info("switch role state:\(state) reason:\(reason.rawValue)", tag: "AUiPlayerViewBinder")
                guard let self = self else {return}
                self.playerView?.joinState = .after
                self.singerRole = .coSinger
                self.preparedToCosinger = false
                self.playerServiceDelegate?.startSing(songCode: songCode)
            })
        }
        
        if singerRole == .audience  {
            playerServiceDelegate?.startSing(songCode: songCode)
        }
        
        playerView?.updateLoadingView(with: 100)
    }
    
    public func onMusicLoadFail(songCode: Int, reason: KTVLoadSongFailReason) {
        aui_info("load music failed", tag: "AUiPlayerViewBinder")
        if preparedToCosinger {
            singerRole = .audience
            playerView?.joinState = .before
            preparedToCosinger = false
        }
        playerView?.updateLoadingView(with: 100)
    }
}

//加入合唱，离开合唱
extension AUiPlayerViewBinder: AUiKaraokeLrcViewDelegate {
    
    func didJoinChorus() {
        if isOnSeat {
            _joinChorus()
            return
        }
        
        var idleIndex = 0
        for i in 1...7 {
            let seat = micSeatArray[i]
            if seat.user?.userId.count ?? 0 == 0, seat.lockSeat == .idle {
                idleIndex = i
                break
            }
        }
        guard idleIndex > 0 else {
            //TODO(chenpan): 麦位提示
            AUiToast.show(text: "麦位已满")
            return
        }
        aui_info("join chorus after enterSeat: \(idleIndex)", tag: "AUiPlayerViewBinder")
        micSeatServiceDelegate?.enterSeat(seatIndex: idleIndex, callback: {[weak self] error in
            if let error = error {
                AUiToast.show(text: error.localizedDescription)
                return
            }
            self?._joinChorus()
        })
    }
    
    private func _joinChorus() {
        guard let currentSong = currentSong else {return}
        
        playerView?.joinState = .loding
        preparedToCosinger = true
        guard let local = getLocalUserId() else {return}

        chorusServiceDelegate?.joinChorus(songCode: currentSong.songCode, userId: local, completion: {[weak self] error in
            self?.isOnSeat = true
            self?.audienceToCosinger(with: currentSong)
        })
        
    }
    
    func didLeaveChorus() {
        guard let currentSong = currentSong else {return}
        playerView?.joinState = .before
        guard let local = getLocalUserId() else {return}
        leaveChorus(uid: local, currentSong: currentSong)
    }
    
    private func leaveChorus(uid: String?, currentSong: AUiChooseMusicModel) {
        chorusServiceDelegate?.leaveChorus(songCode: currentSong.songCode, userId: uid, completion: {[weak self] error in
            self?.singerRole = .audience
            self?.audienceJoinKtv(with: currentSong)
        })
    }
    
    func getLocalUserId() -> String? {
        guard let commonConfig = AUiRoomContext.shared.commonConfig else {return nil}
        return commonConfig.userId
    }
}





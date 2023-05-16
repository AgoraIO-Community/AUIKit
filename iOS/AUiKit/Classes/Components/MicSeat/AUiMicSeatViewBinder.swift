//
//  AUiMicSeatViewBinder.swift
//  AUiKit
//
//  Created by wushengtao on 2023/4/6.
//

import UIKit
import AgoraRtcKit

public class AUiMicSeatViewBinder: NSObject {
    private var micSeatArray: [AUiMicSeatInfo] = []
    private var userMap: [String: AUiUserInfo] = [:]
    private var rtcEngine: AgoraRtcEngineKit!
    private weak var micSeatView: AUiMicSeatView?
    private weak var micSeatDelegate: AUiMicSeatServiceDelegate? {
        didSet {
            micSeatDelegate?.unbindRespDelegate(delegate: self)
            micSeatDelegate?.bindRespDelegate(delegate: self)
        }
    }
    private weak var userDelegate: AUiUserServiceDelegate? {
        didSet {
            userDelegate?.unbindRespDelegate(delegate: self)
            userDelegate?.bindRespDelegate(delegate: self)
        }
    }
    private weak var musicDelegate: AUiMusicServiceDelegate? {
        didSet {
            musicDelegate?.unbindRespDelegate(delegate: self)
            musicDelegate?.bindRespDelegate(delegate: self)
        }
    }
    private weak var chorusDelegate: AUiChorusServiceDelegate? {
        didSet {
            chorusDelegate?.unbindRespDelegate(delegate: self)
            chorusDelegate?.bindRespDelegate(delegate: self)
        }
    }
    
    public convenience init(rtcEngine: AgoraRtcEngineKit) {
        self.init()
        self.rtcEngine = rtcEngine
        for i in 0...(kMicSeatCount - 1) {
            let seatInfo = AUiMicSeatInfo()
            seatInfo.seatIndex = UInt(i)
            micSeatArray.append(seatInfo)
        }
    }
    
    public func bind(micSeatView: AUiMicSeatView,
                     micSeatService: AUiMicSeatServiceDelegate,
                     userService: AUiUserServiceDelegate,
                     musicSeatService: AUiMusicServiceDelegate,
                     chorusService: AUiChorusServiceDelegate) {
        self.micSeatView = micSeatView
        micSeatView.uiDelegate = self
        self.micSeatDelegate = micSeatService
        self.userDelegate = userService
        self.musicDelegate = musicSeatService
        self.chorusDelegate = chorusService
    }
    
    private func enterDialogItem(seatInfo: AUiMicSeatInfo, callback: @escaping ()->()) -> AUiActionSheetItem {
        let item = AUiActionSheetThemeItem()
        item.title = aui_localized("enterSeat")
        item.titleColor = "ActionSheet.normalColor"
        item.callback = { [weak self] in
            self?.micSeatDelegate?.enterSeat(seatIndex: Int(seatInfo.seatIndex), callback: { err in
                guard let err = err else {return}
                AUiToast.show(text: err.localizedDescription)
            })
            callback()
        }
        return item
    }
    
    private func kickDialogItem(seatInfo: AUiMicSeatInfo, callback: @escaping ()->()) -> AUiActionSheetItem {
        let item = AUiActionSheetThemeItem()
        item.title = aui_localized("kickSeat")
        item.titleColor = "ActionSheet.normalColor"
        item.callback = { [weak self] in
            self?.micSeatDelegate?.kickSeat(seatIndex: Int(seatInfo.seatIndex),
                                            callback: { error in
                guard let err = error else {return}
                AUiToast.show(text: err.localizedDescription)
            })
            callback()
        }
        return item
    }
    
    private func leaveDialogItem(seatInfo: AUiMicSeatInfo, callback: @escaping ()->()) -> AUiActionSheetItem {
        let item = AUiActionSheetThemeItem()
        item.title = aui_localized("leaveSeat")
        item.icon = "ActionSheetCell.normalIcon"
        item.titleColor = "ActionSheet.normalColor"
        item.callback = { [weak self] in
            self?.micSeatDelegate?.leaveSeat(callback: { error in
                guard let err = error else {return}
                AUiToast.show(text: err.localizedDescription)
            })
            callback()
        }
        return item
    }
    
    private func muteAudioDialogItem(seatInfo: AUiMicSeatInfo, callback: @escaping ()->()) ->AUiActionSheetItem {
        let item = AUiActionSheetThemeItem()
        item.title = seatInfo.muteAudio ? aui_localized("unmuteAudio") : aui_localized("muteAudio")
//        item.icon = "ActionSheetCell.warnIcon"
        item.titleColor = "ActionSheet.normalColor"
        item.callback = { [weak self] in
            self?.micSeatDelegate?.muteAudioSeat(seatIndex: Int(seatInfo.seatIndex),
                                                 isMute: !seatInfo.muteAudio,
                                                 callback: { error in
                guard let err = error else {return}
                AUiToast.show(text: err.localizedDescription)
            })
            callback()
        }
        
        return item
    }
    
    private func closeDialogItem(seatInfo: AUiMicSeatInfo, callback: @escaping ()->()) -> AUiActionSheetItem {
        let item = AUiActionSheetThemeItem()
        item.title = seatInfo.lockSeat == .locked ? aui_localized("closeSeat") : aui_localized("openSeat")
        item.titleColor = "CommonColor.danger"
        item.callback = { [weak self] in
            self?.micSeatDelegate?.closeSeat(seatIndex: Int(seatInfo.seatIndex),
                                             isClose: seatInfo.lockSeat != .locked,
                                             callback: { error in
                guard let err = error else {return}
                AUiToast.show(text: err.localizedDescription)
            })
            callback()
        }
        
        return item
    }
    
    private func lockDialogItem(seatInfo: AUiMicSeatInfo, callback: @escaping ()->()) -> AUiActionSheetItem {
        let item = AUiActionSheetThemeItem()
        item.title = seatInfo.lockSeat == .locked ? aui_localized("unlockSeat") : aui_localized("lockSeat")
        item.titleColor = "CommonColor.danger"
        item.callback = { [weak self] in
            self?.micSeatDelegate?.closeSeat(seatIndex: Int(seatInfo.seatIndex),
                                             isClose: seatInfo.lockSeat != .locked,
                                             callback: { error in
                guard let err = error else {return}
                AUiToast.show(text: err.localizedDescription)
            })
            callback()
        }
        
        return item
    }
    
    public func getDialogItems(seatInfo: AUiMicSeatInfo, callback: @escaping ()->()) ->[AUiActionSheetItem] {
        var items = [AUiActionSheetItem]()
        
        let channelName: String = micSeatDelegate?.getChannelName() ?? ""
        let currentUserId: String = micSeatDelegate?.getRoomContext().currentUserInfo.userId ?? ""
        //当前麦位用户是否自己
        let isCurrentUser: Bool = seatInfo.user?.userId == currentUserId
        //是否空麦位
        let isEmptySeat: Bool = seatInfo.user == nil || seatInfo.user?.userId.count == 0
        //是否房主
        let isRoomOwner: Bool = micSeatDelegate?.getRoomContext().isRoomOwner(channelName: channelName) ?? false
        //当前用户是否在麦位上
        let currentUserAlreadyEnterSeat: Bool = micSeatArray.filter {$0.user?.userId == currentUserId}.count > 0 ? true : false
        if isRoomOwner {
            if isEmptySeat {
                items.append(muteAudioDialogItem(seatInfo: seatInfo, callback:callback))
                items.append(lockDialogItem(seatInfo: seatInfo, callback:callback))
            } else {
                if isCurrentUser {
                    items.append(muteAudioDialogItem(seatInfo: seatInfo, callback:callback))
                } else {  //other user
                    items.append(kickDialogItem(seatInfo: seatInfo, callback:callback))
                    items.append(muteAudioDialogItem(seatInfo: seatInfo, callback:callback))
                    items.append(closeDialogItem(seatInfo: seatInfo, callback:callback))
                }
            }
        } else {
            if isEmptySeat {
                if currentUserAlreadyEnterSeat {
                } else {
                    items.append(enterDialogItem(seatInfo: seatInfo, callback:callback))
                }
            } else {
                if isCurrentUser {
                    items.append(leaveDialogItem(seatInfo: seatInfo, callback:callback))
                    items.append(muteAudioDialogItem(seatInfo: seatInfo, callback:callback))
                } else {  //other user
                }
            }
        }
        
        return items
    }
}

extension AUiMicSeatViewBinder: AUiMicSeatRespDelegate {
    
    public func onAnchorEnterSeat(seatIndex: Int, user: AUiUserThumbnailInfo) {
        aui_info("onAnchorEnterSeat seat: \(seatIndex)", tag: "AUiMicSeatViewBinder")
        let micSeat = micSeatArray[seatIndex]
        if let fullUser = userMap[user.userId] {
            micSeat.user = fullUser
        } else {
            micSeat.user = user
        }
        micSeatArray[seatIndex] = micSeat
        micSeatView?.collectionView.reloadItems(at: [IndexPath(item: seatIndex, section: 0)])
  
        updateMic(with: seatIndex, role: .onlineAudience)

        //current user enter seat
        if user.userId == micSeatDelegate?.getRoomContext().commonConfig?.userId {
            let mediaOption = AgoraRtcChannelMediaOptions()
            mediaOption.clientRoleType = .broadcaster
            mediaOption.publishMicrophoneTrack = true
            rtcEngine.updateChannel(with: mediaOption)
            aui_info("update clientRoleType: \(mediaOption.clientRoleType.rawValue)", tag: "AUiMicSeatViewBinder")
            return
        }
        
        guard let uid = UInt(micSeat.user?.userId ?? "") else {
            return
        }
        aui_info("mute audio uid: \(uid) isMute: \(micSeat.muteAudio)", tag: "AUiMicSeatViewBinder")
        self.rtcEngine.muteRemoteAudioStream(uid, mute: micSeat.muteAudio)
        aui_info("mute video uid: \(uid) isMute: \(micSeat.muteVideo)", tag: "AUiMicSeatViewBinder")
        self.rtcEngine.muteRemoteVideoStream(uid, mute: micSeat.muteVideo)
    }
    
    public func onAnchorLeaveSeat(seatIndex: Int, user: AUiUserThumbnailInfo) {
        aui_info("onAnchorLeaveSeat seat: \(seatIndex)", tag: "AUiMicSeatViewBinder")
        let micSeat = micSeatArray[seatIndex]
        micSeat.user = nil
        micSeatArray[seatIndex] = micSeat
        micSeatView?.collectionView.reloadItems(at: [IndexPath(item: seatIndex, section: 0)])

        updateMic(with: seatIndex, role: .offlineAudience)
 
        //current user enter seat
        guard user.userId == micSeatDelegate?.getRoomContext().commonConfig?.userId else {
            return
        }
        
        let mediaOption = AgoraRtcChannelMediaOptions()
        mediaOption.clientRoleType = .audience
        rtcEngine.updateChannel(with: mediaOption)
        
        aui_info("update clientRoleType: \(mediaOption.clientRoleType.rawValue)", tag: "AUiMicSeatViewBinder")
    }
    
    public func onSeatAudioMute(seatIndex: Int, isMute: Bool) {
        aui_info("onSeatAudioMute seat: \(seatIndex) isMute: \(isMute)", tag: "AUiMicSeatViewBinder")
        let micSeat = micSeatArray[seatIndex]
        micSeat.muteAudio = isMute
        micSeatArray[seatIndex] = micSeat
        micSeatView?.collectionView.reloadItems(at: [IndexPath(item: seatIndex, section: 0)])
        
        //TODO: 麦位静音表示不听远端用户的声音，目前是mute remote audio
        guard let uid = UInt(micSeat.user?.userId ?? ""),
              micSeat.user?.userId != micSeatDelegate?.getRoomContext().currentUserInfo.userId else {
            return
        }
        aui_info("mute audio uid: \(uid) isMute: \(isMute)", tag: "AUiMicSeatViewBinder")
        self.rtcEngine.muteRemoteAudioStream(uid, mute: isMute)
    }
    
    public func onSeatVideoMute(seatIndex: Int, isMute: Bool) {
        aui_info("onSeatVideoMute  seat: \(seatIndex) isMute: \(isMute)", tag: "AUiMicSeatViewBinder")
        let micSeat = micSeatArray[seatIndex]
        micSeat.muteVideo = isMute
        micSeatArray[seatIndex] = micSeat
        micSeatView?.collectionView.reloadItems(at: [IndexPath(item: seatIndex, section: 0)])
        
        guard let uid = UInt(micSeat.user?.userId ?? ""),
              micSeat.user?.userId != micSeatDelegate?.getRoomContext().currentUserInfo.userId else {
            return
        }
        aui_info("mute video uid: \(uid) isMute: \(isMute)", tag: "AUiMicSeatViewBinder")
        self.rtcEngine.muteRemoteVideoStream(UInt(micSeat.user?.userId ?? "") ?? 0, mute: isMute)
    }
    
    public func onSeatClose(seatIndex: Int, isClose: Bool) {
        aui_info("onSeatClose seat:\(seatIndex) isClose: \(isClose)", tag: "AUiMicSeatViewBinder")
        let micSeat = micSeatArray[seatIndex]
        micSeat.lockSeat = isClose ? AUiLockSeatStatus.locked : AUiLockSeatStatus.idle
        micSeatArray[seatIndex] = micSeat
        micSeatView?.collectionView.reloadItems(at: [IndexPath(item: seatIndex, section: 0)])
    }
}

//MARK: AUiMicSeatViewDelegate
extension AUiMicSeatViewBinder: AUiMicSeatViewDelegate {
    func seatItems(view: AUiMicSeatView) -> [AUiMicSeatCellDataProtocol] {
        return micSeatArray
    }
    
    func onItemDidClick(view: AUiMicSeatView, seatIndex: Int) {
        let micSeat = micSeatArray[seatIndex]

        let dialogItems = getDialogItems(seatInfo: micSeat) {
            AUiCommonDialog.hidden()
        }
        guard dialogItems.count > 0 else {return}
        var headerInfo: AUiActionSheetHeaderInfo? = nil
        if let user = micSeat.user, user.userId.count > 0 {
            headerInfo = AUiActionSheetHeaderInfo()
            headerInfo?.avatar = user.userAvatar
            headerInfo?.title = user.userName
            headerInfo?.subTitle = micSeat.seatIndexDesc()
        }
        let dialogView = AUiActionSheet(title: aui_localized("managerSeat"),
                                        items: dialogItems,
                                        headerInfo: headerInfo)
        dialogView.setTheme(theme: AUiActionSheetTheme())
        AUiCommonDialog.show(contentView: dialogView, theme: AUiCommonDialogTheme())
    }
    
    func onMuteVideo(view: AUiMicSeatView, seatIndex: Int, canvas: UIView, isMuteVideo: Bool) {
        aui_info("onMuteVideo  seatIdx: \(seatIndex) mute: \(isMuteVideo)", tag: "AUiMicSeatViewBinder")
        let videoCanvas = AgoraRtcVideoCanvas()
        let micSeat = micSeatArray[seatIndex]
        if let userId = micSeat.user?.userId, let uid = UInt(userId), !isMuteVideo {
            videoCanvas.uid = uid
            videoCanvas.view = canvas
            videoCanvas.renderMode = .hidden
            if userId == self.micSeatDelegate?.getRoomContext().commonConfig?.userId {
                rtcEngine.setupLocalVideo(videoCanvas)
            } else {
                rtcEngine.setupRemoteVideo(videoCanvas)
            }
            aui_info("onMuteVideo user[\(userId)] seatIdx: \(seatIndex) mute: \(isMuteVideo)", tag: "AUiMicSeatViewBinder")
        } else {
            self.rtcEngine.setupRemoteVideo(videoCanvas)
        }
        
    }
}

//MARK: AUiUserRespDelegate
extension AUiMicSeatViewBinder: AUiUserRespDelegate {
    public func onRoomUserSnapshot(roomId: String, userList: [AUiUserInfo]) {
        aui_info("onRoomUserSnapshot", tag: "AUiMicSeatViewBinder")
        userMap.removeAll()
        userList.forEach { user in
            self.userMap[user.userId] = user
        }
        
        for micSeat in micSeatArray {
            if let userId = micSeat.user?.userId, userId.count > 0, let user = userMap[userId] {
                micSeat.user = user
            }
        }
        micSeatView?.collectionView.reloadData()
    }
    
    public func onRoomUserEnter(roomId: String, userInfo: AUiUserInfo) {
        aui_info("onRoomUserEnter: \(userInfo.userId)", tag: "AUiMicSeatViewBinder")
        userMap[userInfo.userId] = userInfo
    }
    
    public func onRoomUserLeave(roomId: String, userInfo: AUiUserInfo) {
        aui_info("onRoomUserLeave: \(userInfo.userId)", tag: "AUiMicSeatViewBinder")
        userMap[userInfo.userId] = nil
    }
    
    public func onRoomUserUpdate(roomId: String, userInfo: AUiUserInfo) {
        aui_info("onRoomUserUpdate: \(userInfo.userId)", tag: "AUiMicSeatViewBinder")
        userMap[userInfo.userId] = userInfo
    }
    
    public func onUserAudioMute(userId: String, mute: Bool) {
        aui_info("onUserAudioMute userId: \(userId) mute: \(mute)", tag: "AUiMicSeatViewBinder")
        userMap[userId]?.muteAudio = mute
        
        for (seatIndex, micSeat) in micSeatArray.enumerated() {
            if let user = userMap[userId], user.userId == micSeat.user?.userId {
                micSeat.user = user
                micSeatView?.collectionView.reloadItems(at: [IndexPath(item: seatIndex, section: 0)])
                break
            }
        }
    }
    
    public func onUserVideoMute(userId: String, mute: Bool) {
        aui_info("onUserVideoMute userId: \(userId) mute: \(mute)", tag: "AUiMicSeatViewBinder")
        userMap[userId]?.muteVideo = mute
        
        for (seatIndex, micSeat) in micSeatArray.enumerated() {
            if let user = userMap[userId], user.userId == micSeat.user?.userId {
                micSeat.user = userMap[userId]
                micSeatView?.collectionView.reloadItems(at: [IndexPath(item: seatIndex, section: 0)])
                break
            }
        }
    }
}

//MARK: AUiMusicRespDelegate
extension AUiMicSeatViewBinder: AUiMusicRespDelegate {
    public func onAddChooseSong(song: AUiChooseMusicModel) {
        
    }
    
    public func onRemoveChooseSong(song: AUiChooseMusicModel) {
        
    }
    
    public func onUpdateChooseSong(song: AUiChooseMusicModel) {
        
    }
    
    public func onUpdateAllChooseSongs(songs: [AUiChooseMusicModel]) {
        guard let topSong = songs.first else {
            //没有歌曲的话 在麦的用户都要变成onlineAudience
            let _ = micSeatArray.map {[weak self] in
                if ($0.user != nil) {
                    self?.updateMic(with: Int($0.seatIndex), role: .onlineAudience)
                }
            }
            return
        }
        guard let index = getMicIndex(with: topSong.userId ?? "") else {return}
        updateMic(with: index, role: .mainSinger)
    }
    
    
}

//MARK: AUiChorusRespDelegate
extension AUiMicSeatViewBinder: AUiChorusRespDelegate {
    public func onChoristerDidEnter(chorister: AUiChoristerModel) {
        //获取需要更新的麦位UI
        guard let index =  getMicIndex(with: chorister.userId) else {return}
        updateMic(with: index, role: .coSinger)
    }
    
    public func onChoristerDidLeave(chorister: AUiChoristerModel) {
        //获取需要更新的麦位UI
        guard let index =  getMicIndex(with: chorister.userId) else {return}
        updateMic(with: index, role: .onlineAudience)
    }
    
    private func getMicIndex(with userId: String) -> Int? {
        return micSeatArray
            .filter { $0.user?.userId == userId }
            .map { Int($0.seatIndex) }
            .first
    }
    
    private func getLocalUserId() -> String? {
        guard let commonConfig = AUiRoomContext.shared.commonConfig else {return nil}
        return commonConfig.userId
    }
    
    private func updateMic(with index: Int, role: MicRole) {
        let micSeat = micSeatArray[index]
        micSeat.micRole = role
        micSeatArray[index] = micSeat
        micSeatView?.collectionView.reloadItems(at: [IndexPath(item: index, section: 0)])
    }

}

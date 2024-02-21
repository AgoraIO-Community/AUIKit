//
//  AUIPlayerView.swift
//  AUIKit
//
//  Created by wushengtao on 2023/3/29.
//

import Foundation
import SwiftTheme
//import AgoraRtcKit
import AgoraLyricsScore

@objc public enum AUIPlayerViewButtonType: Int {
    case audioSetting = 0  //设置
    case audioEffect       //音效
    case selectSong        //点歌按钮
    case play              //播放
    case pause             //暂停
    case nextSong          //切歌
    case original          //原唱
    case acc               //伴奏
}

public enum JoinChorusState {
    case none //主唱
    case before //观众加入合唱前
    case loding //加入合唱过程中
    case after //合唱
}

/// 用户角色
@objc public enum AUISingRole: Int {
    case soloSinger = 0     //独唱者
    case coSinger           //伴唱
    case leadSinger         //主唱
    case audience           //观众
//    case followSinger       //跟唱
}

public protocol AUIKaraokeLrcViewDelegate: NSObjectProtocol {
    func didJoinChorus()
    func didLeaveChorus()
}

@objc public protocol AUIPlayerViewDelegate: NSObjectProtocol {
    func onButtonTapAction(playerView: AUIPlayerView, actionType: AUIPlayerViewButtonType)
    @objc optional func onVoiceConversionDidChanged(index: Int)
    @objc optional func onSliderValueDidChanged(value: CGFloat, item: AUIPlayerAudioSettingItem)
    @objc optional func onSwitchValueDidChanged(isSwitch: Bool, item: AUIPlayerAudioSettingItem)
    @objc optional func onAudioMixDidChanged(audioMixIndex: Int)
    @objc optional func onSliderCellWillLoad(playerView: AUIPlayerAudioSettingView, item: AUIPlayerAudioSettingItem)
    @objc optional func onSwitchCellWillLoad(playerView: AUIPlayerAudioSettingView, item: AUIPlayerAudioSettingItem)
}


/// 歌曲播放组件
open class AUIPlayerView: UIView {
    public var voiceConversionIdx: Int = 0
    public var audioMixinIdx: Int = 0
    private var mixIdx: Int = 0
    
    public lazy var karaokeLrcView: AUIKaraokeLrcView = {
        let karaokeLrcView = AUIKaraokeLrcView(frame: CGRect(x: 0, y: 0, width: aui_width, height: aui_height - 60))
        karaokeLrcView.lrcView.scoringView.standardPitchStickViewColor = UIColor(hex: "#99D8FF")
        return karaokeLrcView
    }()
    
    var seatInfo: AUIMicSeatInfo? {
        didSet {
            self.userLabel.text = seatInfo?.seatAndUserDesc()
            setNeedsLayout()
        }
    }
    
    public weak var delegate: AUIKaraokeLrcViewDelegate?
    public var joinState: JoinChorusState = .none {
        didSet {
            switch joinState {
            case .none:
                joinChorusButton.isHidden = true
                leaveChorusBtn.isHidden = true
            case .before:
                joinChorusButton.isHidden = false
                leaveChorusBtn.isHidden = true
            case .loding:
                joinChorusButton.isHidden = false
                joinChorusButton.isEnabled = false
                leaveChorusBtn.isHidden = true
            case .after:
                joinChorusButton.isHidden = true
                joinChorusButton.isEnabled = true
                leaveChorusBtn.isHidden = false
                
                leaveChorusBtn.aui_left = chooseSongButton.aui_left + chooseSongButton.aui_width + 15
            }
        }
    }
    
    private var eventHandlers: NSHashTable<AnyObject> = NSHashTable<AnyObject>.weakObjects()
    
    public func addActionHandler(playerViewActionHandler: AUIPlayerViewDelegate) {
        if eventHandlers.contains(playerViewActionHandler) {
            return
        }
        eventHandlers.add(playerViewActionHandler)
    }

    func removeEventHandler(playerViewActionHandler: AUIPlayerViewDelegate) {
        eventHandlers.remove(playerViewActionHandler)
    }
    
    private func getEventHander(callBack:((AUIPlayerViewDelegate)-> Void)) {
        for obj in eventHandlers.allObjects {
            if obj is AUIPlayerViewDelegate {
                callBack(obj as! AUIPlayerViewDelegate)
            }
        }
    }

    public var selectSongBtnNeedHidden: Bool = true {
        didSet {
            selectSongButton.isHidden = selectSongBtnNeedHidden
        }
    }
    
    public var musicInfo: AUIChooseMusicModel? {
        didSet {
            guard let musicInfo = musicInfo
            else {
                musicTitleLabel.text = aui_localized("songListIsEmpty")
                
                musicTitleLabel.sizeToFit()
                updateSelectSongView()
                karaokeLrcView.isHidden = true

                selectSongButton.isHidden = selectSongBtnNeedHidden

                originalButton.isHidden = true
                playOrPauseButton.isHidden = true
                chooseSongButton.isHidden = true
                nextSongButton.isHidden = true
                joinChorusButton.isHidden = true
                leaveChorusBtn.isHidden = true
                audioSettingButton.isHidden = true
                voiceConversionButton.isHidden = true
                setNeedsLayout()
                return
            }

            musicTitleImageView.aui_size = CGSize(width: 12, height: 12)
            musicTitleImageView.aui_tl = CGPoint(x: 18, y: 16)
            musicTitleLabel.aui_left = musicTitleImageView.aui_right + 6
            musicTitleLabel.aui_centerY = musicTitleImageView.aui_centerY
            selectSongButton.isHidden = true
            musicTitleImageView.isHidden = false
            musicTitleLabel.isHidden = false
            
            musicTitleLabel.text = musicInfo.name
            karaokeLrcView.isHidden = false
            selectSongButton.isHidden = true
            setNeedsLayout()
        }
    }
    
    //MARK: lazy load view
    //音乐图标
    private lazy var musicTitleImageView: UIImageView = {
        let imageView = UIImageView()
        imageView.theme_image = "Player.musicTitleIcon"
        imageView.contentMode = .scaleAspectFit
        return imageView
    }()
    
    //歌曲名
    private lazy var musicTitleLabel: UILabel = {
        let label = UILabel()
        label.theme_font = "Player.musicTitleFont"
        label.theme_textColor = "Player.musicTitleColor"
        label.text = aui_localized("songListIsEmpty")
        return label
    }()
    
    private lazy var userLabel: UILabel = {
        let label = UILabel()
        label.theme_font = "Player.ownerTitleFont"
        label.theme_textColor = "Player.ownerTitleColor"
        return label
    }()
    
    //设置按钮
    private lazy var audioSettingButton: AUIButton = {
        let theme = AUIButtonDynamicTheme.toolbarTheme()
        theme.icon =  ThemeAnyPicker(keyPath:"Player.audioSettingIcon")
        let button = AUIButton()
        button.textImageAlignment = .imageTopTextBottom
        button.style = theme
        button.setTitle(aui_localized("setting"), for: .normal)
        button.addTarget(self, action: #selector(onClickSetting), for: .touchUpInside)
        return button
    }()
    
    //音效按钮
    private lazy var voiceConversionButton: AUIButton = {
        let theme = AUIButtonDynamicTheme.toolbarTheme()
        theme.icon =  ThemeAnyPicker(keyPath:"Player.voiceConversionIcon")
        let button = AUIButton()
        button.textImageAlignment = .imageTopTextBottom
        button.style = theme
        button.setTitle(aui_localized("voiceConversion"), for: .normal)
        button.addTarget(self, action: #selector(onClickVoiceConversion), for: .touchUpInside)// 图片在上文字在下 view.addSubview(button)
        return button
    }()

    //点歌按钮 居中显示的按钮
    public lazy var selectSongButton: AUIButton = {
        let theme = AUIButtonDynamicTheme()
        theme.iconWidth = "Player.selectSongButtonWidth"
        theme.iconHeight = "Player.selectSongButtonHeight"
        theme.buttonWidth = "Player.selectSongButtonWidth"
        theme.buttonHeight = "Player.selectSongButtonHeight"
        theme.backgroundColor = AUIColor("Player.selectSongBackgroundColor")
        theme.cornerRadius = "Player.selectSongButtonRadius"
        theme.icon =  ThemeAnyPicker(keyPath:"Player.SelectSongIcon")
        theme.textAlpha = "Player.SelectSongTextAlpha"
        let button = AUIButton()
        button.textImageAlignment = .imageCenterTextCenter
        button.style = theme
        button.setTitle(aui_localized("selectSong"), for: .normal)
        button.addTarget(self, action: #selector(onSelectSong), for: .touchUpInside)
        return button
    }()
    
    //加入合唱按钮
    public lazy var joinChorusButton: AUIButton = {
        let theme = AUIButtonDynamicTheme()
        theme.buttonWidth = "Player.JoinChorusButtonWidth"
        theme.buttonHeight = "Player.JoinChorusButtonHeight"
        theme.iconWidth = "Player.JoinChorusButtonWidth"
        theme.iconHeight = "Player.JoinChorusButtonHeight"
        theme.icon =  ThemeAnyPicker(keyPath:"Player.playerLrcItemIconJoinChorus")
        theme.cornerRadius = nil
        let button = AUIButton()
        button.textImageAlignment = .imageCenterTextCenter
        button.style = theme
        button.setTitle(aui_localized("joinChorus"), for: .normal)
        button.setTitle(aui_localized("loading"), for: .disabled)
        button.titleLabel?.font = UIFont.systemFont(ofSize: 12.0)
        button.addTarget(self, action: #selector(didJoinChorus), for: .touchUpInside)
        return button
    }()
    
    //离开合唱按钮
    public lazy var leaveChorusBtn: AUIButton = {
        let theme = AUIButtonDynamicTheme.toolbarTheme()
        theme.icon =  ThemeAnyPicker(keyPath:"Player.playerLrcItemIconLeaveChorus")
        theme.cornerRadius = nil
        let button = AUIButton()
        button.textImageAlignment = .imageTopTextBottom
        button.style = theme
        button.setTitle("放麦", for: .normal)
        button.titleLabel?.font = UIFont.systemFont(ofSize: 12.0)
        button.addTarget(self, action: #selector(didLeaveChorus), for: .touchUpInside)
        return button
    }()
    
    //暂停播放按钮
    public lazy var playOrPauseButton: AUIButton = {
        let theme = AUIButtonDynamicTheme.toolbarTheme()
        theme.selectedIcon =  ThemeAnyPicker(keyPath:"Player.playerLrcItemIconPlay")
        theme.icon =  ThemeAnyPicker(keyPath:"Player.playerLrcItemIconPause")
        let button = AUIButton()
        button.textImageAlignment = .imageTopTextBottom
        button.style = theme
        button.setTitle(aui_localized("play"), for: .normal)
        button.setTitle(aui_localized("pause"), for: .selected)
        button.addTarget(self, action: #selector(playOrPause), for: .touchUpInside)
        return button
    }()
    
    //切歌按钮
    public lazy var nextSongButton: AUIButton = {
        let theme = AUIButtonDynamicTheme.toolbarTheme()
        theme.icon =  ThemeAnyPicker(keyPath:"Player.playerLrcItemIconNext")
        let button = AUIButton()
        button.textImageAlignment = .imageTopTextBottom
        button.style = theme
        button.setTitle(aui_localized("skipSong"), for: .normal)
        button.addTarget(self, action: #selector(nextSong), for: .touchUpInside)
        return button
    }()
    
    //点歌按钮
    public lazy var chooseSongButton: AUIButton = {
        let theme = AUIButtonDynamicTheme.toolbarTheme()
        theme.icon =  ThemeAnyPicker(keyPath:"Player.playerLrcItemIconChooseSong")
        let button = AUIButton()
        button.textImageAlignment = .imageTopTextBottom
        button.style = theme
        button.setTitle(aui_localized("selectSong"), for: .normal)
        button.addTarget(self, action: #selector(onSelectSong), for: .touchUpInside)
        return button
    }()
    
    //原唱按钮
    public lazy var originalButton: AUIButton = {
        let theme = AUIButtonDynamicTheme.toolbarTheme()
        theme.icon =  ThemeAnyPicker(keyPath:"Player.playerLrcItemIconAcc")
        theme.selectedIcon =  ThemeAnyPicker(keyPath:"Player.playerLrcItemIconOriginal")
        let button = AUIButton()
        button.textImageAlignment = .imageTopTextBottom
        button.style = theme
        button.setTitle(aui_localized("originalArtist"), for: .normal)
        button.addTarget(self, action: #selector(changeAudioTrack), for: .touchUpInside)
        return button
    }()
    
    private var loadingView: AUIKaraokeLoadingView!
    
    //MARK: life cycle
    public override init(frame: CGRect) {
        super.init(frame: frame)
        _loadSubViews()
    }
    
    required public init?(coder: NSCoder) {
        super.init(coder: coder)
        _loadSubViews()
    }
    
    
    //MARK: private
    private func _loadSubViews() {
        self.theme_backgroundColor = AUIColor("Player.backgroundColor")
        self.layer.theme_cornerRadius = "Player.cornerRadius"
        self.clipsToBounds = true

        addSubview(karaokeLrcView)
        karaokeLrcView.isHidden = true

        addSubview(musicTitleImageView)

        addSubview(musicTitleLabel)

        addSubview(selectSongButton)
        selectSongButton.isHidden = true
        
        updateSelectSongView()

        voiceConversionButton.aui_right = aui_width - 55
        voiceConversionButton.aui_bottom = aui_height - 10
        addSubview(voiceConversionButton)
        voiceConversionButton.isHidden = true

        audioSettingButton.aui_right = voiceConversionButton.aui_left - 15
        audioSettingButton.aui_top = voiceConversionButton.aui_top
        addSubview(audioSettingButton)
        audioSettingButton.isHidden = true

        originalButton.aui_right = aui_width - 15
        originalButton.aui_bottom = aui_height - 10
        addSubview(originalButton)
        originalButton.isHidden = true

        joinChorusButton.aui_centerX = aui_width / 2
        joinChorusButton.aui_bottom = aui_height - 10
        addSubview(joinChorusButton)
        joinChorusButton.isHidden = true

        playOrPauseButton.aui_left = 15
        playOrPauseButton.aui_bottom = aui_height - 10
        addSubview(playOrPauseButton)
        playOrPauseButton.isHidden = true

        leaveChorusBtn.aui_left = chooseSongButton.aui_right + 15
        leaveChorusBtn.aui_bottom = aui_height - 10
        addSubview(leaveChorusBtn)
        leaveChorusBtn.isHidden = true

        nextSongButton.aui_bottom = aui_height - 10
        nextSongButton.aui_left = playOrPauseButton.aui_right + 15
        addSubview(nextSongButton)
        nextSongButton.isHidden = true

        chooseSongButton.aui_left = nextSongButton.aui_right + 15
        chooseSongButton.aui_bottom = aui_height - 10
        addSubview(chooseSongButton)
        chooseSongButton.isHidden = true
        
        loadingView = AUIKaraokeLoadingView(frame: CGRect(x: 0, y: 0, width: bounds.width, height: bounds.height))
        addSubview(loadingView)
        loadingView.isHidden = true
        
    }
    
    public func updateLoadingView(with progress: Int) {
        DispatchQueue.main.async {[weak self] in
            if progress == 100 {
                self?.loadingView.isHidden = true
            } else {
                self?.loadingView.isHidden = false
                self?.loadingView.setProgress(progress)
            }
        }
    }
    
    private func updateSelectSongView() {
        //房主和观众的点歌视图居中
        musicTitleImageView.aui_size = CGSize(width: 35, height: 35)
        musicTitleImageView.aui_centerX = aui_width / 2
        musicTitleImageView.aui_top = bounds.height / 2.0 - (selectSongBtnNeedHidden ? 57 : 103) / 2.0

        musicTitleLabel.sizeToFit()
        musicTitleLabel.aui_center = CGPoint(x: aui_width / 2, y: musicTitleImageView.aui_bottom + musicTitleLabel.aui_height / 2 + 10)

        selectSongButton.aui_center = CGPoint(x: aui_width / 2, y: musicTitleLabel.aui_bottom + selectSongButton.aui_height / 2 + 8)
        setNeedsLayout()
    }

}

//MARK: action
extension AUIPlayerView {
    /// 点击点歌按钮
    @objc func onSelectSong() {
        getEventHander { delegate in
            delegate.onButtonTapAction(playerView: self, actionType: .selectSong)
        }
    }
    
    ///点击设置按钮
    @objc func onClickSetting() {
        aui_info("onClickSetting", tag: "AUIPlayerView")
        let dialogView = AUIPlayerAudioSettingView()
        dialogView.delegate = self
        dialogView.sizeToFit()
        AUICommonDialog.show(contentView: dialogView, theme: AUICommonDialogTheme())
    }
    
    @objc func nextSong() {
        AUIAlertView.theme_defaultAlert()
            .isShowCloseButton(isShow: false)
            .title(title: aui_localized("switchToNextSong"))
            .rightButton(title: aui_localized("confirm"))
            .rightButtonTapClosure(onTap: {[weak self] text in
                guard let self = self else { return }
                self.getEventHander { delegate in
                    delegate.onButtonTapAction(playerView: self, actionType: .nextSong)
                }
            })
            .leftButton(title: aui_localized("cancel"))
            .show()
    }
    
    @objc func playOrPause(btn: AUIButton) {
        btn.isSelected = !btn.isSelected
        getEventHander { delegate in
            delegate.onButtonTapAction(playerView: self, actionType: btn.isSelected ? .pause : .play)
        }
    }
    
    @objc func changeAudioTrack(btn: AUIButton){
        btn.isSelected = !btn.isSelected
        getEventHander { delegate in
            delegate.onButtonTapAction(playerView: self, actionType: btn.isSelected ? .original : .acc)
        }
    }

    /// 点击变声按钮
    @objc func onClickVoiceConversion() {
        aui_info("onClickEffect", tag: "AUIPlayerView")
        
        var dialogItems = [AUIActionSheetItem]()
        for i in 1...5 {
            let item = AUIActionSheetThemeItem.vertical()
            item.backgroundIcon = "Player.voiceConversionDialogItemBackgroundIcon"
            item.icon = ThemeImagePicker(keyPath: "Player.voiceConversionDialogItemIcon\(i)")
            item.title = aui_localized("voiceConversionItem\(i)")
            item.callback = { [weak self] in
                aui_info("onClickVoiceConversion click: \(i - 1)", tag: "AUIPlayerView")
                guard let self = self else {return}
                self.voiceConversionIdx = i - 1
                self.getEventHander { delegate in
                    delegate.onVoiceConversionDidChanged?( index: i - 1)
                }
            }
            item.isSelected = { [weak self] in
                return self?.voiceConversionIdx == i - 1
            }
            dialogItems.append(item)
        }
        
        let theme = AUIActionSheetTheme()
        theme.itemType = "Player.voiceConversionDialogItemType"
        theme.itemHeight = "Player.voiceConversionDialogItemHeight"
        theme.collectionViewTopEdge = "Player.collectionViewTopEdge"
        let dialogView = AUIActionSheet(title: aui_localized("voiceConversion"),
                                        items: dialogItems,
                                        headerInfo: nil)
        dialogView.setTheme(theme: theme)
        AUICommonDialog.show(contentView: dialogView, theme: AUICommonDialogTheme())
    }
    
    public func updateBtns(with role: AUISingRole, isMainSinger: Bool, isOnSeat: Bool) {
        switch role {
            case .soloSinger, .leadSinger:
                playOrPauseButton.isHidden = false
                playOrPauseButton.aui_left = 15
                nextSongButton.aui_left = playOrPauseButton.aui_right + 15
                nextSongButton.isHidden = false
                voiceConversionButton.isHidden = false
                originalButton.isHidden = false
                audioSettingButton.isHidden = false
            case .coSinger:
                playOrPauseButton.isHidden = true
                if isMainSinger {
                    nextSongButton.isHidden = false
                    nextSongButton.aui_left = 15
                } else {
                    nextSongButton.isHidden = true
                }
                voiceConversionButton.isHidden = false
                originalButton.isHidden = false
                audioSettingButton.isHidden = false
            case .audience:
                playOrPauseButton.isHidden = true
                if isMainSinger {
                    nextSongButton.isHidden = false
                    nextSongButton.aui_left = 15
                } else {
                    nextSongButton.isHidden = true
                }
                voiceConversionButton.isHidden = true
                originalButton.isHidden = true
                audioSettingButton.isHidden = true
        }
            chooseSongButton.isHidden = !isOnSeat
            chooseSongButton.aui_left = nextSongButton.isHidden ? 15 : nextSongButton.aui_right + 15
            if role == .coSinger {
                chooseSongButton.aui_left = isMainSinger ? nextSongButton.aui_right + 15 : 15
            }
    }
    
    @objc func didJoinChorus() {
        //加入合唱
        guard let delegate = self.delegate else {return}
        delegate.didJoinChorus()
    }

    @objc func didLeaveChorus() {
        //退出合唱
        guard let delegate = self.delegate else {return}
        delegate.didLeaveChorus()
    }
}

//MARK: AUIPlayerAudioSettingViewDelegate
extension AUIPlayerView: AUIPlayerAudioSettingViewDelegate {
    public func onSliderCellWillLoad(playerView: AUIPlayerAudioSettingView, item: AUIPlayerAudioSettingItem) {
        getEventHander { delegate in
            delegate.onSliderCellWillLoad?(playerView: playerView, item: item)
        }
    }
    
    public func onSwitchCellWillLoad(playerView: AUIPlayerAudioSettingView, item: AUIPlayerAudioSettingItem) {
        getEventHander { delegate in
            delegate.onSwitchCellWillLoad?(playerView: playerView, item: item)
        }
    }
    
    public func audioMixIsSelected(playerView: AUIPlayerAudioSettingView, audioMixIndex: Int) -> Bool {
        return self.audioMixinIdx == audioMixIndex
    }
    
    public func onSliderValueDidChanged(playerView: AUIPlayerAudioSettingView, value: CGFloat, item: AUIPlayerAudioSettingItem) {
        aui_info("onSliderValueDidChanged: \(value)", tag: "AUIPlayerView")
        getEventHander { delegate in
            delegate.onSliderValueDidChanged?( value: value, item: item)
        }
    }
    
    public func onSwitchValueDidChanged(playerView: AUIPlayerAudioSettingView, isSwitch: Bool, item: AUIPlayerAudioSettingItem) {
        aui_info("onSwitchValueDidChanged: \(isSwitch)", tag: "AUIPlayerView")
        getEventHander { delegate in
            delegate.onSwitchValueDidChanged?(isSwitch: isSwitch, item: item)
        }
    }
    
    public func onAudioMixDidChanged(playerView: AUIPlayerAudioSettingView, audioMixIndex: Int) {
        aui_info("onAudioMixDidChanged: \(audioMixIndex)", tag: "AUIPlayerView")
        self.audioMixinIdx = audioMixIndex
        getEventHander { delegate in
            delegate.onAudioMixDidChanged?(audioMixIndex: audioMixIndex)
        }
    }
}

extension UIButton {
    func alignTextBelow(spacing: CGFloat = 6.0) {
        if let image = self.imageView?.image {
            let imageSize: CGSize = image.size
            self.titleEdgeInsets = UIEdgeInsets(top: spacing, left: -imageSize.width, bottom: -(imageSize.height / 2), right: 0.0)
            let labelString = NSString(string: self.titleLabel!.text!)
            let titleSize = labelString.size(withAttributes: [NSAttributedString.Key.font: self.titleLabel!.font!])
            self.imageEdgeInsets = UIEdgeInsets(top: -(titleSize.height + (spacing / 2)), left: 0.0, bottom: 0.0, right: -titleSize.width)
            let edgeOffset = abs(titleSize.height - imageSize.height) / 2.0;
            self.contentEdgeInsets = UIEdgeInsets(top: edgeOffset, left: 0.0, bottom: edgeOffset, right: 0.0)
        }
    }
}


//
//  AUiPlayerView.swift
//  AUiKit
//
//  Created by wushengtao on 2023/3/29.
//

import Foundation
import SwiftTheme
import AgoraRtcKit
import AgoraLyricsScore

@objc public enum AUiPlayerViewButtonType: Int {
    case audioSetting = 0  //设置
    case audioEffect       //音效
    case selectSong        //点歌按钮
    case play              //播放
    case pause             //暂停
    case nextSong          //切歌
    case original          //原唱
    case acc               //伴奏
}

enum joinChorusState {
    case none //主唱
    case before //观众加入合唱前
    case loding //加入合唱过程中
    case after //合唱
}

protocol AUiKaraokeLrcViewDelegate: NSObjectProtocol {
    func didJoinChorus()
    func didLeaveChorus()
}

@objc public protocol AUiPlayerViewDelegate: NSObjectProtocol {
    func onButtonTapAction(playerView: AUiPlayerView, actionType: AUiPlayerViewButtonType)
    @objc optional func onVoiceConversionDidChanged(index: Int)
    @objc optional func onSliderValueDidChanged(value: CGFloat, item: AUiPlayerAudioSettingItem)
    @objc optional func onSwitchValueDidChanged(isSwitch: Bool, item: AUiPlayerAudioSettingItem)
    @objc optional func onAudioMixDidChanged(audioMixIndex: Int)
    @objc optional func onSliderCellWillLoad(playerView: AUiPlayerAudioSettingView, item: AUiPlayerAudioSettingItem)
    @objc optional func onSwitchCellWillLoad(playerView: AUiPlayerAudioSettingView, item: AUiPlayerAudioSettingItem)
}


/// 歌曲播放组件
open class AUiPlayerView: UIView {
    public var voiceConversionIdx: Int = 0
    public var audioMixinIdx: Int = 0
    private var mixIdx: Int = 0
    
    public lazy var karaokeLrcView: AUiKaraokeLrcView = {
        let karaokeLrcView = AUiKaraokeLrcView(frame: CGRect(x: 0, y: 0, width: aui_width, height: aui_height - 60))
        return karaokeLrcView
    }()
    
    var seatInfo: AUiMicSeatInfo? {
        didSet {
            self.userLabel.text = seatInfo?.seatAndUserDesc()
            setNeedsLayout()
        }
    }
    
    weak var delegate: AUiKaraokeLrcViewDelegate?
    var joinState: joinChorusState = .none {
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
    
    public func addActionHandler(playerViewActionHandler: AUiPlayerViewDelegate) {
        if eventHandlers.contains(playerViewActionHandler) {
            return
        }
        eventHandlers.add(playerViewActionHandler)
    }

    func removeEventHandler(playerViewActionHandler: AUiPlayerViewDelegate) {
        eventHandlers.remove(playerViewActionHandler)
    }
    
    private func getEventHander(callBack:((AUiPlayerViewDelegate)-> Void)) {
        for obj in eventHandlers.allObjects {
            if obj is AUiPlayerViewDelegate {
                callBack(obj as! AUiPlayerViewDelegate)
            }
        }
    }

    var selectSongBtnNeedHidden: Bool = true {
        didSet {
            selectSongButton.isHidden = selectSongBtnNeedHidden
        }
    }
    
    var musicInfo: AUiChooseMusicModel? {
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
    private lazy var audioSettingButton: AUiButton = {
        let theme = AUiButtonDynamicTheme.toolbarTheme()
        theme.icon = "Player.audioSettingIcon"
        let button = AUiButton()
        button.textImageAlignment = .imageTopTextBottom
        button.style = theme
        button.setTitle("设置", for: .normal)
        button.addTarget(self, action: #selector(onClickSetting), for: .touchUpInside)
        return button
    }()
    
    //音效按钮
    private lazy var voiceConversionButton: AUiButton = {
        let theme = AUiButtonDynamicTheme.toolbarTheme()
        theme.icon = "Player.voiceConversionIcon"
        let button = AUiButton()
        button.textImageAlignment = .imageTopTextBottom
        button.style = theme
        button.setTitle("音效", for: .normal)
        button.addTarget(self, action: #selector(onClickVoiceConversion), for: .touchUpInside)// 图片在上文字在下 view.addSubview(button)
        return button
    }()

    //点歌按钮 居中显示的按钮
    public lazy var selectSongButton: AUiButton = {
        let theme = AUiButtonDynamicTheme()
        theme.iconWidth = "Player.selectSongButtonWidth"
        theme.iconHeight = "Player.selectSongButtonHeight"
        theme.buttonWitdth = "Player.selectSongButtonWidth"
        theme.buttonHeight = "Player.selectSongButtonHeight"
        theme.backgroundColor = AUiColor("Player.selectSongBackgroundColor")
        theme.cornerRadius = "Player.selectSongButtonRadius"
        theme.icon = "Player.SelectSongIcon"
        theme.textAlpha = "Player.SelectSongTextAlpha"
        let button = AUiButton()
        button.textImageAlignment = .imageCenterTextCenter
        button.style = theme
        button.setTitle(aui_localized("selectSong"), for: .normal)
        button.addTarget(self, action: #selector(onSelectSong), for: .touchUpInside)
        return button
    }()
    
    //加入合唱按钮
    lazy var joinChorusButton: AUiButton = {
        let theme = AUiButtonDynamicTheme()
        theme.buttonWitdth = "Player.JoinChorusButtonWidth"
        theme.buttonHeight = "Player.JoinChorusButtonHeight"
        theme.icon = "Player.playerLrcItemIconJoinChorus"
        theme.cornerRadius = nil
        let button = AUiButton()
        button.textImageAlignment = .imageCenterTextCenter
        button.style = theme
        button.setTitle("加入合唱", for: .normal)
        button.setTitle("加载中", for: .disabled)
        button.titleLabel?.font = UIFont.systemFont(ofSize: 12.0)
        button.addTarget(self, action: #selector(didJoinChorus), for: .touchUpInside)
        return button
    }()
    
    //离开合唱按钮
    lazy var leaveChorusBtn: AUiButton = {
        let theme = AUiButtonDynamicTheme.toolbarTheme()
        theme.icon = "Player.playerLrcItemIconLeaveChorus"
        theme.cornerRadius = nil
        let button = AUiButton()
        button.textImageAlignment = .imageTopTextBottom
        button.style = theme
        button.setTitle("放麦", for: .normal)
        button.titleLabel?.font = UIFont.systemFont(ofSize: 12.0)
        button.addTarget(self, action: #selector(didLeaveChorus), for: .touchUpInside)
        return button
    }()
    
    //暂停播放按钮
    lazy var playOrPauseButton: AUiButton = {
        let theme = AUiButtonDynamicTheme.toolbarTheme()
        theme.selectedIcon = "Player.playerLrcItemIconPlay"
        theme.icon = "Player.playerLrcItemIconPause"
        let button = AUiButton()
        button.textImageAlignment = .imageTopTextBottom
        button.style = theme
        button.setTitle("播放", for: .normal)
        button.setTitle("暂停", for: .selected)
        button.addTarget(self, action: #selector(playOrPause), for: .touchUpInside)
        return button
    }()
    
    //切歌按钮
    lazy var nextSongButton: AUiButton = {
        let theme = AUiButtonDynamicTheme.toolbarTheme()
        theme.icon = "Player.playerLrcItemIconNext"
        let button = AUiButton()
        button.textImageAlignment = .imageTopTextBottom
        button.style = theme
        button.setTitle("切歌", for: .normal)
        button.addTarget(self, action: #selector(nextSong), for: .touchUpInside)
        return button
    }()
    
    //点歌按钮
    lazy var chooseSongButton: AUiButton = {
        let theme = AUiButtonDynamicTheme.toolbarTheme()
        theme.icon = "Player.playerLrcItemIconChooseSong"
        let button = AUiButton()
        button.textImageAlignment = .imageTopTextBottom
        button.style = theme
        button.setTitle("点歌", for: .normal)
        button.addTarget(self, action: #selector(onSelectSong), for: .touchUpInside)
        return button
    }()
    
    //原唱按钮
    lazy var originalButton: AUiButton = {
        let theme = AUiButtonDynamicTheme.toolbarTheme()
        theme.icon = "Player.playerLrcItemIconAcc"
        theme.selectedIcon = "Player.playerLrcItemIconOriginal"
        let button = AUiButton()
        button.textImageAlignment = .imageTopTextBottom
        button.style = theme
        button.setTitle("原唱", for: .normal)
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
        self.theme_backgroundColor = AUiColor("Player.backgroundColor")
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
extension AUiPlayerView {
    /// 点击点歌按钮
    @objc func onSelectSong() {
        getEventHander { delegate in
            delegate.onButtonTapAction(playerView: self, actionType: .selectSong)
        }
    }
    
    ///点击设置按钮
    @objc func onClickSetting() {
        aui_info("onClickSetting", tag: "AUiPlayerView")
        let dialogView = AUiPlayerAudioSettingView()
        dialogView.delegate = self
        dialogView.sizeToFit()
        AUiCommonDialog.show(contentView: dialogView, theme: AUiCommonDialogTheme())
    }
    
    @objc func nextSong() {
        AUiAlertView.theme_defaultAlert()
            .isShowCloseButton(isShow: false)
            .title(title: aui_localized("switchToNextSong"))
            .rightButton(title: "确认")
            .rightButtonTapClosure(onTap: {[weak self] text in
                guard let self = self else { return }
                self.getEventHander { delegate in
                    delegate.onButtonTapAction(playerView: self, actionType: .nextSong)
                }
            })
            .leftButton(title: "取消")
            .show()
    }
    
    @objc func playOrPause(btn: AUiButton) {
        btn.isSelected = !btn.isSelected
        getEventHander { delegate in
            delegate.onButtonTapAction(playerView: self, actionType: btn.isSelected ? .pause : .play)
        }
    }
    
    @objc func changeAudioTrack(btn: AUiButton){
        btn.isSelected = !btn.isSelected
        getEventHander { delegate in
            delegate.onButtonTapAction(playerView: self, actionType: btn.isSelected ? .original : .acc)
        }
    }

    /// 点击变声按钮
    @objc func onClickVoiceConversion() {
        aui_info("onClickEffect", tag: "AUiPlayerView")
        
        var dialogItems = [AUiActionSheetItem]()
        for i in 1...5 {
            let item = AUiActionSheetThemeItem.vertical()
            item.backgroundIcon = "Player.voiceConversionDialogItemBackgroundIcon"
            item.icon = ThemeImagePicker(keyPath: "Player.voiceConversionDialogItemIcon\(i)")
            item.title = aui_localized("voiceConversionItem\(i)")
            item.callback = { [weak self] in
                aui_info("onClickVoiceConversion click: \(i - 1)", tag: "AUiPlayerView")
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
        
        let theme = AUiActionSheetTheme()
        theme.itemType = "Player.voiceConversionDialogItemType"
        theme.itemHeight = "Player.voiceConversionDialogItemHeight"
        theme.collectionViewTopEdge = "Player.collectionViewTopEdge"
        let dialogView = AUiActionSheet(title: aui_localized("voiceConversion"),
                                        items: dialogItems,
                                        headerInfo: nil)
        dialogView.setTheme(theme: theme)
        AUiCommonDialog.show(contentView: dialogView, theme: AUiCommonDialogTheme())
    }
    
    public func updateBtns(with role: KTVSingRole, isMainSinger: Bool, isOnSeat: Bool) {
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

//MARK: AUiPlayerAudioSettingViewDelegate
extension AUiPlayerView: AUiPlayerAudioSettingViewDelegate {
    public func onSliderCellWillLoad(playerView: AUiPlayerAudioSettingView, item: AUiPlayerAudioSettingItem) {
        getEventHander { delegate in
            delegate.onSliderCellWillLoad?(playerView: playerView, item: item)
        }
    }
    
    public func onSwitchCellWillLoad(playerView: AUiPlayerAudioSettingView, item: AUiPlayerAudioSettingItem) {
        getEventHander { delegate in
            delegate.onSwitchCellWillLoad?(playerView: playerView, item: item)
        }
    }
    
    public func audioMixIsSelected(playerView: AUiPlayerAudioSettingView, audioMixIndex: Int) -> Bool {
        return self.audioMixinIdx == audioMixIndex
    }
    
    public func onSliderValueDidChanged(playerView: AUiPlayerAudioSettingView, value: CGFloat, item: AUiPlayerAudioSettingItem) {
        aui_info("onSliderValueDidChanged: \(value)", tag: "AUiPlayerView")
        getEventHander { delegate in
            delegate.onSliderValueDidChanged?( value: value, item: item)
        }
    }
    
    public func onSwitchValueDidChanged(playerView: AUiPlayerAudioSettingView, isSwitch: Bool, item: AUiPlayerAudioSettingItem) {
        aui_info("onSwitchValueDidChanged: \(isSwitch)", tag: "AUiPlayerView")
        getEventHander { delegate in
            delegate.onSwitchValueDidChanged?(isSwitch: isSwitch, item: item)
        }
    }
    
    public func onAudioMixDidChanged(playerView: AUiPlayerAudioSettingView, audioMixIndex: Int) {
        aui_info("onAudioMixDidChanged: \(audioMixIndex)", tag: "AUiPlayerView")
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


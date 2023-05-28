//
//  AUIPlayerAudioSettingView.swift
//  AUIKit
//
//  Created by wushengtao on 2023/3/30.
//

import Foundation
import SwiftTheme

public class AUIPlayerAudioSettingItem: NSObject, AUITableViewItemProtocol {
    public var aui_style: AUITableViewCellStyle = .singleLabel
    
    public var aui_title: String?
    
    public var aui_subTitle: String?
    
    public var aui_Detail: String?
    
    public var aui_badge: String?
    
    public var aui_isSwitchOn: Bool = false
    
    public var onSwitchTapped: ((Bool) -> ())?
    
    public var onCellSelected: ((IndexPath) -> ())?
    
    public var uniqueId: Int = 0
    
    public var sliderMinValue: CGFloat = 0
    public var sliderMaxValue: CGFloat = 1
    public var sliderCurrentValue: CGFloat = 0.5
}

public protocol AUIPlayerAudioSettingViewDelegate: NSObjectProtocol {
    func onSliderCellWillLoad(playerView: AUIPlayerAudioSettingView, item: AUIPlayerAudioSettingItem)
    func onSwitchCellWillLoad(playerView: AUIPlayerAudioSettingView, item: AUIPlayerAudioSettingItem)
    func onSliderValueDidChanged(playerView: AUIPlayerAudioSettingView, value: CGFloat, item: AUIPlayerAudioSettingItem)
    func onSwitchValueDidChanged(playerView: AUIPlayerAudioSettingView, isSwitch: Bool, item: AUIPlayerAudioSettingItem)
    func onAudioMixDidChanged(playerView: AUIPlayerAudioSettingView, audioMixIndex: Int)
    func audioMixIsSelected(playerView: AUIPlayerAudioSettingView, audioMixIndex: Int) -> Bool
}

private let kEdgeSpace:CGFloat = 16
private let kAUIPlayerActionSheetCellId = "AUIPlayerActionSheetCellId"
public class AUIPlayerAudioSettingView: UIView {
    public weak var delegate: AUIPlayerAudioSettingViewDelegate?
    private(set) var mix_items: [AUIActionSheetItem] = []
    private var setting_items: [AUIPlayerAudioSettingItem] = []
    
    private lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.theme_font = "ActionSheet.normalFont"
        label.theme_textColor = "ActionSheet.normalColor"
        label.text = aui_localized("audioEffectSetting")
        return label
    }()
    
    private lazy var audioMixTitleLabel: UILabel = {
        let label = UILabel()
        label.theme_font = "ActionSheet.normalFont"
        label.theme_textColor = "ActionSheet.normalColor"
        label.text = aui_localized("audioMix")
        return label
    }()
    private lazy var tableView: AUITableView = {
        let tableView = AUITableView()
        tableView.delegate = self
        tableView.dataSource = self
        tableView.isScrollEnabled = false
        return tableView
    }()
    private lazy var flowLayout = UICollectionViewFlowLayout()
    private lazy var collectionView: UICollectionView = {
        let collectionView = UICollectionView(frame: .zero, collectionViewLayout: flowLayout)
        collectionView.register(AUIActionSheetCell.self, forCellWithReuseIdentifier: kAUIPlayerActionSheetCellId)
        collectionView.delegate = self
        collectionView.dataSource = self
        collectionView.backgroundColor = .clear
        
        return collectionView
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        _loadSubViews()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func _loadSubViews() {
        //load setting items
        let item1 = AUIPlayerAudioSettingItem()
        item1.aui_style = .multiLabelWithSwitch
        item1.aui_title = aui_localized("musicAudition")
        item1.aui_subTitle = aui_localized("musicAuditionDesc")
        item1.uniqueId = 0
        setting_items.append(item1)
        
        let item2 = AUIPlayerAudioSettingItem()
        item2.aui_style = .singleLabel
        item2.aui_title = aui_localized("musicVolume")
        item2.uniqueId = 1
        setting_items.append(item2)
        
        let item3 = AUIPlayerAudioSettingItem()
        item3.aui_style = .singleLabel
        item3.aui_title = aui_localized("recordingVolume")
        item3.uniqueId = 2
        setting_items.append(item3)
        
        let item4 = AUIPlayerAudioSettingItem()
        item4.aui_style = .singleLabel
        item4.aui_title = aui_localized("tones")
        item4.uniqueId = 3
        setting_items.append(item4)
        
        //load audiomix items
        
        for i in 0...8 {
            let item = AUIActionSheetThemeItem()
            item.title = aui_localized("audioMixType\(i + 1)")
            item.titleColor = "ActionSheet.normalColor"
            if i == 0 {
                item.backgroundIcon = "Player.voiceConversionDialogItemBackgroundIcon"
                item.icon = "Player.voiceConversionDialogItemIcon1"
            } else {
                item.backgroundIcon = ThemeImagePicker(keyPath: "Player.settingDialogAudioMixItemIcon\(i + 1)")
                item.icon = item.backgroundIcon
            }
            mix_items.append(item)
        }
        
        addSubview(titleLabel)
        addSubview(tableView)
        addSubview(audioMixTitleLabel)
        addSubview(collectionView)
    }
    
    open override func sizeThatFits(_ size: CGSize) -> CGSize {
        guard let windowFrame = getWindow()?.frame else {
            return .zero
        }
        return CGSize(width: windowFrame.width, height: 467)
    }
    
    public override func layoutSubviews() {
        super.layoutSubviews()
        
        titleLabel.sizeToFit()
        titleLabel.aui_tl = CGPoint(x: kEdgeSpace, y: kEdgeSpace)
        
        tableView.frame = CGRect(x: 0, y: titleLabel.aui_bottom + kEdgeSpace, width: aui_width, height: 257)
        
        audioMixTitleLabel.sizeToFit()
        audioMixTitleLabel.aui_tl = CGPoint(x: kEdgeSpace, y: tableView.aui_bottom)
        
        let width: CGFloat = 72
        let height: CGFloat = 80
        let padding: CGFloat = 8
        flowLayout.sectionInset = UIEdgeInsets(top: 0, left: padding, bottom: 0, right: padding)
        flowLayout.minimumInteritemSpacing = 0
        flowLayout.minimumLineSpacing = 0
        flowLayout.itemSize = CGSize(width: width, height: height)
        flowLayout.scrollDirection = .horizontal
        collectionView.showsHorizontalScrollIndicator = false
        collectionView.frame = CGRect(x: tableView.aui_left, y: audioMixTitleLabel.aui_bottom + kEdgeSpace, width: tableView.aui_width, height: height)
    }
}

extension AUIPlayerAudioSettingView: UITableViewDataSource, UITableViewDelegate {
    public func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        let count = setting_items.count
        return count
    }
    
    public func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let item = setting_items[indexPath.row]
        if item.aui_style.contains(.subTitle) {
            var cell: AUITableViewCell? = tableView.dequeueReusableCell(withIdentifier: "aui_cell") as? AUITableViewCell
            if cell == nil {
                cell = AUITableViewCell(style: .subtitle, reuseIdentifier: "aui_cell")
            }
            delegate?.onSwitchCellWillLoad(playerView: self, item: item)
            cell?.item = item
            item.onSwitchTapped = { [weak self] isOn in
                guard let self = self else {return}
                self.delegate?.onSwitchValueDidChanged(playerView: self, isSwitch: isOn, item: item)
            }
            cell?.setNeedsLayout()
            return cell!
        } else {
            var cell: AUITableViewSliderCell? = tableView.dequeueReusableCell(withIdentifier: "aui_slider_cell") as? AUITableViewSliderCell
            if cell == nil {
                cell = AUITableViewSliderCell(style: .subtitle, reuseIdentifier: "aui_slider_cell")
                cell?.onSliderValueDidChanged = { [weak self] value in
                    guard let self = self else {return}
                    self.delegate?.onSliderValueDidChanged(playerView: self, value: value, item: item)
                }
            }
            delegate?.onSliderCellWillLoad(playerView: self, item: item)
            cell?.item = item
            cell?.slider.minimumValue = item.sliderMinValue
            cell?.slider.maximumValue = item.sliderMaxValue
            cell?.slider.currentValue = item.sliderCurrentValue
            cell?.setNeedsLayout()
            return cell!
        }
    }
    
    public func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
        let item = setting_items[indexPath.row]
        item.onCellSelected?(indexPath)
    }
    
    public func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        let item = setting_items[indexPath.row]
        if item.aui_style.contains(.subTitle) {
            return 73
        }
        return 55
    }
}


extension AUIPlayerAudioSettingView: UICollectionViewDelegate, UICollectionViewDataSource {
    public func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return self.mix_items.count
    }
    
    public func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell: AUIActionSheetCell = collectionView.dequeueReusableCell(withReuseIdentifier: kAUIPlayerActionSheetCellId, for: indexPath) as! AUIActionSheetCell
        cell.itemType = .horizontal
        let item = self.mix_items[indexPath.row]
        item.callback = { [weak self] in
            guard let self = self else {return}
            self.delegate?.onAudioMixDidChanged(playerView: self, audioMixIndex: indexPath.row)
        }
        item.isSelected = { [weak self] in
            guard let self = self else {return false}
            return self.delegate?.audioMixIsSelected(playerView: self, audioMixIndex: indexPath.row) ?? false
        }
        cell.item = item
        return cell
    }
    
    public func numberOfSections(in collectionView: UICollectionView) -> Int {
        return 1
    }
    
    public func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        let item = self.mix_items[indexPath.row]
        item.callback?()
        collectionView.reloadData()
    }
}

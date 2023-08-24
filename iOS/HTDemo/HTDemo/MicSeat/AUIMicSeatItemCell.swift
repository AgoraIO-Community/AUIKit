//
//  AUIMicSeatItemCell.swift
//  AUIKit
//
//  Created by wushengtao on 2023/3/23.
//

import Foundation
import SDWebImage

//public enum MicRole: Int {
//    case mainSinger // 主唱
//    case coSinger   // 副唱
//    case onlineAudience // 上麦观众
//    case offlineAudience    // 没有上麦的观众
//}

/// 麦位管理对话框cell
open class AUIMicSeatItemCell: UICollectionViewCell {
    weak var item: AUIMicSeatCellDataProtocol? {
        didSet {
            reloadData()
        }
    }
    
    public var hiddenRipple = false
    
    lazy var rippleView: AUIRippleAnimationView = {
        let ripple = AUIRippleAnimationView(frame: CGRect(x: 0, y: 0, width: self.contentView.frame.width-30, height: self.contentView.frame.width-30)).backgroundColor(.clear)//"SeatItem.defaultImageWidth" 56
        return ripple
    }()
    
    public lazy var canvasView: UIView = {
        let view = UIView()
        view.backgroundColor = .black
        return view
    }()
    
    private lazy var avatarView: UIImageView = {
        let view = UIImageView()
//        view.backgroundColor = UIColor(red: 255, green: 255, blue: 255, alpha: 0.5)
        view.image = UIImage(named: "aui_transparent_icon_seat")
        return view
    }()
    
    private lazy var defaultImageView: UIImageView = {
        let imageView = UIImageView()
        imageView.contentMode = .scaleAspectFit
        return imageView
    }()
    
    //头像
    private lazy var avatarImageView: UIImageView = {
        let imageView = UIImageView()
        imageView.contentMode = .scaleAspectFill
        return imageView
    }()
    
    private lazy var seatLabel: UILabel = {
        let label = UILabel()
//        label.theme_font = "SeatItem.labelFont"
//        label.theme_textColor = "SeatItem.labelTextColor"
        label.textAlignment = .center
//        label.theme_attributedText = "SeatItem.seatLabelText"
        return label
    }()
    
    //静音/锁麦图标
    private lazy var statusImageView: UIImageView = {
        let imageView = UIImageView()
        return imageView
    }()
    
    //角色标记
    lazy var micRoleBtn: UIButton = {
        let theme = AUIButtonNativeTheme()
        let button = AUIButton()
        button.textImageAlignment = .imageLeftTextRight
        button.style = theme
        button.setTitle("主唱", for: .normal)
        button.setTitle("副唱", for: .selected)
        return button
    }()
    
    //房主标记
    lazy var subTitle: UILabel = {
        let label = UILabel(frame: CGRect(x: 5, y: self.avatarView.frame.maxY + 5, width: self.contentView.frame.width-10, height: 14)).backgroundColor(.clear).textAlignment(.center)
//        label.theme_font = "CommonFont.small"
//        label.theme_textColor = "SeatItem.labelTextColor"
        return label
    }()
    
    //房主标记
    lazy var hostIcon: AUIButton = {
        let theme = AUIButtonNativeTheme()
//        theme.titleFont = "SeatItem.micSeatHostSmall"
//        theme.icon = ThemeAnyPicker(keyPath: "SeatItem.micSeatHostIcon")
//        theme.buttonWidth = "SeatItem.micHostButtonWidth"
//        theme.buttonHeight = "SeatItem.micHostButtonHeight"
        let button = AUIButton()
        button.textImageAlignment = .imageCenterTextCenter
        button.style = theme
        button.setTitle("房主", for: .normal)
        return button
    }()
    
    public override init(frame: CGRect) {
        super.init(frame: frame)
        _loadView()
    }
    
    required public init?(coder: NSCoder) {
        super.init(coder: coder)
        _loadView()
    }
    
    private func _loadView() {
        addSubview(avatarView)
        addSubview(defaultImageView)
        addSubview(rippleView)
        addSubview(avatarImageView)
        addSubview(seatLabel)
        addSubview(micRoleBtn)
        addSubview(hostIcon)
        addSubview(statusImageView)
        avatarImageView.addSubview(canvasView)
        addSubview(subTitle)
        micRoleBtn.isHidden = true
        hostIcon.isHidden = true
    }
    
    private func updateRoleUI(with role:  MicRole) {
        switch role {
        case .mainSinger:
            micRoleBtn.isHidden = false
            micRoleBtn.isSelected = false
        case .coSinger:
            micRoleBtn.isHidden = false
            micRoleBtn.isSelected = true
        case .onlineAudience:
            micRoleBtn.isHidden = true
        case .offlineAudience:
            micRoleBtn.isHidden = true
        }
    }
    
    open override func layoutSubviews() {
        super.layoutSubviews()
        avatarView.aui_left = 15
        avatarView.aui_right = 15
        avatarView.aui_top = 5
        rippleView.frame = CGRect(x: 0, y: 0, width: self.contentView.frame.width-30, height: self.contentView.frame.width-30)
        rippleView.minimumCircleRadius = self.contentView.frame.width-48
        rippleView.diskRadius = (self.contentView.frame.width-30)/2.0

        avatarView.aui_size = CGSize(width: self.contentView.frame.width-30, height: self.contentView.frame.width-30)
        rippleView.aui_centerX = self.contentView.aui_centerX

        rippleView.animationDuration = 0.35

//        avatarView.frame = CGRect(x: 0, y: 0, width: self.bounds.width, height: self.frame.width)
        defaultImageView.aui_width = 24
        defaultImageView.aui_height = 24
        let size = defaultImageView.aui_size
        defaultImageView.frame = CGRect(x: (avatarView.bounds.width - size.width) / 2,
                                        y: (avatarView.bounds.height - size.height) / 2,
                                        width: size.width,
                                        height: size.height)
        defaultImageView.aui_centerX = self.contentView.aui_centerX
        defaultImageView.aui_centerY = self.avatarView.aui_centerY
        avatarImageView.frame = avatarView.bounds
        let r = self.rippleView.frame.width / 4.0
        let length = CGFloat(ceilf(Float(r) / sqrt(2)))
        self.statusImageView.frame = CGRect(x: r + length + size.width, y: r + length + size.height/2.0, width: size.width, height: size.height)
        statusImageView.aui_width = 24
        statusImageView.aui_height = 24
        
        seatLabel.frame = CGRect(x: 5, y: avatarView.frame.height + 5, width: frame.width-10, height: 20)

        
        subTitle.frame = CGRect(x: 5, y: self.seatLabel.frame.maxY, width: self.seatLabel.frame.width, height: 14)
        avatarView.layer.cornerRadius = (self.contentView.frame.width-30)/2.0
        avatarImageView.layer.cornerRadius = (self.contentView.frame.width-30)/2.0
        avatarView.clipsToBounds = true
        avatarImageView.clipsToBounds = true
        avatarView.aui_centerX = self.contentView.aui_centerX
        avatarImageView.aui_centerX = self.contentView.aui_centerX
        avatarImageView.aui_centerY = avatarView.aui_centerY
        rippleView.aui_centerY = avatarView.aui_centerY
        micRoleBtn.aui_centerX = aui_width / 2.0
        micRoleBtn.aui_top = seatLabel.aui_bottom
        
        hostIcon.aui_bottom = avatarView.aui_bottom
        hostIcon.aui_centerX = aui_width / 2.0
        
        canvasView.frame = avatarImageView.bounds
        canvasView.aui_centerX = self.contentView.aui_centerX
    }
    
    private func reloadData() {
        
        avatarImageView.sd_setImage(with: URL(string: item?.avatarUrl ?? ""))
        seatLabel.text = item?.seatName
        if let subtitle = item?.subTitle {
            self.subTitle.text = subtitle
        } else {
            subTitle.text = item?.subTitle
        }
        
        if let _ = item?.avatarUrl {
            avatarImageView.layer.borderColor = UIColor.red.cgColor
            avatarImageView.layer.borderWidth = 1
        } else {
            avatarImageView.layer.borderColor = UIColor.clear.cgColor
            avatarImageView.layer.borderWidth = 0
        }
        
        if item?.isLock ?? false {
            defaultImageView.image = UIImage(named:"aui_micseat_item_icon_lock")
        } else {
            defaultImageView.image = UIImage(named:"aui_micseat_item_icon_idle")
        }
        
        if item?.isMuteAudio ?? false {
            statusImageView.image = UIImage(named:"aui_micseat_item_icon_auido_mute")
        }else{
            statusImageView.image = nil
        }
        
        if item?.avatarUrl?.count ?? 0 > 0 {
            canvasView.alpha = item?.isMuteVideo ?? true ? 0 : 1
        } else {
            canvasView.alpha = 0
        }
        
        hostIcon.isHidden = item?.micSeat != 0
        updateRoleUI(with: item?.role ?? .offlineAudience)
        guard let mic = item else { return }
        if mic.role == .onlineAudience, mic.isMuteAudio == false,mic.isLock == false {
            rippleView.isHidden = false
        } else {
            rippleView.isHidden = true
        }
        if self.hiddenRipple {
            rippleView.isHidden = self.hiddenRipple
        }
        setNeedsLayout()
    }
    
    func updateVolume(_ volume: Int) {
        if !self.hiddenRipple {
            if volume > 1 {
                if !rippleView.isAnimating {
                    rippleView.startAnimation()
                }
            } else {
                rippleView.stopAnimation()
            }
        }
    }
    
}

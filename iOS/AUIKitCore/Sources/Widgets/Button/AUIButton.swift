//
//  AUIButton.swift
//  AUIKit
//
//  Created by wushengtao on 2023/3/29.
//

import Foundation
import SwiftTheme

@objc public enum AUIButtonTextImageAlignment: Int {
    case imageCenterTextCenter = 0
    case imageLeftTextRight
    case textLeftImageRight
    case imageTopTextBottom
    case textTopImageBottom
}

public class AUIButtonStyle: NSObject {
    public func setupStyle(button: AUIButton) {
    }
    
    public func layoutStyle(button: AUIButton) {
    }
}

public class AUIButtonDynamicTheme: AUIButtonStyle {
    public var icon: ThemeAnyPicker?
    public var selectedIcon: ThemeAnyPicker?
    public var iconWidth: ThemeCGFloatPicker = "Button.iconWidth"
    public var iconHeight: ThemeCGFloatPicker = "Button.iconWidth"
    public var padding: ThemeCGFloatPicker = "Button.padding"
    public var buttonWidth: ThemeCGFloatPicker = "Button.buttonWidth"
    public var buttonHeight: ThemeCGFloatPicker = "Button.buttonHeight"
    public var titleFont: ThemeFontPicker = "Button.titleFont"
    public var titleColor: ThemeColorPicker = "Button.titleColor"
    public var selectedTitleColor: ThemeColorPicker = "Button.titleColor"
    public var backgroundColor: ThemeColorPicker = AUIColor("Button.backgroundColor")
    public var cornerRadius: ThemeCGFloatPicker? = "Button.cornerRadius"
    public var textAlpha: ThemeCGFloatPicker = "Button.titleAlpha"
    public var highlightedBackgroundColor: ThemeColorPicker?
    public var selectedBackgroundColor: ThemeColorPicker?
    public var disabledBackgroundColor: ThemeColorPicker?
    public var borderColor: ThemeCGColorPicker?
    public var highlightedBorderColor: ThemeCGColorPicker?
    public var selectedBorderColor: ThemeCGColorPicker?
    public var disabledBorderColor: ThemeCGColorPicker?
    public var highlightedTitleColor: ThemeColorPicker?
    public var disabledTitleColor: ThemeColorPicker?
    public var highlightedIcon: ThemeAnyPicker?
    public var disabledIcon: ThemeAnyPicker?
    
    public static func appearanceTheme(appearance: String) -> AUIButtonDynamicTheme  {
        let theme = AUIButtonDynamicTheme()
        theme.iconWidth = ThemeCGFloatPicker(keyPath: "\(appearance).iconWidth")
        theme.iconHeight = ThemeCGFloatPicker(keyPath: "\(appearance).iconHeight")
        theme.padding = ThemeCGFloatPicker(keyPath: "\(appearance).padding")
        theme.buttonWidth = ThemeCGFloatPicker(keyPath: "\(appearance).buttonWidth")
        theme.buttonHeight = ThemeCGFloatPicker(keyPath: "\(appearance).buttonHeight")
        theme.titleFont = ThemeFontPicker(stringLiteral: "\(appearance).titleFont")
        theme.titleColor = AUIColor("\(appearance).titleColor")
        theme.selectedTitleColor = AUIColor("\(appearance).selectedTitleColor")
        theme.backgroundColor = AUIColor("\(appearance).backgroundColor")
        theme.cornerRadius = ThemeCGFloatPicker(keyPath: "\(appearance).cornerRadius")
        theme.textAlpha = ThemeCGFloatPicker(keyPath: "\(appearance).titleAlpha")
        theme.highlightedBackgroundColor = AUIColor("\(appearance).highlightedBackgroundColor")
        theme.selectedBackgroundColor = AUIColor("\(appearance).selectedBackgroundColor")
        theme.disabledBackgroundColor = AUIColor("\(appearance).disabledBackgroundColor")
        theme.borderColor = AUICGColor("\(appearance).borderColor")
        theme.highlightedBorderColor = AUICGColor("\(appearance).highlightedBorderColor")
        theme.selectedBorderColor = AUICGColor("\(appearance).selectedBorderColor")
        theme.disabledBorderColor = AUICGColor("\(appearance).disabledBorderColor")
        theme.highlightedTitleColor = AUIColor("\(appearance).highlightedTitleColor")
        theme.disabledTitleColor = AUIColor("\(appearance).disabledTitleColor")
        return theme
    }
    
    public static func toolbarTheme() -> AUIButtonDynamicTheme {
        let theme = AUIButtonDynamicTheme()
        theme.titleFont = "CommonFont.small"
        theme.iconWidth = "Player.toolIconWidth"
        theme.iconHeight = "Player.toolIconHeight"
        theme.buttonWidth = "Player.playButtonWidth"
        theme.buttonHeight = "Player.playButtonHeight"
        theme.cornerRadius = nil
        return theme
    }
    
    public override func setupStyle(button: AUIButton) {
        
        button.theme_sd_setImage(self.icon, forState: .normal)
        button.theme_sd_setImage(self.selectedIcon, forState: .selected)
        button.theme_sd_setImage(self.highlightedIcon, forState: .highlighted)
        button.theme_sd_setImage(self.disabledIcon, forState: .disabled)
        
        button.theme_setTitleColor(self.titleColor, forState: .normal)
        button.theme_setTitleColor(self.selectedTitleColor, forState: .selected)
        button.theme_setTitleColor(self.highlightedTitleColor, forState: .highlighted)
        button.theme_setTitleColor(self.disabledTitleColor, forState: .disabled)
        
        button.theme_backgroundColor = backgroundColor
        button.layer.theme_borderColor = borderColor
        if button.isHighlighted {
            button.theme_backgroundColor = highlightedBackgroundColor
            button.layer.theme_borderColor = highlightedBorderColor
        } else if button.isSelected {
            button.theme_backgroundColor = selectedBackgroundColor
            button.layer.theme_borderColor = selectedBorderColor
        } else if !button.isEnabled {
            button.theme_backgroundColor = disabledBackgroundColor
            button.layer.theme_borderColor = disabledBorderColor
        }
        
        button.theme_padding = padding
        button.titleLabel?.theme_alpha = textAlpha
    }
    
    public override func layoutStyle(button: AUIButton) {
        button.imageView?.theme_width = self.iconWidth
        button.imageView?.theme_height = self.iconHeight
        button.theme_width = self.buttonWidth
        button.theme_height = self.buttonHeight
        button.titleLabel?.theme_font = self.titleFont
        button.layer.theme_cornerRadius = self.cornerRadius
    }
}

public class AUIButtonNativeTheme: AUIButtonStyle {
    public var icon: String?
    public var selectedIcon: String?
    public var iconWidth: CGFloat = 0
    public var iconHeight: CGFloat = 0
    public var padding: CGFloat = 0
    public var buttonWidth: CGFloat = 240
    public var buttonHeight: CGFloat = 50
    public var titleFont: UIFont = UIFont(name: "PingFangSC-Semibold", size: 17)!
    public var titleColor: UIColor = .white
    public var selectedTitleColor: UIColor = .white
    public var backgroundColor: UIColor = .clear
    public var cornerRadius: CGFloat = 25
    public var textAlpha: CGFloat = 1
    
    public var highlightedBackgroundColor: UIColor?
    public var selectedBackgroundColor: UIColor?
    public var disabledBackgroundColor: UIColor?
    
    public var borderColor: CGColor?
    public var highlightedBorderColor: CGColor?
    public var selectedBorderColor: CGColor?
    public var disabledBorderColor: CGColor?
    
    public var highlightedTitleColor: UIColor?
    public var disabledTitleColor: UIColor?
    
    public var highlightedIcon: String?
    public var disabledIcon: String?
    
    public override func setupStyle(button: AUIButton) {
        button.aui_setImage(self.icon, for: .normal)
        button.aui_setImage(self.selectedIcon, for: .selected)
        button.aui_setImage(self.highlightedIcon, for: .highlighted)
        button.aui_setImage(self.disabledIcon, for: .disabled)
        
        button.setTitleColor(self.titleColor, for: .normal)
        button.setTitleColor(self.selectedTitleColor, for: .selected)
        button.setTitleColor(self.highlightedTitleColor, for: .highlighted)
        button.setTitleColor(self.disabledTitleColor, for: .disabled)
        
        button.backgroundColor = backgroundColor
        button.layer.borderColor = borderColor
        if button.isHighlighted {
            button.backgroundColor = highlightedBackgroundColor
            button.layer.borderColor = highlightedBorderColor
        } else if button.isSelected {
            button.backgroundColor = selectedBackgroundColor
            button.layer.borderColor = selectedBorderColor
        } else if !button.isEnabled {
            button.backgroundColor = disabledBackgroundColor
            button.layer.borderColor = disabledBorderColor
        }
        
        button.padding = padding
        button.titleLabel?.alpha = textAlpha
    }
    
    public override func layoutStyle(button: AUIButton) {
        button.imageView?.aui_width = self.iconWidth
        button.imageView?.aui_height = self.iconHeight
        button.aui_width = self.buttonWidth
        button.aui_height = self.buttonHeight
        button.layer.cornerRadius = self.cornerRadius
        button.titleLabel?.font = self.titleFont
    }
}

open class AUIButton: UIButton {
    open override var isEnabled: Bool {
        didSet {
            style?.setupStyle(button: self)
        }
    }
    
    open override var isSelected: Bool {
        didSet {
            style?.setupStyle(button: self)
        }
    }
    
    open override var isHighlighted: Bool {
        didSet {
            style?.setupStyle(button: self)
        }
    }
    
    @objc public var textImageAlignment: AUIButtonTextImageAlignment = .imageCenterTextCenter {
        didSet {
            setNeedsLayout()
        }
    }
    public var style: AUIButtonStyle? {
        didSet {
            style?.setupStyle(button: self)
            style?.layoutStyle(button: self)
            setNeedsLayout()
            layoutIfNeeded()
        }
    }
    
    @objc fileprivate var padding: CGFloat = 0 {
        didSet {
            setNeedsLayout()
        }
    }
    
    public override init(frame: CGRect) {
        super.init(frame: frame)
        _loadSubViews()
    }
    
    required public init?(coder: NSCoder) {
        super.init(coder: coder)
        _loadSubViews()
    }
    
    @objc fileprivate func aui_setImage(_ image: String?, for state: UIControl.State) {
        guard let image = image else { return }
        
        if let fileUrl = URL.aui_imageFileURL(named: image) {
            self.sd_setImage(with: fileUrl, for: state)
        }
    }
    
    open override func layoutSubviews() {
        super.layoutSubviews()
        guard let imageView = imageView,let titleLabel = titleLabel else {return}
        style?.layoutStyle(button: self)
        titleLabel.sizeToFit()
        
        switch textImageAlignment {
        case .imageLeftTextRight:
            let width = imageView.aui_width + titleLabel.aui_width + padding
            imageView.aui_center = CGPoint(x: (aui_width - width) / 2 + imageView.aui_width / 2, y: aui_height / 2)
            titleLabel.aui_center = CGPoint(x: imageView.aui_right + titleLabel.aui_width / 2 + padding, y: imageView.aui_centerY)
        case .textLeftImageRight:
            let width = imageView.aui_width + titleLabel.aui_width
            titleLabel.aui_center = CGPoint(x: (aui_width - width) / 2 + titleLabel.aui_width / 2, y: aui_height / 2)
            imageView.aui_center = CGPoint(x: titleLabel.aui_right + imageView.aui_width / 2 + padding, y: titleLabel.aui_centerY)
        case .imageTopTextBottom:
            let height = imageView.aui_height + titleLabel.aui_height + padding
            imageView.aui_center = CGPoint(x: aui_width / 2, y: (aui_height - height) / 2 + imageView.aui_height / 2)
            titleLabel.aui_center = CGPoint(x: imageView.aui_centerX, y: imageView.aui_bottom + titleLabel.aui_height / 2 + padding)
        case .textTopImageBottom:
            let height = imageView.aui_height + titleLabel.aui_height
            titleLabel.aui_center = CGPoint(x: aui_width / 2, y: (aui_height - height) / 2 + titleLabel.aui_height / 2)
            imageView.aui_center = CGPoint(x: titleLabel.aui_centerX, y: titleLabel.aui_bottom + imageView.aui_height / 2 + padding)
        default:
            titleLabel.center = CGPoint(x: aui_width / 2, y: aui_height / 2)
            imageView.center = CGPoint(x: aui_width / 2, y: aui_height / 2)
        }
    }
    
    private func _loadSubViews() {
        imageView?.contentMode = .scaleAspectFit
        self.clipsToBounds = true
    }
}


extension AUIButton {
    var theme_padding: ThemeCGFloatPicker? {
        get { return aui_getThemePicker(self, "setPadding:") as? ThemeCGFloatPicker }
        set { aui_setThemePicker(self, "setPadding:", newValue) }
    }
    
    func theme_sd_setImage(_ picker: ThemeAnyPicker?, forState state: UIControl.State) {
        let statePicker = ThemePicker.makeStatePicker(self, "aui_setImage:for:", picker, state)
        ThemePicker.setThemePicker(self, "aui_setImage:for:", statePicker)
    }
}

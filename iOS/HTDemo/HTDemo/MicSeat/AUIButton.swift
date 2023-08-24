//
//  AUIButton.swift
//  AUIKit
//
//  Created by wushengtao on 2023/3/29.
//

import Foundation
import UIKit

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
        if let fileUrl = URL(string: image) {
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

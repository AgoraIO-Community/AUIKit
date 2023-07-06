//
//  UIKitThemeDSL.swift
//  AUIKit
//
//  Created by 朱继超 on 2023/6/19.
//

import UIKit
import SwiftTheme

@objc public extension UIView
{
    @discardableResult
    func cornerThemeRadius(_ radius: String) -> Self  {
        let view = self
        view.layer.masksToBounds = true
        view.layer.theme_cornerRadius = ThemeCGFloatPicker(keyPath: radius)
        return view
    }
    
    @discardableResult
    func layerThemeProperties(_ color: String,_ width: String) -> Self {
        let view = self
        view.layer.theme_borderWidth(width: width).theme_borderColor(color: color)
        return view
    }
    
    @discardableResult
    func createThemeGradient(_ color: String, _ points: [CGPoint]) -> Self {
        let startPoint = points[0]
        let endPoint = points[1]
        let gradientLayer = CAGradientLayer().startPoint(startPoint).endPoint(endPoint).frame(bounds).backgroundColor(UIColor.clear.cgColor).locations([0,1])
        gradientLayer.theme_colors = AUIGradientColor(color)
        layer.insertSublayer(gradientLayer, at: 0)
        return self
    }
    
    @discardableResult
    func theme_alpha(alpha: String) -> Self {
        self.theme_alpha = ThemeCGFloatPicker(keyPath: alpha)
        return self
    }
    @discardableResult
    func theme_backgroundColor(color: String) -> Self {
        self.theme_backgroundColor = ThemeColorPicker(keyPath: color)
        return self
    }
    @discardableResult
    func theme_tintColor(color: String) -> Self {
        self.theme_tintColor = ThemeColorPicker(keyPath: color)
        return self
    }
}


@objc public extension UILabel
{
    @discardableResult
    func theme_font(font: String) -> Self {
        self.theme_font = ThemeFontPicker(stringLiteral: font)
        return self
    }
    
    @discardableResult
    func theme_textColor(color: String) -> Self {
        self.theme_textColor = ThemeColorPicker(keyPath: color)
        return self
    }
    
    @discardableResult
    func theme_highlightedTextColor(color: String) -> Self {
        self.theme_highlightedTextColor = ThemeColorPicker(keyPath: color)
        return self
    }
    @discardableResult
    func theme_shadowColor(color: String) -> Self {
        self.theme_shadowColor = ThemeColorPicker(keyPath: color)
        return self
    }
    @discardableResult
    func theme_textAttributes(attributes: [NSAttributedString.Key: Any]) -> Self {
        self.theme_textAttributes = ThemeStringAttributesPicker(attributes)
        return self
    }
    @discardableResult
    func theme_attributedText(attributeText: NSAttributedString) -> Self {
        self.theme_attributedText = ThemeAttributedStringPicker([attributeText])
        return self
    }
}

@objc public extension UITableView
{
    @discardableResult
    func theme_separatorColor(color: String) -> Self {
        self.theme_separatorColor = ThemeColorPicker(keyPath: color)
        return self
    }
    
}

@objc public extension UITextField
{
    @discardableResult
    func theme_font(font: String) -> Self {
        self.theme_font = ThemeFontPicker(stringLiteral: font)
        return self
    }
    
    @discardableResult
    func theme_textColor(color: String) -> Self {
        self.theme_textColor = ThemeColorPicker(keyPath: color)
        return self
    }
    
    @discardableResult
    func theme_placeholderAttributes(attributes: [NSAttributedString.Key: Any]) -> Self {
        self.theme_placeholderAttributes = ThemeStringAttributesPicker(attributes)
        return self
    }
    
}
@objc public extension UITextView
{
    @discardableResult
    func theme_font(font: String) -> Self {
        self.theme_font = ThemeFontPicker(stringLiteral: font)
        return self
    }
    
    @discardableResult
    func theme_textColor(color: String) -> Self {
        self.theme_textColor = ThemeColorPicker(keyPath: color)
        return self
    }
}

@objc public extension UIImageView
{
    @discardableResult
    func theme_image(image: String) -> Self {
        self.theme_image = ThemeImagePicker(keyPath: image)
        return self
    }
}

@objc public extension UIButton
{
    @discardableResult
    func themeImage(_ image: String, forState state: UIControl.State) -> Self {
        let statePicker = makeStatePicker(self, "setImage:forState:", ThemeImagePicker(keyPath: image), state)
        setThemePicker(self, "setImage:forState:", statePicker)
        return self
    }
    @discardableResult
    func theme_setBackgroundImage(_ image: String, forState state: UIControl.State) -> Self {
        let statePicker = makeStatePicker(self, "setBackgroundImage:forState:", ThemeImagePicker(keyPath: image), state)
        setThemePicker(self, "setBackgroundImage:forState:", statePicker)
        return self
    }
    @discardableResult
    func themeTitleColor(_ color: String, forState state: UIControl.State) -> Self  {
        let statePicker = makeStatePicker(self, "setTitleColor:forState:", ThemeColorPicker(keyPath: color), state)
        setThemePicker(self, "setTitleColor:forState:", statePicker)
        return self
    }
 
    @discardableResult
    func theme_font(_ font: String) -> Self {
        self.titleLabel?.theme_font = ThemeFontPicker(stringLiteral: font)
        return self
    }
    
}

@objc public extension CALayer
{
    @discardableResult
    func theme_backgroundColor(color: String) -> Self {
        self.theme_backgroundColor = ThemeCGColorPicker(keyPath: color)
        return self
    }
    
    @discardableResult
    func theme_borderWidth(width: String) -> Self {
        self.theme_borderWidth = ThemeCGFloatPicker(keyPath: width)
        return self
    }
    
    @discardableResult
    func theme_borderColor(color: String) -> Self {
        self.theme_borderColor = ThemeCGColorPicker(keyPath: color)
        return self
    }
    
    @discardableResult
    func theme_shadowColor(color: String) -> Self {
        self.theme_shadowColor = ThemeCGColorPicker(keyPath: color)
        return self
    }
    
    @discardableResult
    func theme_strokeColor(color: String) -> Self {
        self.theme_strokeColor = ThemeCGColorPicker(keyPath: color)
        return self
    }
    @discardableResult
    func theme_fillColor(color: String) -> Self {
        self.theme_fillColor = ThemeCGColorPicker(keyPath: color)
        return self
    }
}




private func getThemePicker(
    _ object : NSObject,
    _ selector : String
) -> ThemePicker? {
    return ThemePicker.getThemePicker(object, selector)
}

private func setThemePicker(
    _ object : NSObject,
    _ selector : String,
    _ picker : ThemePicker?
) {
    return ThemePicker.setThemePicker(object, selector, picker)
}

private func makeStatePicker(
    _ object : NSObject,
    _ selector : String,
    _ picker : ThemePicker?,
    _ state : UIControl.State
) -> ThemePicker? {
    return ThemePicker.makeStatePicker(object, selector, picker, state)
}

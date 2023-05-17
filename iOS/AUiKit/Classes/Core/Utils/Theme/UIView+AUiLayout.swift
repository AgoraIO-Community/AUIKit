//
//  UIView+AUiLayout.swift
//  AUiKit
//
//  Created by wushengtao on 2023/3/26.
//

import Foundation
import SwiftTheme

public func aui_getThemePicker(
    _ object : NSObject,
    _ selector : String
) -> ThemePicker? {
    return ThemePicker.getThemePicker(object, selector)
}

public func aui_setThemePicker(
    _ object : NSObject,
    _ selector : String,
    _ picker : ThemePicker?
) {
    return ThemePicker.setThemePicker(object, selector, picker)
}

@objc public extension UIView {
    var theme_width: ThemeCGFloatPicker? {
        get { return aui_getThemePicker(self, "setAui_width:") as? ThemeCGFloatPicker }
        set { aui_setThemePicker(self, "setAui_width:", newValue) }
    }

    var theme_height: ThemeCGFloatPicker? {
        get { return aui_getThemePicker(self, "setAui_height:") as? ThemeCGFloatPicker }
        set { aui_setThemePicker(self, "setAui_height:", newValue) }
    }
    
    var theme_centerX: ThemeCGFloatPicker? {
        get { return aui_getThemePicker(self, "setAui_centerX:") as? ThemeCGFloatPicker }
        set { aui_setThemePicker(self, "setAui_centerX:", newValue) }
    }

    var theme_centerY: ThemeCGFloatPicker? {
        get { return aui_getThemePicker(self, "setAui_centerY:") as? ThemeCGFloatPicker }
        set { aui_setThemePicker(self, "setAui_centerY:", newValue) }
    }
}

@objc public extension CALayer {
    var theme_cornerRadius: ThemeCGFloatPicker? {
        get { return aui_getThemePicker(self, "setCornerRadius:") as? ThemeCGFloatPicker }
        set { aui_setThemePicker(self, "setCornerRadius:", newValue) }
    }
}

//
//  auiThemeImage.swift
//  AUIKit
//
//  Created by wushengtao on 2023/5/4.
//

import SwiftTheme

public func auiThemeImage(_ keyPath: String) -> ThemeImagePicker? {
    ThemeImagePicker {
        if let imageName = ThemeManager.string(for: keyPath), let image = UIImage.aui_Image(named: imageName) {
            return image
        }
        
        return ThemeManager.image(for: keyPath)
    }
}

public func auiThemeAnimatedImagePath(_ keyPath: String) -> ThemeAnyPicker? {
    ThemeAnyPicker {
        if let fileUrl = String.aui_animatedImageFilePath(named: keyPath) {
            return fileUrl
        }
        return nil
    }
}

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

public func auiThemeImageURL(_ keyPath: String) -> ThemeAnyPicker? {
    
    ThemeAnyPicker {
        if let name = ThemeManager.string(for: keyPath), let filePath = String.aui_animatedImageFilePath(named: name) {
            var fileUrl = URL(fileURLWithPath: filePath)
            if fileUrl.pathExtension.isEmpty {
                fileUrl.appendPathExtension("png")
            }
            return fileUrl
        }
        return nil
    }
}

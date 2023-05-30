//
//  UIImage+AUIKit.swift
//  AUIKit
//
//  Created by wushengtao on 2023/4/3.
//

import UIKit
import SwiftTheme

extension UIImage {
    public class func aui_Image(named: String) -> UIImage? {
        for path in AUIRoomContext.shared.themeResourcePaths {
            let filePath = path.appendingPathComponent(named).path
            if let image = UIImage(contentsOfFile: filePath) {
                return image
            }
        }
        if let filePath = ThemeManager.currentThemePath?.URL?.appendingPathComponent(named).path {
            return UIImage(contentsOfFile: filePath)
        }
        
        return nil
    }
}

extension String {
    public static func aui_animatedImageFilePath(named: String) -> String? {
        for path in AUIRoomContext.shared.themeResourcePaths {
            let filePath = path.appendingPathComponent(named).path
            return filePath
        }
        if let filePath = ThemeManager.currentThemePath?.URL?.appendingPathComponent(named).path {
            return filePath
        }
        return nil
    }
}

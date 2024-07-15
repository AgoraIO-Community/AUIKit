//
//  UIImage+AUIKit.swift
//  AUIKit
//
//  Created by wushengtao on 2023/4/3.
//

import UIKit
import SwiftTheme

//public var themeResourcePaths: Set<URL> = Set()
extension UIImage {
    public class func aui_Image(named: String) -> UIImage? {
        for path in AUIThemeManager.shared.themeResourcePaths {
            let filePath = path.appendingPathComponent(named).path
            if let image = UIImage(contentsOfFile: filePath) {
                return image
            }
        }
        if let filePath = ThemeManager.currentThemePath?.URL?.appendingPathComponent(named).path {
            return UIImage(contentsOfFile: filePath)
        }
        
        print("load image not found imagefilePath: \(ThemeManager.currentThemePath?.URL?.appendingPathComponent(named).path ?? named)")
        assert(false)

        return nil
    }
}

extension String {
    public static func aui_imageFilePath(named: String) -> String? {
        for path in AUIThemeManager.shared.themeResourcePaths {
            let filePath = path.appendingPathComponent(named).path.appendPngExentionIfEmpty()
            if FileManager.default.fileExists(atPath: filePath) {
                return filePath
            }
        }
        if let filePath = ThemeManager.currentThemePath?.URL?.appendingPathComponent(named).path.appendPngExentionIfEmpty() {
            return filePath
        }
        return nil
    }
    
    private func appendPngExentionIfEmpty() -> String{
        let url = URL(fileURLWithPath: self)
        if url.pathExtension.isEmpty {
            return appending(".png")
        }
        return self
    }
    
}

extension URL {
    public static func aui_imageFileURL(named: String) -> URL? {
        if let path = String.aui_imageFilePath(named: named) {
            return URL(fileURLWithPath: path)
        }
        return nil
    }
    
   
}

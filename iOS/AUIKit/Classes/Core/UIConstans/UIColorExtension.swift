//
//  UIColorExtension.swift
//  AUIKit
//
//  Created by 朱继超 on 2023/5/15.
//

import UIKit
//MARK: - UIColor extension
public extension UIColor {
    
    /// 生成随机色
    static var randomColor: UIColor {
        let r = CGFloat.random(in: 0...1)
        let g = CGFloat.random(in: 0...1)
        let b = CGFloat.random(in: 0...1)
        let a = CGFloat.random(in: 0...1)
        return UIColor(red: r, green: g, blue: b, alpha: a)
    }
    
    /// Description  init with 0xabcd123
    /// - Parameter rgbValue: hex
    convenience init(_ rgbValue: UInt) {
        self.init(red: CGFloat((CGFloat((rgbValue & 0xff0000) >> 16)) / 255.0),
                  green: CGFloat((CGFloat((rgbValue & 0x00ff00) >> 8)) / 255.0),
                  blue: CGFloat((CGFloat(rgbValue & 0x0000ff)) / 255.0),
                  alpha: 1.0)
    }
}


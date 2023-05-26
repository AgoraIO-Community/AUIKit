//
//  NSObjectExtension.swift
//  AUIKit
//
//  Created by 朱继超 on 2023/5/22.
//

import Foundation

public extension NSObject {
    
    var a: AUIKitSwiftLib<NSObject> {
        AUIKitSwiftLib.init(self)
    }
    
}
    

public extension AUIKitSwiftLib where Base == NSObject {
    
    var swiftClassName: String? {
        let className = type(of: base).description().components(separatedBy: ".").last
        return  className
    }
}

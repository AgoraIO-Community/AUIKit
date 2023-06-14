//
//  UIImageExtension.swift
//  AUIKit
//
//  Created by 朱继超 on 2023/5/15.
//

import UIKit


public extension UIImage {
    convenience init?(_ bundleResourceName: String,_ type: AUIBundleType) {
        if #available(iOS 13.0, *) {
            self.init(named: bundleResourceName, in: Bundle.bundle(for: type), with: nil)
        } else {
            self.init(named: bundleResourceName, in: Bundle.bundle(for: type), compatibleWith: nil)
        }
    }
}

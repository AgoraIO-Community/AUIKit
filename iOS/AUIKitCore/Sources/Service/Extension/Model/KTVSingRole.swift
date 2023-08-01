//
//  KTVSingRole.swift
//  AUIKitCore
//
//  Created by FanPengpeng on 2023/8/1.
//

import Foundation

/// 用户角色
@objc public enum KTVSingRole: Int {
    case soloSinger = 0     //独唱者
    case coSinger           //伴唱
    case leadSinger         //主唱
    case audience           //观众
//    case followSinger       //跟唱
}

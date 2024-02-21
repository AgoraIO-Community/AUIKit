//
//  AUIUserCellUserDataProtocol.swift
//  AUIKitCore
//
//  Created by FanPengpeng on 2023/8/3.
//

import Foundation

@objc public protocol AUIUserCellUserDataProtocol: NSObjectProtocol {
    var userAvatar: String {set get}
    var userId: String {set get}
    var userName: String {set get}
    var seatIndex: Int {set get}
    var isOwner: Bool {get}
}

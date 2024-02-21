//
//  AUIMicSeatCellDataProtocol.swift
//  AUIKit
//
//  Created by wushengtao on 2023/4/6.
//

import Foundation

public protocol AUIMicSeatCellDataProtocol: NSObjectProtocol {
    var seatName: String {get}
    var subTitle: String {get}
    var subIcon: String {get}
    var isMuteAudio: Bool {get}
    var isMuteVideo: Bool {get}
    var isLock: Bool {get}
    var avatarUrl: String? {get}
    var role: MicRole {get}
    var micSeat: UInt {get}
    var isEmptySeat: Bool {get}
}

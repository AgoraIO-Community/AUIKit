//
//  AUiMicSeatCellDataProtocol.swift
//  AUiKit
//
//  Created by wushengtao on 2023/4/6.
//

import Foundation

public protocol AUiMicSeatCellDataProtocol: NSObjectProtocol {
    var seatName: String {get}
    var isMuteAudio: Bool {get}
    var isMuteVideo: Bool {get}
    var isLock: Bool {get}
    var avatarUrl: String? {get}
    var role: MicRole {get}
    var micSeat: UInt {get}
}

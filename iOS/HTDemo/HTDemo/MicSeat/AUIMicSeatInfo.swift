//
//  AUIMicSeatInfo.swift
//  HTDemo
//
//  Created by FanPengpeng on 2023/8/24.
//

import UIKit


class AUIMicSeatInfo: NSObject, AUIMicSeatCellDataProtocol {
    var role: MicRole = .offlineAudience
    
    var seatName: String = ""
    
    var subTitle: String = ""
    
    var subIcon: String = ""
    
    var isMuteAudio: Bool = false
    
    var isMuteVideo: Bool = true
    
    var isLock: Bool = false
    
    var avatarUrl: String?
    
    var micSeat: UInt = 0
    
    var onSeat = false
}

public enum MicRole: Int {
    case mainSinger // 主唱
    case coSinger   // 副唱
    case onlineAudience // 上麦观众
    case offlineAudience    // 没有上麦的观众
}

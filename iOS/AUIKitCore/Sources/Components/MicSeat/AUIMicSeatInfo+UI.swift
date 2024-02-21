//
//  AUIMicSeatInfo+UI.swift
//  AUIKit
//
//  Created by wushengtao on 2023/4/6.
//

import Foundation

extension AUIMicSeatInfo: AUIMicSeatCellDataProtocol {
    public var subTitle: String {
        ""
    }
    
    public var subIcon: String {
        ""
    }
    
    public var isMuteAudio: Bool {
        var userMuteAudio = kUserMuteAudioInitStatus
        if let fullUser = user as? AUIUserInfo {
            userMuteAudio = fullUser.muteAudio
        }
        return muteAudio || userMuteAudio
    }
    
    public var isMuteVideo: Bool {
        //TODO: remove it
        return true
        
//        var userMuteVideo: Bool = kUserMuteVideoInitStatus
//        if let fullUser = user as? AUIUserInfo {
//            userMuteVideo = fullUser.muteVideo
//        }
//        return muteVideo || userMuteVideo
    }
    
    public var isLock: Bool {
        return lockSeat == .locked
    }
    
    public var avatarUrl: String? {
        return self.user?.userAvatar
    }
    
    public var seatName: String {
        return self.seatIndexDesc()
    }
    
    //todo shengtao 优化
    public var role: MicRole {
        return self.micRole
    }
    
    public var micSeat: UInt {
        return self.seatIndex
    }
    
    public var isEmptySeat: Bool {
        return self.user?.userId.isEmpty ?? true
    }
}

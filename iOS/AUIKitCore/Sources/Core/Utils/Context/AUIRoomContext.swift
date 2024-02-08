//
//  AUIRoomContext.swift
//  AUIKit
//
//  Created by wushengtao on 2023/3/3.
//

import Foundation

open class AUIRoomContext: NSObject {
    public static let shared: AUIRoomContext = AUIRoomContext()
    public let currentUserInfo: AUIUserThumbnailInfo = AUIUserThumbnailInfo()
    public var commonConfig: AUICommonConfig? {
        didSet {
            guard let userInfo = commonConfig?.owner else {return}
            currentUserInfo.userName = userInfo.userName
            currentUserInfo.userId = userInfo.userId
            currentUserInfo.userAvatar = userInfo.userAvatar
        }
    }
    
    public var roomInfoMap: [String: AUIRoomInfo] = [:]
    public var roomConfigMap: [String: AUIRoomConfig] = [:]
    public var roomArbiterMap: [String: AUIArbiter] = [:]
    
    public var seatType: AUIMicSeatViewLayoutType = .eight {
        willSet {
            switch newValue {
            case .one: self.seatCount = 1
            case .six: self.seatCount = 6
            case .eight: self.seatCount = 8
            case .nine: self.seatCount = 9
            }
        }
    }
    
    public var seatCount: UInt = 8
    
    private var ntpTimeClosure: (()-> Int64)?
    
    public func setNtpTime(callback: @escaping ()-> Int64) {
        self.ntpTimeClosure = callback
    }
    
    public func getNtpTime() -> Int64 {
        return ntpTimeClosure?() ?? Int64(Date().timeIntervalSince1970 * 1000)
    }
    
    public func isRoomOwner(channelName: String) ->Bool {
        return isRoomOwner(channelName: channelName, userId: currentUserInfo.userId)
    }
    
    public func isRoomOwner(channelName: String, userId: String) ->Bool {
        return roomInfoMap[channelName]?.owner?.userId == userId
    }
    
    public func getArbiter(channelName: String) -> AUIArbiter? {
//        guard let _ = roomInfoMap[channelName] else {return nil}
        if let handler = roomArbiterMap[channelName] {
            return handler
        }
        
//        assert(false, "arbiter == nil!")
        return nil
    }
    
    public func clean(channelName: String) {
        roomConfigMap[channelName] = nil
        roomInfoMap[channelName] = nil
        roomArbiterMap[channelName] = nil
    }
    
    public private(set) var currentThemeName: String?
    
    public private(set) var themeIdx = 0

}

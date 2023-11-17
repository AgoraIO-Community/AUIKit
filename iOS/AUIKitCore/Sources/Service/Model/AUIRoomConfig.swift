//
//  AUIRoomConfig.swift
//  AUIKit
//
//  Created by wushengtao on 2023/2/24.
//

import Foundation

@objcMembers
open class AUICommonConfig: NSObject {
    public var appId: String = ""
    /// 网络请求域名
    public var host: String = ""
    
    //用户信息
    public var userId: String = ""
    public var userName: String = ""
    public var userAvatar: String = ""
    
    
    public func isValidate() -> Bool {
        if appId.isEmpty || host.isEmpty || userId.isEmpty || userName.isEmpty  {
            return false
        }
        
        return true
    }
    
    open override var description: String {
        return "AUICommonConfig: userId: \(userId) userName: \(userName)"
    }
}

@objcMembers
open class AUIRoomConfig: NSObject {
    public var channelName: String = ""     //正常rtm使用的频道
    public var rtmToken007: String = ""     //rtm login用，只能007
    public var rtcToken007: String = ""     //rtm join用(rtm stream channel)
    
    public var rtcChannelName: String = ""  //rtc使用的频道
    public var rtcRtcToken: String = ""  //rtc join使用
    public var rtcRtmToken: String = ""  //rtc mcc使用
    
    public var rtcChorusChannelName: String = ""  //rtc 合唱使用的频道
    public var rtcChorusRtcToken: String = ""  //rtc 合唱join使用
}


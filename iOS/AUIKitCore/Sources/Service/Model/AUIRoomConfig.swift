//
//  AUIRoomConfig.swift
//  AUIKit
//
//  Created by wushengtao on 2023/2/24.
//

import Foundation

@objcMembers
open class AUICommonConfig: NSObject {
    /// 声网AppId
    public var appId: String = ""
    /// 声网App证书(可选，如果没有用到后端token生成服务可以不设置)
    public var appCert: String = ""
    /// 域名(可选，如果没有用到后端服务可以不设置)
    public var host: String = "" //(optional)
    /// 用户信息
    public var owner: AUIUserThumbnailInfo?
    
    public func isValidate() -> Bool {
        if appId.isEmpty || owner?.isEmpty() ?? true  {
            return false
        }
        
        return true
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


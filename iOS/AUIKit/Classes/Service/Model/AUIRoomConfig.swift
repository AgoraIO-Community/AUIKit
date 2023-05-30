//
//  AUIRoomConfig.swift
//  AUIKit
//
//  Created by wushengtao on 2023/2/24.
//

import Foundation

open class AUICommonConfig: NSObject {
    /// appid
    public var appId: String = ""
    /// 网络请求域名
    public var host: String = ""
    
    //用户信息
    public var userId: String = ""
    public var userName: String = ""
    public var userAvatar: String = ""
    
    open override var description: String {
        return "AUICommonConfig: userId: \(userId) userName: \(userName)"
    }
}

open class AUIRoomConfig: NSObject {
    public var channelName: String = ""     //正常rtm使用的频道
    public var rtmToken007: String = ""     //rtm login用，只能007
    public var rtcToken007: String = ""     //rtm join用
    
    public var rtcChannelName: String = ""  //rtc使用的频道
    public var rtcRtcToken: String = ""  //rtc join使用
    public var rtcRtmToken: String = ""  //rtc mcc使用
    
    public var rtcChorusChannelName: String = ""  //rtc 合唱使用的频道
    public var rtcChorusRtcToken: String = ""  //rtc 合唱join使用
}


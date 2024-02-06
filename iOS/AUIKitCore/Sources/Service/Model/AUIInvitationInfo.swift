//
//  AUIInvitationInfo.swift
//  AUIKitCore
//
//  Created by wushengtao on 2024/2/5.
//

import Foundation

@objc public enum AUIInvitationType: Int {
    case apply = 1     // 观众申请
    case invite        // 主播邀请
}

@objc public  enum AUIInvitationStatus: Int {
    case waiting = 1   // 等待确认
    case accept = 2    // 同意
    case reject = 3    // 拒绝
    case timeout = 4   // 超时
    case cancel = 5    // 取消
}

@objcMembers public class AUIInvitationInfo: NSObject {
    public var userId: String = ""
    public var seatNo: Int = 0
    public var type: AUIInvitationType = .apply
    public var status: AUIInvitationStatus = .waiting
    public var createTime: Int64 = 0
    public var editTime: Int64 = 0
}

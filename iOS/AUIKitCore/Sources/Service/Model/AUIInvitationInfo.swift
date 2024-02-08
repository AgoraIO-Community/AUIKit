//
//  AUIInvitationInfo.swift
//  AUIKitCore
//
//  Created by wushengtao on 2024/2/5.
//

import Foundation

let kInvitationTimeoutTs: Int64 = 10 * 1000
let kInvitationInvalidTs: Int64 = 20 * 1000

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
    public var createTime: Int64 = 0            //创建时间，和19700101的差，单位ms
    public var editTime: Int64 = 0              //编辑时间，和19700101的差，单位ms
    public var timeoutTs: Int64 = kInvitationTimeoutTs     //请求超时时间，单位ms
    public var invalidTs: Int64 = kInvitationInvalidTs    //无效数据(status非waitting)保存时间，单位ms
}

//
//  AUIInvitationInfo.swift
//  AUIKitCore
//
//  Created by wushengtao on 2024/2/5.
//

import Foundation

@objc enum AUIInvitationType: Int {
    case apply = 1     // 观众申请
    case invite        // 主播邀请
}

@objc enum AUIInvitationStatus: Int {
    case waiting = 1   // 等待确认
    case accept = 2    // 同意
    case reject = 3    // 拒绝
    case timeout = 4   // 超时
    case cancel = 5    // 取消
}

@objcMembers public class AUIInvitationInfo: NSObject {
    var userId: String = ""
    var seatNo: Int = 0
    var type: AUIInvitationType = .apply
    var status: AUIInvitationStatus = .waiting
    
    
    //做变化比较用
    public static func == (lhs: AUIInvitationInfo, rhs: AUIInvitationInfo) -> Bool {
//        if lhs.songCode != rhs.songCode {
//            return false
//        }
//            
//        if lhs.musicUrl != rhs.musicUrl {
//            return false
//        }
//            
//        if lhs.lrcUrl != rhs.lrcUrl {
//            return false
//        }
//            
//        if lhs.owner?.userId ?? "" != rhs.owner?.userId ?? "" {
//            return false
//        }
//            
//        if lhs.pinAt != rhs.pinAt {
//            return false
//        }
//            
//        if lhs.createAt != rhs.createAt {
//            return false
//        }
//            
//        if lhs.playStatus.rawValue != rhs.playStatus.rawValue {
//            return false
//        }
        
        return true
    }
}

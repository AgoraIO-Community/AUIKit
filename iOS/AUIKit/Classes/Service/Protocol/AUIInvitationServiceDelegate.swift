//
//  AUIInvitationServiceDelegate.swift
//  AUIKit
//
//  Created by wushengtao on 2023/2/21.
//

import Foundation

@objc public protocol AUIInvitationUserDataProtocol: NSObjectProtocol {
    var userAvatar: String {get}
    var userId: String {get}
    var userName: String {get}
    var seatIndex: Int {set get}
}


/// 邀请Service抽象协议
public protocol AUIInvitationServiceDelegate: AUICommonServiceDelegate {
    
    /// 绑定响应回调
    /// - Parameter delegate: 需要回调的对象
    func bindRespDelegate(delegate: AUIInvitationRespDelegate)
    
    /// 解除绑响应回调
    /// - Parameter delegate: 需要回调的对象
    func unbindRespDelegate(delegate: AUIInvitationRespDelegate)
    
    /// 向用户发送邀请
    /// - Parameters:
    ///   - userId: <#userId description#>
    ///   - callback: <#callback description#>
    /// - Returns: <#description#>
    func sendInvitation(userId: String, seatIndex: Int?, callback:@escaping AUICallback)
    
    /// 接受邀请
    /// - Parameters:
    ///   - userId: <#id description#>
    ///   - callback: <#callback description#>
    func acceptInvitation(userId: String, seatIndex: Int?, callback:@escaping AUICallback)
    
    
    /// 拒绝邀请
    /// - Parameters:
    ///   - userId: <#id description#>
    ///   - callback: <#callback description#>
    func rejectInvitation(userId: String, callback:@escaping AUICallback)
    
    /// 取消邀请
    /// - Parameters:
    ///   - id: <#id description#>
    ///   - callback: <#callback description#>
    func cancelInvitation(userId: String, callback:@escaping AUICallback)

    /// 发送申请
    /// - Parameter callback: <#callback description#>
    func sendApply(seatIndex: Int?, callback:@escaping AUICallback)
    
    /// 取消申请
    /// - Parameter callback: <#callback description#>
    func cancelApply(callback:@escaping AUICallback)
    
    /// 接受申请(房主同意)
    /// - Parameters:
    ///   - userId: <#userId description#>
    ///   - callback: <#callback description#>
    func acceptApply(userId: String, seatIndex: Int?, callback:@escaping AUICallback)
    
    
    /// 拒绝申请(房主同意)
    /// - Parameters:
    ///   - userId: <#userId description#>
    ///   - callback: <#callback description#>
    func rejectApply(userId: String, callback:@escaping AUICallback)
}


/// 邀请相关操作的响应
@objc public protocol AUIInvitationRespDelegate:AnyObject, NSObjectProtocol {
    
    /// 收到新的邀请请求(不动态显示content)
    /// - Parameters:
    ///   - userId: <#id description#>
    ///   - seatIndex: <#cmd description#>
    func onReceiveNewInvitation(userId: String, seatIndex: Int)
    
    
    /// 被邀请者接受邀请
    /// - Parameters:
    ///   - userId: <#id description#>
    ///   - inviteeId: <#inviteeId description#>
    func onInviteeAccepted(userId: String)
    
    
    /// 被邀请者拒绝邀请
    /// - Parameters:
    ///   - userId: <#id description#>
    ///   - invitee: <#invitee description#>
    func onInviteeRejected(userId: String)
    
    
    /// 邀请人取消邀请
    /// - Parameters:
    ///   - id: <#id description#>
    ///   - inviter: <#inviter description#>
    func onInvitationCancelled(userId: String)
    
    
    /// Description 邀请列表数据更新
    /// - Parameter inviteeList: 邀请列表
    func onInviteeListUpdate(inviteeList: [String:AUIInvitationCallbackModel])
    
    
    /// 收到新的申请信息
    /// - Parameters:
    ///   - userId: <#userId description#>
    ///   - seatIndex: <#seatIndex description#>
    func onReceiveNewApply(userId: String, seatIndex: Int)
    
    
    /// 房主接受申请
    /// - Parameter userId: <#userId description#>
    func onApplyAccepted(userId: String)
    
    
    /// 房主拒接申请
    /// - Parameter userId: <#userId description#>
    func onApplyRejected(userId: String)
    
    
    /// 取消申请
    /// - Parameter userId: <#userId description#>
    func onApplyCanceled(userId: String)
    
    /// Description 收到申请用户全量变更
    /// - Parameter users info: users key is userId,value is apply index
    func onReceiveApplyUsersUpdate(users: [String:AUIInvitationCallbackModel])

}

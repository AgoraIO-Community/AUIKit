//
//  AUIInvitationServiceImpl.swift
//  AUIKit
//
//  Created by wushengtao on 2023/2/23.
//

import Foundation
import AgoraRtcKit

//邀请Service实现
open class AUIInvitationServiceImpl: NSObject {
    private var respDelegates: NSHashTable<AnyObject> = NSHashTable<AnyObject>.weakObjects()
    private var channelName: String!
    private var rtmManager: AUIRtmManager!
    
    deinit {
        aui_info("deinit AUIInvitationServiceImpl", tag: "AUIInvitationServiceImpl")
    }
    
    init(channelName: String, rtmManager: AUIRtmManager) {
        self.channelName = channelName
        self.rtmManager = rtmManager
        super.init()
        
        aui_info("init AUIInvitationServiceImpl", tag: "AUIInvitationServiceImpl")
    }
}

extension AUIInvitationServiceImpl: AUIInvitationServiceDelegate {
    public func getChannelName() -> String {
        return channelName
    }
    
    public func bindRespDelegate(delegate: AUIInvitationRespDelegate) {
        respDelegates.add(delegate)
    }
    
    public func unbindRespDelegate(delegate: AUIInvitationRespDelegate) {
        respDelegates.remove(delegate)
    }
    
    public func sendInvitation(userId: String, seatIndex: Int?, callback: @escaping (NSError?) -> ()) {
        let model = AUIInvitationNetworkModel()
        model.roomId = channelName
        model.userId = userId
        model.micSeatNo = seatIndex
        model.request { error, _ in
            callback(error as? NSError)
        }
    }
    
    public func acceptInvitation(userId: String, seatIndex: Int?, callback: @escaping (NSError?) -> ()) {
        let model = AUIInvitationNetworkModel()
        model.interfaceName = "/v1/invitation/user/accept/"
        model.roomId = channelName
        model.userId = userId
        model.micSeatNo = seatIndex
        model.request { error, _ in
            callback(error as? NSError)
        }
    }
    
    public func rejectInvitation(userId: String, callback: @escaping (NSError?) -> ()) {
        let model = AUIInvitationNetworkModel()
        model.interfaceName = "/v1/invitation/user/reject/"
        model.roomId = channelName
        model.userId = userId
        model.micSeatNo = nil
        model.request { error, _ in
            callback(error as? NSError)
        }
    }
    
    public func cancelInvitation(userId: String, callback: @escaping (NSError?) -> ()) {
        let model = AUIInvitationNetworkModel()
        model.interfaceName = "/v1/invitation/user/cancel/"
        model.roomId = channelName
        model.userId = userId
        model.micSeatNo = nil
        model.request { error, _ in
            callback(error as? NSError)
        }
    }
    
    public func sendApply(seatIndex: Int?, callback: @escaping (NSError?) -> ()) {
        let model = AUIApplyNetworkModel()
        model.roomId = channelName
        model.userId = getRoomContext().currentUserInfo.userId
        model.userName = getRoomContext().currentUserInfo.userName
        model.userAvatar = getRoomContext().currentUserInfo.userAvatar
        model.micSeatNo = seatIndex
        model.request { error, _ in
            callback(error as? NSError)
        }
    }
    
    public func cancelApply(callback: @escaping (NSError?) -> ()) {
        let model = AUIApplyNetworkModel()
        model.interfaceName = "/v1/apply/user/cancel/"
        model.roomId = channelName
        model.userId = getRoomContext().currentUserInfo.userId
        model.request { error, _ in
            callback(error as? NSError)
        }
    }
    
    public func acceptApply(userId: String, seatIndex: Int?, callback: @escaping (NSError?) -> ()) {
        let model = AUIApplyNetworkModel()
        model.interfaceName = "/v1/apply/user/accept/"
        model.roomId = channelName
        model.userId = userId
        model.micSeatNo = seatIndex
        model.request { error, _ in
            callback(error as? NSError)
        }
    }
    
    public func rejectApply(userId: String, callback: @escaping (NSError?) -> ()) {
        let model = AUIApplyNetworkModel()
        model.interfaceName = "/v1/apply/user/reject/"
        model.roomId = channelName
        model.userId = userId
        model.request { error, _ in
            callback(error as? NSError)
        }
    }
    
}

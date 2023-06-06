//
//  AUIInvitationServiceImpl.swift
//  AUIKit
//
//  Created by wushengtao on 2023/2/23.
//

import Foundation
import AgoraRtcKit

fileprivate let AUIInviteKey = "invite"

//邀请Service实现
open class AUIInvitationServiceImpl: NSObject {
    private var respDelegates: NSHashTable<AUIInvitationRespDelegate> = NSHashTable<AUIInvitationRespDelegate>.weakObjects()
    private var channelName: String!
    private var rtmManager: AUIRtmManager!
    
    deinit {
        aui_info("deinit AUIInvitationServiceImpl", tag: "AUIInvitationServiceImpl")
    }
    
    public init(channelName: String, rtmManager: AUIRtmManager) {
        self.channelName = channelName
        self.rtmManager = rtmManager
        super.init()
        rtmManager.subscribeAttributes(channelName: channelName, itemKey: AUIInviteKey, delegate: self)
        
        aui_info("init AUIInvitationServiceImpl", tag: "AUIInvitationServiceImpl")
    }
}

extension AUIInvitationServiceImpl: AUIRtmAttributesProxyDelegate {
    public func onAttributesDidChanged(channelName: String, key: String, value: Any) {
        if key == AUIInviteKey {
            aui_info("recv invitation list attr did changed \(value)", tag: "AUIInvitationServiceImpl")
            guard let invitations = (value as! NSObject).yy_modelToJSONObject(),
                    let inviteList = NSArray.yy_modelArray(with: AUIUserThumbnailInfo.self, json: invitations) as? [AUIUserThumbnailInfo] else {
                return
            }
            self.respDelegates.allObjects.forEach {
                $0.onInviteeListUpdate(inviteeList: inviteList)
            }
        }
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
        model.channelName = channelName
        model.toUserId = userId
        model.fromUserId = getRoomContext().currentUserInfo.userId
        model.payload = AUIPayloadModel()
        model.payload?.seatNo = seatIndex ?? 1
        model.request { error, _ in
            callback(error as? NSError)
        }
    }
    
    public func acceptInvitation(userId: String, seatIndex: Int?, callback: @escaping (NSError?) -> ()) {
        let model = AUIInvitationAcceptNetworkModel()
        model.channelName = channelName
        model.fromUserId = userId
        model.request { error, _ in
            callback(error as? NSError)
        }
    }
    
    public func rejectInvitation(userId: String, callback: @escaping (NSError?) -> ()) {
        let model = AUIInvitationAcceptRejectNetworkModel()
        model.channelName = channelName
        model.fromUserId = userId
        model.request { error, _ in
            callback(error as? NSError)
        }
    }
    
    public func cancelInvitation(userId: String, callback: @escaping (NSError?) -> ()) {
        let model = AUIInvitationAcceptCancelNetworkModel()
        model.channelName = channelName
        model.userId = userId
        model.request { error, _ in
            callback(error as? NSError)
        }
    }
    
    public func sendApply(seatIndex: Int?, callback: @escaping (NSError?) -> ()) {
        let model = AUIApplyNetworkModel()
        model.channelName = channelName
        model.fromUserId = getRoomContext().currentUserInfo.userId
        model.payload = AUIPayloadModel()
        model.payload?.seatNo = seatIndex ?? 1
        model.request { error, _ in
            callback(error as? NSError)
        }
    }
    
    public func cancelApply(callback: @escaping (NSError?) -> ()) {
        let model = AUIApplyAcceptCancelNetworkModel()
        model.channelName = channelName
        model.fromUserId = getRoomContext().currentUserInfo.userId
        model.request { error, _ in
            callback(error as? NSError)
        }
    }
    
    public func acceptApply(userId: String, seatIndex: Int?, callback: @escaping (NSError?) -> ()) {
        let model = AUIApplyAcceptNetworkModel()
        model.channelName = channelName
        model.fromUserId = userId
        model.request { error, _ in
            callback(error as? NSError)
        }
    }
    
    public func rejectApply(userId: String, callback: @escaping (NSError?) -> ()) {
        let model = AUIApplyAcceptRejectNetworkModel()
        model.channelName = channelName
        model.userId = userId
        model.request { error, _ in
            callback(error as? NSError)
        }
    }
    
}

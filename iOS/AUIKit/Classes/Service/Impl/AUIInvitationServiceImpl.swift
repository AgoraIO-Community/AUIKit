//
//  AUIInvitationServiceImpl.swift
//  AUIKit
//
//  Created by wushengtao on 2023/2/23.
//

import Foundation
import AgoraRtcKit

fileprivate let AUIInviteKey = "AUIInvite"

fileprivate let AUIInvitationKey = "invitation"

fileprivate let AUIApplyKey = "application"

fileprivate let AUIApplyOperationKey = "AUIApply"


//邀请Service实现
@objc open class AUIInvitationServiceImpl: NSObject {
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
        rtmManager.subscribeAttributes(channelName: channelName, itemKey: AUIInvitationKey, delegate: self)
        rtmManager.subscribeAttributes(channelName: channelName, itemKey: AUIApplyOperationKey, delegate: self)
        rtmManager.subscribeAttributes(channelName: channelName, itemKey: AUIInviteKey, delegate: self)
        rtmManager.subscribeAttributes(channelName: channelName, itemKey: AUIApplyKey, delegate: self)

        aui_info("init AUIInvitationServiceImpl", tag: "AUIInvitationServiceImpl")
    }
}

extension AUIInvitationServiceImpl: AUIRtmAttributesProxyDelegate {
    public func onAttributesDidChanged(channelName: String, key: String, value: Any) {
        aui_info("recv invitation list attr did changed \(value)", tag: "AUIInvitationServiceImpl")
        if AUIInvitationKey == key {
            //TODO: - 根据返回数据判断邀请的是否是自己
            guard let object = value as? [String:Any],let seat = object["micSeat"] as? [String:Any] else { return }
            guard let queue = seat["queue"] as? [Dictionary<String,Any>], let inviteList = NSArray.yy_modelArray(with: AUIInvitationCallbackModel.self, json: queue) as? [AUIInvitationCallbackModel] else {
                return
            }
            self.respDelegates.allObjects.forEach {
                if let userId = inviteList.first?.userId,userId == AUIRoomContext.shared.currentUserInfo.userId {
                    $0.onReceiveNewInvitation(userId: inviteList.first?.userId ?? "", seatIndex: inviteList.first?.payload?.seatNo ?? 1)
                }
            }
            guard let actionList = seat["removed"] as? [Dictionary<String,Any>],let actions = NSArray.yy_modelArray(with: AUIInvitationNetworkModel.self, json: actionList) as? [AUIInvitationNetworkModel] else { return }
            if let actionType = actionList.first?["actionType"] as? Int {
                switch actionType {
                case 1:
                    self.respDelegates.allObjects.forEach {
                        if let userId = actions.first?.fromUserId {
                            $0.onInviteeAccepted(userId: userId)
                        }
                    }
                case 2:
                    self.respDelegates.allObjects.forEach {
                        if let userId = actions.first?.fromUserId {
                            $0.onInviteeRejected(userId: userId)
                        }
                    }
                case 3:
                    self.respDelegates.allObjects.forEach {
                        if let userId = actions.first?.fromUserId {
                            $0.onInvitationCancelled(userId: userId)
                        }
                    }
                default:
                    break
                }
            }
        } else if AUIApplyKey == key {
            guard let object = value as? [String:Any],let seat = object["micSeat"] as? [String:Any] else { return }
            guard let queue = seat["queue"] as? [Dictionary<String,Any>], let inviteList = NSArray.yy_modelArray(with: AUIInvitationCallbackModel.self, json: queue) as? [AUIInvitationCallbackModel] else {
                return
            }
            var userAttributes = [String:Int]()
            for item in inviteList {
                if let userId = item.userId,let index = item.payload?.seatNo {
                    userAttributes[userId] = index
                }
            }
            self.respDelegates.allObjects.forEach {//全量回调
                $0.onReceiveApplyUsersUpdate(users: userAttributes)
            }
            guard let actionList = seat["removed"] as? [Dictionary<String,Any>],let actions = NSArray.yy_modelArray(with: AUIInvitationNetworkModel.self, json: actionList) as? [AUIInvitationNetworkModel] else { return }
            if let actionType = actionList.first?["actionType"] as? Int {
                switch actionType {
                case 1:
                    self.respDelegates.allObjects.forEach {
                        if let userId = actions.first?.fromUserId {
                            $0.onApplyAccepted(userId: userId)
                        }
                    }
                case 2:
                    self.respDelegates.allObjects.forEach {
                        if let userId = actions.first?.fromUserId {
                            $0.onApplyRejected(userId: userId)
                        }
                    }
                case 3:
                    self.respDelegates.allObjects.forEach {
                        if let userId = actions.first?.fromUserId {
                            $0.onApplyCanceled(userId: userId)
                        }
                    }
                default:
                    break
                }
            }
        }
    }

    
}

extension AUIInvitationServiceImpl: AUIInvitationServiceDelegate {
    
    public func getRoomContext() -> AUIRoomContext {
        return AUIRoomContext.shared
    }
    
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
        model.roomId = channelName
        model.fromUserId = userId
        model.request { error, _ in
            callback(error as? NSError)
        }
    }
    
    public func rejectInvitation(userId: String, callback: @escaping (NSError?) -> ()) {
        let model = AUIInvitationAcceptRejectNetworkModel()
        model.roomId = channelName
        model.fromUserId = userId
        model.request { error, _ in
            callback(error as? NSError)
        }
    }
    
    public func cancelInvitation(userId: String, callback: @escaping (NSError?) -> ()) {
        let model = AUIInvitationAcceptCancelNetworkModel()
        model.roomId = channelName
        model.userId = userId
        model.request { error, _ in
            callback(error as? NSError)
        }
    }
    
    public func sendApply(seatIndex: Int?, callback: @escaping (NSError?) -> ()) {
        let model = AUIApplyNetworkModel()
        model.roomId = channelName
        model.fromUserId = getRoomContext().currentUserInfo.userId
        model.payload = AUIPayloadModel()
        model.payload?.seatNo = seatIndex ?? 1
        model.request { error, _ in
            callback(error as? NSError)
        }
    }
    
    public func cancelApply(callback: @escaping (NSError?) -> ()) {
        let model = AUIApplyAcceptCancelNetworkModel()
        model.roomId = channelName
        model.fromUserId = getRoomContext().currentUserInfo.userId
        model.request { error, _ in
            callback(error as? NSError)
        }
    }
    
    public func acceptApply(userId: String, seatIndex: Int?, callback: @escaping (NSError?) -> ()) {
        let model = AUIApplyAcceptNetworkModel()
        model.roomId = channelName
        model.fromUserId = AUIRoomContext.shared.currentUserInfo.userId
        model.toUserId = userId
        model.request { error, _ in
            callback(error as? NSError)
        }
    }
    
    public func rejectApply(userId: String, callback: @escaping (NSError?) -> ()) {
        let model = AUIApplyAcceptRejectNetworkModel()
        model.roomId = channelName
        model.userId = userId
        model.request { error, _ in
            callback(error as? NSError)
        }
    }
    
}

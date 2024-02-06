//
//  AUIInvitationServiceImpl.swift
//  AUIKit
//
//  Created by wushengtao on 2023/2/23.
//

import Foundation
import AgoraRtcKit
import YYModel

fileprivate let AUIInvitationKey = "invitation"

//fileprivate let AUIApplyKey = "application"
//
//
//fileprivate let AUIRemoved = "removed"
//
//fileprivate let AUIActionType = "actionType"
//
//fileprivate let AUIQueue = "queue"

//fileprivate let AUIMicSeat = "micSeat"

private enum AUIInvitationCmd: String {
    case sendApply = "sendApply"
    case cancelApply = "cancelApply"
    case acceptApply = "acceptApply"
    case rejectApply = "rejectApply"
    
    case sendInvit = "sendInvit"
    case cancelInvit = "cancelInvit"
    case acceptInvit = "acceptInvit"
    case rejectInvit = "rejectInvit"
}

/**
 邀请
 1：被邀请人同意
 2：被邀请人拒绝
 3：邀请人人取消
 4：超时
 5：并发上麦失败 别人先上了
 申请
 被移除原因：
 1：房主同意
 2：房主拒绝
 3：申请人取消
 4：超时
 5：并发上麦失败 别人先上了
 */
@objc public enum AUIActionOperation: Int {
    case agree = 1
    case refuse
    case cancel
    case timeout
    case failed
}


//邀请Service实现
@objc open class AUIInvitationServiceImpl: NSObject {
    private var respDelegates: NSHashTable<AUIInvitationRespDelegate> = NSHashTable<AUIInvitationRespDelegate>.weakObjects()
    private var channelName: String!
    private var rtmManager: AUIRtmManager!
    private var invitationCollection:AUIListCollection!
    private var invitationList: [AUIInvitationInfo] = []
    
    deinit {
        aui_info("deinit AUIInvitationServiceImpl", tag: "AUIInvitationServiceImpl")
    }
    
    public init(channelName: String, rtmManager: AUIRtmManager) {
        self.channelName = channelName
        self.rtmManager = rtmManager
        self.invitationCollection = AUIListCollection(channelName: channelName,
                                                      observeKey: AUIInvitationKey,
                                                      rtmManager: rtmManager)
        super.init()
        invitationCollection.subscribeWillAdd {[weak self] publisherId, dataCmd, newItem, attr in
            return self?.metadataWillAdd(publiserId: publisherId,
                                         dataCmd: dataCmd,
                                         newItem: newItem,
                                         attr: attr)
        }
        
        invitationCollection.subscribeAttributesDidChanged {[weak self] channelName, key, value in
            self?.onAttributesDidChanged(channelName: channelName, key: key, value: value)
        }

        aui_info("init AUIInvitationServiceImpl", tag: "AUIInvitationServiceImpl")
    }
}

extension AUIInvitationServiceImpl {
    
    private func metadataWillAdd(publiserId: String,
                                 dataCmd: String?,
                                 newItem: [String: Any],
                                 attr: AUIAttributesModel) -> NSError? {
        guard let dataCmd = AUIInvitationCmd(rawValue: dataCmd ?? "") else {
            return AUICommonError.unknown.toNSError()
        }
        
        guard let model = AUIInvitationInfo.yy_model(with: newItem) else {
            return AUICommonError.unknown.toNSError()
        }
        switch dataCmd {
        case .sendApply, .sendInvit:
            //能走到这里，如果有老的userId的数据，需要清理掉，防止重复，因为add请求的时候过滤了status为waitting/accept的，剩余状态的无效数据需要清理
            if var list = attr.getList() {
                list = list.filter { $0["userId"] as? String != model.userId }
                attr.setList(list)
            }
            return nil
        default:
            break
        }
        
        return NSError.auiError("add invitation cmd incorrect")
    }
    
    private func onAttributesDidChanged(channelName: String, key: String, value: AUIAttributesModel) {
        guard AUIInvitationKey == key else { return}
        aui_info("recv chorus attr did changed \(value)", tag: "AUIInvitationServiceImpl")
        guard let invitationArray = (value.getList() as? AnyObject)?.yy_modelToJSONObject(),
                let invitationList = NSArray.yy_modelArray(with: AUIInvitationInfo.self, json: invitationArray) as? [AUIInvitationInfo] else {
            return
        }
        
        var unChangesOldList = self.invitationList
        //TODO: optimize
        let difference = invitationList.difference(from: self.invitationList)
        for change in difference {
            switch change {
            case let .remove(offset, oldElement, _):
                unChangesOldList.remove(at: offset)
                self.respDelegates.allObjects.forEach { obj in
//                    obj.onChoristerDidLeave(chorister: oldElement)
                }
            case let .insert(_, newElement, _):
                self.respDelegates.allObjects.forEach { obj in
//                    obj.onChoristerDidEnter(chorister: newElement)
                }
            }
        }
        
        self.invitationList = invitationList
            
//            //TODO: - 根据返回数据判断邀请的是否是自己
//            guard let object = value as? [String:Any],let seat = object[AUIMicSeat] as? [String:Any] else { return }
//            guard let queue = seat[AUIQueue] as? [Dictionary<String,Any>], let inviteList = NSArray.yy_modelArray(with: AUIInvitationCallbackModel.self, json: queue) as? [AUIInvitationCallbackModel] else {
//                return
//            }
//            self.respDelegates.allObjects.forEach {
//                if let userId = inviteList.first?.userId,userId == AUIRoomContext.shared.currentUserInfo.userId {
//                    $0.onReceiveNewInvitation(userId: inviteList.first?.userId ?? "", seatIndex: inviteList.first?.payload?.seatNo ?? 1)
//                }
//            }
//            guard let actionList = seat[AUIRemoved] as? [Dictionary<String,Any>],let actions = NSArray.yy_modelArray(with: AUIInvitationCallbackModel.self, json: actionList) as? [AUIInvitationCallbackModel] else { return }
//            if let actionType = actionList.first?[AUIActionType] as? Int {
//                if let action = AUIActionOperation(rawValue: actionType) {
//                    switch action {
//                    case .agree:
//                        self.respDelegates.allObjects.forEach {
//                            if let userId = actions.first?.fromUserId {
//                                $0.onInviteeAccepted(userId: userId)
//                            }
//
//                        }
//                    case .refuse:
//                        self.respDelegates.allObjects.forEach {
//                            if let userId = actions.first?.fromUserId {
//                                $0.onInviteeRejected(userId: userId)
//                            }
//                        }
//                    case .cancel:
//                        self.respDelegates.allObjects.forEach {
//                            if let userId = actions.first?.fromUserId {
//                                $0.onInvitationCancelled(userId: userId)
//                            }
//                        }
//                    case .failed:
//                        self.respDelegates.allObjects.forEach {
//                            if let userId = actions.first?.fromUserId {
//                                $0.onInviteeAcceptedButFailed(userId: userId)
//                            }
//                        }
//                    default:
//                        break
//                    }
//                }
//            }
//        } else if AUIApplyKey == key {
//            guard let object = value as? [String:Any],let seat = object[AUIMicSeat] as? [String:Any] else { return }
//            guard let queue = seat[AUIQueue] as? [Dictionary<String,Any>], let inviteList = NSArray.yy_modelArray(with: AUIInvitationCallbackModel.self, json: queue) as? [AUIInvitationCallbackModel] else {
//                return
//            }
//            var userAttributes = [String:AUIInvitationCallbackModel]()
//            for item in inviteList {
//                if let userId = item.userId {
//                    userAttributes[userId] = item
//                }
//            }
//            self.respDelegates.allObjects.forEach {//全量回调
//                $0.onReceiveApplyUsersUpdate(users: userAttributes)
//            }
//            guard let actionList = seat[AUIRemoved] as? [Dictionary<String,Any>],let actions = NSArray.yy_modelArray(with: AUIInvitationCallbackModel.self, json: actionList) as? [AUIInvitationCallbackModel] else { return }
//            if let actionType = actionList.first?[AUIActionType] as? Int {
//                if let action = AUIActionOperation(rawValue: actionType) {
//                    switch action {
//                    case .agree:
//                        self.respDelegates.allObjects.forEach {
//                            if let userId = actions.first?.fromUserId {
//                                $0.onApplyAccepted(userId: userId)
//                            }
//                        }
//                    case .refuse:
//                        self.respDelegates.allObjects.forEach {
//                            if let userId = actions.first?.fromUserId {
//                                $0.onApplyRejected(userId: userId)
//                            }
//                        }
//                    case .cancel:
//                        self.respDelegates.allObjects.forEach {
//                            if let userId = actions.first?.fromUserId {
//                                $0.onApplyCanceled(userId: userId)
//                            }
//                        }
//                    case .failed:
//                        self.respDelegates.allObjects.forEach {
//                            if let userId = actions.first?.fromUserId {
//                                $0.onApplyAcceptedButFailed(userId: userId)
//                            }
//                        }
//                    
//                    default:
//                        break
//                    }
//                }
//            }
//        }
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
//        let model = AUIInvitationNetworkModel()
//        model.roomId = channelName
//        model.toUserId = userId
//        model.fromUserId = getRoomContext().currentUserInfo.userId
//        model.payload = AUIPayloadModel()
//        model.payload?.seatNo = seatIndex ?? 1
//        model.request { error, _ in
//            callback(error as? NSError)
//        }
        
        let model = AUIInvitationInfo()
        model.type = .invite
        model.seatNo = seatIndex ?? 0
        model.userId = userId
        guard let value = model.yy_modelToJSONObject() as? [String: Any] else {
            callback(NSError.auiError("convert to json fail"))
            return
        }
        //当当前状态是waitting和accept时不允许写入，无论邀请申请
        let filter = [
            ["userId": model.userId, "status": AUIInvitationStatus.waiting.rawValue],
            ["userId": model.userId, "status": AUIInvitationStatus.accept.rawValue]
        ]
        invitationCollection.addMetaData(valueCmd: AUIInvitationCmd.sendInvit.rawValue,
                                         value: value, 
                                         filter: filter,
                                         callback: callback)
    }
    
    public func acceptInvitation(userId: String, seatIndex: Int?, callback: @escaping (NSError?) -> ()) {
//        let model = AUIInvitationAcceptNetworkModel()
//        model.roomId = channelName
//        model.fromUserId = userId
//        model.request { error, _ in
//            callback(error as? NSError)
//        }
        
        invitationCollection.mergeMetaData(valueCmd: AUIInvitationCmd.acceptInvit.rawValue,
                                           value: ["status": AUIInvitationStatus.accept.rawValue],
                                           filter: [["userId": userId, "type": AUIInvitationType.invite.rawValue]],
                                           callback: callback)
    }
    
    public func rejectInvitation(userId: String, callback: @escaping (NSError?) -> ()) {
//        let model = AUIInvitationAcceptRejectNetworkModel()
//        model.roomId = channelName
//        model.fromUserId = userId
//        model.request { error, _ in
//            callback(error as? NSError)
//        }
        
        invitationCollection.mergeMetaData(valueCmd: AUIInvitationCmd.rejectInvit.rawValue,
                                           value: ["status": AUIInvitationStatus.reject.rawValue],
                                           filter: [["userId": userId, "type": AUIInvitationType.invite.rawValue]],
                                           callback: callback)
    }
    
    public func cancelInvitation(userId: String, callback: @escaping (NSError?) -> ()) {
//        let model = AUIInvitationAcceptCancelNetworkModel()
//        model.roomId = channelName
//        model.userId = userId
//        model.request { error, _ in
//            callback(error as? NSError)
//        }
        
        invitationCollection.mergeMetaData(valueCmd: AUIInvitationCmd.cancelInvit.rawValue,
                                           value: ["status": AUIInvitationStatus.cancel.rawValue],
                                           filter: [["userId": userId, "type": AUIInvitationType.invite.rawValue]],
                                           callback: callback)
    }
    
    public func sendApply(seatIndex: Int?, callback: @escaping (NSError?) -> ()) {
//        let model = AUIApplyNetworkModel()
//        model.roomId = channelName
//        model.fromUserId = getRoomContext().currentUserInfo.userId
//        model.payload = AUIPayloadModel()
//        model.payload?.seatNo = seatIndex ?? 1
//        model.request { error, _ in
//            callback(error as? NSError)
//        }
        
        let model = AUIInvitationInfo()
        model.type = .apply
        model.seatNo = seatIndex ?? 0
        model.userId = getRoomContext().currentUserInfo.userId
        guard let value = model.yy_modelToJSONObject() as? [String: Any] else {
            callback(NSError.auiError("convert to json fail"))
            return
        }
        //当当前状态是waitting和accept时不允许写入，无论邀请申请
        let filter = [
            ["userId": model.userId, "status": AUIInvitationStatus.waiting.rawValue],
            ["userId": model.userId, "status": AUIInvitationStatus.accept.rawValue]
        ]
        invitationCollection.addMetaData(valueCmd: AUIInvitationCmd.sendApply.rawValue,
                                         value: value,
                                         filter: filter,
                                         callback: callback)
    }
    
    public func cancelApply(callback: @escaping (NSError?) -> ()) {
//        let model = AUIApplyAcceptCancelNetworkModel()
//        model.roomId = channelName
//        model.fromUserId = getRoomContext().currentUserInfo.userId
//        model.request { error, _ in
//            callback(error as? NSError)
//        }
        
        let userId = getRoomContext().currentUserInfo.userId
        invitationCollection.mergeMetaData(valueCmd: AUIInvitationCmd.cancelApply.rawValue,
                                           value: ["status": AUIInvitationStatus.cancel.rawValue],
                                           filter: [["userId": userId, "type": AUIInvitationType.apply.rawValue]],
                                           callback: callback)
    }
    
    public func acceptApply(userId: String, seatIndex: Int?, callback: @escaping (NSError?) -> ()) {
//        let model = AUIApplyAcceptNetworkModel()
//        model.roomId = channelName
//        model.fromUserId = AUIRoomContext.shared.currentUserInfo.userId
//        model.toUserId = userId
//        model.request { error, _ in
//            callback(error as? NSError)
//        }
        
        let userId = getRoomContext().currentUserInfo.userId
        invitationCollection.mergeMetaData(valueCmd: AUIInvitationCmd.acceptApply.rawValue,
                                           value: ["status": AUIInvitationStatus.accept.rawValue],
                                           filter: [["userId": userId, "type": AUIInvitationType.apply.rawValue]],
                                           callback: callback)
    }
    
    public func rejectApply(userId: String, callback: @escaping (NSError?) -> ()) {
//        let model = AUIApplyAcceptRejectNetworkModel()
//        model.roomId = channelName
//        model.userId = userId
//        model.request { error, _ in
//            callback(error as? NSError)
//        }
        
        let userId = getRoomContext().currentUserInfo.userId
        invitationCollection.mergeMetaData(valueCmd: AUIInvitationCmd.rejectApply.rawValue,
                                           value: ["status": AUIInvitationStatus.reject.rawValue],
                                           filter: [["userId": userId, "type": AUIInvitationType.apply.rawValue]],
                                           callback: callback)
    }
    
}

extension AUIInvitationServiceImpl {
    public func initService(completion: @escaping ((NSError?) -> ())){
//        guard let roomInfo = getRoomContext().roomInfoMap[channelName] else {
//            completion(AUICommonError.unknown.toNSError())
//            return
//        }
//        var seatMap: [String: [String: Any]] = [:]
//        for i in 0...roomInfo.micSeatCount {
//            let seat = AUIMicSeatInfo()
//            seat.seatIndex = i
//            if i == 0 {
//                seat.user = getRoomContext().currentUserInfo
//                seat.lockSeat = .user
//            }
//            seatMap["\(i)"] = seat.yy_modelToJSONObject() as? [String : Any]
//        }
//        
//        let data = try! JSONSerialization.data(withJSONObject: seatMap, options: .prettyPrinted)
//        let str = String(data: data, encoding: .utf8)!
//        var metaData = [String: String]()
//        metaData[kSeatAttrKey] = str
//        rtmManager.setBatchMetadata(channelName: channelName,
//                                    lockName: "",
//                                    metadata: metaData,
//                                    completion: completion)
    }
    
    public func cleanUserInfo(userId: String, completion: @escaping ((NSError?) -> ())) {
//        let micSeatMetaData = rtmLeaveSeatMetaData(userId: userId)
//        let str = micSeatMetaData.yy_modelToJSONString() ?? ""
//        var metaData = [String: String]()
//        metaData[kSeatAttrKry] = str
//
//        rtmManager.setBatchMetadata(channelName: channelName,
//                                    lockName: kRTM_Referee_LockName,
//                                    metadata: metaData,
//                                    completion: completion)
        
//        leaveSeat(userId: userId, callback: completion)
        
        invitationCollection.removeMetaData(valueCmd: nil,
                                            filter: [["userId": userId]],
                                            callback: completion)
    }
    
    public func deinitService(completion:  @escaping  ((NSError?) -> ())) {
//        rtmManager.cleanBatchMetadata(channelName: channelName,
//                                      lockName: kRTM_Referee_LockName,
//                                      removeKeys: [kSeatAttrKry],
//                                      completion: completion)
        invitationCollection.cleanMetaData(callback: completion)
    }
}

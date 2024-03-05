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

private let kEditTimeKey = "editTime"
private let kStatusKey = "status"
private let kUserIdKey = "userId"
private let kTypeKey = "type"
private let kSeatNoKey = "seatNo"

//邀请Service实现
@objc open class AUIInvitationServiceImpl: NSObject {
    private var respDelegates: NSHashTable<AUIInvitationRespDelegate> = NSHashTable<AUIInvitationRespDelegate>.weakObjects()
    private var channelName: String!
    private var rtmManager: AUIRtmManager!
    private var invitationCollection:AUIListCollection!
    private var invitationMap: [String: AUIInvitationInfo] = [:]
    
    lazy var checkThrottler: AUIThrottler = AUIThrottler()
    private var observerList: [AUIInvitationInfo] = []
    private var timer: Timer? {
        didSet {
            oldValue?.invalidate()
        }
    }
    
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
        invitationCollection.subsceibeValueWillChange {[weak self] publisherId, dataCmd, newItem in
            return self?.valueWillChange(publiserId: publisherId,
                                         dataCmd: dataCmd,
                                         newItem: newItem)
        }
        invitationCollection.subscribeWillAdd {[weak self] publisherId, dataCmd, newItem, attr in
            return self?.metadataWillAdd(publiserId: publisherId,
                                         dataCmd: dataCmd,
                                         newItem: newItem,
                                         attr: attr)
        }
        
        invitationCollection.subscribeWillMerge {[weak self] publisherId, dataCmd, updateMap, currentMap in
            return self?.metadataWillMerge(publiserId: publisherId, dataCmd: dataCmd, updateMap: updateMap, currentMap: currentMap)
        }
        
        invitationCollection.subscribeAttributesWillSet {[weak self] channelName, key, valueCmd, attr in
            guard let self = self else { return }
            guard let value = attr.getList() else { return }
            let currentTime = self.getRoomContext().getNtpTime()
            //把已经accept的麦位申请&邀请给reject
            var filterList = self.rejectDuplicateAcceptedRequests(list: value)
            filterList = filterList.filter { attr in
                let editTime = attr[kEditTimeKey] as? Int64 ?? 0
                let invalidTs = attr["invalidTs"] as? Int64 ?? kInvitationInvalidTs
                let status = attr[kStatusKey] as? Int
                if status == AUIInvitationStatus.waiting.rawValue {
                    return true
                }
                //过滤超时的无效(非waitting)数据
                if currentTime - editTime < invalidTs { return true }
                return false
            }
            aui_info("invitation will set filter list: \(filterList.count) / \(value.count)", tag: "AUIInvitationServiceImpl")
            attr.setList(filterList)
        }
        
        invitationCollection.subscribeAttributesDidChanged {[weak self] channelName, key, value in
            self?.onAttributesDidChanged(channelName: channelName, key: key, value: value)
        }

        aui_info("init AUIInvitationServiceImpl", tag: "AUIInvitationServiceImpl")
    }
}

extension AUIInvitationServiceImpl {
    //检查是否需要开启定时器：1.邀请申请列表变更， 2.仲裁者切换
    private func checkWaittingTimeout() {
        guard getRoomContext().getArbiter(channelName: channelName)?.isArbiter() ?? false else { return }
        let observerList = invitationMap.compactMap({$1.status == .waiting ? $1 : nil})
        if observerList.isEmpty {
            timer = nil
            return
        }
        self.observerList = observerList
        timer = Timer.scheduledTimer(withTimeInterval: 1, repeats: true, block: {[weak self] t in
            guard let self = self else {return}
            let currentTs = self.getRoomContext().getNtpTime()
            let observerList = self.observerList
            self.observerList.removeAll()
            observerList.forEach { info in
                if currentTs - info.createTime > info.timeoutTs {
                    if info.type == .apply {
                        aui_info("apply checkWaittingTimeout: \(info.userId)", tag: "AUIInvitationServiceImpl")
                        self.cancelApply { err in }
                    } else {
                        aui_info("invit checkWaittingTimeout: \(info.userId)", tag: "AUIInvitationServiceImpl")
                        self.cancelInvitation(userId: info.userId) { err in }
                    }
                } else {
                    self.observerList.append(info)
                }
            }
            if self.observerList.isEmpty {
                self.timer = nil
            }
        })
        timer?.fire()
    }
    
    private func rejectDuplicateAcceptedRequests(list: [[String: Any]]) -> [[String: Any]] {
        //获取accept的麦位数组
        let acceptSeatNos = list.map({ item in
            if item[kStatusKey] as? Int == AUIInvitationStatus.accept.rawValue {
                return item[kSeatNoKey] as? Int
            }
            return nil
        })
        
        var updateList = [[String: Any]]()
        for var item in list {
            //如果accept过的麦位还有申请的，设置为reject
            if let seatNo = item[kSeatNoKey] as? Int,
               let status = item[kStatusKey] as? Int,
               status == AUIInvitationStatus.waiting.rawValue,
                acceptSeatNos.contains(seatNo) {
                let currentTime = self.getRoomContext().getNtpTime()
                item[kStatusKey] = AUIInvitationStatus.reject.rawValue
                item[kEditTimeKey] = currentTime
            }
            updateList.append(item)
        }
        
        return updateList
    }
    
    private func valueWillChange(publiserId: String,
                                 dataCmd: String?,
                                 newItem: [String: Any]) -> [String: Any]? {
        guard let dataCmd = AUIInvitationCmd(rawValue: dataCmd ?? "") else {
            return newItem
        }
        var tempItem = newItem
        let currentTime = self.getRoomContext().getNtpTime()
        tempItem[kEditTimeKey] = currentTime
        switch dataCmd {
        case .sendApply, .sendInvit:
            tempItem["createTime"] = currentTime
            break
        default:
            break
        }
        
        return tempItem
    }
    
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
                list = list.filter { $0[kUserIdKey] as? String != model.userId }
                attr.setList(list)
            }
            return nil
        default:
            break
        }
        
        return NSError.auiError("add invitation cmd incorrect")
    }
    
    private func metadataWillMerge(publiserId: String,
                                   dataCmd: String?,
                                   updateMap: [String: Any],
                                   currentMap: [String: Any]) -> NSError? {
        guard let dataCmd = AUIInvitationCmd(rawValue: dataCmd ?? "") else {
            return AUICommonError.unknown.toNSError()
        }
        
        let userId: String = currentMap[kUserIdKey] as? String ?? ""
        let seatIndex: Int = currentMap[kSeatNoKey] as? Int ?? 0
        let metaData = NSMutableDictionary()
        var err: NSError? = nil
        switch dataCmd {
        case .acceptApply:
            for obj in respDelegates.allObjects {
                err = obj.onApplyWillAccept?(userId: userId, seatIndex: seatIndex, metaData: metaData)
            }
        case .acceptInvit:
            for obj in respDelegates.allObjects {
                err = obj.onInviteWillAccept?(userId: userId, seatIndex: seatIndex, metaData: metaData)
            }
        default:
            break
        }
        
        return err
    }
    
    private func onAttributesDidChanged(channelName: String, key: String, value: AUIAttributesModel) {
        guard AUIInvitationKey == key else { return}
        aui_info("recv chorus attr did changed \(value)", tag: "AUIInvitationServiceImpl")
        guard let invitationArray = value.getList(),
              let invitationList = NSArray.yy_modelArray(with: AUIInvitationInfo.self, json: invitationArray) as? [AUIInvitationInfo] else {
            return
        }
        
        let oldMap = self.invitationMap
        let newMap = Dictionary(uniqueKeysWithValues: invitationList.map { ($0.userId, $0) })
        self.invitationMap = newMap
        var applyList: [AUIInvitationInfo] = []
        var invitList: [AUIInvitationInfo] = []
        newMap.forEach { userId, newInfo in
            let oldInfo = oldMap[userId]
            let isTargetUser = newInfo.userId == getRoomContext().currentUserInfo.userId
            if oldInfo?.type == newInfo.type || oldInfo == nil {
                if newInfo.type == .invite {
                    if newInfo.status == .waiting {
                        invitList.append(newInfo)
                    }
                    if oldInfo?.status != newInfo.status {
                        if isTargetUser {
                            switch newInfo.status {
                            case .waiting:
                                self.respDelegates.allObjects.forEach {
                                    $0.onReceiveNewInvitation(userId: userId, seatIndex: newInfo.seatNo)
                                }
                            case .cancel, .timeout:
                                self.respDelegates.allObjects.forEach {
                                    $0.onInvitationCancelled(userId: userId)
                                }
                            case .reject:
                                self.respDelegates.allObjects.forEach {
                                    $0.onInviteeRejected(userId: userId)
                                }
                            case .accept:
                                self.respDelegates.allObjects.forEach {
                                    $0.onInviteeAccepted(userId: userId)
                                }
                            }
                        }
                    }
                } else if newInfo.type == .apply {
                    if newInfo.status == .waiting {
                        applyList.append(newInfo)
                    }
                    if oldInfo?.status != newInfo.status {
                        if isTargetUser {
                            switch newInfo.status {
                            case .waiting:
                                self.respDelegates.allObjects.forEach {
                                    $0.onReceiveNewApply(userId: userId, seatIndex: newInfo.seatNo)
                                }
                            case .cancel, .timeout:
                                self.respDelegates.allObjects.forEach {
                                    $0.onApplyCanceled(userId: userId)
                                }
                            case .reject:
                                self.respDelegates.allObjects.forEach {
                                    $0.onApplyRejected(userId: userId)
                                }
                            case .accept:
                                self.respDelegates.allObjects.forEach {
                                    $0.onApplyAccepted(userId: userId)
                                }
                            }
                        }
                    }
                }
            } else {
                aui_warn("invitation type changed \(oldInfo?.type.rawValue ?? -1) -> \(newInfo.type.rawValue)", tag: "AUIInvitationServiceImpl")
            }
        }
        
        self.respDelegates.allObjects.forEach {
            $0.onReceiveApplyUsersUpdate(applyList: applyList)
        }
        self.respDelegates.allObjects.forEach {
            $0.onInviteeListUpdate(inviteeList: invitList)
        }
        
        checkThrottler.triggerLastEvent(after: 0.3) {
            self.checkWaittingTimeout()
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
            [kUserIdKey: model.userId, kStatusKey: AUIInvitationStatus.waiting.rawValue],
            [kUserIdKey: model.userId, kStatusKey: AUIInvitationStatus.accept.rawValue]
        ]
        invitationCollection.addMetaData(valueCmd: AUIInvitationCmd.sendInvit.rawValue,
                                         value: value, 
                                         filter: filter,
                                         callback: callback)
    }
    
    public func acceptInvitation(userId: String, seatIndex: Int?, callback: @escaping (NSError?) -> ()) {
        let value: [String: Any] = [kStatusKey: AUIInvitationStatus.accept.rawValue]
        invitationCollection.mergeMetaData(valueCmd: AUIInvitationCmd.acceptInvit.rawValue,
                                           value: value,
                                           filter: [[kUserIdKey: userId, kTypeKey: AUIInvitationType.invite.rawValue]],
                                           callback: callback)
    }
    
    public func rejectInvitation(userId: String, callback: @escaping (NSError?) -> ()) {
        let value: [String: Any] = [kStatusKey: AUIInvitationStatus.reject.rawValue]
        invitationCollection.mergeMetaData(valueCmd: AUIInvitationCmd.rejectInvit.rawValue,
                                           value: value,
                                           filter: [[kUserIdKey: userId, kTypeKey: AUIInvitationType.invite.rawValue]],
                                           callback: callback)
    }
    
    public func cancelInvitation(userId: String, callback: @escaping (NSError?) -> ()) {
        let value: [String: Any] = [kStatusKey: AUIInvitationStatus.cancel.rawValue]
        invitationCollection.mergeMetaData(valueCmd: AUIInvitationCmd.cancelInvit.rawValue,
                                           value: value,
                                           filter: [[kUserIdKey: userId, kTypeKey: AUIInvitationType.invite.rawValue]],
                                           callback: callback)
    }
    
    public func sendApply(seatIndex: Int?, callback: @escaping (NSError?) -> ()) {
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
            [kUserIdKey: model.userId, kStatusKey: AUIInvitationStatus.waiting.rawValue],
            [kUserIdKey: model.userId, kStatusKey: AUIInvitationStatus.accept.rawValue]
        ]
        invitationCollection.addMetaData(valueCmd: AUIInvitationCmd.sendApply.rawValue,
                                         value: value,
                                         filter: filter,
                                         callback: callback)
    }
    
    public func cancelApply(callback: @escaping (NSError?) -> ()) {
        let value: [String: Any] = [kStatusKey: AUIInvitationStatus.cancel.rawValue]
        let userId = getRoomContext().currentUserInfo.userId
        invitationCollection.mergeMetaData(valueCmd: AUIInvitationCmd.cancelApply.rawValue,
                                           value: value,
                                           filter: [[kUserIdKey: userId, kTypeKey: AUIInvitationType.apply.rawValue]],
                                           callback: callback)
    }
    
    public func acceptApply(userId: String, seatIndex: Int?, callback: @escaping (NSError?) -> ()) {
        let value: [String: Any] = [kStatusKey: AUIInvitationStatus.accept.rawValue]
        
        invitationCollection.mergeMetaData(valueCmd: AUIInvitationCmd.acceptApply.rawValue,
                                           value: value,
                                           filter: [[kUserIdKey: userId, kTypeKey: AUIInvitationType.apply.rawValue]],
                                           callback: callback)
    }
    
    public func rejectApply(userId: String, callback: @escaping (NSError?) -> ()) {
        let value: [String: Any] = [kStatusKey: AUIInvitationStatus.reject.rawValue]
        invitationCollection.mergeMetaData(valueCmd: AUIInvitationCmd.rejectApply.rawValue,
                                           value: value,
                                           filter: [[kUserIdKey: userId, kTypeKey: AUIInvitationType.apply.rawValue]],
                                           callback: callback)
    }
}

extension AUIInvitationServiceImpl {
    public func initService(completion: @escaping ((NSError?) -> ())){
    }
    
    public func cleanUserInfo(userId: String, completion: @escaping ((NSError?) -> ())) {
        invitationCollection.removeMetaData(valueCmd: nil,
                                            filter: [[kUserIdKey: userId]],
                                            callback: completion)
    }
    
    public func deinitService(completion: @escaping ((NSError?) -> ())) {
        invitationCollection.cleanMetaData(callback: completion)
    }
}

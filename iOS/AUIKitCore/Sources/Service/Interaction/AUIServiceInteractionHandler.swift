//
//  AUIServiceInteractionHandler.swift
//  AUIKitCore
//
//  Created by wushengtao on 2023/11/2.
//

import AgoraRtmKit

@objc public class AUIServiceInteractionHandler: NSObject {
//    private(set) var delegateList:NSHashTable<AUIServiceInteractionDelegate> = NSHashTable<AUIServiceInteractionDelegate>.weakObjects()
    private var channelName: String!
    private var rtmManager: AUIRtmManager!
    private var currentUserInfo: AUIUserThumbnailInfo!
    private(set) var lockOwnerId: String = ""
    
//    public func addDelegate(delegate: AUIServiceInteractionDelegate) {
//        if delegateList.contains(delegate) {
//            return
//        }
//        delegateList.add(delegate)
//    }
//    
//    public func removeDelegate(delegate: AUIServiceInteractionDelegate) {
//        delegateList.remove(delegate)
//    }
    
    
    deinit {
        aui_info("deinit AUIMicSeatServiceImpl", tag: "AUIMicSeatServiceImpl")
        rtmManager.unsubscribeLock(channelName: channelName, lockName: kRTM_Referee_LockName, delegate: self)
    }
    
    public init(channelName: String, rtmManager: AUIRtmManager, userInfo: AUIUserThumbnailInfo) {
        self.rtmManager = rtmManager
        self.channelName = channelName
        self.currentUserInfo = userInfo
        super.init()
        rtmManager.subscribeLock(channelName: channelName, lockName: kRTM_Referee_LockName, delegate: self)
        aui_info("init AUIMicSeatServiceImpl", tag: "AUIMicSeatServiceImpl")
    }
}

//extension AUIServiceInteractionHandler {
//    public func initRoom(channelName: String, userId: String, metaData: NSMutableDictionary? = nil) {
//        guard lockOwnerId == currentUserInfo.userId else { return }
//        let _metaData = metaData ?? NSMutableDictionary()
//        _ = onRoomWillInit(channelName: channelName, metaData: _metaData)
//        rtmManager.setMetadata(channelName: channelName, lockName: kRTM_Referee_LockName, metadata: _metaData as! [String : String]) { err in
//        }
//    }
//    
//    public func cleanUserInfo(channelName: String, userId: String, metaData: NSMutableDictionary? = nil) {
//        guard lockOwnerId == currentUserInfo.userId else { return }
//        let _metaData = metaData ?? NSMutableDictionary()
//        _ = onUserInfoClean(channelName: channelName, userId: userId, metaData: _metaData)
//        rtmManager.setMetadata(channelName: channelName, lockName: kRTM_Referee_LockName, metadata: _metaData as! [String : String]) { err in
//        }
//    }
//}

//MARK: AUIServiceInteractionDelegate
//extension AUIServiceInteractionHandler: AUIServiceInteractionDelegate {
//    public func onRoomWillInit(channelName: String, metaData: NSMutableDictionary) -> NSError? {
//        for delegate in delegateList.allObjects {
//            if let error = delegate.onRoomWillInit?(channelName: channelName, metaData: metaData) {
//                return error
//            }
//        }
//        return nil
//    }
//    
//    public func onUserInfoClean(channelName: String, userId: String, metaData: NSMutableDictionary) -> NSError? {
//        for delegate in delegateList.allObjects {
//            if let error = delegate.onUserInfoClean?(channelName: channelName, userId: userId, metaData: metaData) {
//                return error
//            }
//        }
//        return nil
//    }
//    
//    public func onSongWillSelect(channelName: String, userId: String, metaData: NSMutableDictionary) -> NSError? {
//        for delegate in delegateList.allObjects {
//            if let error = delegate.onSongWillSelect?(channelName: channelName, userId: userId, metaData: metaData) {
//                return error
//            }
//        }
//        return nil
//    }
//    
//    public func onSongDidRemove(channelName: String, songCode: String, metaData: NSMutableDictionary) -> NSError? {
//        for delegate in delegateList.allObjects {
//            if let error = delegate.onSongDidRemove?(channelName: channelName, songCode: songCode, metaData: metaData) {
//                return error
//            }
//        }
//        return nil
//    }
//    
//    public func onWillJoinChours(channelName: String, userId: String, metaData: NSMutableDictionary) -> NSError? {
//        for delegate in delegateList.allObjects {
//            if let error = delegate.onWillJoinChours?(channelName: channelName, userId: userId, metaData: metaData) {
//                return error
//            }
//        }
//        return nil
//    }
//}

//MARK: AUIRtmLockProxyDelegate
extension AUIServiceInteractionHandler: AUIRtmLockProxyDelegate {
    public func onReceiveLockDetail(channelName: String, lockDetail: AgoraRtmLockDetail) {
        aui_info("onReceiveLockDetail[\(channelName)]: \(lockDetail.owner)")
        guard channelName == self.channelName else {return}
        lockOwnerId = lockDetail.owner
    }
    
    public func onReleaseLockDetail(channelName: String, lockDetail: AgoraRtmLockDetail) {
        aui_info("onReleaseLockDetail[\(channelName)]: \(lockDetail.owner)")
        guard channelName == self.channelName else {return}
        rtmManager.acquireLock(channelName: channelName, lockName: kRTM_Referee_LockName) { err in
        }
    }
}

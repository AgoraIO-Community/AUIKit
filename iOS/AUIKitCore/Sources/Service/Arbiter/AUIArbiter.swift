//
//  AUIArbiter.swift
//  AUIKitCore
//
//  Created by wushengtao on 2023/11/2.
//

import AgoraRtmKit

@objc public class AUIArbiter: NSObject {
    private var channelName: String!
    private var rtmManager: AUIRtmManager!
    private var currentUserInfo: AUIUserThumbnailInfo!
    private(set) var lockOwnerId: String = ""
    
    deinit {
        aui_info("deinit AUIArbiter", tag: "AUIArbiter")
        rtmManager.unsubscribeLock(channelName: channelName, lockName: kRTM_Referee_LockName, delegate: self)
    }
    
    public init(channelName: String, rtmManager: AUIRtmManager, userInfo: AUIUserThumbnailInfo) {
        self.rtmManager = rtmManager
        self.channelName = channelName
        self.currentUserInfo = userInfo
        super.init()
        rtmManager.subscribeLock(channelName: channelName, lockName: kRTM_Referee_LockName, delegate: self)
        aui_info("init AUIArbiter", tag: "AUIArbiter")
    }
    
    public func create() {
        rtmManager.setLock(channelName: channelName, lockName: kRTM_Referee_LockName) { err in
        }
    }
    
    public func destroy() {
        rtmManager.removeLock(channelName: channelName, lockName: kRTM_Referee_LockName) { err in
        }
    }
    
    public func acquire() {
        rtmManager.acquireLock(channelName: channelName, lockName: kRTM_Referee_LockName) { err in
        }
    }
    
    public func release() {
        rtmManager.releaseLock(channelName: channelName, lockName: kRTM_Referee_LockName) { err in
        }
    }
    
    
    public func isArbiter() -> Bool {
        return lockOwnerId == currentUserInfo.userId
    }
}

//MARK: AUIRtmLockProxyDelegate
extension AUIArbiter: AUIRtmLockProxyDelegate {
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

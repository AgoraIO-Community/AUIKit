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
    
    public func sendInvitation(userId: String, seatIndex: Int?, callback: (Error?) -> ()) {
        
    }
    
    public func acceptInvitation(userId: String, seatIndex: Int?, callback: (Error?) -> ()) {
        
    }
    
    public func rejectInvitation(userId: String, callback: (Error?) -> ()) {
    }
    
    public func cancelInvitation(userId: String, callback: (Error?) -> ()) {
    }
    
    public func sendApply(seatIndex: Int?, callback: (Error?) -> ()) {
        
    }
    
    public func cancelApply(callback: (Error?) -> ()) {
        
    }
    
    public func acceptApply(userId: String, seatIndex: Int?, callback: (Error?) -> ()) {
        
    }
    
    public func rejectApply(userId: String, callback: (Error?) -> ()) {
        
    }
    
}

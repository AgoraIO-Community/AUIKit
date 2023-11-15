//
//  AUICommonServiceDelegate.swift
//  AUIKit
//
//  Created by wushengtao on 2023/3/8.
//

import Foundation

@objc public protocol AUICommonServiceDelegate: NSObjectProtocol {
    
    func getChannelName() -> String
    
    /// The room is about to be created, and initial metadata needs to be set up
    /// - Parameters:
    ///   - metaData: meta data
    /// - Returns: Error, if there is an error, it will interrupt the creation process
    @objc optional func onRoomWillInit(completion:  @escaping  ((NSError?) -> ()))
    
    
    /// <#Description#>
    /// - Parameters:
    ///   - metaData: <#metaData description#>
    /// - Returns: <#description#>
    @objc optional func onRoomWillDestroy(removeKeys: NSMutableArray) -> NSError?
    
    /// Clean up information for specified users
    /// - Parameters:
    ///   - userId: user id
    ///   - metaData: meta data
    /// - Returns: Error, if there is an error, it will interrupt the creation process
    @objc optional func onUserInfoClean(userId: String, completion:  @escaping  ((NSError?) -> ()))
    
    /// 获取当前房间上下文
    /// - Returns: <#description#>
    func getRoomContext() -> AUIRoomContext
}

extension AUICommonServiceDelegate {
    public func getRoomContext() -> AUIRoomContext {
        return AUIRoomContext.shared
    }
    
    public func currentUserIsRoomOwner() -> Bool {
        return getRoomContext().isRoomOwner(channelName: getChannelName())
    }
}

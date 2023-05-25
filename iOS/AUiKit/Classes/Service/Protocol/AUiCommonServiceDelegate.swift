//
//  AUICommonServiceDelegate.swift
//  AUIKit
//
//  Created by wushengtao on 2023/3/8.
//

import Foundation

public protocol AUICommonServiceDelegate: NSObjectProtocol {
    
    func getChannelName() -> String
    
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

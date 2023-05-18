//
//  AUiCommonServiceDelegate.swift
//  AUiKit
//
//  Created by wushengtao on 2023/3/8.
//

import Foundation

public protocol AUiCommonServiceDelegate: NSObjectProtocol {
    
    func getChannelName() -> String
    
    /// 获取当前房间上下文
    /// - Returns: <#description#>
    func getRoomContext() -> AUiRoomContext
}

extension AUiCommonServiceDelegate {
    public func getRoomContext() -> AUiRoomContext {
        return AUiRoomContext.shared
    }
    
    public func currentUserIsRoomOwner() -> Bool {
        return getRoomContext().isRoomOwner(channelName: getChannelName())
    }
}

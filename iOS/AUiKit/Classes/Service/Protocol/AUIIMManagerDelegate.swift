//
//  AUIIMManagerDelegate.swift
//  AUiKit
//
//  Created by 朱继超 on 2023/5/17.
//

import Foundation
import YYModel

public class AgoraChatTextMessage:NSObject {
 
    var messageId: String?
    
    var content: String?
    
    var user: AUiUserThumbnailInfo?
    
    func modelContainerPropertyGenericClass() -> [String: AnyClass] {
        return ["user": AUiUserThumbnailInfo.self]
    }
 
}
 
 
public enum AgoraChatroomBeKickedReason: Int {
 
    case kicked
 
    case offline
 
    case destroyed
 
}
 
 
 
 
public protocol AUIMManagerServiceDelegate: NSObjectProtocol {
     
 
    /// Description 发送文本消息
 
    /// - Parameters:
 
    ///   - roomId: 聊天室id
 
    ///   - text: 文本
 
    ///   - userInfo: 用户信息
 
    ///   - completion: 回调包含发送的消息以及是否成功
 
    func sendMessage(roomId: String, text: String, userInfo: AUiUserThumbnailInfo, completion: @escaping (AgoraChatTextMessage?, NSError?) -> Void)
 
     
 
    /// Description 加入聊天室
 
    /// - Parameters:
 
    ///   - roomId: 聊天室id
 
    ///   - completion: 回调包含聊天室id以及是否成功
 
    func joinedChatRoom(roomId: String, completion: @escaping ((String?, NSError?) -> Void))
 
     
 
    /// Description 退出聊天室
 
    /// - Parameter completion: 是否退出成功
 
    func userQuitRoom(completion: ((NSError?) -> Void)?)
 
     
 
    /// Description 销毁聊天室
 
    func userDestroyedChatroom()
 
     
 
}
 
 
 
 
public protocol AUIMManagerRespDelegate: NSObjectProtocol {
 
     
 
    /// Description 收到消息回调
 
    /// - Parameter messages: 消息数组
 
    func messagesDidReceive(messages: [AgoraChatTextMessage])
     
 
}
 
 
 
 

//
//  AUIIMManagerDelegate.swift
//  AUIKit
//
//  Created by 朱继超 on 2023/5/17.
//

import Foundation
import YYModel

public class AgoraChatTextMessage:NSObject {
    
    public var messageId: String?
    
    public var content: String?
    
    public var user: AUIUserThumbnailInfo?
    
    func modelContainerPropertyGenericClass() -> [String: AnyClass] {
        return ["user": AUIUserThumbnailInfo.self]
    }
    
}


public enum AgoraChatroomBeKickedReason: Int {
    
    case kicked
    
    case offline
    
    case destroyed
    
}




public protocol AUIMManagerServiceDelegate: NSObjectProtocol {
    /// 绑定响应回调
    /// - Parameter delegate: 需要回调的对象
    func bindRespDelegate(delegate: AUIMManagerRespDelegate)
    
    /// 解除绑响应回调
    /// - Parameter delegate: 需要回调的对象
    func unbindRespDelegate(delegate: AUIMManagerRespDelegate)
    
    /// Description 配置IMSDK
    /// - Parameters:
    ///   - appKey: AgoraChat  app key
    ///   - user: AUIUserThumbnailInfo instance
    /// - Returns: error
    func configIM(appKey: String, user:AUIUserThumbnailInfo,completion: @escaping (NSError?) -> Void)
    
    /// Description 创建聊天室
    /// - Parameters:
    ///   - roomId: 语聊房id
    ///   - completion: 回调 成功后会将聊天室id存储在implement实现类中
    func createChatRoom(roomId: String,completion: @escaping (String,NSError?) -> Void)
    /// Description 发送聊天室消息
    /// - Parameters:
    ///   - roomId: 聊天室id
    ///   - text: 文本内容
    ///   - userInfo: 用户信息
    ///   - completion: 回调包含发送成功的文本消息，失败消息为空，错误不为空
    func sendMessage(roomId: String, text: String, userInfo: AUIUserThumbnailInfo, completion: @escaping (AgoraChatTextMessage?, NSError?) -> Void)
    
    /// Description 加入聊天室
    /// - Parameters:
    ///   - roomId: 聊天室id
    ///   - completion: 回调包含聊天室id以及错误
    func joinedChatRoom(roomId: String, completion: @escaping ((String?, NSError?) -> Void))
    
    /// Description 退出聊天室
    /// - Parameter completion: 错误为空即为成功
    func userQuitRoom(completion: ((NSError?) -> Void)?)
    
    /// Description 销毁聊天室
    func userDestroyedChatroom()
    
}




@objc public protocol AUIMManagerRespDelegate: NSObjectProtocol {
    
    
    /// Description 接收到消息
    /// - Parameters:
    ///   - roomId: 聊天室id
    ///   - message: 接受到的文本小安溪
    func messageDidReceive(roomId: String, message: AgoraChatTextMessage)
    
    
    /// Description 用户加入聊天室
    /// - Parameters:
    ///   - roomId: 聊天室id
    ///   - user: 用户信息
    func onUserDidJoinRoom(roomId: String, user: AUIUserThumbnailInfo)
    
    
}





//
//  AUIIMServiceImplement.swift
//  AUiKit
//
//  Created by 朱继超 on 2023/5/17.
//implement

import Foundation
import AgoraChat
import YYModel

fileprivate let AUIChatRoomGift = "AUIChatRoomGift"

fileprivate let AUIChatRoomJoinedMember = "AUIChatRoomJoinedMember"

fileprivate let once = AUIIMManagerServiceImplement()

open class AUIIMManagerServiceImplement: NSObject {
    
    public var currentRoomId = ""
    
    /// Description 回调协议
    public weak var responseDelegate: AUIMManagerRespDelegate?
    
    /// Description 请求协议
    public weak var requestDelegate: AUIMManagerServiceDelegate?
    
    /// Description 消息回调协议
    public weak var delegate: AUIChatRoomIMDelegate?
    
    
    /// Description 单例
    public static var shared: AUIIMManagerServiceImplement? = once
    
    override init() {
        super.init()
        self.responseDelegate = self
        self.requestDelegate = self
    }
 
    
    /// Description judge login state
    public var isLogin: Bool {
        AgoraChatClient.shared().isLoggedIn
    }
    
    /// Description  登录IMSDK
    /// - Parameters:
    ///   - chatId: AgoraChat chatId
    ///   - token: chat token
    ///   - completion: 回调
    public func login(chatId: String,token: String, completion: @escaping (NSError?) -> Void) {
        if self.isLogin {
            completion(nil)
        } else {
            AgoraChatClient.shared().login(withUsername: chatId, token: token) { name, error in
                completion(AUiCommonError.httpError(error?.code.rawValue ?? 400, error?.errorDescription ?? "unknown error").toNSError())
            }
        }
    }
 
    /// Description 退出登录IMSDK
    public func logout() {
        AgoraChatClient.shared().logout(false)
    }
    
    /// Description 配置IMSDK
    /// - Parameters:
    ///   - appKey: AgoraChat  app key
    ///   - user: AUiUserThumbnailInfo instance
    /// - Returns: error
    public func configIM(appKey: String, user:[AUiUserThumbnailInfo]) -> NSError? {
        if appKey.isEmpty {
            return AUiCommonError.httpError(400, "app key is empty.").toNSError()
        }
        let options = AgoraChatOptions(appkey: appKey.isEmpty ? "easemob-demo#easeim" : appKey)
        options.enableConsoleLog = true
        options.isAutoLogin = false
        options.setValue("https://a1.chat.agora.io", forKeyPath: "restServer")
        let error = AgoraChatClient.shared().initializeSDK(with: options)
        return AUiCommonError.httpError(error?.code.rawValue ?? 400, error?.errorDescription ?? "unknown error").toNSError()
    }
    
    private func mapError(error: AgoraChatError?) -> NSError {
        return AUiCommonError.httpError(error?.code.rawValue ?? 400, error?.errorDescription ?? "unknown error").toNSError()
    }
    
}

//MARK: - AUIMManagerServiceDelegate
extension AUIIMManagerServiceImplement: AUIMManagerServiceDelegate {
    public func sendMessage(roomId: String, text: String, userInfo: AUiUserThumbnailInfo, completion: @escaping (AgoraChatTextMessage?, NSError?) -> Void) {
        let message = AgoraChatMessage(conversationID: roomId, body: AgoraChatTextMessageBody(text: text), ext: nil)
        message.chatType = .chatRoom
        AgoraChatClient.shared().chatManager?.send(message, progress: nil) { message, error in
            guard let responseMessage = message else { return }
            completion(self.convertTextMessage(message: responseMessage), self.mapError(error: error))
        }
    }
 
    public func joinedChatRoom(roomId: String, completion: @escaping ((String?, NSError?) -> Void)) {
        AgoraChatClient.shared().roomManager?.joinChatroom(roomId, completion: { room, error in
            if error == nil, let id = room?.chatroomId {
                self.currentRoomId = id
            }
            completion(self.currentRoomId, self.mapError(error: error))
        })
    }
 
    public func userQuitRoom(completion: ((NSError?) -> Void)?) {
        AgoraChatClient.shared().roomManager?.leaveChatroom(currentRoomId, completion: { error in
            if error == nil {
                self.currentRoomId = ""
            }
            if completion != nil {
                completion!(self.mapError(error: error))
            }
        })
    }
 
    public func userDestroyedChatroom() {
        AgoraChatClient.shared().roomManager?.destroyChatroom(self.currentRoomId)
    }
    
    private func convertTextMessage(message: AgoraChatMessage) -> AgoraChatTextMessage {
        let body = message.body as! AgoraChatTextMessageBody
        let textMessage = AgoraChatTextMessage()
        textMessage.messageId = message.messageId
        textMessage.content = body.text
        textMessage.user = AUiUserThumbnailInfo.yy_model(with: message.ext!)
        return textMessage
    }
    
}

//MARK: - AUIMManagerRespDelegate
extension AUIIMManagerServiceImplement: AUIMManagerRespDelegate {
    public func messagesDidReceive(messages: [AgoraChatTextMessage]) {
//        for message in messages {
//            if message.body is AgoraChatTextMessageBody {
//                if let callback = self.delegate, callback.responds(to: #selector(VoiceRoomIMDelegate.receiveTextMessage(roomId:message:))) {
//                    self.delegate?.receiveTextMessage(roomId: self.currentRoomId, message: self.convertTextMessage(message: message))
//                }
//                continue
//            }
//            if let body = message.body as? AgoraChatCustomMessageBody {
//                switch body.event {
//                case AUIChatRoomJoinedMember:
//                    if let callback = self.delegate, callback.responds(to: #selector(AUIChatRoomIMDelegate.userJoinedRoom(roomId:chatId:user:))) {
//                        if let ext = body.customExt["user"], let user = AUiUserThumbnailInfo.yy_model(with: ext) {
//                            self.delegate?.userJoinedRoom(roomId: message.to, chatId: message.from ?? "", user: user)
//                        }
//                    }
//                default:
//                    break
//                }
//            }
//        }
    }
    
}


@objc public protocol AUIChatRoomIMDelegate: NSObjectProtocol {
    /// Description you'll call login api,when you receive this message
    /// - Parameter code: AgoraChatErrorCode
    func chatTokenWillExpire(code: AgoraChatErrorCode)
    /// Description receive text message
    /// - Parameters:
    ///   - roomId: AgoraChat's uid
    ///   - message: VoiceRoomChatEntity
    func receiveTextMessage(roomId: String, message: AgoraChatTextMessage)
    
//    /// Description 收到礼物
//    /// - Parameters:
//    ///   - roomId: 聊天室id
//    ///   - meta: 扩展礼物信息
//    func receiveGift(roomId: String, gift: [String: String]?)

    
    /// Description 用户加入聊天室（携带用户信息）
    /// - Parameters:
    ///   - roomId: 聊天室id
    ///   - chatId: 用户聊天id
    ///   - user: 用户信息
    func userJoinedRoom(roomId: String, chatId: String, user: AUiUserThumbnailInfo)

   
}

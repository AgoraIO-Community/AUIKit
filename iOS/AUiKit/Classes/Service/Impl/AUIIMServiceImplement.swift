//
//  AUIIMServiceImplement.swift
//  AUiKit
//
//  Created by 朱继超 on 2023/5/17.
//implement

import Foundation
import AgoraChat
import YYModel

//fileprivate let AUIChatRoomGift = "AUIChatRoomGift"

fileprivate let AUIChatRoomJoinedMember = "AUIChatRoomJoinedMember"

fileprivate let once = AUIIMManagerServiceImplement()

open class AUIIMManagerServiceImplement: NSObject {
    
    public var currentRoomId = ""
    
    private var currentUser:AUiUserThumbnailInfo?
    
    /// Description 回调协议
    public weak var responseDelegate: AUIMManagerRespDelegate?
    
    /// Description 请求协议
    public weak var requestDelegate: AUIMManagerServiceDelegate?
    
    
    /// Description 单例
    public static var shared: AUIIMManagerServiceImplement? = once
    
    override init() {
        super.init()
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
    public func configIM(appKey: String, user:AUiUserThumbnailInfo) -> NSError? {
        if appKey.isEmpty {
            return AUiCommonError.httpError(400, "app key is empty.").toNSError()
        }
        let options = AgoraChatOptions(appkey: appKey.isEmpty ? "easemob-demo#easeim" : appKey)
        options.enableConsoleLog = true
        options.isAutoLogin = false
        options.setValue("https://a1.chat.agora.io", forKeyPath: "restServer")
        let error = AgoraChatClient.shared().initializeSDK(with: options)
        if error == nil {
            self.currentUser = user
        }
        return AUiCommonError.httpError(error?.code.rawValue ?? 400, error?.errorDescription ?? "unknown error").toNSError()
    }
    
    private func mapError(error: AgoraChatError?) -> NSError {
        return AUiCommonError.httpError(error?.code.rawValue ?? 400, error?.errorDescription ?? "unknown error").toNSError()
    }
    
    private func addChatRoomListener() {
        AgoraChatClient.shared().add(self, delegateQueue: .main)
        AgoraChatClient.shared().chatManager?.add(self, delegateQueue: .main)
        AgoraChatClient.shared().roomManager?.add(self, delegateQueue: .main)
    }

    private func removeListener() {
        AgoraChatClient.shared().removeDelegate(self)
        AgoraChatClient.shared().roomManager?.remove(self)
        AgoraChatClient.shared().chatManager?.remove(self)
    }
    
}

//MARK: - AUIMManagerServiceDelegate
extension AUIIMManagerServiceImplement: AUIMManagerServiceDelegate {
    public func sendMessage(roomId: String, text: String, userInfo: AUiUserThumbnailInfo, completion: @escaping (AgoraChatTextMessage?, NSError?) -> Void) {
        if !self.isLogin {
            completion(nil, AUiCommonError.httpError(400, "please login first.").toNSError())
            return
        }
        let message = AgoraChatMessage(conversationID: roomId, body: AgoraChatTextMessageBody(text: text), ext: nil)
        message.chatType = .chatRoom
        AgoraChatClient.shared().chatManager?.send(message, progress: nil) { message, error in
            guard let responseMessage = message else { return }
            completion(self.convertTextMessage(message: responseMessage), self.mapError(error: error))
        }
    }
 
    public func joinedChatRoom(roomId: String, completion: @escaping ((String?, NSError?) -> Void)) {
        if !self.isLogin {
            completion(nil, AUiCommonError.httpError(400, "please login first.").toNSError())
            return
        }
        AgoraChatClient.shared().roomManager?.joinChatroom(roomId, completion: { room, error in
            if error == nil, let id = room?.chatroomId {
                self.currentRoomId = id
                self.addChatRoomListener()
            }
            completion(self.currentRoomId, self.mapError(error: error))
        })
    }
 
    public func userQuitRoom(completion: ((NSError?) -> Void)?) {
        if !self.isLogin {
            if completion != nil {
                completion!(AUiCommonError.httpError(400, "please login first.").toNSError())
            } else {
                aui_error("quitChatroom failed! please login first.")
            }
            return
        }
        AgoraChatClient.shared().roomManager?.leaveChatroom(currentRoomId, completion: { error in
            if error == nil {
                self.currentRoomId = ""
                self.removeListener()
            }
            if completion != nil {
                completion!(self.mapError(error: error))
            }
        })
    }
 
    public func userDestroyedChatroom() {
        if !self.isLogin {
            aui_error("destroyChatroom failed! please login first.")
            return
        }
        AgoraChatClient.shared().roomManager?.destroyChatroom(self.currentRoomId)
        self.removeListener()
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

//MARK: - AgoraChat Delegate
extension AUIIMManagerServiceImplement: AgoraChatManagerDelegate, AgoraChatroomManagerDelegate, AgoraChatClientDelegate {
    public func messagesDidReceive(_ aMessages: [AgoraChatMessage]) {
        for message in aMessages {
            if message.body is AgoraChatTextMessageBody {
                if self.responseDelegate != nil {
                    self.responseDelegate?.messageDidReceive(roomId: self.currentRoomId, message: self.convertTextMessage(message: message))
                }
                continue
            }
            if let body = message.body as? AgoraChatCustomMessageBody {
                switch body.event {
                case AUIChatRoomJoinedMember:
                    if self.responseDelegate != nil {
                        if let ext = body.customExt["user"]?.a.jsonToDictionary(), let user = AUiUserThumbnailInfo.yy_model(with: ext) {
                            self.responseDelegate?.onUserDidJoinRoom(roomId: message.to, user: user)
                        }
                    }
                default:
                    break
                }
            }
        }
    }
    
}



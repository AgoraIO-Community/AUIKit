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

@objcMembers open class AUIIMManagerServiceImplement: NSObject {
    
    public var currentRoomId = ""
    
    public var chatId = ""
    
    public var chatToken = ""
    
    private var currentUser:AUiUserThumbnailInfo?
    
    private var responseDelegates: NSHashTable<AUIMManagerRespDelegate> = NSHashTable<AUIMManagerRespDelegate>.weakObjects()
    
    /// Description 请求协议
    public weak var requestDelegate: AUIMManagerServiceDelegate?
    
    @objc public override init() {
        super.init()
        self.requestDelegate = self
    }
    /// Description judge login state
    private var isLogin: Bool {
        AgoraChatClient.shared().isLoggedIn
    }
    
    /// Description  登录IMSDK
    /// - Parameters:
    ///   - completion: 回调
    private func login(completion: @escaping (NSError?) -> Void) {
        if self.isLogin {
            completion(nil)
        } else {
            AgoraChatClient.shared().login(withUsername: self.chatId, token: self.chatToken) { name, error in
                completion(AUiCommonError.httpError(error?.code.rawValue ?? 400, error?.errorDescription ?? "unknown error").toNSError())
            }
        }
        
    }
 
    /// Description 退出登录IMSDK
    private func logout() {
        AgoraChatClient.shared().logout(false)
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
    
    public func bindRespDelegate(delegate: AUIMManagerRespDelegate) {
        self.responseDelegates.add(delegate)
    }
    
    public func unbindRespDelegate(delegate: AUIMManagerRespDelegate) {
        self.responseDelegates.remove(delegate)
    }
    
    /// Description 配置IMSDK
    /// - Parameters:
    ///   - appKey: AgoraChat  app key
    ///   - user: AUiUserThumbnailInfo instance
    /// - Returns: error
    public func configIM(appKey: String, user:AUiUserThumbnailInfo, completion: @escaping (NSError?) -> Void) {
        var error: AgoraChatError?
        if !self.isLogin {
            let options = AgoraChatOptions(appkey: appKey.isEmpty ? "1129210531094378#auikit-voiceroom" : appKey)
            options.enableConsoleLog = true
            error = AgoraChatClient.shared().initializeSDK(with: options)
        }
        if error == nil {
            let model = AUIIMUserCreateNetworkModel()
            model.userName = user.userId
            model.request { error, obj in
                var callError: NSError?
                if error == nil,obj != nil,let data = obj as? Dictionary<String,String>,let userId = data["userName"],let accessToken = data["accessToken"] {
                    self.chatId = userId
                    self.chatToken = accessToken
                    self.login(completion: completion)
                    return
                } else {
                    callError = error as? NSError
                }
                completion(callError)
            }
            self.currentUser = user
        }
    }
    
    public func createChatRoom(roomId: String,completion: @escaping (String,NSError?) -> Void) {
        if !self.isLogin {
            completion("", AUiCommonError.httpError(400, "please login first.").toNSError())
            return
        }
        let model = AUIIMChatroomCreateNetworkModel()
        model.userId = self.chatId
        model.roomId = roomId//dic["roomId"]
        model.request { error, obj in
            var callError: NSError?
            var chatroomId = ""
            if error == nil,obj != nil,let data = obj as? Dictionary<String,String> {
                chatroomId = data["chatRoomId"] ?? ""
            } else {
                callError = error as? NSError
            }
            completion(chatroomId, callError)
        }
//        let room = AUiRoomCreateNetworkModel()
//        room.roomName = "test"
//        room.userId = self.chatId
//        room.userName = "UIKitTest1"
//        room.userAvatar = "testAvatar"
//        room.micSeatCount = 2
//        room.request { error, data in
//            if error == nil,data != nil,let dic = data as? Dictionary<String,String> {
                
//            }
//        }
    }
    
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
        AgoraChatClient.shared().roomManager?.leaveChatroom(self.currentRoomId, completion: { error in
            if error == nil {
                self.currentRoomId = ""
                self.removeListener()
                self.logout()
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
        self.logout()
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
                for response in self.responseDelegates.allObjects {
                    response.messageDidReceive(roomId: self.currentRoomId, message: self.convertTextMessage(message: message))
                }
                continue
            }
            if let body = message.body as? AgoraChatCustomMessageBody {
                switch body.event {
                case AUIChatRoomJoinedMember:
                    for response in self.responseDelegates.allObjects {
                        if let ext = body.customExt["user"]?.a.jsonToDictionary(), let user = AUiUserThumbnailInfo.yy_model(with: ext) {
                            response.onUserDidJoinRoom(roomId: message.to, user: user)
                        }
                    }
                default:
                    break
                }
            }
        }
    }
    
}



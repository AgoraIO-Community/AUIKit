//
//  AUIIMServiceImplement.swift
//  AUIKit
//
//  Created by 朱继超 on 2023/5/17.
//implement

import Foundation
import AgoraChat
import YYModel

private let kChatAttrKey = "chatRoom"
private let kChatIdKey = "chatRoomId"
fileprivate let AUIChatRoomJoinedMember = "AUIChatRoomJoinedMember"

@objcMembers open class AUIIMManagerServiceImplement: NSObject {
    
    public var currentRoomId = ""
    public var userId = ""
    public var chatToken = ""
    
    private var currentUser:AUIUserThumbnailInfo {
        return getRoomContext().currentUserInfo
    }
    
    private var channelName = ""
    
    private var mapCollection: AUIMapCollection!
    
    private var responseDelegates: NSHashTable<AUIMManagerRespDelegate> = NSHashTable<AUIMManagerRespDelegate>.weakObjects()
    
    /// Description 请求协议
    public weak var requestDelegate: AUIMManagerServiceDelegate?
    
    public init(channelName: String, rtmManager: AUIRtmManager) {
        self.channelName = channelName
        self.mapCollection = AUIMapCollection(channelName: channelName,
                                              observeKey: kChatAttrKey,
                                              rtmManager: rtmManager)
        super.init()
        
        mapCollection.subscribeAttributesDidChanged {[weak self] channelName, key, value in
            self?.onAttributesDidChanged(channelName: channelName, key: key, value: value)
        }
        aui_info("init AUIIMManagerServiceImplement", tag: "AUIIMManagerServiceImplement")
    }
    /// Description judge login state
    private var isLogin: Bool {
        AgoraChatClient.shared().isLoggedIn
    }
    
    /// Description  登录IMSDK
    /// - Parameters:
    ///   - completion: 回调
    private func login(completion: @escaping (NSError?) -> Void) {
        if isLogin {
            completion(nil)
            return
        }
        let userId = self.userId
        AgoraChatClient.shared().login(withUsername: userId, token: self.chatToken) { _, error in
            if let err = error {
                aui_warn("login fail: userId: \(userId) error: \(err.errorDescription ?? "")")
            }
            completion(error == nil ? nil : AUICommonError.httpError(error?.code.rawValue ?? 400, error?.errorDescription ?? "unknown error").toNSError())
        }
    }
    
    private func loginAndJoinChatRoom() {
        guard !self.currentRoomId.isEmpty, !self.chatToken.isEmpty else { return }
        self.login {[weak self] error in
            guard let self = self else { return }
            if error == nil {
                self.joinedChatRoom(roomId: self.currentRoomId) { message, error in
                    aui_info("joinedChatRoom:\(error == nil ? "successful!" : "\(error!.localizedDescription)")")
                }
            }
            aui_info("IM.onAttributesDidChanged login:\(error == nil ? "successful!":"failed! error = \(error!.localizedDescription)")")
        }
    }
 
    /// Description 退出登录IMSDK
    private func logout() {
        AgoraChatClient.shared().logout(false)
    }
    
    private func mapError(error: AgoraChatError?) -> NSError? {
        return error != nil ? AUICommonError.httpError(error?.code.rawValue ?? 400, error?.errorDescription ?? "unknown error").toNSError():nil
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
    
    private func joinChatRoom(completion: @escaping (NSError?) -> Void) {
        AgoraChatClient.shared().roomManager?.joinChatroom(self.currentRoomId, completion: { chatRoom, error in
            completion(self.mapError(error: error))
        })
    }
    
    deinit {
        requestDelegate = nil
        removeListener()
        if AUIRoomContext.shared.isRoomOwner(channelName: self.channelName) {
            userDestroyedChatroom()
        } else {
            userQuitRoom(completion: nil)
        }
        aui_info("deinit AUIIMManagerServiceImplement", tag: "AUIIMManagerServiceImplement")
    }
    
}
//MARK: - AUIRtmAttributesProxyDelegate
extension AUIIMManagerServiceImplement {
    public func getChannelName() -> String {
        self.channelName
    }
    
    private func onAttributesDidChanged(channelName: String, key: String, value: AUIAttributesModel) {
        if key == kChatAttrKey,
           let attributes = value.getMap(),
           let chatroomId = attributes[kChatIdKey] {
            aui_info("IM.onAttributesDidChanged chatroomId:\(chatroomId)")
            self.currentRoomId = "\(chatroomId)"
            if !AUIRoomContext.shared.isRoomOwner(channelName: self.channelName) {
                self.loginAndJoinChatRoom()
            }
        }
    }
}

//MARK: - AUIMManagerServiceDelegate
extension AUIIMManagerServiceImplement: AUIMManagerServiceDelegate {
    public func sereviceDidLoad() {
        configIM(channelName: channelName) { [weak self] error in
            guard let `self` = self else { return }
            aui_info(error != nil ? "IM initialize failed!":"IM initialize successful!")
            if error == nil {
                self.loginAndJoinChatRoom()
            }
        }
        self.requestDelegate = self
    }
    
    public func getRoomContext() -> AUIRoomContext {
        return AUIRoomContext.shared
    }
    
    public func bindRespDelegate(delegate: AUIMManagerRespDelegate) {
        self.responseDelegates.add(delegate)
    }
    
    public func unbindRespDelegate(delegate: AUIMManagerRespDelegate) {
        self.responseDelegates.remove(delegate)
    }
    
    public func configIM(channelName: String, completion: @escaping (NSError?) -> Void) {
        let model = AUIChatCreateNetworkModel()
        let isRoomOwner = AUIRoomContext.shared.isRoomOwner(channelName: channelName)
        if isRoomOwner {
            model.type = .userAndRoom
            let chatRoomConfig = AUIChatRoomConfig()
            chatRoomConfig.name = channelName
            model.chatRoomConfig = chatRoomConfig
        } else {
            model.type = .user
        }
        if let config = getRoomContext().commonConfig {
            let imConfig = AUIChatConfig()
            imConfig.appKey = config.imAppKey
            imConfig.clientId = config.imClientId
            imConfig.clientSecret = config.imClientSecret
            model.imConfig = imConfig
        }
        let chatUser = AUIChatUser()
        chatUser.username = currentUser.userId
        model.user = chatUser
        
        model.request {[weak self] error, obj in
            guard let self = self else { return }
            var callError: NSError?
            if error == nil,
               let data = obj as? [String: Any],
               let userToken = data["userToken"] as? String,   //用户accessToken
                let appKey = data["appKey"] as? String {
                self.userId = self.currentUser.userId //语聊房房间ID
                self.chatToken = userToken
                if let chatId = data["chatId"] as? String, !chatId.isEmpty {
                    self.mapCollection.addMetaData(valueCmd: nil, 
                                                   value: [kChatIdKey: chatId], filter: nil) { err in
                    }
                    self.currentRoomId = chatId
                }
                let options = AgoraChatOptions(appkey: appKey)
                options.isAutoLogin = false
                //TODO: - assert appkey empty
                options.enableConsoleLog = true
                let initializeError = AgoraChatClient.shared().initializeSDK(with: options)
                callError = self.mapError(error: initializeError)
            } else {
                callError = error as? NSError
            }
            completion(callError)
        }
    }
    
    public func sendMessage(roomId: String, text: String, completion: @escaping (AgoraChatTextMessage?, NSError?) -> Void) {
        if !self.isLogin {
            completion(nil, AUICommonError.httpError(400, "please login first.").toNSError())
            return
        }
        let message = AgoraChatMessage(conversationID: self.currentRoomId, body: AgoraChatTextMessageBody(text: text), ext: ["user": currentUser.yy_modelToJSONString() ?? ""])
        message.chatType = .chatRoom
        AgoraChatClient.shared().chatManager?.send(message, progress: nil) { message, error in
            guard let responseMessage = message else { return }
            completion(self.convertTextMessage(message: responseMessage,receive: false), self.mapError(error: error))
        }
    }
 
    public func joinedChatRoom(roomId: String, completion: @escaping ((AgoraChatTextMessage?, NSError?) -> Void)) {
        self.addChatRoomListener()
        self.joinChatRoom { error in
            if error == nil {
                if !self.isLogin {
                    completion(nil, AUICommonError.httpError(400, "please login first.").toNSError())
                    return
                }
                let message = AgoraChatMessage(conversationID: self.currentRoomId,
                                               body: AgoraChatCustomMessageBody(event: AUIChatRoomJoinedMember,
                                                                                customExt: ["user" : self.currentUser.yy_modelToJSONString() ?? ""]),
                                               ext: nil)
                message.chatType = .chatRoom
                AgoraChatClient.shared().chatManager?.send(message, progress: nil, completion: { message, error in
                    var textMessage: AgoraChatTextMessage?
                    if error == nil {
                        guard let responseMessage = message else { return }
                        textMessage = AgoraChatTextMessage()
                        textMessage?.messageId = responseMessage.messageId
                        textMessage?.content = "Joined".a.localize(type: .chat)
                        textMessage?.user = self.currentUser
                        for del in self.responseDelegates.allObjects {
                            del.onUserDidJoinRoom(roomId: self.currentRoomId, message:  textMessage ?? AgoraChatTextMessage())
                        }
                        self.currentRoomId = roomId
                    }
                    completion(textMessage, self.mapError(error: error))
                })
            } else {
                completion(nil,error)
            }
        }
    }
 
    public func userQuitRoom(completion: ((NSError?) -> Void)?) {
        if !self.isLogin {
            if completion != nil {
                completion!(AUICommonError.httpError(400, "please login first.").toNSError())
            } else {
                aui_error("quitChatroom failed! please login first.")
            }
            return
        }
        AgoraChatClient.shared().roomManager?.leaveChatroom(self.currentRoomId, completion:nil)
        self.currentRoomId = ""
        self.channelName = ""
        self.removeListener()
        self.logout()

    }
 
    public func userDestroyedChatroom() {
        if !self.isLogin {
            aui_error("destroyChatroom failed! please login first.")
            return
        }
        AgoraChatClient.shared().roomManager?.destroyChatroom(self.currentRoomId)
        self.removeListener()
        self.logout()
        self.channelName = ""
        self.currentRoomId = ""
    }
    
    private func convertTextMessage(message: AgoraChatMessage,receive: Bool) -> AgoraChatTextMessage {
        let body = message.body as! AgoraChatTextMessageBody
        let textMessage = AgoraChatTextMessage()
        textMessage.messageId = message.messageId
        textMessage.content = body.text
        if receive {
            if let jsonString = message.ext?["user"] as? String {
                textMessage.user = AUIUserThumbnailInfo.yy_model(with: jsonString.a.jsonToDictionary() )
            }
        } else {
            textMessage.user = self.currentUser
        }
        return textMessage
    }
    
    public func deinitService(completion: @escaping  ((NSError?) -> ())) {
        mapCollection.cleanMetaData(callback: completion)
    }
}

//MARK: - AgoraChat Delegate
extension AUIIMManagerServiceImplement: AgoraChatManagerDelegate, AgoraChatroomManagerDelegate, AgoraChatClientDelegate {
    public func messagesDidReceive(_ aMessages: [AgoraChatMessage]) {
        for message in aMessages {
            if message.body is AgoraChatTextMessageBody {
                for response in self.responseDelegates.allObjects {
                    response.messageDidReceive(roomId: self.currentRoomId, message: self.convertTextMessage(message: message,receive: true))
                }
                continue
            }
            if let body = message.body as? AgoraChatCustomMessageBody {
                switch body.event {
                case AUIChatRoomJoinedMember:
                    for response in self.responseDelegates.allObjects {
                        if let ext = body.customExt["user"], !ext.isEmpty{
                            let user = AUIUserThumbnailInfo.yy_model(with: ext.a.jsonToDictionary()) 
                            let textMessage = AgoraChatTextMessage()
                            textMessage.messageId = message.messageId
                            textMessage.content = "Joined".a.localize(type: .chat)
                            textMessage.user = user
                            response.onUserDidJoinRoom(roomId: message.to, message: textMessage)
                        }
                    }
                default:
                    break
                }
            }
        }
    }
    
    public func didDismiss(from aChatroom: AgoraChatroom, reason aReason: AgoraChatroomBeKickedReason) {
//        AUIToast.show(text: "You were kicked out of the chatroom".a.localize(type: .chat))
    }
}



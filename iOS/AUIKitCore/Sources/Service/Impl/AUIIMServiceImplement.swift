//
//  AUIIMServiceImplement.swift
//  AUIKit
//
//  Created by 朱继超 on 2023/5/17.
//implement

import Foundation
import AgoraChat
import YYModel


private let kChatIdKey = "chatRoomId"
fileprivate let AUIChatRoomJoinedMember = "AUIChatRoomJoinedMember"

@objcMembers open class AUIIMManagerServiceImplement: NSObject {
    
    public var currentRoomId = ""
    
    public var chatId = ""
    
    public var chatToken = ""
    
    private var currentUser:AUIUserThumbnailInfo?
    
    private var channelName = ""
    
    private var rtmManager: AUIRtmManager!
    
    
    private var responseDelegates: NSHashTable<AUIMManagerRespDelegate> = NSHashTable<AUIMManagerRespDelegate>.weakObjects()
    
    /// Description 请求协议
    public weak var requestDelegate: AUIMManagerServiceDelegate?
    
    public init(channelName: String, rtmManager: AUIRtmManager) {
        self.channelName = channelName
        self.rtmManager = rtmManager
        super.init()
        self.configIM(user: AUIRoomContext.shared.currentUserInfo, completion: { [weak self] error in
            guard let `self` = self else { return }
            aui_info(error != nil ? "IM initialize failed!":"IM initialize successful!")
            if error == nil {
                self.subscribeChatroomId()
                let channelName = self.channelName
                if AUIRoomContext.shared.isRoomOwner(channelName: channelName) {
                    self.login { error in
                        if error == nil {
                            self.createChatRoom(roomId: channelName) { id, error in
                                aui_info(error == nil ? "Create chatroom successful!":"Create chatroom failed!")
                            }
                        }
                        aui_info("login IM \(error == nil ? "successful!":"failed!")")
                    }
                    
                }
            }
        })
        self.requestDelegate = self
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
        AgoraChatClient.shared().login(withUsername: self.chatId, token: self.chatToken) { _, error in
            completion(error == nil ? nil:AUICommonError.httpError(error?.code.rawValue ?? 400, error?.errorDescription ?? "unknown error").toNSError())
        }
    }
    
    private func subscribeChatroomId() {
        self.rtmManager?.subscribeAttributes(channelName: channelName, itemKey: "chatRoom", delegate: self)
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
extension AUIIMManagerServiceImplement: AUIRtmAttributesProxyDelegate {
    
    public func getChannelName() -> String {
        self.channelName
    }
    
    public func onAttributesDidChanged(channelName: String, key: String, value: Any) {
        aui_info("IM.onAttributesDidChanged:\(value as? [String:String]) channelName:\(channelName)")
        if let attributes = value as? [String:String],let chatroomId = attributes[kChatIdKey]{
            aui_info("IM.onAttributesDidChanged chatroomId:\(chatroomId)")
            self.currentRoomId = "\(chatroomId)"
            if !AUIRoomContext.shared.isRoomOwner(channelName: self.channelName) {
                self.login { error in
                    if error == nil {
                        self.joinedChatRoom(roomId: self.currentRoomId) { message, error in
//                            AUIToast.show(text: "Join chatroom\(error == nil ? "successful!":"failed!")")
                            aui_info("IM.onAttributesDidChanged joinedChatRoom:\(error == nil ? "successful!":"failed! error = \(error!.localizedDescription)")")
                        }
                    }
//                    AUIToast.show(text: "login IM \(error == nil ? "successful!":"failed!")")
                    aui_info("IM.onAttributesDidChanged login:\(error == nil ? "successful!":"failed! error = \(error!.localizedDescription)")")
                }
            }
        }
    }
}

//MARK: - AUIMManagerServiceDelegate
extension AUIIMManagerServiceImplement: AUIMManagerServiceDelegate {
    
    public func getRoomContext() -> AUIRoomContext {
        return AUIRoomContext.shared
    }
    
    public func bindRespDelegate(delegate: AUIMManagerRespDelegate) {
        self.responseDelegates.add(delegate)
    }
    
    public func unbindRespDelegate(delegate: AUIMManagerRespDelegate) {
        self.responseDelegates.remove(delegate)
    }
    
    /// Description 配置IMSDK
    /// - Parameters:
    ///   - user: AUIUserThumbnailInfo instance
    /// - Returns: error
    public func configIM(user:AUIUserCellUserDataProtocol, completion: @escaping (NSError?) -> Void) {
        let userInfo = AUIUserThumbnailInfo()
        userInfo.userId = user.userId
        userInfo.userName = user.userName
        userInfo.userAvatar = user.userAvatar
        let model = AUIIMUserCreateNetworkModel()
        model.userName = user.userId
        model.request { error, obj in
            var callError: NSError?
            if error == nil,obj != nil,let data = obj as? Dictionary<String,String>,let userId = data["userName"],let accessToken = data["accessToken"],let appKey = data["appKey"] {
                self.chatId = userId
                self.chatToken = accessToken
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
        self.currentUser = userInfo
    }
    
    public func createChatRoom(roomId: String,completion: @escaping (String,NSError?) -> Void) {
        if !self.isLogin {
            completion("", AUICommonError.httpError(400, "please login first.").toNSError())
            return
        }
        let model = AUIIMChatroomCreateNetworkModel()
        model.userId = self.currentUser?.userId ?? ""
        model.roomId = roomId//dic["roomId"]
        model.userName = self.chatId
        model.request { error, obj in
            var callError: NSError?
            var chatroomId = ""
            if error == nil,obj != nil,let data = obj as? Dictionary<String,String> {
                chatroomId = data[kChatIdKey] ?? ""
                self.currentRoomId = chatroomId
                self.joinedChatRoom(roomId: chatroomId) { message, error in
                    callError = error
                    completion(chatroomId, callError)
                }
            } else {
                callError = error as? NSError
                completion(chatroomId, callError)
            }
        }

    }
    
    public func sendMessage(roomId: String, text: String, completion: @escaping (AgoraChatTextMessage?, NSError?) -> Void) {
        if !self.isLogin {
            completion(nil, AUICommonError.httpError(400, "please login first.").toNSError())
            return
        }
        let message = AgoraChatMessage(conversationID: self.currentRoomId, body: AgoraChatTextMessageBody(text: text), ext: ["user":self.currentUser?.yy_modelToJSONString() ?? ""])
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
                let message = AgoraChatMessage(conversationID: self.currentRoomId, body: AgoraChatCustomMessageBody(event: AUIChatRoomJoinedMember, customExt: ["user" : self.currentUser?.yy_modelToJSONString() ?? ""]), ext: nil)
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
        self.currentUser = nil

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
        self.currentUser = nil
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
    
    public func onRoomWillDestroy(completion:  @escaping  ((NSError?) -> ())) {
        rtmManager.cleanBatchMetadata(channelName: channelName,
                                      lockName: kRTM_Referee_LockName,
                                      removeKeys: [kChatIdKey],
                                      completion: completion)
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



//
//  AUIIMServiceImplement.swift
//  AUIKit
//
//  Created by 朱继超 on 2023/5/17.
//implement

import Foundation
import AgoraChat
import YYModel

import Foundation
import AgoraChat
import YYModel

//fileprivate let AUIChatRoomGift = "AUIChatRoomGift"

fileprivate let AUIChatRoomJoinedMember = "AUIChatRoomJoinedMember"

@objcMembers open class AUIIMManagerServiceImplement: NSObject {
    
    public var currentRoomId = ""
    
    public var chatId = ""
    
    public var chatToken = ""
    
    private var currentUser:AUIUserThumbnailInfo?
    
    private var channelName = ""
    
    private var rtmManager: AUIRtmManager?
    
    
    private var responseDelegates: NSHashTable<AUIMManagerRespDelegate> = NSHashTable<AUIMManagerRespDelegate>.weakObjects()
    
    /// Description 请求协议
    public weak var requestDelegate: AUIMManagerServiceDelegate?
    
    public init(channelName: String, rtmManager: AUIRtmManager) {
        self.channelName = channelName
        self.rtmManager = rtmManager
        super.init()
        self.requestDelegate = self
        self.subscribeChatroomId()
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
        if self.isLogin {
            completion(nil)
        } else {
            AgoraChatClient.shared().login(withUsername: self.chatId, token: self.chatToken) { [weak self] name, error in
                completion(AUICommonError.httpError(error?.code.rawValue ?? 400, error?.errorDescription ?? "unknown error").toNSError())
            }
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
    
}
//MARK: - AUIRtmAttributesProxyDelegate
extension AUIIMManagerServiceImplement: AUIRtmAttributesProxyDelegate {
    
    public func getChannelName() -> String {
        self.channelName
    }
    
    public func onAttributesDidChanged(channelName: String, key: String, value: Any) {
        print("IM.onAttributesDidChanged:\(value as? [String:String]) channelName:\(channelName)")
        if let attributes = value as? [String:String],let chatroomId = attributes["chatRoomId"]{
            print("IM.onAttributesDidChanged chatroomId:\(chatroomId)")
            self.currentRoomId = "\(chatroomId)"
            self.joinedChatRoom(roomId: self.currentRoomId) { message, error in
                
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
    ///   - appKey: AgoraChat  app key
    ///   - user: AUIUserThumbnailInfo instance
    /// - Returns: error
    public func configIM(appKey: String, user:AUIUserCellUserDataProtocol, completion: @escaping (NSError?) -> Void) {
        let userInfo = AUIUserThumbnailInfo()
        userInfo.userId = user.userId
        userInfo.userName = user.userName
        userInfo.userAvatar = user.userAvatar
        var error: AgoraChatError?
        if !self.isLogin {
            let options = AgoraChatOptions(appkey: appKey.isEmpty ? "1129210531094378#auikit-voiceroom" : appKey)
            options.enableConsoleLog = true
            error = AgoraChatClient.shared().initializeSDK(with: options)
        }
        if error == nil {
            let model = AUIIMUserCreateNetworkModel()
            model.userName = "agora\(user.userId)"
            model.host = "https://uikit-voiceroom-staging.bj2.agoralab.co"
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
            self.currentUser = userInfo
        }
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
                chatroomId = data["chatRoomId"] ?? ""
            } else {
                callError = error as? NSError
            }
            completion(chatroomId, callError)
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
        self.joinChatRoom { error in
            if error == nil {
                if !self.isLogin {
                    completion(nil, AUICommonError.httpError(400, "please login first.").toNSError())
                    return
                }
                
                self.addChatRoomListener()
                let message = AgoraChatMessage(conversationID: self.currentRoomId, body: AgoraChatCustomMessageBody(event: AUIChatRoomJoinedMember, customExt: ["user" : self.currentUser?.yy_modelToJSONString() ?? ""]), ext: nil)
                message.chatType = .chatRoom
                AgoraChatClient.shared().chatManager?.send(message, progress: nil, completion: { message, error in
                    guard let responseMessage = message else { return }
                    var textMessage: AgoraChatTextMessage?
                    if error == nil {
                        for del in self.responseDelegates.allObjects {
                            (del as? AUIMManagerRespDelegate)?.onUserDidJoinRoom(roomId: self.currentRoomId, user: self.currentUser!)
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
    
    private func convertTextMessage(message: AgoraChatMessage,receive: Bool) -> AgoraChatTextMessage {
        let body = message.body as! AgoraChatTextMessageBody
        let textMessage = AgoraChatTextMessage()
        textMessage.messageId = message.messageId
        textMessage.content = body.text
        if receive {
            if let jsonString = message.ext?["user"] as? String {
                textMessage.user = AUIUserThumbnailInfo.yy_model(with: jsonString.a.jsonToDictionary() ?? [:])
            }
        } else {
            textMessage.user = self.currentUser
        }
        return textMessage
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
                        if let ext = body.customExt["user"]?.a.jsonToDictionary(), let user = AUIUserThumbnailInfo.yy_model(with: ext) {
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



//
//  AUIGiftServiceImplement.swift
//  AUiKit
//
//  Created by 朱继超 on 2023/5/18.
//

import UIKit
import AgoraChat
import YYModel


fileprivate let AUIChatRoomGift = "AUIChatRoomGift"

fileprivate let once = AUIGiftServiceImplement()


class AUIGiftServiceImplement: NSObject {
    
    public var currentRoomId = ""
    
    private var currentUser:AUiUserThumbnailInfo?
    
    /// Description 回调协议
    public weak var responseDelegate: AUIGiftsManagerRespDelegate?
    
    /// Description 请求协议
    public weak var requestDelegate: AUIGiftsManagerServiceDelegate?
    
    /// Description 单例
    public static var shared: AUIGiftServiceImplement? = once
    
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

extension AUIGiftServiceImplement: AUIGiftsManagerServiceDelegate,AgoraChatManagerDelegate, AgoraChatroomManagerDelegate, AgoraChatClientDelegate {
    
    public func messagesDidReceive(_ aMessages: [AgoraChatMessage]) {
        for message in aMessages {
            if let body = message.body as? AgoraChatCustomMessageBody {
                switch body.event {
                case AUIChatRoomGift:
                    if self.responseDelegate != nil {
                        if let ext = body.customExt["user"]?.a.jsonToDictionary(), let gift = AUIGiftEntity.yy_model(with: ext) {
                            self.responseDelegate?.receiveGift(gift: gift)
                        }
                    }
                default:
                    break
                }
            }
        }
    }
    
    func giftsFromService(roomId: String, completion: @escaping ([AUIGiftTabEntity], Error?) -> Void) {
        //TODO: - mock data
    }
    
    func sendGift(gift: AUIGiftEntity, completion: @escaping (Error?) -> Void) {
        let jsonDic = gift.yy_modelToJSONObject() as? Dictionary<String,String>
        let message = AgoraChatMessage(conversationID: self.currentRoomId, body: AgoraChatCustomMessageBody(event: AUIChatRoomGift, customExt: jsonDic), ext: nil)
        message.chatType = .chatRoom
        AgoraChatClient.shared().chatManager?.send(message, progress: nil) { message,error in
            completion(self.mapError(error: error))
        }
    }
    
    
}

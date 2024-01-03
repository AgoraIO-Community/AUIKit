//
//  AUIGiftServiceImplement.swift
//  AUIKit
//
//  Created by 朱继超 on 2023/5/18.
//

import UIKit
import YYModel


fileprivate let AUIChatRoomGift = "AUIChatRoomGift"


@objc public class AUIGiftServiceImplement: NSObject {
        
    private var responseDelegates: NSHashTable<AnyObject> = NSHashTable<AnyObject>.weakObjects()
    
    /// Description 请求协议
    public weak var requestDelegate: AUIGiftsManagerServiceDelegate?
        
    private var channelName: String = ""
    private var rtmManager: AUIRtmManager?
    
    deinit {
        requestDelegate = nil
        aui_info("deinit AUIUserServiceImpl", tag: "AUIGiftServiceImplement")
        rtmManager?.unsubscribeMessage(channelName: channelName, delegate: self)
    }
    
    convenience public init(channelName: String, rtmManager: AUIRtmManager) {
        self.init()
        self.rtmManager = rtmManager
        self.channelName = channelName
        self.requestDelegate = self
        self.rtmManager?.subscribeMessage(channelName: channelName, delegate: self)
        aui_info("init AUIUserServiceImpl", tag: "AUIGiftServiceImplement")
    }
}

extension AUIGiftServiceImplement: AUIGiftsManagerServiceDelegate,AUIRtmMessageProxyDelegate {
    
    public func getRoomContext() -> AUIRoomContext {
        return AUIRoomContext.shared
    }
    
    public func getChannelName() -> String {
        self.channelName
    }
    
    public func bindRespDelegate(delegate: AUIGiftsManagerRespDelegate) {
        self.responseDelegates.add(delegate)
    }
    
    public func unbindRespDelegate(delegate: AUIGiftsManagerRespDelegate) {
        self.responseDelegates.remove(delegate)
    }
    
    
    public func onMessageReceive(publisher: String, message: String) {
        let messageJson = message.a.jsonToDictionary()
        aui_info("messageJson :\(messageJson)")
        guard let messageType = messageJson["messageType"] as? String,let messageInfo = messageJson["messageInfo"] as? String else { return }
        let dic = messageInfo.a.jsonToDictionary()
        guard let gift = AUIGiftEntity.yy_model(with: dic) else { return }
        switch messageType {
        case AUIChatRoomGift:
            for response in self.responseDelegates.allObjects {
                (response as? AUIGiftsManagerRespDelegate)?.receiveGift(gift: gift)
            }
        
        default:
            break
        }
    }
    
    public func giftsFromService(roomId: String, completion: @escaping ([AUIGiftTabEntity], NSError?) -> Void) {
        let model = AUIGiftNetworkModel()
        model.method = .get
        model.request { error, obj in
            if error == nil,obj != nil {
                let tabs = NSArray.yy_modelArray(with: AUIGiftTabEntity.self, json: obj!) as? [AUIGiftTabEntity]
                completion(tabs!, nil)
            } else {
                completion([], error as? NSError)
            }
        }
    }
    
    public func sendGift(gift: AUIGiftEntity, completion: @escaping (NSError?) -> Void) {
        gift.sendUser = AUIRoomContext.shared.currentUserInfo
        let json = gift.yy_modelToJSONString() ?? ""
        guard let message = ["messageType":AUIChatRoomGift,"messageInfo":json].a.toJsonString() else {
            completion(NSError(domain: "sendGift json error", code: 400))
            return
        }
        
        self.rtmManager?.publish(channelName: self.channelName, message: message,completion: completion)
    }
    
    
}

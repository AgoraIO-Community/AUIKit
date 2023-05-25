//
//  AUIGiftServiceImplement.swift
//  AUiKit
//
//  Created by 朱继超 on 2023/5/18.
//

import UIKit
import YYModel


fileprivate let AUIChatRoomGift = "AUIChatRoomGift"


public class AUIGiftServiceImplement: NSObject {
        
    private var responseDelegates: NSHashTable<AnyObject> = NSHashTable<AnyObject>.weakObjects()
    
    /// Description 请求协议
    public weak var requestDelegate: AUIGiftsManagerServiceDelegate?
        
    private var channelName: String = ""
    private var rtmManager: AUiRtmManager?
    
    deinit {
        aui_info("deinit AUiUserServiceImpl", tag: "AUiUserServiceImpl")
        rtmManager?.unsubscribeMessage(channelName: channelName, delegate: self)
    }
    
    convenience public init(channelName: String, rtmManager: AUiRtmManager) {
        self.init()
        self.rtmManager = rtmManager
        self.channelName = channelName
        self.requestDelegate = self
        self.rtmManager?.subscribeMessage(channelName: channelName, delegate: self)
        aui_info("init AUiUserServiceImpl", tag: "AUiUserServiceImpl")
    }
}

extension AUIGiftServiceImplement: AUIGiftsManagerServiceDelegate,AUiRtmMessageProxyDelegate {
    public func bindRespDelegate(delegate: AUIGiftsManagerRespDelegate) {
        self.responseDelegates.add(delegate)
    }
    
    public func unbindRespDelegate(delegate: AUIGiftsManagerRespDelegate) {
        self.responseDelegates.remove(delegate)
    }
    
    
    public func onMessageReceive(channelName: String, message: String) {
        switch message {
        case AUIChatRoomGift:
            for response in self.responseDelegates.allObjects {
                guard let gift = AUIGiftEntity.yy_model(with: message.a.jsonToDictionary()) else { return }
                (response as? AUIGiftsManagerRespDelegate)?.receiveGift(gift: gift)
            }
        
        default:
            break
        }
    }
    
    public func giftsFromService(roomId: String, completion: @escaping ([AUIGiftTabEntity], NSError?) -> Void) {
        //TODO: - mock data
    }
    
    public func sendGift(gift: AUIGiftEntity, completion: @escaping (NSError?) -> Void) {
        let json = gift.yy_modelToJSONString() ?? ""
        guard let message = ["messageType":AUIChatRoomGift,"messageInfo":json].a.toJsonString() else {
            completion(NSError(domain: "sendGift json error", code: 400))
            return
        }
        
        self.rtmManager?.publish(channelName: self.channelName, message: message,completion: completion)
    }
    
    
}

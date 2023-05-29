//
//  AUIGiftServiceImplement.swift
//  AUIKit
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
    private var rtmManager: AUIRtmManager?
    
    deinit {
        aui_info("deinit AUIUserServiceImpl", tag: "AUIUserServiceImpl")
        rtmManager?.unsubscribeMessage(channelName: channelName, delegate: self)
    }
    
    convenience public init(channelName: String, rtmManager: AUIRtmManager) {
        self.init()
        self.rtmManager = rtmManager
        self.channelName = channelName
        self.requestDelegate = self
        self.rtmManager?.subscribeMessage(channelName: channelName, delegate: self)
        aui_info("init AUIUserServiceImpl", tag: "AUIUserServiceImpl")
    }
}

extension AUIGiftServiceImplement: AUIGiftsManagerServiceDelegate,AUIRtmMessageProxyDelegate {
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
        let model = AUIGiftNetworkModel()
        model.method = .get
        model.host = "https://uikit-voiceroom-staging.bj2.agoralab.co"
        model.request { error, obj in
            if error == nil,obj != nil,let jsons = obj as? [[String:Any]] {
                var tabs = [AUIGiftTabEntity]()
                for json in jsons {
                    guard let gift = AUIGiftTabEntity.yy_model(with: json) else {
                        completion([], NSError(domain: "giftsFromService json error", code: 400))
                        return
                    }
                    tabs.append(gift)
                }
                completion(tabs, nil)
            } else {
                completion([], error as? NSError)
            }
        }
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

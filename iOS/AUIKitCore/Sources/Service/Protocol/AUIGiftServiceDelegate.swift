//
//  AUIGiftServiceDelegate.swift
//  AUiKit
//
//  Created by 朱继超 on 2023/5/18.
//

import Foundation
 
@objc public protocol AUIGiftsManagerServiceDelegate: AUICommonServiceDelegate {
    
    /// 绑定响应回调
    /// - Parameter delegate: 需要回调的对象
    func bindRespDelegate(delegate: AUIGiftsManagerRespDelegate)
    
    /// 解除绑响应回调
    /// - Parameter delegate: 需要回调的对象
    func unbindRespDelegate(delegate: AUIGiftsManagerRespDelegate)
 
    /// Description 礼物列表
    /// - Parameters:
    ///   - roomId: 房间id
    ///   - completion: 回调包含礼物数组
    func giftsFromService(roomId: String,completion: @escaping ([AUIGiftTabEntity],NSError?) -> Void)
     
    /// Description 发送礼物
    /// - Parameters:
    ///   - gift: 礼物模型
    ///   - completion: 回调
    func sendGift(gift: AUIGiftEntity,completion: @escaping (NSError?) -> Void)
}
 
@objc public protocol AUIGiftsManagerRespDelegate: NSObjectProtocol {
 
    /// Description 接收到礼物
    /// - Parameter gift: 收到的礼物
    func receiveGift(gift: AUIGiftEntity)
}

//
//  AUIGiftServiceDelegate.swift
//  AUiKit
//
//  Created by 朱继超 on 2023/5/18.
//

import Foundation
import YYModel

@objc public protocol AUIGiftEntityProtocol: NSObjectProtocol {
    var giftId: String {get}
    var giftName: String {get}
    var giftPrice: String {get}
    var giftCount: String {get}
    var giftIcon: String {get}
    /// Description 开发者可以上传服务器一个匹配礼物id的特效  特效名称为礼物的id  sdk会进入房间时拉取礼物资源并下载对应礼物id的特效，如果收到的礼物这个值为true 则会找到对应的特效全屏播放加广播，礼物资源以及特效资源下载服务端可做一个web页面供用户使用，每个app启动后加载场景之前预先去下载礼物资源缓存到磁盘供UIKit取用
    var giftEffect: String {get}
    
    var selected: Bool {get}
    
    var sendUser: AUIUserThumbnailInfo {get}
}

@objcMembers public class AUIGiftEntity:NSObject,NSMutableCopying,AUIGiftEntityProtocol {
      
    public func mutableCopy(with zone: NSZone? = nil) -> Any {
        let model = AUIGiftEntity()
        model.giftId = self.giftId
        model.giftCount = self.giftCount
        model.giftPrice = self.giftPrice
        model.giftName = self.giftName
        model.giftIcon = self.giftIcon
        model.giftEffect = self.giftEffect
        model.selected = self.selected
        let user = AUIUserThumbnailInfo()
        user.userId = self.sendUser.userId
        user.userName = self.sendUser.userName
        user.userAvatar = self.sendUser.userAvatar
        user.seatIndex = self.sendUser.seatIndex
        model.sendUser = user
        return model
    }

    public var giftId: String = ""
    public var giftName: String = ""
    public var giftPrice: String = ""
    public var giftCount: String = "1"
    public var giftIcon: String = ""
    /// Description 开发者可以上传服务器一个匹配礼物id的特效  特效名称为礼物的id  sdk会进入房间时拉取礼物资源并下载对应礼物id的特效，如果收到的礼物这个值为true 则会找到对应的特效全屏播放加广播，礼物资源以及特效资源下载服务端可做一个web页面供用户使用，每个app启动后加载场景之前预先去下载礼物资源缓存到磁盘供UIKit取用
    public var giftEffect: String = ""
    
    public var effectMD5: String = ""//礼物特效文件的md5和值，用于比对礼物是否发生变更下载替换文件
    
    public var selected = false
    
    public var sendUser: AUIUserThumbnailInfo = AUIUserThumbnailInfo()
 
    class func modelContainerPropertyGenericClass() -> Dictionary<String,Any> {
        return ["sendUser": AUIUserThumbnailInfo.self]
    }
         
    override public required init() {}
}
 
 
 
 
@objcMembers public class AUIGiftTabEntity: NSObject {
    
    /// Description 对应哪个tab index
    public var tabId: Int64 = 0
    
    /// Description 显示名称
    public var displayName: String?
    
    /// Description tab下礼物数据
    public var gifts: [AUIGiftEntity]?
    
    class func modelContainerPropertyGenericClass() -> Dictionary<String,Any> {
        return ["gifts": AUIGiftEntity.self]
    }
 
}
 
 
 
public protocol AUIGiftsManagerServiceDelegate: AUICommonServiceDelegate {
    
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
 
 
 
 
public protocol AUIGiftsManagerRespDelegate: NSObjectProtocol {
 
     
    /// Description 接收到礼物
    /// - Parameter gift: 收到的礼物
    func receiveGift(gift: AUIGiftEntity)
 
}

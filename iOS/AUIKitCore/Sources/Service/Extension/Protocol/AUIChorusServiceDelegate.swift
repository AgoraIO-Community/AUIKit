//
//  AUIChorusServiceDelegate.swift
//  AUIKit
//
//  Created by wushengtao on 2023/4/7.
//

import Foundation


/// 合唱者模型
@objcMembers open class AUIChoristerModel: NSObject {
    @objc public var userId: String = ""
    @objc public var chorusSongNo: String?          //合唱者演唱歌曲
//    public var owner: AUIUserThumbnailInfo?   //合唱者信息
}

/// 合唱Service
@objc public protocol AUIChorusServiceDelegate: AUICommonServiceDelegate {
    
    /// 绑定响应协议
    func bindRespDelegate(delegate: AUIChorusRespDelegate)
    
    /// 解绑响应协议
    /// - Parameter delegate: 需要回调的对象
    func unbindRespDelegate(delegate: AUIChorusRespDelegate)
    
    /// 获取合唱者列表
    /// - Parameter completion: 需要回调的对象
    func getChoristersList(completion: @escaping (Error?, [AUIChoristerModel]?)->())
    
    /// 加入合唱
    /// - Parameters:
    ///   - completion: 操作完成回调
    func joinChorus(songCode: String, userId: String?, completion: @escaping AUICallback)
    
    /// 退出合唱
    /// - Parameter completion: 操作完成回调
    func leaveChorus(songCode: String, userId: String?, completion: @escaping AUICallback)
}


/// 合唱响应协议
@objc public protocol AUIChorusRespDelegate: NSObjectProtocol {
    
    /// 合唱者加入
    /// - Parameter chorus: 加入的合唱者信息
    func onChoristerDidEnter(chorister: AUIChoristerModel)
    
    /// 合唱者离开
    /// - Parameter chorister: 离开的合唱者
    func onChoristerDidLeave(chorister: AUIChoristerModel)
    
    @objc optional func onWillJoinChours(songCode: String, userId: String, metaData: NSMutableDictionary) -> NSError?
}

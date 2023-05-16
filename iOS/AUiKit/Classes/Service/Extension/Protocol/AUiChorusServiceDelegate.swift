//
//  AUiChorusServiceDelegate.swift
//  AUiKit
//
//  Created by wushengtao on 2023/4/7.
//

import Foundation


/// 合唱者模型
open class AUiChoristerModel: NSObject {
    @objc public var userId: String = ""
    public var chorusSongNo: String?          //合唱者演唱歌曲
    public var owner: AUiUserThumbnailInfo?   //合唱者信息
}

/// 合唱Service
public protocol AUiChorusServiceDelegate: AUiCommonServiceDelegate {
    
    /// 绑定响应协议
    func bindRespDelegate(delegate: AUiChorusRespDelegate)
    
    /// 解绑响应协议
    /// - Parameter delegate: 需要回调的对象
    func unbindRespDelegate(delegate: AUiChorusRespDelegate)
    
    /// 获取合唱者列表
    /// - Parameter completion: 需要回调的对象
    func getChoristersList(completion: (Error?, [AUiChoristerModel]?)->())
    
    /// 加入合唱
    /// - Parameters:
    ///   - completion: 操作完成回调
    func joinChorus(songCode: String, userId: String?, completion: @escaping AUiCallback)
    
    /// 退出合唱
    /// - Parameter completion: 操作完成回调
    func leaveChorus(songCode: String, userId: String?, completion: @escaping AUiCallback)
}


/// 合唱响应协议
public protocol AUiChorusRespDelegate: NSObject {
    
    /// 合唱者加入
    /// - Parameter chorus: 加入的合唱者信息
    func onChoristerDidEnter(chorister: AUiChoristerModel)
    
    /// 合唱者离开
    /// - Parameter chorister: 离开的合唱者
    func onChoristerDidLeave(chorister: AUiChoristerModel)
}

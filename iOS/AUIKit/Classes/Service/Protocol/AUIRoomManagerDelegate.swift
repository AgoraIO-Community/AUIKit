
//
//  AUIRoomManagerDelegate.swift
//  AUIKit
//
//  Created by wushengtao on 2023/2/20.
//

import Foundation


/// 房间管理抽象协议
public protocol AUIRoomManagerDelegate: NSObjectProtocol {
    
    /// 绑定响应
    /// - Parameter delegate: 需要回调的对象
    func bindRespDelegate(delegate: AUIRoomManagerRespDelegate)
    
    /// 解绑响应
    /// - Parameter delegate: 需要回调的对象
    func unbindRespDelegate(delegate: AUIRoomManagerRespDelegate)
    
    /// 创建房间（房主调用），若房间不存在，系统将自动创建一个新房间
    /// - Parameters:
    ///   - roomId: 房间Id
    ///   - room: 房间信息
    ///   - callback: 操作完成回调
    func createRoom(room: AUICreateRoomInfo, callback: @escaping AUICreateRoomCallback)
    
    /// 销毁房间（房主调用）
    /// - Parameters:
    ///   - roomId: 房间id
    ///   - callback: 操作完成回调
    func destroyRoom(roomId: String, callback: @escaping AUICallback)
    
    /// 进入房间（听众调用）
    /// - Parameters:
    ///   - roomId: 房间id
    ///   - callback: 操作完成回调
    func enterRoom(roomId: String, callback: @escaping AUICallback)
    
    
    /// 退出房间（听众调用）
    /// - Parameters:
    ///   - roomId: 房间id
    ///   - callback: 操作完成回调
    func exitRoom(roomId: String, callback: @escaping AUICallback)
    
    /// 获取房间列表
    /// - Parameters:
    ///   - lastCreateTime: 最后1条数据的创建时间, 返回数据list的createTime字段值，如果为空, 默认会设置为服务器当前时间戳
    ///   - pageSize: 分页大小
    ///   - callback: 操作完成回调
    func getRoomInfoList(lastCreateTime: Int64?, pageSize: Int, callback: @escaping AUIRoomListCallback)
    
    /// Description 修改房间公告
    /// - Parameters:
    ///   - roomId: 房间id
    ///   - announcement: 房间公告内容
    ///   - callback: callback ,error nil 表示成功，否则表示失败
    func changeRoomAnnouncement(roomId: String, announcement: String, callback: @escaping AUICallback)
}

/// 房间操作对应的响应
public protocol AUIRoomManagerRespDelegate: NSObjectProtocol {

    /// 房间被销毁的回调
    /// - Parameter roomId: 房间id
    func onRoomDestroy(roomId: String)
    
    /// 房间信息变更回调
    /// - Parameters:
    ///   - roomId: 房间id
    ///   - roomInfo: 房间信息
    func onRoomInfoChange(roomId: String, roomInfo: AUIRoomInfo)
    
    /// Description 房间公告发生变更
    /// - Parameters:
    ///   - roomId: 房间id
    ///   - announcement: 公告变更内容
    func onRoomAnnouncementChange(roomId: String, announcement: String)
}

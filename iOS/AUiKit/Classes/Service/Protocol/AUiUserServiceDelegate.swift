//
//  AUiUserServiceDelegate.swift
//  AUiKit
//
//  Created by wushengtao on 2023/4/7.
//

import Foundation

public protocol AUiUserServiceDelegate: AUiCommonServiceDelegate {
    
    /// 绑定响应
    /// - Parameter delegate: 需要回调的对象
    func bindRespDelegate(delegate: AUiUserRespDelegate)
    
    /// 解绑协议
    /// - Parameter delegate: 需要回调的对象
    func unbindRespDelegate(delegate: AUiUserRespDelegate)
    
    /// 获取指定 userId 的用户信息，如果为 null，则获取房间内所有人的信息
    /// - Parameters:
    ///   - roomId: 房间id
    ///   - userIdList: 用户列表
    ///   - callback: 操作完成回调
    func getUserInfoList(roomId: String, userIdList: [String] , callback: @escaping AUiUserListCallback)

    /// 获取用户信息
    /// - Parameter userId: <#userId description#>
    /// - Returns: <#description#>
//    func getUserInfo(by userId: String) -> AUiUserThumbnailInfo?
    
    /// 对自己静音/解除静音
    /// - Parameters:
    ///   - isMute: true: 关闭麦克风 false: 开启麦克风
    ///   - callback: 操作完成回调
    func muteUserAudio(isMute: Bool, callback: @escaping AUiCallback)
    
    /// 对自己禁摄像头/解禁摄像头
    /// - Parameters:
    ///   - isMute: true: 关闭摄像头 false: 开启摄像头
    ///   - callback: 操作完成回调
    func muteUserVideo(isMute: Bool, callback: @escaping AUiCallback)
}

public protocol AUiUserRespDelegate: NSObject {
    
    /// 用户进入房间后获取到的所有用户信息
    /// - Parameters:
    ///   - roomId: 房间id
    ///   - userList: 用户列表
    func onRoomUserSnapshot(roomId: String, userList: [AUiUserInfo])
    
    /// 用户进入房间回调
    /// - Parameters:
    ///   - roomId: 房间id
    ///   - userInfo: 用户信息
    func onRoomUserEnter(roomId: String, userInfo: AUiUserInfo)
    
    ///  用户离开房间回调
    /// - Parameters:
    ///   - roomId: 房间id
    ///   - userInfo:  用户信息
    func onRoomUserLeave(roomId: String, userInfo: AUiUserInfo)
    
    /// 用户的信息被修改
    /// - Parameters:
    ///   - roomId: 房间id
    ///   - userInfo: 用户信息
    func onRoomUserUpdate(roomId: String, userInfo: AUiUserInfo)
    
    /// 用户关闭/开启了麦克风
    /// - Parameters:
    ///   - userId: 用户id
    ///   - mute: 静音状态
    func onUserAudioMute(userId: String, mute: Bool)
    
    /// 用户关闭/开启了摄像头
    /// - Parameters:
    ///   - userId: 用户id
    ///   - mute: 摄像头状态
    func onUserVideoMute(userId: String, mute: Bool)
}

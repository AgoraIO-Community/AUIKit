//
//  AUIJukeBoxServiceDelegate.swift
//  AUIKit
//
//  Created by wushengtao on 2023/3/6.
//

import Foundation

//歌曲管理Service协议
@objc public protocol AUIMusicServiceDelegate: AUICommonServiceDelegate {
    
    /// 绑定响应
    /// - Parameter delegate: 需要回调的对象
    func bindRespDelegate(delegate: AUIMusicRespDelegate)
    
    /// 解绑响应
    /// - Parameter delegate: 需要回调的对象
    func unbindRespDelegate(delegate: AUIMusicRespDelegate)
    
    /// 获取歌曲列表
    /// - Parameters:
    ///   - chartId: 榜单类型
    ///   - page: 页数，从1开始
    ///   - pageSize: 一页返回数量，最大50
    ///   - completion: 操作完成回调
    func getMusicList(chartId: Int,
                      page: Int,
                      pageSize: Int,
                      completion: @escaping AUIMusicListCompletion)
    
    /// 搜索歌曲
    /// - Parameters:
    ///   - keyword: 关键字
    ///   - page: 页数，从1开始
    ///   - pageSize: 一页返回数量，最大50
    ///   - completion: 操作完成回调
    func searchMusic(keyword: String,
                     page: Int,
                     pageSize: Int,
                     completion: @escaping AUIMusicListCompletion)
    
    /// 获取当前点歌列表
    /// - Parameter completion: 操作完成回调
    func getAllChooseSongList(completion: AUIChooseSongListCompletion?)
    
    /// 点一首歌
    /// - Parameters:
    ///   - songModel: 歌曲对象(是否需要只传songNo，后端通过mcc查？)
    ///   - completion: 操作完成回调
    func chooseSong(songModel:AUIMusicModel, completion: AUICallback?)
    
    /// 移除一首自己点的歌
    /// - Parameters:
    ///   - songCode: 歌曲id
    ///   - completion: 操作完成回调
    func removeSong(songCode: String, completion: AUICallback?)
    
    /// 置顶歌曲
    /// - Parameters:
    ///   - songCode: 歌曲id
    ///   - completion: 操作完成回调
    func pinSong(songCode: String, completion: AUICallback?)
    
    /// 更新歌曲播放状态
    /// - Parameters:
    ///   - playStatus: 播放状态
    ///   - completion: 操作完成回调
    func updatePlayStatus(songCode: String, playStatus: AUIPlayStatus, completion: AUICallback?)
}

//歌曲管理操作相关响应
@objc public protocol AUIMusicRespDelegate: NSObjectProtocol {
    /// 新增一首歌曲回调
    /// - Parameter song: <#song description#>
    func onAddChooseSong(song: AUIChooseMusicModel)
    
    /// 删除一首歌歌曲回调
    /// - Parameter song: <#song description#>
    func onRemoveChooseSong(song: AUIChooseMusicModel)
    
    /// 更新一首歌曲回调（例如修改play status）
    /// - Parameter song: <#song description#>
    func onUpdateChooseSong(song: AUIChooseMusicModel)
    
    /// 更新所有歌曲回调（例如pin）
    /// - Parameter song: <#song description#>
    func onUpdateAllChooseSongs(songs: [AUIChooseMusicModel])
    
    /// 歌曲即将添加
    /// - Parameters:
    ///   - userId: <#userId description#>
    ///   - metaData: <#metaData description#>
    /// - Returns: <#description#>
    @objc optional func onSongWillAdd(userId: String, metaData: NSMutableDictionary) -> NSError?
    
    /// 歌曲即将删除
    /// - Parameters:
    ///   - songCode: <#songCode description#>
    ///   - metaData: <#metaData description#>
    /// - Returns: <#description#>
    @objc optional func onSongWillRemove(songCode: String, metaData: NSMutableDictionary) -> NSError?

}

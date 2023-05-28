//
//  AUIJukeBoxServiceDelegate.swift
//  AUIKit
//
//  Created by wushengtao on 2023/3/6.
//

import Foundation

public typealias AUIMusicListCompletion = (Error?, [AUIMusicModel]?)->()
public typealias AUIChooseSongListCompletion = (Error?, [AUIChooseMusicModel]?)->()
public typealias AUILoadSongCompletion = (Error?, String?, String?)->()

public enum AUIPlayStatus: Int {
    case idle = 0      //待播放
    case playing       //播放中
}

@objcMembers
open class AUIMusicModel: NSObject {
    public var songCode: String = ""     //歌曲id，mcc则对应songCode
    public var name: String = ""         //歌曲名称
    public var singer: String = ""       //演唱者
    public var poster: String = ""       //歌曲封面海报
    public var releaseTime: String = ""  //发布时间
    public var duration: Int = 0         //歌曲长度，单位秒
    public var musicUrl: String = ""     //歌曲url，mcc则为空
    public var lrcUrl: String = ""       //歌词url，mcc则为空
}

@objcMembers
open class AUIChooseMusicModel: AUIMusicModel {
    public var owner: AUIUserThumbnailInfo?          //点歌用户
    public var pinAt: Int64 = 0                      //置顶歌曲时间，与19700101的时间差，单位ms，为0则无置顶操作
    public var createAt: Int64 = 0                   //点歌时间，与19700101的时间差，单位ms
    public var playStatus: AUIPlayStatus {    //播放状态
        AUIPlayStatus(rawValue: status) ?? .idle
    }
    
    @objc public var status: Int = 0
    
    class func modelContainerPropertyGenericClass() -> NSDictionary {
        return [
            "owner": AUIUserThumbnailInfo.self
        ]
    }
    
    //做歌曲变化比较用
    public static func == (lhs: AUIChooseMusicModel, rhs: AUIChooseMusicModel) -> Bool {
        aui_info("\(lhs.name)-\(rhs.name)   \(lhs.pinAt)-\(rhs.pinAt)", tag: "AUIChooseMusicModel")
        if lhs.songCode != rhs.songCode {
            return false
        }
            
        if lhs.musicUrl != rhs.musicUrl {
            return false
        }
            
        if lhs.lrcUrl != rhs.lrcUrl {
            return false
        }
            
        if lhs.owner?.userId ?? "" != rhs.owner?.userId ?? "" {
            return false
        }
            
        if lhs.pinAt != rhs.pinAt {
            return false
        }
            
        if lhs.createAt != rhs.createAt {
            return false
        }
            
        if lhs.playStatus.rawValue != rhs.playStatus.rawValue {
            return false
        }
        
        return true
    }
}

//歌曲管理Service协议
public protocol AUIMusicServiceDelegate: AUICommonServiceDelegate {
    
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
public protocol AUIMusicRespDelegate: NSObjectProtocol {
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
}

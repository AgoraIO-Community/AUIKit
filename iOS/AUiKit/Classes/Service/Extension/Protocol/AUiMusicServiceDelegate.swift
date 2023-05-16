//
//  AUiJukeBoxServiceDelegate.swift
//  AUiKit
//
//  Created by wushengtao on 2023/3/6.
//

import Foundation

public typealias AUiMusicListCompletion = (Error?, [AUiMusicModel]?)->()
public typealias AUiChooseSongListCompletion = (Error?, [AUiChooseMusicModel]?)->()
public typealias AUiLoadSongCompletion = (Error?, String?, String?)->()

public enum AUiPlayStatus: Int {
    case idle = 0      //待播放
    case playing       //播放中
}

@objcMembers
open class AUiMusicModel: NSObject {
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
open class AUiChooseMusicModel: AUiMusicModel {
    public var owner: AUiUserThumbnailInfo?          //点歌用户
    public var pinAt: Int64 = 0                      //置顶歌曲时间，与19700101的时间差，单位ms，为0则无置顶操作
    public var createAt: Int64 = 0                   //点歌时间，与19700101的时间差，单位ms
    public var playStatus: AUiPlayStatus {    //播放状态
        AUiPlayStatus(rawValue: status) ?? .idle
    }
    
    @objc public var status: Int = 0
    
    class func modelContainerPropertyGenericClass() -> NSDictionary {
        return [
            "owner": AUiUserThumbnailInfo.self
        ]
    }
    
    //做歌曲变化比较用
    public static func == (lhs: AUiChooseMusicModel, rhs: AUiChooseMusicModel) -> Bool {
        aui_info("\(lhs.name)-\(rhs.name)   \(lhs.pinAt)-\(rhs.pinAt)", tag: "AUiChooseMusicModel")
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
public protocol AUiMusicServiceDelegate: AUiCommonServiceDelegate {
    
    /// 绑定响应
    /// - Parameter delegate: 需要回调的对象
    func bindRespDelegate(delegate: AUiMusicRespDelegate)
    
    /// 解绑响应
    /// - Parameter delegate: 需要回调的对象
    func unbindRespDelegate(delegate: AUiMusicRespDelegate)
    
    /// 获取歌曲列表
    /// - Parameters:
    ///   - chartId: 榜单类型 
    ///   - page: 页数，从1开始
    ///   - pageSize: 一页返回数量，最大50
    ///   - completion: 操作完成回调
    func getMusicList(chartId: Int,
                      page: Int,
                      pageSize: Int,
                      completion: @escaping AUiMusicListCompletion)
    
    /// 搜索歌曲
    /// - Parameters:
    ///   - keyword: 关键字
    ///   - page: 页数，从1开始
    ///   - pageSize: 一页返回数量，最大50
    ///   - completion: 操作完成回调
    func searchMusic(keyword: String,
                     page: Int,
                     pageSize: Int,
                     completion: @escaping AUiMusicListCompletion)
    
    /// 获取当前点歌列表
    /// - Parameter completion: 操作完成回调
    func getAllChooseSongList(completion: AUiChooseSongListCompletion?)
    
    /// 点一首歌
    /// - Parameters:
    ///   - songModel: 歌曲对象(是否需要只传songNo，后端通过mcc查？)
    ///   - completion: 操作完成回调
    func chooseSong(songModel:AUiMusicModel, completion: AUiCallback?)
    
    /// 移除一首自己点的歌
    /// - Parameters:
    ///   - songCode: 歌曲id
    ///   - completion: 操作完成回调
    func removeSong(songCode: String, completion: AUiCallback?)
    
    /// 置顶歌曲
    /// - Parameters:
    ///   - songCode: 歌曲id
    ///   - completion: 操作完成回调
    func pinSong(songCode: String, completion: AUiCallback?)
    
    /// 更新歌曲播放状态
    /// - Parameters:
    ///   - playStatus: 播放状态
    ///   - completion: 操作完成回调
    func updatePlayStatus(songCode: String, playStatus: AUiPlayStatus, completion: AUiCallback?)
}

//歌曲管理操作相关响应
public protocol AUiMusicRespDelegate: NSObjectProtocol {
    /// 新增一首歌曲回调
    /// - Parameter song: <#song description#>
    func onAddChooseSong(song: AUiChooseMusicModel)
    
    /// 删除一首歌歌曲回调
    /// - Parameter song: <#song description#>
    func onRemoveChooseSong(song: AUiChooseMusicModel)
    
    /// 更新一首歌曲回调（例如修改play status）
    /// - Parameter song: <#song description#>
    func onUpdateChooseSong(song: AUiChooseMusicModel)
    
    /// 更新所有歌曲回调（例如pin）
    /// - Parameter song: <#song description#>
    func onUpdateAllChooseSongs(songs: [AUiChooseMusicModel])
}

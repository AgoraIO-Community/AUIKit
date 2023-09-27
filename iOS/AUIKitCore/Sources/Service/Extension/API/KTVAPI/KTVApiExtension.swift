//
//  KTVLrcViewDelegate.swift
//  AUIKitCore
//
//  Created by FanPengpeng on 2023/9/7.
//

@objc public protocol KTVLrcViewDelegate: NSObjectProtocol {
    func onUpdatePitch(pitch: Float)
    func onUpdateProgress(progress: Int)
    func onDownloadLrcData(url: String)
    func onHighPartTime(highStartTime: Int, highEndTime: Int)
}

public typealias LyricCallback = ((String?) -> Void)

/// 用户角色
@objc public enum KTVSingRole: Int {
    case soloSinger = 0     //独唱者
    case coSinger           //伴唱
    case leadSinger         //主唱
    case audience           //观众
//    case followSinger       //跟唱
}

//
//  AUiSongNetworkModel.swift
//  AUiKit
//
//  Created by FanPengpeng on 2023/3/22.
//

import UIKit

@objcMembers
open class AUiSongAddNetworkModel: AUiCommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = "/v1/song/add"
    }
    
    public var roomId: String?
    public var songCode: String?
    public var singer: String?
    public var name: String?
    public var poster: String?
    public var releaseTime: String?
    public var duration: Int = 0
    public var musicUrl: String?
    public var lrcUrl: String?
    public var micSeatNo: Int = 0
    public var owner: AUiUserThumbnailInfo?  //房主信息
    
    class func modelContainerPropertyGenericClass() -> NSDictionary {
        return [
            "owner": AUiUserThumbnailInfo.self
        ]
    }
}

open class AUiSongPinNetworkModel: AUiCommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = "/v1/song/pin"
    }
    
    public var roomId: String?
    public var songCode: String?
}

open class AUiSongRemoveNetworkModel: AUiCommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = "/v1/song/remove"
    }
    
    public var roomId: String?
    public var songCode: String?
}

open class AUiSongPlayNetworkModel: AUiCommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = "/v1/song/play"
    }
    
    public var roomId: String?
    public var songCode: String?
}

open class AUiSongStopNetworkModel: AUiCommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = "/v1/song/stop"
    }
    
    public var roomId: String?
    public var songCode: String?
}




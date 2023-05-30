//
//  AUISongNetworkModel.swift
//  AUIKit
//
//  Created by FanPengpeng on 2023/3/22.
//

import UIKit

@objcMembers
open class AUISongAddNetworkModel: AUICommonNetworkModel {
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
    public var owner: AUIUserThumbnailInfo?  //房主信息
    
    class func modelContainerPropertyGenericClass() -> NSDictionary {
        return [
            "owner": AUIUserThumbnailInfo.self
        ]
    }
}

open class AUISongPinNetworkModel: AUICommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = "/v1/song/pin"
    }
    
    public var roomId: String?
    public var songCode: String?
}

open class AUISongRemoveNetworkModel: AUICommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = "/v1/song/remove"
    }
    
    public var roomId: String?
    public var songCode: String?
}

open class AUISongPlayNetworkModel: AUICommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = "/v1/song/play"
    }
    
    public var roomId: String?
    public var songCode: String?
}

open class AUISongStopNetworkModel: AUICommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = "/v1/song/stop"
    }
    
    public var roomId: String?
    public var songCode: String?
}




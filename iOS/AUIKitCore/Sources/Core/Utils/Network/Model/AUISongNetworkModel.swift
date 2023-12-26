//
//  AUISongNetworkModel.swift
//  AUIKit
//
//  Created by FanPengpeng on 2023/3/22.
//

import UIKit

public let kAUISongAddNetworkInterface = "/v1/song/add"
public let kAUISongPinNetworkInterface = "/v1/song/pin"
public let kAUISongRemoveNetworkInterface = "/v1/song/remove"
public let kAUISongPlayNetworkInterface = "/v1/song/play"
public let kAUISongStopNetworkInterface = "/v1/song/stop"

@objcMembers
open class AUISongAddNetworkModel: AUICommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = kAUISongAddNetworkInterface
    }
    
    public var songCode: String?
    public var singer: String?
    public var name: String?
    public var poster: String?
//    public var releaseTime: String?
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
        interfaceName = kAUISongPinNetworkInterface
    }
    
    public var songCode: String?
}

open class AUISongRemoveNetworkModel: AUICommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = kAUISongRemoveNetworkInterface
    }
    
    public var songCode: String?
}

open class AUISongPlayNetworkModel: AUICommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = kAUISongPlayNetworkInterface
    }
    
    public var songCode: String?
}

open class AUISongStopNetworkModel: AUICommonNetworkModel {
    public override init() {
        super.init()
        interfaceName = kAUISongStopNetworkInterface
    }
    
    public var songCode: String?
}




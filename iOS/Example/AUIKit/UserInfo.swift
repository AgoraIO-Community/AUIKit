//
//  UserInfo.swift
//  AUIKit_Example
//
//  Created by 朱继超 on 2023/8/15.
//  Copyright © 2023 CocoaPods. All rights reserved.
//

import Foundation
import AUIKitCore

@objc class UserInfo:NSObject, AUIUserCellUserDataProtocol {
    var userName: String = ""
    var userAvatar: String = ""
    var userId: String = ""
    var seatIndex: Int = 0
    var isOwner: Bool = true
    
    class var users: [UserInfo] {
        var users = [UserInfo]()
        for i in 0..<userNames.count {
            let user = UserInfo()
            user.userName = userNames[i]
            user.userAvatar = userAvatars[i]
            users.append(user)
        }
        return users
    }
    
    static private let userNames = ["安迪","路易","汤姆","杰瑞","杰森","布朗","吉姆"]

    static private let userAvatars = [
        "https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/sample_avatar/sample_avatar_1.png",
        "https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/sample_avatar/sample_avatar_2.png",
        "https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/sample_avatar/sample_avatar_3.png",
        "https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/sample_avatar/sample_avatar_4.png",
        "https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/sample_avatar/sample_avatar_5.png",
        "https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/sample_avatar/sample_avatar_6.png",
        "https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/sample_avatar/sample_avatar_7.png"
    ]

}

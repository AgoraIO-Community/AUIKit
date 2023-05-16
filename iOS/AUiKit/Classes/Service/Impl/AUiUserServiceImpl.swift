//
//  AUiUserServiceImpl.swift
//  AUiKit
//
//  Created by wushengtao on 2023/4/7.
//

import Foundation


open class AUiUserServiceImpl: NSObject {
    private var userList: [AUiUserInfo] = []
    private var respDelegates: NSHashTable<AnyObject> = NSHashTable<AnyObject>.weakObjects()
    private var channelName: String!
    private let rtmManager: AUiRtmManager!
    private let roomManager: AUiRoomManagerDelegate!
    
    deinit {
        aui_info("deinit AUiUserServiceImpl", tag: "AUiUserServiceImpl")
        self.rtmManager.unsubscribeUser(channelName: channelName, delegate: self)
    }
    
    public init(channelName: String, rtmManager: AUiRtmManager, roomManager: AUiRoomManagerDelegate) {
        self.rtmManager = rtmManager
        self.channelName = channelName
        self.roomManager = roomManager
        super.init()
        self.rtmManager.subscribeUser(channelName: channelName, delegate: self)
        aui_info("init AUiUserServiceImpl", tag: "AUiUserServiceImpl")
    }
}


extension AUiUserServiceImpl: AUiRtmUserProxyDelegate {
    public func onUserDidUpdated(channelName: String, userId: String, userInfo: [String : Any]) {
        aui_info("onUserDidUpdated: \(userId)", tag: "AUiUserServiceImpl")
        let user = AUiUserInfo.yy_model(withJSON: userInfo)!
        user.userId = userId
        if let oldUser = self.userList.first(where: {$0.userId == userId}) {
            if oldUser.muteAudio != user.muteAudio {
                oldUser.muteAudio = user.muteAudio
                self.respDelegates.allObjects.forEach { obj in
                    guard let obj = obj as? AUiUserRespDelegate else {return}
                    obj.onUserAudioMute(userId: userId, mute: user.muteAudio)
                }
            }
            
            if oldUser.muteVideo != user.muteVideo {
                oldUser.muteVideo = user.muteVideo
                self.respDelegates.allObjects.forEach { obj in
                    guard let obj = obj as? AUiUserRespDelegate else {return}
                    obj.onUserVideoMute(userId: userId, mute: user.muteVideo)
                }
            }
        }
        
        if let idx = self.userList.firstIndex(where: {$0.userId == userId}) {
            self.userList.replaceSubrange(idx...idx, with: [user])
        } else {
            self.userList.append(user)
        }
        self.respDelegates.allObjects.forEach { obj in
            guard let obj = obj as? AUiUserRespDelegate else {return}
            obj.onRoomUserUpdate(roomId: channelName, userInfo: user)
        }
    }
    
    public func onUserSnapshotRecv(channelName: String, userId: String, userList: [[String : Any]]) {
        aui_info("onUserSnapshotRecv: \(userId)", tag: "AUiUserServiceImpl")
        guard let users = NSArray.yy_modelArray(with: AUiUserInfo.self, json: userList) as? [AUiUserInfo] else {
            assert(false, "onUserSnapshotRecv recv fail")
            return
        }
        self.respDelegates.allObjects.forEach { obj in
            guard let obj = obj as? AUiUserRespDelegate else {return}
            self.userList = users
            obj.onRoomUserSnapshot(roomId: channelName, userList: users)
        }
        
        //对于2.1.0版本。我们推荐在join之后收到snapshot之后再去设置state
        _setupUserAttr(roomId: channelName) { error in
            //TODO: retry if fail
        }
    }
    
    public func onUserDidJoined(channelName: String, userId: String, userInfo: [String : Any]) {
        aui_info("onUserDidJoined: \(userId)", tag: "AUiUserServiceImpl")
        let user = AUiUserInfo.yy_model(withJSON: userInfo)!
        user.userId = userId
        self.userList.append(user)
        self.respDelegates.allObjects.forEach { obj in
            guard let obj = obj as? AUiUserRespDelegate else {return}
            obj.onRoomUserEnter(roomId: channelName, userInfo: user)
        }
    }
    
    public func onUserDidLeaved(channelName: String, userId: String, userInfo: [String : Any]) {
        aui_info("onUserDidLeaved: \(userId)", tag: "AUiUserServiceImpl")
        let user = userList.filter({$0.userId == userId}).first ?? AUiUserInfo.yy_model(withJSON: userInfo)!
        self.userList = userList.filter({$0.userId != userId})
        self.respDelegates.allObjects.forEach { obj in
            guard let obj = obj as? AUiUserRespDelegate else {return}
            obj.onRoomUserLeave(roomId: channelName, userInfo: user)
        }
    }
}

extension AUiUserServiceImpl: AUiUserServiceDelegate {
    public func getChannelName() -> String {
        return channelName
    }
    
    public func bindRespDelegate(delegate: AUiUserRespDelegate) {
        respDelegates.add(delegate)
    }
    
    public func unbindRespDelegate(delegate: AUiUserRespDelegate) {
        respDelegates.remove(delegate)
    }
    
    public func getUserInfoList(roomId: String, userIdList: [String], callback:@escaping AUiUserListCallback) {
        self.rtmManager.whoNow(channelName: roomId) { error, userList in
            if let error = error {
                callback(error, nil)
                return
            }
            
            var users = [AUiUserInfo]()
            userList?.forEach { attr in
                let user = AUiUserInfo.yy_model(withJSON: attr)!
                users.append(user)
            }
            self.userList = users
            callback(nil, users)
        }
    }
    
//    public func getUserInfo(by userId: String) -> AUiUserThumbnailInfo? {
//        return self.userList.filter {$0.userId == userId}.first
//    }
    
    public func muteUserAudio(isMute: Bool, callback: @escaping AUiCallback) {
        aui_info("muteUserAudio: \(isMute)", tag: "AUiUserServiceImpl")
        let currentUserId = getRoomContext().currentUserInfo.userId
        let currentUser = userList.first(where: {$0.userId == currentUserId})
        currentUser?.muteAudio = isMute
        let userAttr = currentUser?.yy_modelToJSONObject() as? [String: Any] ?? [:]
//        print("muteUserAudio user:  \(userDic)")
        self.rtmManager.setPresenceState(channelName: channelName, attr: userAttr) {[weak self] error in
            guard let self = self else {return}
            if let error = error {
                callback(error)
                return
            }
            
            callback(nil)
            
            //自己状态不会更新，在这里手动回调
            self.respDelegates.allObjects.forEach { obj in
                guard let obj = obj as? AUiUserRespDelegate else {return}
                obj.onUserAudioMute(userId: currentUserId, mute: isMute)
            }
        }
    }
    
    public func muteUserVideo(isMute: Bool, callback: @escaping AUiCallback) {
        aui_info("muteUserVideo: \(isMute)", tag: "AUiUserServiceImpl")
        let currentUserId = getRoomContext().currentUserInfo.userId
        let currentUser = userList.first(where: {$0.userId == currentUserId})
        currentUser?.muteVideo = isMute
        let userAttr = currentUser?.yy_modelToJSONObject() as? [String: Any] ?? [:]
//        print("muteUserAudio user:  \(userDic)")
        self.rtmManager.setPresenceState(channelName: channelName, attr: userAttr) {[weak self] error in
            guard let self = self else {return}
            if let error = error {
                callback(error)
                return
            }
            
            callback(nil)
            
            //自己状态不会更新，在这里手动回调
            self.respDelegates.allObjects.forEach { obj in
                guard let obj = obj as? AUiUserRespDelegate else {return}
                obj.onUserVideoMute(userId: currentUserId, mute: isMute)
            }
        }
    }
}

extension AUiUserServiceImpl {
    //设置用户属性到presence
    private func _setupUserAttr(roomId: String, completion: ((Error?) -> ())?) {
        let userId = AUiRoomContext.shared.currentUserInfo.userId
        let userInfo = self.userList.filter({$0.userId == userId}).first ?? AUiUserInfo()
        userInfo.userId = AUiRoomContext.shared.currentUserInfo.userId
        userInfo.userName = AUiRoomContext.shared.currentUserInfo.userName
        userInfo.userAvatar = AUiRoomContext.shared.currentUserInfo.userAvatar
        
        let userAttr = userInfo.yy_modelToJSONObject() as? [String: Any] ?? [:]
        aui_info("_setupUserAttr: \(roomId) : \(userAttr)", tag: "AUiUserServiceImpl")
        self.rtmManager.setPresenceState(channelName: roomId, attr: userAttr) { error in
            defer {
                completion?(error)
            }
            if let error = error {
                aui_info("_setupUserAttr: \(roomId) fail: \(error.localizedDescription)", tag: "AUiUserServiceImpl")
                //TODO: retry
                return
            }
            
            //rtm不会返回自己更新的数据，需要手动处理
            self.onUserDidUpdated(channelName: roomId, userId: userId, userInfo: userAttr)
        }
    }
}

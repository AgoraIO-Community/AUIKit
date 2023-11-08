//
//  AUIServiceInteractionDelegate.swift
//  AUIKitCore
//
//  Created by wushengtao on 2023/11/2.
//

import Foundation


/// Protocol for interaction between services
@objc public protocol AUIServiceInteractionDelegate: NSObjectProtocol {
    
    /// The room is about to be created, and initial metadata needs to be set up
    /// - Parameters:
    ///   - channelName: channel name
    ///   - metaData: meta data
    /// - Returns: Error, if there is an error, it will interrupt the creation process
    @objc optional func onRoomWillInit(channelName: String, metaData: NSMutableDictionary) -> NSError?
    
    /// Clean up information for specified users
    /// - Parameters:
    ///   - channelName: channel name
    ///   - userId: user id
    ///   - metaData: meta data
    /// - Returns: Error, if there is an error, it will interrupt the creation process
    @objc optional func onUserInfoClean(channelName: String, userId: String, metaData: NSMutableDictionary) -> NSError?
    
    
    @objc optional func onSongWillSelect(channelName: String, userId: String, metaData: NSMutableDictionary) -> NSError?
    
    @objc optional func onSongDidRemove(channelName: String, songCode: String, metaData: NSMutableDictionary) -> NSError?
    
    @objc optional func onWillJoinChours(channelName: String, userId: String, metaData: NSMutableDictionary) -> NSError?
}

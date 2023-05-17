//
//  AUiRoomContext.swift
//  AUiKit
//
//  Created by wushengtao on 2023/3/3.
//

import Foundation
import SwiftTheme

private let themeNames = ["UIKit", "KTV"]
open class AUiRoomContext: NSObject {
    public static let shared: AUiRoomContext = AUiRoomContext()
    
    public let currentUserInfo: AUiUserThumbnailInfo = AUiUserThumbnailInfo()
    public var commonConfig: AUiCommonConfig? {
        didSet {
            guard let config = commonConfig else {return}
            currentUserInfo.userName = config.userName
            currentUserInfo.userId = config.userId
            currentUserInfo.userAvatar = config.userAvatar
        }
    }
    
    public var roomInfoMap: [String: AUiRoomInfo] = [:]
    public var roomConfigMap: [String: AUiRoomConfig] = [:]
    
    public func isRoomOwner(channelName: String) ->Bool {
        return roomInfoMap[channelName]?.owner?.userId == currentUserInfo.userId
    }
    
    public func clean(channelName: String) {
//        roomConfig = nil
        roomInfoMap[channelName] = nil
    }
    
    override init() {
        super.init()
        switchTheme(themeName: "UIKit")
    }
    
    
    var themeIdx = 0
    
    
    public private(set) var themeResourcePaths: Set<URL> = Set()
    private var themeFolderPaths: Set<URL> = Set()
    
    public func addThemeFolderPath(path: URL) {
        themeFolderPaths.insert(path)
        resetTheme()
    }
    
    public func resetTheme() {
        CATransaction.begin()
        CATransaction.setDisableActions(true)
        switchTheme(themeName: themeNames[themeIdx])
        CATransaction.commit()
    }
    
    public func switchThemeToNext() {
        themeIdx = (themeIdx + 1) % themeNames.count
        resetTheme()
    }
    
    public func switchTheme(themeName: String) {
        guard let folderPath = Bundle.main.path(forResource: "auiTheme", ofType: "bundle") else {return}
        
        aui_info("switchTheme: \(themeName)", tag: "AUiKaraokeRoomView")
        let themeFolderPath = "\(folderPath)/\(themeName)/theme"
        
        let jsonDict = themeFolderPath.aui_theme()
        guard jsonDict.count > 0 else {
            aui_error("SwiftTheme WARNING: Can't read json '\(themeName)' at: \(themeFolderPath)", tag: "AUiKaraokeRoomView")
            return
        }
        
        themeResourcePaths.removeAll()
        themeFolderPaths.forEach { path in
            let themeFolderPath = "\(path.relativePath)/\(themeName)/theme"
            let resourceFolderPath = "\(path.relativePath)/\(themeName)/resource/"
            themeResourcePaths.insert(URL(fileURLWithPath: resourceFolderPath))
            let dic = themeFolderPath.aui_theme()
            dic.forEach { (key: Any, value: Any) in
                guard let key = key as? String else {return}
                jsonDict.setValue(value, forKey: key)
            }
        }
        
        let path = "\(folderPath)/\(themeName)/resource/"
        ThemeManager.setTheme(dict: jsonDict, path: .sandbox(URL(fileURLWithPath: path)))
    }
}

//
//  BundleExtension.swift
//  AgoraLyricsScore
//
//  Created by 朱继超 on 2023/5/15.
//

import Foundation

public enum AUIBundleType: Int {
    case voiceRoom
    case karaoke
    case live
}

public let VoiceResourceBundle = Bundle(path: Bundle.main.path(forResource: "VoiceChatRoomResource", ofType: "bundle") ?? "") ?? Bundle.main

public let KaraokeResourceBundle = Bundle(path: Bundle.main.path(forResource: "KaraokeResource", ofType: "bundle") ?? "") ?? Bundle.main

public let LiveResourceBundle = Bundle(path: Bundle.main.path(forResource: "LiveResource", ofType: "bundle") ?? "") ?? Bundle.main

public extension Bundle {
    class var voiceRoomBundle: Bundle { VoiceResourceBundle }
    class var karaokeRoomBundle: Bundle { KaraokeResourceBundle }
    class var liveRoomBundle: Bundle { LiveResourceBundle }
    
    class func bundle(for type: AUIBundleType) -> Bundle {
        switch type {
        case .voiceRoom:
            return voiceRoomBundle
        case .karaoke:
            return karaokeRoomBundle
        case .live:
            return liveRoomBundle
        }
    }
    
}

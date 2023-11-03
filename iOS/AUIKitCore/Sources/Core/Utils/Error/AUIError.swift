//
//  AUIError.swift
//  AUIKit
//
//  Created by wushengtao on 2023/3/29.
//

import Foundation

public enum AUICommonError {
    case unknown      //未知错误
    case rtcError(Int32)    //rtc错误
    case rtmError(Int32)    //rtm错误
    case rtmNotPresence   //rtm presence错误
    case httpError(Int, String)  //http错误
    case networkParseFail   //http响应解析错误
    case missmatchRoomConfig  //找不到对应房间token信息
    case micSeatNotIdle   //麦位不空闲
    case micSeatAlreadyEnter   //已经上麦过了
    case userNoEnterSeat   //观众未上麦
    case chooseSongAlreadyExist   //歌曲已经选择过了
    case chooseSongNotExist   //歌曲已经选择过了
    case noPermission
    case chooseSongIsFail
    
    public func toNSError() -> NSError {
        switch self {
        case .httpError(let error, let msg):
            if error == 10001 {
                return NSError(domain: "AUIKit Error", code: Int(error), userInfo: [ NSLocalizedDescriptionKey : "the room has been destroyed"])
            }
            return NSError(domain: "AUIKit Error", code: Int(error), userInfo: [ NSLocalizedDescriptionKey : msg])
        case .rtcError(let error):
            return NSError(domain: "AUIKit Error", code: Int(error), userInfo: [ NSLocalizedDescriptionKey : "rtc operation fail: \(error)"])
        case .rtmError(let error):
            return NSError(domain: "AUIKit Error", code: Int(error), userInfo: [ NSLocalizedDescriptionKey : "rtm error: \(error)"])
        case .rtmNotPresence:
            return NSError(domain: "AUIKit Error", code: -1, userInfo: [ NSLocalizedDescriptionKey : "rtm fail: not presence"])
        case .networkParseFail:
            return NSError(domain: "AUIKit Error", code: -1, userInfo: [ NSLocalizedDescriptionKey : "http parse fail"])
        case .missmatchRoomConfig:
            return NSError(domain: "AUIKit Error", code: -1, userInfo: [ NSLocalizedDescriptionKey : "room config missmatch"])
        case .micSeatNotIdle:
            return NSError(domain: "AUIKit Error", code: -1, userInfo: [ NSLocalizedDescriptionKey : "mic seat not idle"])
        case .micSeatAlreadyEnter:
            return NSError(domain: "AUIKit Error", code: -1, userInfo: [ NSLocalizedDescriptionKey : "user already enter seat"])
        case .userNoEnterSeat:
            return NSError(domain: "AUIKit Error", code: -1, userInfo: [ NSLocalizedDescriptionKey : "user not enter seat"])
        case .chooseSongAlreadyExist:
            return NSError(domain: "AUIKit Error", code: -1, userInfo: [ NSLocalizedDescriptionKey : "already choost song"])
        case .chooseSongNotExist:
            return NSError(domain: "AUIKit Error", code: -1, userInfo: [ NSLocalizedDescriptionKey : "song not exist"])
        case .noPermission:
            return NSError(domain: "AUIKit Error", code: -1, userInfo: [ NSLocalizedDescriptionKey : "no permission"])
        case .chooseSongIsFail:
            return NSError(domain: "AUIKit Error", code: -1, userInfo: [ NSLocalizedDescriptionKey : "choost song model fail"])
        default:
            return NSError(domain: "AUIKit Error", code: -1, userInfo: [ NSLocalizedDescriptionKey : "unknown error"])
        }
    }
}

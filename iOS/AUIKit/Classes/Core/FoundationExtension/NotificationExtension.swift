//
//  NotificationExtension.swift
//  AgoraLyricsScore
//
//  Created by 朱继超 on 2023/5/15.
//

import Foundation

public extension Notification {
    
    
    /// Description keyboardEndFrame
    var keyboardEndFrame: CGRect? {
        return (userInfo?[UIApplication.keyboardFrameEndUserInfoKey] as? NSValue)?.cgRectValue
    }

    
    /// Description keyboard animation duration
    var keyboardAnimationDuration: TimeInterval? {
        return (userInfo?[UIApplication.keyboardAnimationDurationUserInfoKey] as? NSNumber)?.doubleValue
    }
}

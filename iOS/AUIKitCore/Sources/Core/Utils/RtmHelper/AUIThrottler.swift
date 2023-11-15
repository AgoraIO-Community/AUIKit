//
//  AUIThrottler.swift
//  AUIKitCore
//
//  Created by wushengtao on 2023/11/14.
//

import Foundation

class AUIThrottler {
    private var workItem: DispatchWorkItem?
    
    func triggerLastEvent(after delay: TimeInterval, execute: @escaping () -> Void) {
        workItem?.cancel()
        
        let newWorkItem = DispatchWorkItem { execute() }
        workItem = newWorkItem
        
        DispatchQueue.main.asyncAfter(deadline: .now() + delay) {
            if newWorkItem.isCancelled == false {
                newWorkItem.perform()
            }
        }
    }
    
    func triggerNow() {
        if workItem?.isCancelled ?? false == false {
            workItem?.perform()
            workItem?.cancel()
        }
    }
}

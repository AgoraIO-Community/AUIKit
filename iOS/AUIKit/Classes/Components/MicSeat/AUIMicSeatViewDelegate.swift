//
//  AUIMicSeatViewDelegate.swift
//  AUIKit
//
//  Created by wushengtao on 2023/4/6.
//

import Foundation

public protocol AUIMicSeatViewDelegate: NSObjectProtocol {
    func seatItems(view: AUIMicSeatView) -> [AUIMicSeatCellDataProtocol]
    
    func onItemDidClick(view: AUIMicSeatView, seatIndex: Int)
    
    func onMuteVideo(view: AUIMicSeatView, seatIndex: Int, canvas: UIView, isMuteVideo: Bool)
}


//
//  AUIRoomGiftDialog.swift
//  AScenesKit
//
//  Created by 朱继超 on 2023/5/31.
//

import UIKit

@objc public protocol IAUIRoomGiftDialog: NSObjectProtocol {
    func fillTabs(tabs: [AUIGiftTabEntity])
    
    func addActionHandler(actionHandler: AUIRoomGiftDialogEventsDelegate)
    
    func removeEventHandler(actionHandler: AUIRoomGiftDialogEventsDelegate)

}

@objc public protocol AUIRoomGiftDialogEventsDelegate: NSObjectProtocol {
    
    /// Description 送礼action
    /// - Parameter gift: 礼物
    func sendGiftAction(gift: AUIGiftEntity)
}
/// Description 礼物列表的整体容器
public class AUIRoomGiftDialog: UIView,IAUIRoomGiftDialog {
    
    private var eventHandlers: NSHashTable<AnyObject> = NSHashTable<AnyObject>.weakObjects()
    
    public func addActionHandler(actionHandler: AUIRoomGiftDialogEventsDelegate) {
        if self.eventHandlers.contains(actionHandler) {
            return
        }
        self.eventHandlers.add(actionHandler)
    }

    public func removeEventHandler(actionHandler: AUIRoomGiftDialogEventsDelegate) {
        self.eventHandlers.remove(actionHandler)
    }
    
    private var containers = [AUITabsPageContainerCellDelegate]()
    
    public var tabs = [AUIGiftTabEntity]() {
        didSet {
            if self.tabs.count > 0 {
                self.subviews.forEach { $0.removeFromSuperview() }
                self.titles = tabs.map { $0.displayName ?? "" }
                DispatchQueue.main.async {
                    let containers = self.tabs.map({
                        AUIGiftListView(frame: CGRect(x: 0, y: 0, width: Int(self.frame.width), height: Int(self.frame.height)-44), gifts: $0.gifts ?? [],sentGift: { gift in
                            self.eventHandlers.allObjects.forEach { handler in
                                handler.sendGiftAction(gift: gift)
                            }
                        }).backgroundColor(.clear).tag(Int($0.tabId))
                    })
                    let container = AUITabsPageContainer(frame: CGRect(x: 0, y: 0, width: self.frame.width, height: self.frame.height), barStyle: AUITabsStyle(), containers: containers, titles: self.titles)
                    self.addSubViews([container])
                }
            }
        }
    }
    
    private var titles = [String]()


    override init(frame: CGRect) {
        super.init(frame: frame)
    }
    
    public convenience init(frame: CGRect, tabs: [AUIGiftTabEntity]) {
        self.init(frame: frame)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public func fillTabs(tabs: [AUIGiftTabEntity]) {
        self.tabs = tabs
    }
}

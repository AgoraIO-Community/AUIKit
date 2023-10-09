//
//  AUIInvitationView.swift
//  AUIKit
//
//  Created by wushengtao on 2023/3/3.
//

import SDWebImage
import UIKit
import SwiftTheme

@objc public protocol IAUIListViewBinderRefresh: NSObjectProtocol {
    func filter(userId: String)
    
    func refreshUsers(users: [AUIUserCellUserDataProtocol])
    
    func updateUser(user: AUIUserCellUserDataProtocol)
}

@objc public enum AUIUserOperationEventsSource: Int {
    case invite
    case apply
}

@objc public protocol AUIUserOperationEventsDelegate: NSObjectProtocol {
    func operationUser(user: AUIUserCellUserDataProtocol,source: AUIUserOperationEventsSource)

}

/// 邀请列表组件
@objc open class AUIInvitationView: UIView,IAUIListViewBinderRefresh {
    
    public var index: Int?
    
    private var eventHandlers: NSHashTable<AnyObject> = NSHashTable<AnyObject>.weakObjects()
    
    public func addActionHandler(actionHandler: AUIUserOperationEventsDelegate) {

        if self.eventHandlers.contains(actionHandler) {
            return
        }
        self.eventHandlers.add(actionHandler)
    }

    public func removeEventHandler(actionHandler: AUIUserOperationEventsDelegate) {

        self.eventHandlers.remove(actionHandler)
    }
    
    public lazy var tabs: AUITabs = {
        var tabStyle = AUITabsStyle()
        tabStyle.indicatorHeight = 4
        tabStyle.indicatorWidth = 28
        tabStyle.indicatorCornerRadius = 2
        tabStyle.indicatorStyle = .line
        tabStyle.indicatorColor = UIColor(0x009EFF)
        tabStyle.selectedTitleColor = UIColor(0x171a1c)
        tabStyle.normalTitleColor = UIColor(0xFFFFFF)
        tabStyle.titleFont = .systemFont(ofSize: 14, weight: .semibold)
        let tab = AUITabs(frame: CGRect(x: 0, y: 10, width: self.frame.width, height: 44), segmentStyle: tabStyle, titles: [aui_localized("Invite List")]).backgroundColor(.clear)
        tab.theme_selectedTitleColor = ThemeColorPicker(keyPath: "Alert.titleColor")
        tab.theme_normalTitleColor = ThemeColorPicker(keyPath: "CommonColor.primary")
        tab.theme_backgroundColor = AUIColor("Invitation.backgroundColor")
        return tab
    }()
    
    public lazy var tableView: UITableView = {
        let tableView = UITableView(frame: CGRect(x: 0, y: 54, width: self.frame.width, height: self.frame.height-54), style: .plain).backgroundColor(.clear).separatorStyle(.none)
        tableView.delegate = self
        tableView.dataSource = self
        tableView.rowHeight = 60
        tableView.backgroundColor = .clear
        return tableView
    }()
    
    private lazy var empty: AUIEmptyView = {
        AUIEmptyView(frame: CGRect(x: 0, y: 0, width: self.frame.width, height: self.frame.height),title: "", image: nil)
    }()
    
    public var userList: [AUIUserCellUserDataProtocol] = []
    
    public override init(frame: CGRect) {
        super.init(frame: frame)
        _loadSubViews()
    }
    
    public required init?(coder: NSCoder) {
        super.init(coder: coder)
        _loadSubViews()
    }
    
    private func _loadSubViews() {
        addSubview(empty)
        addSubview(tabs)
        addSubview(tableView)
        self.theme_backgroundColor = AUIColor("Invitation.backgroundColor")
    }
    
    public override func layoutSubviews() {
        super.layoutSubviews()
    }
    
    public func refreshUsers(users: [AUIUserCellUserDataProtocol]) {
        self.userList.removeAll()
        self.userList = users
        self.tableView.reloadData()
        if self.userList.count == 0 {
            self.addSubview(self.empty)
        } else {
            self.empty.removeFromSuperview()
        }
    }
    
    public func filter(userId: String) {
        userList = userList.filter({
            $0.userId != userId
        })
        self.tableView.reloadData()
        if self.userList.count == 0 {
            self.addSubview(self.empty)
        } else {
            self.empty.removeFromSuperview()
        }
    }
    
    public func updateUser(user: AUIUserCellUserDataProtocol) {
        let userInfo = self.userList.first {
            $0.userId == user.userId
        }
        userInfo?.userAvatar = user.userAvatar
        userInfo?.userName = user.userName
        self.tableView.reloadData()
    }
}



private let kAUIInvitationCellId = "AUIInvitationCell"
extension AUIInvitationView: UITableViewDelegate, UITableViewDataSource {
    public func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return self.userList.count
    }
    
    public func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        var cell = tableView.dequeueReusableCell(withIdentifier: kAUIInvitationCellId) as? AUIUserOperationCell
        if cell == nil {
            cell = AUIUserOperationCell(reuseIdentifier: kAUIInvitationCellId,config: AUIUserOperationCellConfig())
        }
        let user = userList[indexPath.row]
        cell?.refreshUser(user: user)
        cell?.userName.theme_textColor = "Invitation.cellTitleColor"
        cell?.userName.theme_font = "Invitation.bigTitleFont"
        cell?.actionClosure = { [weak self] in
            guard let user = $0 else { return }
            self?.eventHandlers.allObjects.forEach({ delegate in
                delegate.operationUser(user: user,source: .invite)
            })
        }
        return cell ?? AUIUserOperationCell()

    }
}


/// 申请列表组件
@objc open class AUIApplyView: UIView,IAUIListViewBinderRefresh {
        
    private var eventHandlers: NSHashTable<AnyObject> = NSHashTable<AnyObject>.weakObjects()
    
    public func addActionHandler(actionHandler: AUIUserOperationEventsDelegate) {
        if self.eventHandlers.contains(actionHandler) {
            return
        }
        self.eventHandlers.add(actionHandler)
    }

    public func removeEventHandler(actionHandler: AUIUserOperationEventsDelegate) {
        self.eventHandlers.remove(actionHandler)
    }
    
    public lazy var tabs: AUITabs = {
        var tabStyle = AUITabsStyle()
        tabStyle.indicatorHeight = 4
        tabStyle.indicatorWidth = 28
        tabStyle.indicatorCornerRadius = 2
        tabStyle.indicatorStyle = .line
        tabStyle.indicatorColor = UIColor(0x009EFF)
        tabStyle.selectedTitleColor = UIColor(0x171a1c)
        tabStyle.normalTitleColor = .white
        tabStyle.titleFont = .systemFont(ofSize: 16, weight: .semibold)
        
        let tab = AUITabs(frame: CGRect(x: 0, y: 10, width: self.frame.width, height: 44), segmentStyle: tabStyle, titles: [aui_localized("Application List")]).backgroundColor(.clear)
        tab.theme_selectedTitleColor = ThemeColorPicker(keyPath: "Alert.titleColor")
        tab.theme_normalTitleColor = ThemeColorPicker(keyPath: "Alert.titleColor")
        tab.theme_backgroundColor = AUIColor("Apply.backgroundColor")
        return tab
    }()
    
    public lazy var tableView: UITableView = {
        let tableView = UITableView(frame: CGRect(x: 0, y: 54, width: self.frame.width, height: self.frame.height-54), style: .plain).separatorStyle(.none)
        tableView.delegate = self
        tableView.dataSource = self
        tableView.rowHeight = 60
        tableView.backgroundColor = .clear
        return tableView
    }()
    
    private lazy var empty: AUIEmptyView = {
        AUIEmptyView(frame: CGRect(x: 0, y: 0, width: self.frame.width, height: self.frame.height),title: "", image: nil)
    }()
    
    
    public var userList: [AUIUserCellUserDataProtocol] = []
    
    public override init(frame: CGRect) {
        super.init(frame: frame)
        _loadSubViews()
    }
    
    public required init?(coder: NSCoder) {
        super.init(coder: coder)
        _loadSubViews()
    }
    
    private func _loadSubViews() {
        addSubview(empty)
        addSubview(tabs)
        addSubview(tableView)
        self.theme_backgroundColor = AUIColor("Apply.backgroundColor")
    }
    
    public override func layoutSubviews() {
        super.layoutSubviews()
    }
    
    public func refreshUsers(users: [AUIUserCellUserDataProtocol]) {
        self.userList.removeAll()
        self.userList = users
        self.tableView.reloadData()
        if self.userList.count == 0 {
            self.addSubview(self.empty)
        } else {
            self.empty.removeFromSuperview()
        }
    }
    
    public func filter(userId: String) {
        userList = userList.filter({
            $0.userId != userId
        })
        self.tableView.reloadData()
        if self.userList.count == 0 {
            self.addSubview(self.empty)
        } else {
            self.sendSubviewToBack(self.empty)
        }
    }
    
    public func updateUser(user: AUIUserCellUserDataProtocol) {
        let userInfo = self.userList.first {
            $0.userId == user.userId
        }
        userInfo?.userAvatar = user.userAvatar
        userInfo?.userName = user.userName
        self.tableView.reloadData()
    }
}



private let kAUIApplyCellId = "AUIApplyCell"
extension AUIApplyView: UITableViewDelegate, UITableViewDataSource {
    public func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return self.userList.count
    }
    
    public func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        var cell = tableView.dequeueReusableCell(withIdentifier: kAUIApplyCellId) as? AUIUserOperationCell
        if cell == nil {
            let config = AUIUserOperationCellConfig()
            config.actionTitle = aui_localized("Accept")
            cell = AUIUserOperationCell(reuseIdentifier: kAUIApplyCellId,config: config)
        }
        let user = userList[indexPath.row]
        cell?.refreshUser(user: user)
        cell?.userName.theme_textColor = "Apply.cellTitleColor"
        cell?.userName.theme_font = "Apply.bigTitleFont"
        cell?.actionClosure = { [weak self] in
            guard let user = $0 else { return }
            self?.eventHandlers.allObjects.forEach({ delegate in
                delegate.operationUser(user: user,source: .apply)
            })
        }
        return cell ?? AUIUserOperationCell()
    }
}



@objc public final class AUIUserOperationCell: UITableViewCell {
    
    public var actionClosure: ((AUIUserCellUserDataProtocol?) -> ())?
    
    private var user: AUIUserCellUserDataProtocol?
    
    lazy var config = AUIUserOperationCellConfig()
    
    lazy var userIcon: UIImageView = {
        UIImageView(frame: CGRect(x: 16, y: 10, width: 40, height: 40)).backgroundColor(.clear).cornerRadius(self.config.iconCornerRadius)
    }()
    
    lazy var userName: UILabel = {
        let label = UILabel(frame: CGRect(x: self.userIcon.frame.maxX+12, y: 19, width: self.contentView.frame.width - self.userIcon.frame.maxX - 12 - 98, height: 20))
        return label
    }()
    
    lazy var action: UIButton = {
        UIButton(type: .custom).frame(CGRect(x: self.contentView.frame.width - 80, y: (self.contentView.frame.height-28)/2.0, width: 80, height: 28)).setGradient(self.config.gradientColors, self.config.gradientLocations).title(self.config.actionTitle, .normal).textColor(self.config.textColor, .normal).font(self.config.textFont).addTargetFor(self, action: #selector(sendAction), for: .touchUpInside).cornerRadius(14)
    }()
    
    public override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
    }
    
    convenience init(reuseIdentifier: String?,config: AUIUserOperationCellConfig) {
        self.init(style: .default, reuseIdentifier: reuseIdentifier)
        self.config = config
        _loadSubViews()
    }
    
    public required init?(coder: NSCoder) {
        super.init(coder: coder)
        _loadSubViews()
    }
    
    private func _loadSubViews() {
        self.theme_backgroundColor = AUIColor("Invitation.backgroundColor")
        self.contentView.addSubViews([self.userIcon,self.userName,self.action])
    }
    
    public override func layoutSubviews() {
        super.layoutSubviews()
        self.action.frame = CGRect(x: self.contentView.frame.width - 90, y: (self.contentView.frame.height-28)/2.0, width: 80, height: 28)
        self.action.aui_centerY = self.contentView.aui_centerY
    }
    
    @objc private func sendAction() {
        self.actionClosure?(self.user)
    }
    
    public func refreshUser(user: AUIUserCellUserDataProtocol) {
        self.user = user
        self.userIcon.sd_setImage(with: URL(string: user.userAvatar), placeholderImage: UIImage("mine_avatar_placeHolder", .gift))
        self.userName.text = user.userName
    }

}

@objc public final class AUIUserOperationCellConfig: NSObject {
    
    public var iconCornerRadius: CGFloat = 20

    public var gradientColors: [UIColor] = [UIColor(red: 0, green: 0.62, blue: 1, alpha: 1),UIColor(red: 0.487, green: 0.358, blue: 1, alpha: 1)]
    
    public var gradientLocations: [CGPoint] = [ CGPoint(x: 0, y: 0.25),  CGPoint(x: 1, y: 0.75)]
    
    public var textFont: UIFont = .systemFont(ofSize: 14, weight: .medium)
    
    public var textColor: UIColor = UIColor(0x171A1C)
    
    public var actionTitle: String = aui_localized("Invite")
    
    var mode: AUIThemeMode = .light {
        willSet {
            switch newValue {
            case .light:
                self.gradientColors = [UIColor(red: 0, green: 0.62, blue: 1, alpha: 1),UIColor(red: 0.487, green: 0.358, blue: 1, alpha: 1)]
                self.textColor = UIColor(0x171A1C)
            case .dark:
                self.gradientColors = [UIColor(red: 0, green: 0.248, blue: 0.4, alpha: 1),
                                        UIColor(red: 0.104, green: 0, blue: 0.4, alpha: 0.2)]
                self.textColor = UIColor(0xACB4B9)
            }
        }
    }
}

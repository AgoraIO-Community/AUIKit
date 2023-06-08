//
//  AUIInvitationView.swift
//  AUIKit
//
//  Created by wushengtao on 2023/3/3.
//

import Kingfisher
import UIKit

@objc public enum AUIUserOperationEventsSource: Int {
    case invite
    case apply
}

@objc public protocol AUIUserOperationEventsDelegate: NSObjectProtocol {
    func operationUser(user: AUIUserCellUserDataProtocol,source: AUIUserOperationEventsSource)

}

/// 邀请列表组件
@objc open class AUIInvitationView: UIView {
    
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

    
    public lazy var tableView: UITableView = {
        let tableView = UITableView(frame: bounds, style: .plain)
        tableView.delegate = self
        tableView.dataSource = self
        tableView.estimatedRowHeight = 100
        tableView.backgroundColor = .clear
        return tableView
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
        addSubview(tableView)
        backgroundColor = .clear
    }
    
    public override func layoutSubviews() {
        super.layoutSubviews()
        tableView.frame = bounds
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
        cell?.actionClosure = { [weak self] in
            guard let user = $0 else { return }
            self?.eventHandlers.allObjects.forEach({ delegate in
                delegate.operationUser(user: user,source: .invite)
            })
        }
        return cell ?? AUIUserOperationCell()

    }
}


/// 邀请列表组件
@objc open class AUIApplyView: UIView {
    
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
    
    public lazy var tableView: UITableView = {
        let tableView = UITableView(frame: bounds, style: .plain)
        tableView.delegate = self
        tableView.dataSource = self
        tableView.estimatedRowHeight = 100
        tableView.backgroundColor = .clear
        return tableView
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
        addSubview(tableView)
        backgroundColor = .clear
    }
    
    public override func layoutSubviews() {
        super.layoutSubviews()
        tableView.frame = bounds
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
            cell = AUIUserOperationCell(reuseIdentifier: kAUIApplyCellId,config: AUIUserOperationCellConfig())
        }
        let user = userList[indexPath.row]
        cell?.refreshUser(user: user)
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
        UIImageView(frame: CGRect(x: 16, y: 10, width: 40, height: 40)).backgroundColor(.clear)
    }()
    
    lazy var userName: UILabel = {
        let label = UILabel(frame: CGRect(x: self.userIcon.frame.maxX+12, y: 19, width: self.contentView.frame.width - self.userIcon.frame.maxX - 12 - 98, height: 20))
        label.theme_textColor = "MemberUserCell.titleColor"
        label.theme_font = "MemberUserCell.bigTitleFont"
        return label
    }()
    
    lazy var action: UIButton = {
        UIButton(type: .custom).frame(CGRect(x: 0, y: self.contentView.frame.height-28, width: 76, height: 28)).setGradient(self.config.gradientColors, self.config.gradientLocations).title(self.config.actionTitle, .normal).textColor(self.config.textColor, .normal).font(self.config.textFont).addTargetFor(self, action: #selector(sendAction), for: .touchUpInside).cornerRadius(14)
    }()
    
    public override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
    }
    
    convenience init(reuseIdentifier: String?,config: AUIUserOperationCellConfig) {
        self.init(style: .default, reuseIdentifier: reuseIdentifier)
        _loadSubViews()
    }
    
    public required init?(coder: NSCoder) {
        super.init(coder: coder)
        _loadSubViews()
    }
    
    private func _loadSubViews() {
        self.backgroundColor = .clear
        self.contentView.addSubViews([self.userIcon,self.userName,self.action])
    }
    
    public override func layoutSubviews() {
        super.layoutSubviews()
    }
    
    @objc private func sendAction() {
        self.actionClosure?(self.user)
    }
    
    public func refreshUser(user: AUIUserCellUserDataProtocol) {
        self.user = user
        self.userIcon.kf.setImage(with: URL(string: user.userAvatar)!,placeholder: UIImage("mine_avatar_placeHolder", .gift))
        self.userName.text = user.userName
    }

}

@objc public final class AUIUserOperationCellConfig: NSObject {
    
    var mode: AUIThemeMode = .light {
        willSet {
            switch newValue {
            case .light:
                self.textColor = UIColor(0x171A1C)
            case .dark:
                self.textColor = UIColor(0xF9FAFA)
            }
        }
    }
    
    public var gradientColors: [UIColor] = [UIColor(red: 0, green: 0.62, blue: 1, alpha: 1),UIColor(red: 0.487, green: 0.358, blue: 1, alpha: 1)]
    
    public var gradientLocations: [CGPoint] = [CGPoint(x: 0, y: 0), CGPoint(x: 0, y: 1)]
    
    public var textFont: UIFont = .systemFont(ofSize: 14, weight: .medium)
    
    public var textColor: UIColor = UIColor(0x171A1C)
    
    public var actionTitle: String = "Invite".a.localize(type: .gift)
    
    
    public var actionTitle: String = "Invite".a.localize(type: .gift)
    
    
}

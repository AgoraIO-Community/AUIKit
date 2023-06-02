//
//  AUIInvitationView.swift
//  AUIKit
//
//  Created by wushengtao on 2023/3/3.
//

import Foundation


/// 邀请列表组件
@objc open class AUIInvitationView: UIView {
    public weak var invitationdelegate: AUIInvitationServiceDelegate? {
        didSet {
            oldValue?.unbindRespDelegate(delegate: self)
            invitationdelegate?.unbindRespDelegate(delegate: self)
        }
    }
    
    public weak var roomDelegate: AUIRoomManagerDelegate? {
        didSet {
            oldValue?.unbindRespDelegate(delegate: self)
            roomDelegate?.bindRespDelegate(delegate: self)
        }
    }
    
    private lazy var tableView: UITableView = {
        let tableView = UITableView(frame: bounds, style: .plain)
        tableView.delegate = self
        tableView.dataSource = self
        tableView.estimatedRowHeight = 100
        tableView.backgroundColor = .clear
        return tableView
    }()
    
    
    private var userList: [AUIUserInfo] = []
    
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

extension AUIInvitationView: AUIInvitationRespDelegate {
    public func onReceiveNewInvitation(userId: String, seatIndex: Int?) {
        
    }
    
    public func onInviteeAccepted(userId: String) {
        
    }
    
    public func onInviteeRejected(userId: String) {
        
    }
    
    public func onInvitationCancelled(userId: String) {
        
    }
    
    public func onReceiveNewApply(userId: String, seatIndex: Int?) {
        
    }
    
    public func onApplyAccepted(userId: String) {
        
    }
    
    public func onApplyRejected(userId: String) {
        
    }
    
    public func onApplyCanceled(userId: String) {
        
    }
    
    
}

extension AUIInvitationView: AUIRoomManagerRespDelegate {
    public func onRoomAnnouncementChange(roomId: String, announcement: String) {
        //TODO: - update room announcement
    }
    
    public func onRoomUserSnapshot(roomId: String, userList: [AUIUserInfo]) {
        self.userList = userList
        self.tableView.reloadData()
    }
    
    public func onRoomDestroy(roomId: String) {
        
    }
    
    public func onRoomInfoChange(roomId: String, roomInfo: AUIRoomInfo) {
        
    }
    
    public func onRoomUserEnter(roomId: String, userInfo: AUIUserInfo) {
        self.userList = self.userList.filter({$0.userId != userInfo.userId})
        self.userList.append(userInfo)
        self.tableView.reloadData()
    }
    
    public func onRoomUserLeave(roomId: String, userInfo: AUIUserInfo) {
        self.userList = self.userList.filter({$0.userId != userInfo.userId})
        self.tableView.reloadData()
    }
    
    public func onRoomUserUpdate(roomId: String, userInfo: AUIUserInfo) {
        self.userList = self.userList.filter({$0.userId != userInfo.userId})
        self.userList.append(userInfo)
        self.tableView.reloadData()
    }
}


private let kAUIInvitationCellId = "invitation_cell"
extension AUIInvitationView: UITableViewDelegate, UITableViewDataSource {
    public func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return self.userList.count
    }
    
    public func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: kAUIInvitationCellId) ?? UITableViewCell(style: .subtitle, reuseIdentifier: kAUIInvitationCellId)
        let user = userList[indexPath.row]
        cell.backgroundColor = .clear
        cell.textLabel?.text = "name: \(user.userName)"
        cell.detailTextLabel?.text = "id: \(user.userId)"
        return cell
    }
}

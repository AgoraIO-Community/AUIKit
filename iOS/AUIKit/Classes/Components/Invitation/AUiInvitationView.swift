//
//  AUIInvitationView.swift
//  AUIKit
//
//  Created by wushengtao on 2023/3/3.
//

import Foundation


/// 邀请列表组件
class AUIInvitationView: UIView {
    weak var invitationdelegate: AUIInvitationServiceDelegate? {
        didSet {
            oldValue?.unbindRespDelegate(delegate: self)
            invitationdelegate?.unbindRespDelegate(delegate: self)
        }
    }
    
    weak var roomDelegate: AUIRoomManagerDelegate? {
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
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        _loadSubViews()
    }
    
    required init?(coder: NSCoder) {
        super.init(coder: coder)
        _loadSubViews()
    }
    
    private func _loadSubViews() {
        addSubview(tableView)
        backgroundColor = .clear
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        tableView.frame = bounds
    }
}

extension AUIInvitationView: AUIInvitationRespDelegate {
    func onReceiveNewInvitation(userId: String, seatIndex: Int?) {
        
    }
    
    func onInviteeAccepted(userId: String) {
        
    }
    
    func onInviteeRejected(userId: String) {
        
    }
    
    func onInvitationCancelled(userId: String) {
        
    }
    
    func onReceiveNewApply(userId: String, seatIndex: Int?) {
        
    }
    
    func onApplyAccepted(userId: String) {
        
    }
    
    func onApplyRejected(userId: String) {
        
    }
    
    func onApplyCanceled(userId: String) {
        
    }
    
    
}

extension AUIInvitationView: AUIRoomManagerRespDelegate {
    func onRoomAnnouncementChange(roomId: String, announcement: String) {
        //TODO: - update room announcement
    }
    
    func onRoomUserSnapshot(roomId: String, userList: [AUIUserInfo]) {
        self.userList = userList
        self.tableView.reloadData()
    }
    
    func onRoomDestroy(roomId: String) {
        
    }
    
    func onRoomInfoChange(roomId: String, roomInfo: AUIRoomInfo) {
        
    }
    
    func onRoomUserEnter(roomId: String, userInfo: AUIUserInfo) {
        self.userList = self.userList.filter({$0.userId != userInfo.userId})
        self.userList.append(userInfo)
        self.tableView.reloadData()
    }
    
    func onRoomUserLeave(roomId: String, userInfo: AUIUserInfo) {
        self.userList = self.userList.filter({$0.userId != userInfo.userId})
        self.tableView.reloadData()
    }
    
    func onRoomUserUpdate(roomId: String, userInfo: AUIUserInfo) {
        self.userList = self.userList.filter({$0.userId != userInfo.userId})
        self.userList.append(userInfo)
        self.tableView.reloadData()
    }
}


private let kAUIInvitationCellId = "invitation_cell"
extension AUIInvitationView: UITableViewDelegate, UITableViewDataSource {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return self.userList.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: kAUIInvitationCellId) ?? UITableViewCell(style: .subtitle, reuseIdentifier: kAUIInvitationCellId)
        let user = userList[indexPath.row]
        cell.backgroundColor = .clear
        cell.textLabel?.text = "name: \(user.userName)"
        cell.detailTextLabel?.text = "id: \(user.userId)"
        return cell
    }
}

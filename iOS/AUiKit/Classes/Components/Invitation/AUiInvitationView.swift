//
//  AUiInvitationView.swift
//  AUiKit
//
//  Created by wushengtao on 2023/3/3.
//

import Foundation


/// 邀请列表组件
class AUiInvitationView: UIView {
    weak var invitationdelegate: AUiInvitationServiceDelegate? {
        didSet {
            oldValue?.unbindRespDelegate(delegate: self)
            invitationdelegate?.unbindRespDelegate(delegate: self)
        }
    }
    
    weak var roomDelegate: AUiRoomManagerDelegate? {
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
    
    
    private var userList: [AUiUserInfo] = []
    
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

extension AUiInvitationView: AUiInvitationRespDelegate {
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

extension AUiInvitationView: AUiRoomManagerRespDelegate {
    func onRoomUserSnapshot(roomId: String, userList: [AUiUserInfo]) {
        self.userList = userList
        self.tableView.reloadData()
    }
    
    func onRoomDestroy(roomId: String) {
        
    }
    
    func onRoomInfoChange(roomId: String, roomInfo: AUiRoomInfo) {
        
    }
    
    func onRoomUserEnter(roomId: String, userInfo: AUiUserInfo) {
        self.userList = self.userList.filter({$0.userId != userInfo.userId})
        self.userList.append(userInfo)
        self.tableView.reloadData()
    }
    
    func onRoomUserLeave(roomId: String, userInfo: AUiUserInfo) {
        self.userList = self.userList.filter({$0.userId != userInfo.userId})
        self.tableView.reloadData()
    }
    
    func onRoomUserUpdate(roomId: String, userInfo: AUiUserInfo) {
        self.userList = self.userList.filter({$0.userId != userInfo.userId})
        self.userList.append(userInfo)
        self.tableView.reloadData()
    }
}


private let kAUiInvitationCellId = "invitation_cell"
extension AUiInvitationView: UITableViewDelegate, UITableViewDataSource {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return self.userList.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: kAUiInvitationCellId) ?? UITableViewCell(style: .subtitle, reuseIdentifier: kAUiInvitationCellId)
        let user = userList[indexPath.row]
        cell.backgroundColor = .clear
        cell.textLabel?.text = "name: \(user.userName)"
        cell.detailTextLabel?.text = "id: \(user.userId)"
        return cell
    }
}

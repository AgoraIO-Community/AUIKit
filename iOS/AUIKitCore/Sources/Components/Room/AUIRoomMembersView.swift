//
//  AUIRoomMembersView.swift
//  AScenesKit
//
//  Created by 朱继超 on 2023/5/31.
//

import UIKit
import SwiftTheme

private let headImageWidth: CGFloat = 26

@objc public protocol IAUIRoomMembersView: NSObjectProtocol {
    func updateMembers(members: [AUIUserCellUserDataProtocol],channelName: String)
    func appendMember(member: AUIUserCellUserDataProtocol)
    func removeMember(member: AUIUserCellUserDataProtocol)
    func updateMember(member: AUIUserCellUserDataProtocol)
    func updateSeatInfo(member: AUIUserCellUserDataProtocol,seatIndex: Int)
}

@objc public protocol AUIUserCellUserDataProtocol: NSObjectProtocol {
    var userAvatar: String {set get}
    var userId: String {set get}
    var userName: String {set get}
    var seatIndex: Int {set get}
    var isOwner: Bool {set get}

}

public typealias AUIRoomMembersViewMoreBtnAction = (_ members: [AUIUserCellUserDataProtocol])->()

//用户头像展示
public class AUIRoomMembersView: UIView {
    public var onClickMoreButtonAction: AUIRoomMembersViewMoreBtnAction?
    
    public var members: [AUIUserCellUserDataProtocol] = [] {
        didSet {
            let imgs = members.map({$0.userAvatar})
            updateWithMemberImgs(imgs)
        }
    }
    
    public var seatMap: [String: Int] = [:]
    
    public var roomId: String?
    
    private lazy var moreButton: AUIButton = {
        let theme = AUIButtonDynamicTheme()
        theme.icon = ThemeAnyPicker(keyPath: "Room.membersMoreIcon")
        theme.iconWidth = "Room.membersMoreIconWidth"
        theme.iconHeight = "Room.membersMoreIconHeight"
        theme.buttonWidth = "Room.membersMoreWidth"
        theme.buttonHeight = "Room.membersMoreHeight"
        theme.backgroundColor = "Room.membersMoreBgColor"
        theme.cornerRadius = "Room.membersMoreCornerRadius"
        
        let button = AUIButton()
        button.style = theme
        button.addTarget(self, action: #selector(clickMoreButtonAction), for: .touchUpInside)
        return button
    }()
    
    private lazy var leftImgView: UIImageView = {
        let imgview = UIImageView()
        imgview.layer.cornerRadius = headImageWidth * 0.5
        imgview.layer.masksToBounds = true
        imgview.contentMode = .scaleAspectFill
        imgview.isHidden = true
        return imgview
    }()
    
    private lazy var rightImgView: UIImageView = {
        let imgview = UIImageView()
        imgview.layer.cornerRadius = headImageWidth * 0.5
        imgview.layer.masksToBounds = true
        imgview.contentMode = .scaleAspectFill
        imgview.isHidden = true
        return imgview
    }()
    
    private lazy var countLabel: UILabel = {
        let label = UILabel()
        label.numberOfLines = 1
        label.textColor = .white
        label.font = UIFont.systemFont(ofSize: 14)
        label.textAlignment = .center
        label.text = ""
        return label
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        _createSubviews()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func _createSubviews(){
        addSubview(moreButton)
        addSubview(rightImgView)
        addSubview(leftImgView)
 
        let views = [leftImgView,rightImgView,moreButton]
        for (i, view) in views.enumerated() {
            view.frame = CGRect(x: CGFloat(i) * (headImageWidth + 6) , y: 0, width: headImageWidth, height: headImageWidth)
        }
        self.bounds = CGRect(x: 0, y: 0, width: CGFloat(views.count) * headImageWidth + CGFloat(views.count - 1) * 8, height: headImageWidth)
        
        rightImgView.addSubview(countLabel)
        countLabel.frame = rightImgView.bounds
        
    }
    
    public func updateWithMemberImgs(_ imgs: [String]) {
        if imgs.count < 1 {
            aui_error("err = empty member", tag: "AUIRoomMembersView")
            return
        }
        
        for (i, imgView) in [rightImgView, leftImgView].enumerated() {
            imgView.isHidden = false
            if imgs.count > i {
                imgView.sd_setImage(with: URL(string: imgs[i]), placeholderImage: UIImage.aui_Image(named: "aui_micseat_dialog_avatar_idle"), context: nil)
            }else{
                imgView.isHidden = true
            }
        }
        if imgs.count > 2 {
            countLabel.text = "\(imgs.count)"
        }
    }
    
//    public func requestData(roomId: String){
////        guard let roomId = roomId else { return }
//        userServiceDelegate?.getUserInfoList(roomId: roomId, userIdList: [], callback: { err, userList in
//            if let userList = userList {
//                self.members = userList
//            }
//        })
//        /*
//        for _ in 0...5 {
//            let user = AUIUserInfo()
//            user.userName = "user"
//            user.userAvatar = "https://img1.baidu.com/it/u=413643897,2296924942&fm=253&app=138&size=w931&n=0&f=JPEG&fmt=auto?sec=1680627600&t=e945c37a601f9ee7ca3be932c73e605a"
//            members.append(user)
//        }
//         */
//    }
}

extension AUIRoomMembersView:IAUIRoomMembersView {
    
    public func updateMembers(members: [AUIUserCellUserDataProtocol],channelName: String) {
        members.forEach {
            if $0.userId == AUIRoomContext.shared.roomInfoMap[channelName]?.owner?.userId ?? "" {
                $0.isOwner = true
            }
        }
        self.members = members
    }
    
    public func appendMember(member: AUIUserCellUserDataProtocol) {
        members.append(member)
    }
    
    public func removeMember(member: AUIUserCellUserDataProtocol) {
        self.members.removeAll(where: {$0.userId == member.userId})
    }
    
    public func updateMember(member: AUIUserCellUserDataProtocol) {
        if let index = members.firstIndex(where: {$0.userId == member.userId}) {
            self.members[index] = member
        } else {
            self.members.append(member)
        }
    }
    
    public func updateSeatInfo(member: AUIUserCellUserDataProtocol, seatIndex: Int) {
        seatMap[member.userId] = seatIndex
        members.first(where: {
            $0.userId == member.userId
        })?.seatIndex = seatIndex
        var users = members.map {$0}
        self.members = users
    }
    
    
    @objc public func clickMoreButtonAction() {
        self.onClickMoreButtonAction?(self.members)
    }
}



//
//  AUIRoomMembersView.swift
//  AScenesKit
//
//  Created by 朱继超 on 2023/5/31.
//

import UIKit
import SwiftTheme

@objc public protocol IAUIRoomMembersView: NSObjectProtocol {
    func updateMembers(members: [AUIUserCellUserDataProtocol],channelName: String)
    func appendMember(member: AUIUserCellUserDataProtocol)
    func updateMember(member: AUIUserCellUserDataProtocol)
    func removeMember(userId: String)
    func updateSeatInfo(userId: String,seatIndex: Int)
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
    
    private lazy var imageViews: [UIImageView] = []
    
    private lazy var countLabel: UILabel = {
        let label = UILabel()
        label.numberOfLines = 1
        label.textColor = .white
        label.backgroundColor = .black.withAlphaComponent(0.5)
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
    
    private func _createImageView() -> UIImageView {
        let imgview = UIImageView()
        imgview.layer.cornerRadius = moreButton.aui_height * 0.5
        imgview.layer.masksToBounds = true
        imgview.contentMode = .scaleAspectFill
        imgview.isHidden = true
        return imgview
    }
    
    private func _createSubviews(){
        addSubview(moreButton)
 
        for _ in 0...2 {
            let view = _createImageView()
            addSubview(view)
            imageViews.append(view)
        }
        let views = imageViews + [moreButton]
        var right = 0.0
        let padding = 2.0
        views.forEach { view in
            view.frame = CGRect(x: right, y: 0, width: moreButton.aui_height, height: moreButton.aui_height)
            right += moreButton.aui_height + padding
        }
        self.bounds = CGRect(x: 0, y: 0, width: right, height: moreButton.aui_height)
        
        if let rightImgView = imageViews.last {
            rightImgView.addSubview(countLabel)
            countLabel.frame = rightImgView.bounds
        }
    }
    
    public func updateWithMemberImgs(_ imgs: [String]) {
        if imgs.count < 1 {
            aui_error("err = empty member", tag: "AUIRoomMembersView")
            return
        }
        
        let startIdx = max(imageViews.count - imgs.count, 0)
        let placeholder = UIImage.aui_Image(named: "aui_micseat_dialog_avatar_idle")
        for (i, imgView) in imageViews.enumerated() {
            imgView.isHidden = false
            if i >= startIdx {
                imgView.sd_setImage(with: URL(string: imgs[i - startIdx]),
                                    placeholderImage: placeholder)
            } else {
                imgView.isHidden = true
            }
        }
        if imgs.count > 3 {
            countLabel.text = "\(imgs.count - 2)"
            countLabel.isHidden = false
        } else {
            countLabel.isHidden = true
        }
    }
}

extension AUIRoomMembersView:IAUIRoomMembersView {
    
    public func updateMembers(members: [AUIUserCellUserDataProtocol],channelName: String) {
        self.members = members
    }
    
    public func appendMember(member: AUIUserCellUserDataProtocol) {
        members.append(member)
    }
    
    public func removeMember(userId: String) {
        self.members.removeAll(where: {$0.userId == userId})
    }
    
    public func updateMember(member: AUIUserCellUserDataProtocol) {
        if let index = members.firstIndex(where: {$0.userId == member.userId}) {
            self.members[index] = member
        } else {
            self.members.append(member)
        }
    }
    
    public func updateSeatInfo(userId: String, seatIndex: Int) {
        members.first(where: {
            $0.userId == userId
        })?.seatIndex = seatIndex
        let users = members.map {$0}
        self.members = users
    }
    
    @objc public func clickMoreButtonAction() {
        self.onClickMoreButtonAction?(self.members)
    }
}



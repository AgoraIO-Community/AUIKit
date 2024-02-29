//
//  AUIRoomInfoView.swift
//  AScenesKit
//
//  Created by 朱继超 on 2023/5/31.
//

import UIKit
import SDWebImage
import SwiftTheme


/// 房间信息展示
public final class AUIRoomInfoView: UIView {
    
    private var headImageView: UIImageView = {
        let imgview = UIImageView()
        imgview.layer.masksToBounds = true
        imgview.contentMode = .scaleAspectFill
        return imgview
    }()
    
    private var roomNameLabel: UILabel = {
        let label = UILabel()
        label.numberOfLines = 1
        label.theme_textColor = AUIColor("Room.roomInfoTitleColor")
        label.theme_font = "CommonFont.big"
        label.text =  aui_localized("roomInfoRoomName")
        return label
    }()
    
    private var roomIdLabel: UILabel = {
        let label = UILabel()
        label.numberOfLines = 1
        label.theme_textColor = AUIColor("Room.roomInfoSubTitleColor")
        label.theme_font = "CommonFont.small"
        label.text = aui_localized("roomInfoRoomID")
        return label
    }()
    
    private lazy var extensionButton: UIButton = {
        UIButton(type: .custom).frame(.zero).backgroundColor(.clear)
    }()
    
    private var show = false
    
    override init(frame: CGRect) {
        super.init(frame: frame)
    }
    
    @objc public convenience init(frame: CGRect,showExtension: Bool = false) {
        self.init(frame: frame)
        self.show = showExtension
        _createSubviews()
        self.extensionButton.isHidden = !showExtension
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func _createSubviews(){
        self.theme_backgroundColor = AUIColor("Room.infoBackgroundColor")
        layer.masksToBounds = true
        addSubview(headImageView)
        addSubview(roomNameLabel)
        addSubview(roomIdLabel)
        addSubview(self.extensionButton)
        headImageView.translatesAutoresizingMaskIntoConstraints = false
        roomNameLabel.translatesAutoresizingMaskIntoConstraints = false
        roomIdLabel.translatesAutoresizingMaskIntoConstraints = false
        let avatarHeight = self.frame.height-4
        NSLayoutConstraint.activate([
            headImageView.topAnchor.constraint(equalTo: self.topAnchor,constant: 2),
            headImageView.leftAnchor.constraint(equalTo: self.leftAnchor, constant: 2),
            headImageView.widthAnchor.constraint(equalToConstant: avatarHeight),
            headImageView.heightAnchor.constraint(equalToConstant: avatarHeight)
        ])
        headImageView.cornerRadius(avatarHeight/2.0)
        var width = (44/185.0)*self.frame.width
        if !self.show {
           width = 0
        }
        NSLayoutConstraint.activate([
            roomNameLabel.topAnchor.constraint(equalTo: headImageView.topAnchor),
            roomNameLabel.leftAnchor.constraint(equalTo: headImageView.rightAnchor, constant: 8),
            roomNameLabel.rightAnchor.constraint(equalTo: self.rightAnchor, constant: -10-width)
        ])
        
        NSLayoutConstraint.activate([
            roomIdLabel.bottomAnchor.constraint(equalTo: headImageView.bottomAnchor),
            roomIdLabel.leftAnchor.constraint(equalTo: roomNameLabel.leftAnchor),
            roomIdLabel.rightAnchor.constraint(equalTo: self.rightAnchor, constant: -10-width)
        ])
        self.extensionButton.setImage(UIImage.aui_Image(named: "person_add_fill"), for: .normal)
    }
    
    public override func layoutSubviews() {
        super.layoutSubviews()
        layer.cornerRadius = bounds.height * 0.5
        let width = (44/185.0)*self.frame.width
        self.extensionButton.frame(CGRect(x: self.frame.width-width-2, y: 2, width: width, height: self.frame.height-4)).cornerRadius((self.frame.height-4)/2.0).setGradient([UIColor(0x009EFF),UIColor(0x7C5BFF)], [CGPoint(x: 0, y: 0),CGPoint(x: 0, y: 1)])
    }
}


extension AUIRoomInfoView {
    
    public func updateRoomInfo(withRoomId roomId:String, roomName: String?, ownerHeadImg:String?){
        roomNameLabel.text = (roomName ?? "")
        roomIdLabel.text = aui_localized("roomInfoRoomID") + roomId
        headImageView.sd_setImage(with: URL(string: ownerHeadImg ?? ""), placeholderImage: UIImage.aui_Image(named: "mine_avatar_placeHolder"))
    }
}


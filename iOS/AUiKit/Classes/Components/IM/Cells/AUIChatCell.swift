//
//  AUIChatCell.swift
//  AgoraLyricsScore
//
//  Created by 朱继超 on 2023/5/15.
//

import UIKit

public class AUIChatCell: UITableViewCell {

    public lazy var container: UIImageView = {
        UIImageView(frame: CGRect(x: 15, y: 6, width: self.contentView.frame.width - 30, height: self.frame.height - 6)).backgroundColor(UIColor(red: 0, green: 0, blue: 0, alpha: 0.2)).layerProperties(UIColor(red: 0.978, green: 0.98, blue: 0.982, alpha: 0.6), 1).cornerRadius(12)
    }()

    public lazy var content: UILabel = {
        UILabel(frame: CGRect(x: 10, y: 7, width: self.container.frame.width - 20, height: self.container.frame.height - 18)).backgroundColor(.clear).numberOfLines(0).lineBreakMode(.byWordWrapping)
    }()

    override public init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        self.backgroundColor = .clear
        self.contentView.backgroundColor = .clear
        self.contentView.addSubview(self.container)
        self.container.addSubview(self.content)
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    func refresh(chat: AUIChatEntity) {
        self.container.frame = CGRect(x: 15, y: 6, width: chat.width! + 30, height: chat.height! - 6)
        self.content.attributedText = chat.attributeContent
        self.content.preferredMaxLayoutWidth =  self.container.frame.width - 24
        self.content.frame = CGRect(x: 12, y: 7, width:  self.container.frame.width - 24, height:  self.container.frame.height - 16)
        
    }
}


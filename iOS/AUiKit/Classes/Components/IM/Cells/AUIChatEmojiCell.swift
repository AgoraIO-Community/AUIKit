//
//  AUIChatEmojiCell.swift
//  AgoraLyricsScore
//
//  Created by 朱继超 on 2023/5/15.
//

import UIKit

public class AUIChatEmojiCell: UICollectionViewCell {
    lazy var icon: UIImageView = .init(frame: CGRect(x: 7, y: 7, width: self.contentView.frame.width - 14, height: self.contentView.frame.height - 14)).contentMode(.scaleAspectFit).backgroundColor(.white)

    override public init(frame: CGRect) {
        super.init(frame: frame)
        contentView.backgroundColor = .white
        contentView.addSubview(icon)
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override public func layoutSubviews() {
        super.layoutSubviews()
        icon.frame = CGRect(x: 7, y: 7, width: contentView.frame.width - 14, height: contentView.frame.height - 14)
    }
}


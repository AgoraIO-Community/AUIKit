//
//  AUIChatBarFunctionCell.swift
//  AgoraLyricsScore
//
//  Created by 朱继超 on 2023/5/15.
//

import UIKit

public class AUIChatBarFunctionCell: UICollectionViewCell {
    lazy var container: UIImageView = {
        UIImageView(frame: CGRect(x: 0, y: 0, width: self.contentView.frame.width, height: self.contentView.frame.height)).contentMode(.scaleAspectFit).backgroundColor(UIColor(red: 0, green: 0, blue: 0, alpha: 0.2)).cornerRadius(self.contentView.frame.height / 2.0)
    }()

    lazy var icon: UIImageView = {
        UIImageView(frame: CGRect(x: 0, y: 0, width: self.contentView.frame.width, height: self.contentView.frame.height)).contentMode(.scaleAspectFill).backgroundColor(.clear)
    }()

    let redDot = UIView().backgroundColor(.red).cornerRadius(3)

    override public init(frame: CGRect) {
        super.init(frame: frame)
        self.contentView.backgroundColor = .clear
        self.contentView.addSubViews([self.container,self.redDot,self.icon])
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override public func layoutSubviews() {
        super.layoutSubviews()
        let r = contentView.frame.width / 2.0
        let length = CGFloat(ceilf(Float(r) / sqrt(2)))
        self.redDot.frame = CGRect(x: frame.width / 2.0 + length, y: contentView.frame.height / 2.0 - length, width: 6, height: 6)
        self.icon.frame = CGRect(x: 7, y: 7, width: contentView.frame.width - 14, height: contentView.frame.height - 14)
    }
}

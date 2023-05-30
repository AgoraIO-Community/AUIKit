//
//  AUIReceiveGiftCell.swift
//  AUiKit
//
//  Created by 朱继超 on 2023/5/17.
//

import UIKit
import Kingfisher

public class AUIReceiveGiftCell: UITableViewCell {
    
    var gift: AUIGiftEntity?

    lazy var container: UIView = {
        UIView(frame: CGRect(x: 0, y: 5, width: self.contentView.frame.width, height: self.contentView.frame.height - 5)).backgroundColor(.clear)
    }()

    lazy var avatar: UIImageView = UIImageView(frame: CGRect(x: 5, y: 5, width: self.frame.width / 5.0, height: self.frame.width / 5.0)).contentMode(.scaleAspectFit)

    lazy var userName: UILabel = {
        UILabel(frame: CGRect(x: self.avatar.frame.maxX + 6, y: 8, width: self.frame.width / 5.0 * 2 - 12, height: 15)).font(.systemFont(ofSize: 12, weight: .semibold)).textColor(.white)
    }()

    lazy var giftName: UILabel = {
        UILabel(frame: CGRect(x: self.avatar.frame.maxX + 6, y: self.userName.frame.maxY, width: self.frame.width / 5.0 * 2 - 12, height: 15)).font(.systemFont(ofSize: 12, weight: .regular)).textColor(.white)
    }()

    lazy var giftIcon: UIImageView = {
        UIImageView(frame: CGRect(x: self.frame.width / 5.0 * 3, y: 0, width: self.frame.width / 5.0, height: self.contentView.frame.height)).contentMode(.scaleAspectFit)
    }()

    lazy var giftNumbers: UILabel = {
        UILabel(frame: CGRect(x: self.frame.width / 5.0 * 4 + 8, y: 10, width: self.frame.width / 5.0 - 16, height: self.frame.height - 20)).font(UIFont(name: "HelveticaNeue-BoldItalic", size: 18)).textColor(.white)
    }()

    override public init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
    }
    
    convenience init(reuseIdentifier: String?,config: AUIReceiveGiftCellConfig) {
        self.init(style: .default, reuseIdentifier: reuseIdentifier)
        contentView.backgroundColor = .clear
        backgroundColor = .clear
        contentView.addSubview(container)
        container.addSubViews([avatar, userName, giftName, giftIcon, giftNumbers])
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override public func layoutSubviews() {
        super.layoutSubviews()
        container.frame = CGRect(x: 0, y: 5, width: contentView.frame.width, height: contentView.frame.height - 5)
        container.cornerRadius(22).setGradient([UIColor(red: 0.05, green: 0, blue: 0.76, alpha: 0.24), UIColor(red: 0.71, green: 0.37, blue: 1, alpha: 0.64)], [CGPoint(x: 0, y: 0), CGPoint(x: 0, y: 1)])
        avatar.frame = CGRect(x: 5, y: 5, width: contentView.frame.height - 15, height: contentView.frame.height - 15)
        avatar.cornerRadius((contentView.frame.height - 15) / 2.0)
        userName.frame = CGRect(x: avatar.frame.maxX + 6, y: 5, width: frame.width / 5.0 * 2 - 12, height: 15)
        giftName.frame = CGRect(x: avatar.frame.maxX + 6, y: userName.frame.maxY + 2, width: frame.width / 5.0 * 2 - 12, height: 15)
        giftIcon.frame = CGRect(x: frame.width / 5.0 * 3, y: 0, width: container.frame.height, height: container.frame.height)
        giftNumbers.frame = CGRect(x: giftIcon.frame.maxX + 5, y: 5, width: container.frame.width - giftIcon.frame.maxX - 5, height: container.frame.height - 5)
    }

    func refresh(item: AUIGiftEntity) {
        if self.gift == nil {
            self.gift = item
        }
        self.avatar.kf.setImage(with: URL(string: item.sendUser?.userAvatar ?? ""), placeholder:UIImage("mine_avatar_placeHolder",.gift))
        self.userName.text = item.sendUser?.userName ?? ""
        self.giftName.text = "Sent ".a.localize(type: .gift) + (item.giftName ?? "")
        self.giftIcon.kf.setImage(with: URL(string: item.giftIcon ?? ""),placeholder: UIImage("\(item.giftId ?? "")",.gift))
        self.giftNumbers.text = "X \(item.giftCount ?? "1")"
    }
}


public class AUIReceiveGiftCellConfig: NSObject {
    public var containerCornerRadius: CGFloat = 22
    
    public var containerGradientColors: [UIColor] = [UIColor(red: 0.05, green: 0, blue: 0.76, alpha: 0.24), UIColor(red: 0.71, green: 0.37, blue: 1, alpha: 0.64)]
    
    public var containerGradientLocations: [CGPoint] = [CGPoint(x: 0, y: 0), CGPoint(x: 0, y: 1)]
    
    public var userNameFont: UIFont = UIFont.systemFont(ofSize: 12, weight: .semibold)
    
    public var userNameColor: UIColor = .white
    
    public var giftNameFont: UIFont = UIFont.systemFont(ofSize: 12, weight: .regular)
    
    public var giftNameColor: UIColor = .white
    
    public var giftNumbersFont: UIFont = UIFont(name: "HelveticaNeue-BoldItalic", size: 18) ?? UIFont.systemFont(ofSize: 18, weight: .bold)
    
    public var giftNumbersColor: UIColor = .white
    
    
}

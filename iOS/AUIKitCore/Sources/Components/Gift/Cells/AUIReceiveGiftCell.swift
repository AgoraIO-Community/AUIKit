//
//  AUIReceiveGiftCell.swift
//  AUiKit
//
//  Created by 朱继超 on 2023/5/17.
//

import UIKit
import SDWebImage

/*!
 *  \~Chinese
 *  收礼物的cell
 *
 *  \~English
 *  Cell receive gift
 */
@objcMembers public class AUIReceiveGiftCell: UITableViewCell {
    /*!
     *  \~Chinese
     *  收礼物的实体模型
     *
     *  \~English
     *  Mock-up for receiving presents
     */
    var gift: AUIGiftEntity?
    /*!
     *  \~Chinese
     *  收礼物整体的容器包含所有子视图
     *
     *  \~English
     *  The overall container contains all subviews
     */
    lazy var container: UIToolbar = {
        UIToolbar(frame: CGRect(x: 0, y: 5, width: self.contentView.frame.width, height: self.contentView.frame.height - 10)).backgroundColor(.clear).isUserInteractionEnabled(false)
    }()
    /*!
     *  \~Chinese
     *  用户头像
     *
     *  \~English
     *  User avatar
     */
    lazy var avatar: UIImageView = UIImageView(frame: CGRect(x: 5, y: 5, width: self.frame.width / 5.0, height: self.frame.width / 5.0)).contentMode(.scaleAspectFit)
    /*!
     *  \~Chinese
     *  用户名称
     *
     *  \~English
     *  User nickname
     */
    lazy var userName: UILabel = {
        UILabel(frame: CGRect(x: self.avatar.frame.maxX + 6, y: 8, width: self.frame.width / 5.0 * 2 - 12, height: 15)).theme_font(font: "ReceiveGift.userNameFont").theme_textColor(color: "ReceiveGift.userNameColor")
    }()
    /*!
     *  \~Chinese
     *  礼物名称
     *
     *  \~English
     *  Gift name
     */
    lazy var giftName: UILabel = {
        UILabel(frame: CGRect(x: self.avatar.frame.maxX + 6, y: self.userName.frame.maxY, width: self.frame.width / 5.0 * 2 - 12, height: 15)).theme_font(font: "ReceiveGift.giftNameFont").theme_textColor(color: "ReceiveGift.giftNameColor")
    }()
    /*!
     *  \~Chinese
     *  礼物图标
     *
     *  \~English
     *  Gift icon
     */
    lazy var giftIcon: UIImageView = {
        UIImageView(frame: CGRect(x: self.frame.width / 5.0 * 3, y: 0, width: self.frame.width / 5.0, height: self.contentView.frame.height)).contentMode(.scaleAspectFit)
    }()
    /*!
     *  \~Chinese
     *  礼物数目
     *
     *  \~English
     *  Gift count
     */
    lazy var giftNumbers: UILabel = {
        UILabel(frame: CGRect(x: self.frame.width / 5.0 * 4 + 8, y: 10, width: self.frame.width / 5.0 - 16, height: self.frame.height - 20)).theme_font(font: "ReceiveGift.giftNumbersFont").theme_textColor(color: "ReceiveGift.giftNumbersColor")
    }()

    override public init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
    }
    
    @objc public convenience init(reuseIdentifier: String?,config: AUIReceiveGiftCellConfig) {
        self.init(style: .default, reuseIdentifier: reuseIdentifier)
        contentView.isUserInteractionEnabled = false
        contentView.backgroundColor = .clear
        self.backgroundColor = .clear
        contentView.addSubview(self.container)
        self.container.addSubViews([self.avatar, self.userName, self.giftName, self.giftIcon, self.giftNumbers])
        self.container.barStyle = .default
        self.container.isTranslucent = false
        self.container.isOpaque = false
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override public func layoutSubviews() {
        super.layoutSubviews()
        self.container.frame = CGRect(x: 0, y: 5, width: contentView.frame.width, height: contentView.frame.height - 10)
        self.container.createThemeGradient("ReceiveGift.containerGradientColors", [CGPoint(x: 0, y: 0), CGPoint(x: 0, y: 1)])
        self.container.cornerRadius(self.container.frame.height/2.0)
        self.avatar.frame = CGRect(x: 5, y: 5, width: self.container.frame.height - 10, height: self.container.frame.height - 10)
        self.avatar.cornerRadius((self.container.frame.height - 10) / 2.0)
        self.userName.frame = CGRect(x: self.avatar.frame.maxX + 6, y: self.container.height/2.0 - 15, width: frame.width / 5.0 * 2 - 12, height: 15)
        self.giftName.frame = CGRect(x: self.avatar.frame.maxX + 6, y: self.container.height/2.0 , width: frame.width / 5.0 * 2 - 12, height: 15)
        self.giftIcon.frame = CGRect(x: frame.width / 5.0 * 3, y: 0, width: container.frame.height, height: self.container.frame.height)
        self.giftNumbers.frame = CGRect(x: self.giftIcon.frame.maxX + 5, y: 5, width: self.container.frame.width - self.giftIcon.frame.maxX - 5, height: self.container.frame.height - 5)
    }
    
    public override func hitTest(_ point: CGPoint, with event: UIEvent?) -> UIView? {
        super.hitTest(point, with: event)
    }

    @objc public func refresh(item: AUIGiftEntity) {
        if self.gift == nil {
            self.gift = item
        }
        self.avatar.sd_setImage(with: URL(string: item.sendUser.userAvatar), placeholderImage: UIImage.aui_Image(named: "mine_avatar_placeHolder"))
        self.userName.text = item.sendUser.userName
        self.giftName.text = "Sent ".a.localize(type: .gift) + (item.giftName)
        self.giftIcon.sd_setImage(with: URL(string: item.giftIcon), placeholderImage: UIImage.aui_Image(named: "\(item.giftId)"))
        self.giftNumbers.text = "X \(item.giftCount)"
    }
}


public class AUIReceiveGiftCellConfig: NSObject {
        
    public var containerCornerRadius: CGFloat = 22
    
    public var containerGradientColors: [UIColor] = [UIColor(red: 0.004, green: 0.122, blue: 0.678, alpha: 0.25),UIColor(red: 0.341, green: 0.004, blue: 0.678, alpha: 0.35)]
    
    public var containerGradientLocations: [CGPoint] = [CGPoint(x: 0, y: 0), CGPoint(x: 0, y: 1)]
    
    public var userNameFont: UIFont = UIFont.systemFont(ofSize: 12, weight: .semibold)
    
    public var userNameColor: UIColor = .white
    
    public var giftNameFont: UIFont = UIFont.systemFont(ofSize: 12, weight: .regular)
    
    public var giftNameColor: UIColor = .white
    
    public var giftNumbersFont: UIFont = UIFont(name: "HelveticaNeue-BoldItalic", size: 18) ?? UIFont.systemFont(ofSize: 18, weight: .bold)
    
    public var giftNumbersColor: UIColor = .white
    
    public var mode: AUIThemeMode = .light {
        willSet {
            switch newValue {
            case .light:
                self.containerGradientColors = [UIColor(red: 0.004, green: 0.122, blue: 0.678, alpha: 0.25),UIColor(red: 0.341, green: 0.004, blue: 0.678, alpha: 0.35)]
            case .dark:
                self.containerGradientColors = [UIColor(red: 0.004, green: 0.122, blue: 0.678, alpha: 0.25),UIColor(red: 0.341, green: 0.004, blue: 0.678, alpha: 0.35)]
            }
        }
    }
}

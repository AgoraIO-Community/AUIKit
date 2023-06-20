//
//  AUISendGiftCell.swift
//  AUiKit
//
//  Created by 朱继超 on 2023/5/17.
//

import UIKit
import Kingfisher

public class AUISendGiftCell: UICollectionViewCell {
    
    private var gift: AUIGiftEntity?
    
    public var sendCallback: ((AUIGiftEntity?)->Void)?
    
    public var config = AUISendGiftCellConfig() {
        didSet {
            self.cover.layerProperties(self.config.coverLayerColor, self.config.coverLayerWidth).setGradient(self.config.coverGradientColors, self.config.coverGradientPoints)
            self.send.setGradient(self.config.sendGradientColors, self.config.sendGradientPoints).textColor(self.config.sendTextColor, .normal).font(self.config.sendFont)
            self.name.font(self.config.nameFont).textColor(self.config.nameTextColor)
            self.displayValue.font(self.config.priceFont).textColor(self.config.priceTextColor, .normal)
        }
    }

    lazy var cover: UIView = {
        UIView(frame: CGRect(x: 0, y: 5, width: self.contentView.frame.width, height: self.contentView.frame.height - 5)).cornerThemeRadius("SendGift.coverCornerRadius").layerThemeProperties("SendGift.coverLayerColor", "SendGift.coverLayerWidth").setThemeGradient("SendGift.coverGradientColors", self.config.coverGradientPoints).theme_backgroundColor(color: "SendGift.backgroundColor")
    }()
    
    lazy var send: UIButton = {
        UIButton(type: .custom).frame(CGRect(x: 0, y: self.cover.frame.height-28, width: self.cover.frame.width, height: 28)).setThemeGradient("SendGift.sendGradientColors", self.config.sendGradientPoints).title("Send".a.localize(type: .gift), .normal).themeTitleColor("SendGift.sendTextColor", forState: .normal).theme_font("SendGift.sendFont").addTargetFor(self, action: #selector(sendAction), for: .touchUpInside)
    }()

    lazy var icon: UIImageView = {
        UIImageView(frame: CGRect(x: self.contentView.frame.width / 2.0 - 24, y: 16.5, width: 48, height: 48)).contentMode(.scaleAspectFit).theme_backgroundColor(color: "SendGift.backgroundColor")
    }()

    lazy var name: UILabel = {
        UILabel(frame: CGRect(x: 0, y: self.icon.frame.maxY + 4, width: self.contentView.frame.width, height: 18)).textAlignment(.center).theme_font(font: "SendGift.nameFont").theme_textColor(color: "SendGift.nameTextColor").theme_backgroundColor(color: "SendGift.backgroundColor")
    }()

    lazy var displayValue: UIButton = {
        UIButton(type: .custom).frame(CGRect(x: 0, y: self.name.frame.maxY + 1, width: self.contentView.frame.width, height: 15)).theme_font("SendGift.priceFont").themeTitleColor("SendGift.priceTextColor", forState: .normal).isUserInteractionEnabled(false).backgroundColor(.clear)
    }()

    override public init(frame: CGRect) {
        super.init(frame: frame)
        self.contentView.backgroundColor = .clear
        self.contentView.addSubViews([self.cover, self.icon, self.name, self.displayValue])
        self.cover.addSubview(self.send)
        self.displayValue.imageEdgeInsets(UIEdgeInsets(top: self.displayValue.imageEdgeInsets.top, left: -10, bottom: self.displayValue.imageEdgeInsets.bottom, right: self.displayValue.imageEdgeInsets.right))
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    func refresh(item: AUIGiftEntity?) {
        self.gift = item
        self.contentView.isHidden = (item == nil)

        let url = self.icon.ossPictureCrop(url: item?.giftIcon ?? "")
        self.icon.kf.setImage(with: URL(string: url)!,placeholder: UIImage(item?.giftName ?? "",.gift))

        self.name.text = item?.giftName
        self.displayValue.setImage(self.config.priceIcon, for: .normal)
        self.displayValue.setTitle(item?.giftPrice ?? "100", for: .normal)
        self.cover.isHidden = !(item?.selected ?? false)
        self.displayValue.frame = CGRect(x: 0, y: item!.selected ? self.icon.frame.maxY + 4:self.name.frame.maxY + 1, width: self.contentView.frame.width, height: 15)
        self.name.isHidden = item?.selected ?? false
        self.cover.frame = CGRect(x: 0, y: 5, width: self.contentView.frame.width, height: self.contentView.frame.height - 5)
        self.icon.frame = CGRect(x: self.contentView.frame.width / 2.0 - 24, y: 16.5, width: 48, height: 48)
    }
    
    @objc private func sendAction() {
        if self.sendCallback != nil,self.gift?.selected ?? false == true {
            self.sendCallback!(self.gift)
        }
    }

}

public class AUISendGiftCellConfig: NSObject {
    
    public var coverLayerColor: UIColor = UIColor(0x009EFF)
    
    public var coverLayerWidth: CGFloat = 1
    
    public var coverCornerRadius: CGFloat = 12
    
    public var coverGradientColors: [UIColor] = [UIColor(red: 0.8, green: 0.924, blue: 1, alpha: 1),UIColor(red: 0.888, green: 0.8, blue: 1, alpha: 0)]
    
    public var coverGradientPoints: [CGPoint] = [CGPoint(x: 0, y: 0), CGPoint(x: 0, y: 1)]
    
    public var sendGradientColors: [UIColor] = [UIColor(red: 0, green: 0.62, blue: 1, alpha: 1),UIColor(red: 0.487, green: 0.358, blue: 1, alpha: 1)]
    
    public var sendGradientPoints: [CGPoint] = [CGPoint(x: 0, y: 0), CGPoint(x: 0, y: 1)]
    
    public var sendFont: UIFont = .systemFont(ofSize: 14, weight: .medium)
    
    public var sendTextColor: UIColor = UIColor(0xF9FAFA)
    
    public var nameFont: UIFont = .systemFont(ofSize: 12, weight: .regular)
    
    public var nameTextColor: UIColor = UIColor(0x040925)
    
    public var priceFont: UIFont = .systemFont(ofSize: 12, weight: .regular)
    
    public var priceTextColor: UIColor = UIColor(red: 0.425, green: 0.445, blue: 0.573, alpha: 0.5)
    
    public var priceIcon: UIImage? = UIImage("dollagora",.gift)
    
}

extension UIImageView {
    func ossPictureCrop(url: String) -> String {
        var text = url
        if text.contains("?") {
            text += "x-oss-process=image/resize,w_\(Int(UIScreen.main.scale*self.width)),h_\(Int(UIScreen.main.scale*self.height))"
        } else {
            text += "?x-oss-process=image/resize,w_\(Int(UIScreen.main.scale*self.width)),h_\(Int(UIScreen.main.scale*self.height))"
        }
        return text
    }
}

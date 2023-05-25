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
    

    lazy var cover: UIView = {
        UIView(frame: CGRect(x: 0, y: 5, width: self.contentView.frame.width, height: self.contentView.frame.height - 5)).cornerRadius(12).layerProperties(UIColor(0x009EFF), 1).setGradient( [UIColor(red: 0.8, green: 0.924, blue: 1, alpha: 1),UIColor(red: 0.888, green: 0.8, blue: 1, alpha: 0)], [CGPoint(x: 0, y: 0), CGPoint(x: 0, y: 1)]).backgroundColor(.clear)
    }()
    
    lazy var send: UIButton = {
        UIButton(type: .custom).frame(CGRect(x: 0, y: self.cover.frame.height-28, width: self.cover.frame.width, height: 28)).setGradient([UIColor(red: 0, green: 0.62, blue: 1, alpha: 1),UIColor(red: 0.487, green: 0.358, blue: 1, alpha: 1)], [CGPoint(x: 0, y: 0), CGPoint(x: 0, y: 1)]).title("Send".a.localize(type: .gift), .normal).textColor(UIColor(0xF9FAFA), .normal).font(.systemFont(ofSize: 14, weight: .medium))
    }()

    lazy var icon: UIImageView = {
        UIImageView(frame: CGRect(x: self.contentView.frame.width / 2.0 - 24, y: 16.5, width: 48, height: 48)).contentMode(.scaleAspectFit).backgroundColor(.clear)
    }()

    lazy var name: UILabel = {
        UILabel(frame: CGRect(x: 0, y: self.icon.frame.maxY + 4, width: self.contentView.frame.width, height: 18)).textAlignment(.center).font(.systemFont(ofSize: 12, weight: .regular)).textColor(UIColor(0x040925)).backgroundColor(.clear)
    }()

    lazy var displayValue: UIButton = {
        UIButton(type: .custom).frame(CGRect(x: 0, y: self.name.frame.maxY + 1, width: self.contentView.frame.width, height: 15)).font(.systemFont(ofSize: 12, weight: .regular)).textColor(UIColor(red: 0.425, green: 0.445, blue: 0.573, alpha: 0.5), .normal).isUserInteractionEnabled(false).backgroundColor(.clear)
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
        self.icon.image = UIImage(item?.gift_id ?? "",.gift)
        self.name.text = item?.gift_name
        self.displayValue.setImage(UIImage("dollagora",.gift), for: .normal)
        self.displayValue.setTitle(item?.gift_price ?? "100", for: .normal)
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

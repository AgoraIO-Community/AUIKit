//
//  AUIGiftsView.swift
//  AUiKit
//
//  Created by 朱继超 on 2023/5/17.
//

import UIKit

public class AUIGiftsView: UIView, UICollectionViewDelegate, UICollectionViewDataSource {
    var gifts = [AUIGiftEntity]() {
        willSet {
            current = gifts.last
        }
    }

    public var sendClosure: ((AUIGiftEntity) -> Void)?

    var lastPoint = CGPoint.zero

    lazy var header: UIView = .init(frame: CGRect(x: 0, y: 0, width: AScreenWidth, height: 60)).backgroundColor(.white)

    lazy var flowLayout: UICollectionViewFlowLayout = {
        let layout = UICollectionViewFlowLayout()
        layout.itemSize = CGSize(width: (AScreenWidth - 30) / 4.0, height: (110 / 84.0) * (AScreenWidth - 30) / 4.0)
        layout.minimumLineSpacing = 0
        layout.minimumInteritemSpacing = 0
        layout.scrollDirection = .vertical
        return layout
    }()

    lazy var giftList: UICollectionView = {
        UICollectionView(frame: CGRect(x: 15, y: self.header.frame.maxY, width: AScreenWidth - 30, height: (110 / 84.0) * ((AScreenWidth - 30) / 4.0)), collectionViewLayout: self.flowLayout).registerCell(AUISendGiftCell.self, forCellReuseIdentifier: "AUISendGiftCell").delegate(self).dataSource(self).showsHorizontalScrollIndicator(false).backgroundColor(.white)
    }()

    lazy var contribution: UILabel = {
        UILabel(frame: CGRect(x: 20, y: self.giftList.frame.maxY + 50, width: AScreenWidth / 2.0 - 40, height: 20)).font(.systemFont(ofSize: 12, weight: .regular)).textColor(UIColor(0x6C7192))
    }()

    lazy var lineLayer: UIView = {
        UIView(frame: CGRect(x: AScreenWidth - 172, y: self.giftList.frame.maxY + 38.5, width: 155, height: 40)).cornerRadius(20).layerProperties(UIColor(0xB4D6FF), 1)
    }()

    lazy var chooseQuantity: UIButton = {
        UIButton(type: .custom).frame(CGRect(x: 0, y: 0, width: 76, height: 40)).font(.systemFont(ofSize: 14, weight: .semibold)).textColor(.black, .normal).title("1", .normal).backgroundColor(.white).addTargetFor(self, action: #selector(chooseCount), for: .touchUpInside)

    }()
    lazy var send: UIButton = {
        UIButton(type: .custom).frame(CGRect(x: self.chooseQuantity.frame.maxX, y: 0, width: 79, height: 40)).font(.systemFont(ofSize: 14, weight: .semibold)).setGradient([UIColor(0x219BFF), UIColor(0x345DFF)], [CGPoint(x: 0, y: 0), CGPoint(x: 0, y: 1)]).textColor(.white, .normal).title("Send".a.localize(type: .gift), .normal).addTargetFor(self, action: #selector(sendAction), for: .touchUpInside)
    }()

    lazy var title: UILabel = {
        UILabel(frame: CGRect(x: AScreenWidth / 2.0 - 30, y: 25.5, width: 60, height: 20)).textAlignment(.center).textColor(UIColor(0x040925)).font(.systemFont(ofSize: 16, weight: .semibold)).text("Gifts".a.localize(type: .gift))
    }()

    lazy var disableView: UIView = {
        UIView(frame: CGRect(x: AScreenWidth / 2.0, y: self.lineLayer.frame.minY, width: AScreenWidth / 2.0, height: 40)).backgroundColor(UIColor(white: 1, alpha: 0.7))
    }()

    var gift_count = "1" {
        willSet {
            DispatchQueue.main.async {
                self.chooseQuantity.setTitle(newValue, for: .normal)
            }
        }
    }

    var current: AUIGiftEntity?

    override public init(frame: CGRect) {
        super.init(frame: frame)
    }

    public convenience init(frame: CGRect, gifts: [AUIGiftEntity]) {
        self.init(frame: frame)
        self.gifts = gifts
        addSubViews([header, giftList, contribution, lineLayer, disableView])
        disableView.isHidden = true
        bringSubviewToFront(disableView)
        chooseQuantity.setImage(UIImage("arrow_down",.gift), for: .normal)
        chooseQuantity.setImage(UIImage("arrow_up",.gift), for: .selected)
        chooseQuantity.imageEdgeInsets = UIEdgeInsets(top: 5, left: 55, bottom: 5, right: 10)
        chooseQuantity.titleEdgeInsets = UIEdgeInsets(top: 5, left: 5, bottom: 5, right: 30)
        lineLayer.addSubViews([chooseQuantity, send])
        giftList.isPagingEnabled = true
        giftList.alwaysBounceHorizontal = true
        header.addSubview(title)
        
        current = self.gifts.first
        contribution.text = "Contribution Total".a.localize(type: .gift) + ": " + "1"
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

public extension AUIGiftsView {
    @objc internal func chooseCount() {
        chooseQuantity.isSelected = !chooseQuantity.isSelected
        
    }

    @objc internal func sendAction() {
        disableView.isHidden = false
        if sendClosure != nil, current != nil {
            current?.gift_count = gift_count
            chooseQuantity.setTitle("1", for: .normal)
            sendClosure!(current!.mutableCopy() as! AUIGiftEntity)
        }
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
            self.disableView.isHidden = true
            self.contribution.text = "Contribution Total".a.localize(type: .gift) + ": " + "\(self.current?.gift_price ?? "1")"
        }
    }    

    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        gifts.count
    }

    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "AUISendGiftCell", for: indexPath) as? AUISendGiftCell
        cell?.refresh(item: gifts[safe: indexPath.row])
        return cell ?? AUISendGiftCell()
    }

    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        collectionView.deselectItem(at: indexPath, animated: true)
        gifts.forEach { $0.selected = false }
        let gift = gifts[safe: indexPath.row]
        gift?.selected = true
        current = gift
        if let value = gift?.gift_price {
            if Int(value)! >= 100 {
                gift_count = "1"
                chooseQuantity.setTitle(gift_count, for: .normal)
                chooseQuantity.setTitleColor(.lightGray, for: .normal)
                gift?.gift_count = "1"
                chooseQuantity.isEnabled = false
            } else {
                gift?.gift_count = gift_count
                chooseQuantity.isEnabled = true
                chooseQuantity.setTitleColor(.darkText, for: .normal)
            }
        }
        let total = Int(gift_count)! * Int(gift!.gift_price ?? "1")!
        contribution.text = "Contribution Total".a.localize(type: .gift) + ": " + "\(total)"
        giftList.reloadData()
    }
}


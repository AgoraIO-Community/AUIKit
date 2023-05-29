//
//  AUIGiftContainer.swift
//  AUIKit
//
//  Created by 朱继超 on 2023/5/19.
//

import UIKit
import YYModel

//let giftMap = [["gift_id": "AUIKitGift1", "gift_name": "Sweet Heart", "gift_price": "1", "gift_count": "1", "selected": true], ["gift_id": "AUIKitGift2", "gift_name": "Flower", "gift_price": "5", "gift_count": "1", "selected": false], ["gift_id": "AUIKitGift3", "gift_name": "Crystal Box", "gift_price": "10", "gift_count": "1", "selected": false], ["gift_id": "AUIKitGift4", "gift_name": "Super Agora", "gift_price": "20", "gift_count": "1", "selected": false], ["gift_id": "AUIKitGift5", "gift_name": "Star", "gift_price": "50", "gift_count": "1", "selected": false], ["gift_id": "AUIKitGift6", "gift_name": "Lollipop", "gift_price": "100", "gift_count": "1", "selected": false], ["gift_id": "AUIKitGift7", "gift_name": "Diamond", "gift_price": "500", "gift_count": "1", "selected": false], ["gift_id": "AUIKitGift8", "gift_name": "Crown", "gift_price": "1000", "gift_count": "1", "selected": false], ["gift_id": "AUIKitGift9", "gift_name": "Rocket", "gift_price": "1500", "gift_count": "1", "selected": false]]
//
//
//public class AUIGiftContainer: UIView {
//    
//    lazy var gifts: AUIGiftsView = {
//        AUIGiftsView(frame: CGRect(x: 0, y: 0, width: AScreenWidth, height: AScreenWidth*(3.0/3.9)), gifts: self.giftList()).backgroundColor(.white)
//    }()
//    
//    lazy var giftsContainer: AUITabsPageContainer = {
//        AUITabsPageContainer(frame: CGRect(x: 0, y: 0, width: AScreenWidth, height: 316), barStyle: AUITabsStyle(), containers: [self.gifts,self.gifts], titles: ["Gifts","Gifts"])
//    }()
//    
//    lazy var contribution: UILabel = {
//        UILabel(frame: CGRect(x: 20, y: self.giftList.frame.maxY + 50, width: AScreenWidth / 2.0 - 40, height: 20)).font(.systemFont(ofSize: 12, weight: .regular)).textColor(UIColor(0x6C7192))
//    }()
//
//    lazy var lineLayer: UIView = {
//        UIView(frame: CGRect(x: AScreenWidth - 172, y: self.giftList.frame.maxY + 38.5, width: 155, height: 40)).cornerRadius(20).layerProperties(UIColor(0xB4D6FF), 1)
//    }()
//
//    lazy var chooseQuantity: UIButton = {
//        UIButton(type: .custom).frame(CGRect(x: 0, y: 0, width: 76, height: 40)).font(.systemFont(ofSize: 14, weight: .semibold)).textColor(.black, .normal).title("1", .normal).backgroundColor(.white).addTargetFor(self, action: #selector(chooseCount), for: .touchUpInside)
//
//    }()
//    lazy var send: UIButton = {
//        UIButton(type: .custom).frame(CGRect(x: self.chooseQuantity.frame.maxX, y: 0, width: 79, height: 40)).font(.systemFont(ofSize: 14, weight: .semibold)).setGradient([UIColor(0x219BFF), UIColor(0x345DFF)], [CGPoint(x: 0, y: 0), CGPoint(x: 0, y: 1)]).textColor(.white, .normal).title("Send".a.localize(type: .gift), .normal).addTargetFor(self, action: #selector(sendAction), for: .touchUpInside)
//    }()
//
//    lazy var disableView: UIView = {
//        UIView(frame: CGRect(x: AScreenWidth / 2.0, y: self.lineLayer.frame.minY, width: AScreenWidth / 2.0, height: 40)).backgroundColor(UIColor(white: 1, alpha: 0.7))
//    }()
//    
//    var gift_count = "1" {
//        willSet {
//            DispatchQueue.main.async {
//                self.chooseQuantity.setTitle(newValue, for: .normal)
//            }
//        }
//    }
//
//    public override init(frame: CGRect) {
//        super.init(frame: frame)
//        self.disableView.isHidden = true
//        self.bringSubviewToFront(self.disableView)
//        self.chooseQuantity.setImage(UIImage("arrow_down",.gift), for: .normal)
//        self.chooseQuantity.setImage(UIImage("arrow_up",.gift), for: .selected)
//        self.chooseQuantity.imageEdgeInsets = UIEdgeInsets(top: 5, left: 55, bottom: 5, right: 10)
//        self.chooseQuantity.titleEdgeInsets = UIEdgeInsets(top: 5, left: 5, bottom: 5, right: 30)
//        self.lineLayer.addSubViews([self.chooseQuantity, self.send])
//        self.gifts.giftList.isPagingEnabled = true
//        self.gifts.giftList.alwaysBounceHorizontal = false
//        
//        self.current = self.gifts.gifts.first
//        self.contribution.text = "Contribution Total".a.localize(type: .gift) + ": " + "1"
//    }
//    
//    required init?(coder: NSCoder) {
//        fatalError("init(coder:) has not been implemented")
//    }
//    
//}
//
//extension AUIGiftContainer {
//    
//    func giftList() -> [AUIGiftEntity] {
//        var gifts = [AUIGiftEntity]()
//        for dic in giftMap {
//            guard let entity = AUIGiftEntity.yy_model(with: dic) else { continue }
//            gifts.append(entity)
//        }
//        return gifts
//    }
//    
//    @objc internal func chooseCount() {
//        chooseQuantity.isSelected = !chooseQuantity.isSelected
//        
//    }
//
//    @objc internal func sendAction() {
//        disableView.isHidden = false
//        if sendClosure != nil, current != nil {
//            current?.gift_count = gift_count
//            chooseQuantity.setTitle("1", for: .normal)
//            sendClosure!(current!.mutableCopy() as! AUIGiftEntity)
//        }
//        DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
//            self.disableView.isHidden = true
//            self.contribution.text = "Contribution Total".a.localize(type: .gift) + ": " + "\(self.current?.gift_price ?? "1")"
//        }
//    }
//}

//
//  AUIGiftsView.swift
//  AUiKit
//
//  Created by 朱继超 on 2023/5/17.
//

import UIKit
/*!
 *  \~Chinese
 *  礼物列表
 *
 *  \~English
 *  Gifts list.
 *
 */
public class AUIGiftListView: UIView, UICollectionViewDelegate, UICollectionViewDataSource,AUITabsPageContainerCellDelegate {
    
    public func viewIdentity() -> String {
        self.a.swiftClassName ?? "AUIKit.AUIGiftsView"
    }
    
    public func create(frame: CGRect, datas: [NSObject]) -> UIView? {
        guard let dataSource = datas as? [AUIGiftEntity] else { return AUIGiftListView() }
        return AUIGiftListView(frame: frame, gifts: dataSource,sentGift: self.sendClosure!)
    }
    
    public func rawFrame() -> CGRect {
        self.frame
    }
    
    public func rawDatas() -> [NSObject] {
        self.gifts
    }
        
    var gifts = [AUIGiftEntity]()

    private var sendClosure: ((AUIGiftEntity) -> Void)?

    var lastPoint = CGPoint.zero


    lazy var flowLayout: UICollectionViewFlowLayout = {
        let layout = UICollectionViewFlowLayout()
        layout.itemSize = CGSize(width: (AScreenWidth - 30) / 4.0, height: (110 / 84.0) * (AScreenWidth - 30) / 4.0)
        layout.minimumLineSpacing = 0
        layout.minimumInteritemSpacing = 0
        layout.footerReferenceSize = CGSize(width: self.frame.width, height: 120)
        return layout
    }()

    lazy var giftList: UICollectionView = {
        UICollectionView(frame: CGRect(x: 15, y: 10, width: AScreenWidth - 30, height: self.frame.height), collectionViewLayout: self.flowLayout).registerCell(AUISendGiftCell.self, forCellReuseIdentifier: "AUISendGiftCell").delegate(self).dataSource(self).showsHorizontalScrollIndicator(false).backgroundColor(.clear).showsVerticalScrollIndicator(false).backgroundColor(.clear).registerView(UICollectionReusableView.self, UICollectionView.elementKindSectionFooter , "AUIGiftsFooter")
    }()

    override public init(frame: CGRect) {
        super.init(frame: frame)
    }

    /*!
     *  \~Chinese
     *  初始化方法
     *
     *  @param gifts     礼物模型数组
     *  @param sentGift  发送礼物的回调
     *
     *  \~English
     *  Initialize method
     *
     *  @param gifts       Gifts entity array.
     *  @param sentGift    Send action callback.
     */
    public convenience init(frame: CGRect, gifts: [AUIGiftEntity], sentGift: @escaping ((AUIGiftEntity) -> Void)) {
        self.init(frame: frame)
        self.sendClosure = sentGift
        self.gifts = gifts
        self.giftList.bounces = false
        self.addSubViews([self.giftList])
        self.giftList.alwaysBounceHorizontal = true
        self.backgroundColor = .clear
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

public extension AUIGiftListView {

    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        gifts.count
    }

    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "AUISendGiftCell", for: indexPath) as? AUISendGiftCell
        cell?.refresh(item: self.gifts[safe: indexPath.row])
        cell?.sendCallback = { [weak self] in
            guard let entity = $0 else { return }
            if self?.sendClosure != nil {
                self?.sendClosure!(entity)
            }
        }
        return cell ?? AUISendGiftCell()
    }
    
    func collectionView(_ collectionView: UICollectionView, viewForSupplementaryElementOfKind kind: String, at indexPath: IndexPath) -> UICollectionReusableView {
        if kind == UICollectionView.elementKindSectionFooter {
            let reusableView = collectionView.dequeueReusableSupplementaryView(ofKind: kind, withReuseIdentifier: "AUIGiftsFooter", for: indexPath)
            reusableView.backgroundColor = .clear
            reusableView.frame = CGRect(origin: reusableView.frame.origin, size: CGSize(width: self.frame.width, height: 120))
            return reusableView
        }
        return UICollectionReusableView()
    }

    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        collectionView.deselectItem(at: indexPath, animated: true)
        gifts.forEach { $0.selected = false }
        let gift = gifts[safe: indexPath.row]
        gift?.selected = true
        
        giftList.reloadData()
    }
}


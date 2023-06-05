//
//  AUIMicSeatView.swift
//  AUIKit
//
//  Created by wushengtao on 2023/2/23.
//

import Foundation

private let kMicSeatCellId = "kMicSeatCellId"

@objc public enum AUIMicSeatViewLayoutType: Int {
    case one
    case six
    case eight
    case nine
}


/// 麦位管理组件
public class AUIMicSeatView: UIView {
    
    public weak var uiDelegate: AUIMicSeatViewDelegate?
    
    private lazy var collectionLayout: UICollectionViewLayout = {
        let flowLayout = UICollectionViewFlowLayout()
        let width: CGFloat = 56//min(bounds.size.width / 4.0, bounds.size.height / 2)
        let height: CGFloat = 106
        let hPadding = Int((bounds.width - width * 4) / 3)
        flowLayout.itemSize = CGSize(width: width, height: height)
        flowLayout.minimumLineSpacing = 0
        flowLayout.minimumInteritemSpacing = CGFloat(hPadding)
        return flowLayout
    }()
    
    public lazy var collectionView: UICollectionView = {
        let collectionView = UICollectionView(frame: bounds, collectionViewLayout: self.collectionLayout)
        collectionView.register(AUIMicSeatItemCell.self, forCellWithReuseIdentifier: kMicSeatCellId)
        collectionView.delegate = self
        collectionView.dataSource = self
        collectionView.backgroundColor = .clear
        
        return collectionView
    }()
    
    public override init(frame: CGRect) {
        super.init(frame: frame)
    }
    
    @objc public convenience init(frame: CGRect, style: AUIMicSeatViewLayoutType) {
        self.init(frame: frame)
        self.settingLayout(type: style)
        _loadSubViews()
    }
    
    @objc public convenience init(frame: CGRect, layout: UICollectionViewLayout) {
        self.init(frame: frame)
        self.collectionLayout = layout
        _loadSubViews()
    }
    
    required init?(coder: NSCoder) {
        super.init(coder: coder)
        _loadSubViews()
    }
    
    private func _loadSubViews() {
        addSubview(collectionView)
        self.backgroundColor = .clear
    }
    
    public override func layoutSubviews() {
        super.layoutSubviews()
        collectionView.frame = bounds
    }
    
    private func settingLayout(type: AUIMicSeatViewLayoutType) {
        switch type {
        case .one,.six:
            let layout = AUIMicSeatCircleLayout()
            layout.dataSource = self
            let width: CGFloat = 56//min(bounds.size.width / 4.0, bounds.size.height / 2)
            let height: CGFloat = 92
            layout.itemSize = CGSize(width: width, height: height)
            layout.minimumLineSpacing = 0
            layout.minimumInteritemSpacing = 0
            self.collectionLayout = layout
        case .nine:
            let layout = AUIMicSeatHostAudienceLayout()
            layout.dataSource = self
            self.collectionLayout = layout
        default:
            break
        }
    }
    
}

extension AUIMicSeatView: UICollectionViewDelegate, UICollectionViewDataSource,AUIMicSeatCircleLayoutDataSource,AUIMicSeatHostAudienceLayoutDataSource {
    
    public var radius: CGFloat {
        return min(self.frame.width, self.frame.height)/2.8
    }
    
    public func rowSpace() -> CGFloat {
        10
    }
    
    public func hostSize() -> CGSize {
        CGSize(width: 102, height: 120)
    }
    
    public func otherSize() -> CGSize {
        CGSize(width: 80, height: 92)
    }
    
    public func numberOfSections(in collectionView: UICollectionView) -> Int {
        return 1
    }
    
    public func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return uiDelegate?.seatItems(view: self).count ?? 0
    }
    
    public func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell: AUIMicSeatItemCell = collectionView.dequeueReusableCell(withReuseIdentifier: kMicSeatCellId, for: indexPath) as! AUIMicSeatItemCell
        let seatInfo = uiDelegate?.seatItems(view: self)[indexPath.item]
        cell.item = seatInfo
        uiDelegate?.onMuteVideo(view: self, seatIndex: indexPath.item, canvas: cell.canvasView, isMuteVideo: seatInfo?.isMuteVideo ?? true)
        return cell
    }
    
    public func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        let idx = indexPath.row
        uiDelegate?.onItemDidClick(view: self, seatIndex: idx)
    }
}


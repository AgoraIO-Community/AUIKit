//
//  AUIMicSeatView.swift
//  AUIKit
//
//  Created by wushengtao on 2023/2/23.
//

import Foundation

private let kMicSeatCellId = "kMicSeatCellId"


/// 麦位管理组件
public class AUIMicSeatView: UIView {
    public weak var uiDelegate: AUIMicSeatViewDelegate?
    
    public lazy var collectionView: UICollectionView = {
        let flowLayout = UICollectionViewFlowLayout()
        let width: CGFloat = 56//min(bounds.size.width / 4.0, bounds.size.height / 2)
        let height: CGFloat = 106
        let hPadding = Int((bounds.width - width * 4) / 3)
        flowLayout.itemSize = CGSize(width: width, height: height)
        flowLayout.minimumLineSpacing = 0
        flowLayout.minimumInteritemSpacing = CGFloat(hPadding)
        
        let collectionView = UICollectionView(frame: bounds, collectionViewLayout: flowLayout)
        collectionView.register(AUIMicSeatItemCell.self, forCellWithReuseIdentifier: kMicSeatCellId)
        collectionView.delegate = self
        collectionView.dataSource = self
        collectionView.backgroundColor = .clear
        
        return collectionView
    }()
    
    public override init(frame: CGRect) {
        super.init(frame: frame)
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
    
}

extension AUIMicSeatView: UICollectionViewDelegate, UICollectionViewDataSource {
    
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


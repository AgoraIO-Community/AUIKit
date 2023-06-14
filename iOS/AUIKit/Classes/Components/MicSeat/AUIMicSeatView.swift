//
//  AUIMicSeatView.swift
//  AUIKit
//
//  Created by wushengtao on 2023/2/23.
//

import Foundation

private let kMicSeatCellId = "kMicSeatCellId"

@objc public enum AUIMicSeatViewLayoutType: UInt {
    case one = 1
    case six
    case eight
    case nine
}


/// 麦位管理组件
public class AUIMicSeatView: UIView {
    
    public weak var uiDelegate: AUIMicSeatViewDelegate?
        
    public lazy var collectionView: UICollectionView = {
        let collectionView = UICollectionView(frame: bounds, collectionViewLayout: UICollectionViewLayout())
        collectionView.register(AUIMicSeatItemCell.self, forCellWithReuseIdentifier: kMicSeatCellId)
        collectionView.delegate = self
        collectionView.dataSource = self
        collectionView.backgroundColor = .clear
        
        return collectionView
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
    }
    
    @objc public convenience init(frame: CGRect, layout: UICollectionViewLayout) {
        self.init(frame: frame)
        _loadSubViews()
        self.collectionView.setCollectionViewLayout(layout, animated: true)
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
    
   
    
    public func updateMicVolume(index: Int,volume: Int) {
        let cell = collectionView.cellForItem(at: IndexPath(item: index, section: 0)) as? AUIMicSeatItemCell
        cell?.updateVolume(volume)
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
        uiDelegate?.onMuteVideo(view: self, seatIndex: indexPath.item, canvas: cell.canvasView, isMuteVideo: seatInfo?.isMuteAudio ?? true)
        return cell
    }
    
    public func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        let idx = indexPath.row
        uiDelegate?.onItemDidClick(view: self, seatIndex: idx)
    }
}


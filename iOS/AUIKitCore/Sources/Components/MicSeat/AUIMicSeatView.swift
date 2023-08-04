//
//  AUIMicSeatView.swift
//  AUIKit
//
//  Created by wushengtao on 2023/2/23.
//

import Foundation

private let kMicSeatCellId = "kMicSeatCellId"

//@objc public enum AUIMicSeatViewLayoutType: UInt {
//    case one = 1
//    case six
//    case eight
//    case nine
//}

@objc public protocol IAUIMicSeatView: NSObjectProtocol {
    
    /// Description 刷新AUIMicSeatView
    /// - Parameter index: 正整数刷新局部 负整数刷新全部
    func refresh(index: Int)
    
    /// Description 更新麦位音量
    /// - Parameters:
    ///   - index: 麦位位置
    ///   - volume: 音量大小
    func updateMicVolume(index: Int,volume: Int)
}


/// 麦位UI组件
public class AUIMicSeatView: UIView,IAUIMicSeatView {
        
    public weak var uiDelegate: AUIMicSeatViewDelegate?
    
    public var hiddenRipple = false
        
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
    
    ///  Chinese
    /// - Parameters:
    ///   - frame: frame
    ///   - layout: 布局
    ///   - hiddenRipple: 是否显示呼吸灯麦位动画
    ///  English
    /// - Parameters:
    ///   - frame: frame
    ///   - layout: layout
    ///   - hiddenRipple: Whether to display the breathing light animation
    @objc public convenience init(frame: CGRect, layout: UICollectionViewLayout,hiddenRipple: Bool = true) {
        self.init(frame: frame)
        self.hiddenRipple = hiddenRipple
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
    
    public func refresh(index: Int) {
        if index < 0 {
            self.collectionView.reloadData()
        } else {
            collectionView.reloadItems(at: [IndexPath(item: Int(index), section: 0)])
        }
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
        cell.hiddenRipple = self.hiddenRipple
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


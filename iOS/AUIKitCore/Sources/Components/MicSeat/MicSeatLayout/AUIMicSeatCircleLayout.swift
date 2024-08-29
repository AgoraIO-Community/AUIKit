//
//  AUIMicSeatCircleLayout.swift
//  AUIKit
//
//  Created by 朱继超 on 2023/6/1.
//

import UIKit


/// Description 环形布局半径与弧度数据源
@objc public protocol AUIMicSeatCircleLayoutDataSource:AnyObject,NSObjectProtocol {
        
    /// Description Radius for circle
    var radius: CGFloat { get }
    
    /// Description Deflection angle.Rows count even number does not support setting variable angle
    @objc optional func degree() -> CGFloat
}


/// Description 环形布局
@objcMembers public final class AUIMicSeatCircleLayout: UICollectionViewFlowLayout {
    
    internal var center: CGPoint!
    internal var radius: CGFloat!
    internal var rows: Int!
    
    public weak var dataSource: AUIMicSeatCircleLayoutDataSource? {
        didSet {
            if self.dataSource?.radius ?? 70 > 0 {
                self.radius = self.dataSource?.radius ?? 70
            }
        }
    }
    
    private var deleteIndexPaths: [IndexPath]?
    private var insertIndexPaths: [IndexPath]?
    
    public override func prepare() {
        let size = self.collectionView?.frame.size ?? .zero
        self.rows = self.collectionView?.numberOfItems(inSection: 0) ?? 0
        self.center = CGPoint(x: size.width / 2, y: size.height / 2)
        self.radius = self.dataSource?.radius ?? min(size.width, size.height) / 3.0
    }
    
    
    public override func layoutAttributesForItem(at indexPath: IndexPath) -> UICollectionViewLayoutAttributes? {
        //Calculate per item center
        let attributes = UICollectionViewLayoutAttributes(forCellWith: indexPath)
        attributes.size = self.itemSize
        if self.rows == 1 {
            attributes.center = self.center
        } else {
            if self.rows%2 == 0 {
                var angle = Float(2 * indexPath.item) * Float(Double.pi) / Float(self.rows)
                if let degree = self.dataSource?.degree?(),degree > 0 {
                    angle = Float(degree)
                }
                
                attributes.center = CGPoint(
                    x: self.center.x + self.radius * CGFloat(cosf(angle)),
                    y: self.center.y + self.radius * CGFloat(sinf(angle)))
            } else {
                var angle = 2 * CGFloat(Double.pi) / CGFloat(self.rows) * CGFloat(indexPath.row)
                if let degree = self.dataSource?.degree?(),degree > 0 {
                    angle = degree
                }
                attributes.center = CGPoint(x: self.center.x + radius * sin(angle), y: self.center.y + self.radius * cos(angle))
            }
        }
        return attributes
    }
    
    public override func layoutAttributesForElements(in rect: CGRect) -> [UICollectionViewLayoutAttributes]? {
        
        var attributesArray = [UICollectionViewLayoutAttributes]()
        for index in 0 ..< self.rows {
            let indexPath = IndexPath(item: index, section: 0)
            attributesArray.append(self.layoutAttributesForItem(at:indexPath)!)
        }
        return attributesArray
    }
    
    
    
    public override func prepare(forCollectionViewUpdates updateItems: [UICollectionViewUpdateItem]) {
        self.deleteIndexPaths = [IndexPath]()
        self.insertIndexPaths = [IndexPath]()
        
        for updateItem in updateItems {
            if updateItem.updateAction == UICollectionViewUpdateItem.Action.delete {
                guard let indexPath = updateItem.indexPathBeforeUpdate else { return }
                self.deleteIndexPaths?.append(indexPath)
            } else if updateItem.updateAction == UICollectionViewUpdateItem.Action.insert {
                guard let indexPath = updateItem.indexPathAfterUpdate else { return }
                self.insertIndexPaths?.append(indexPath)
            }
        }
        
    }
    
    public override func finalizeCollectionViewUpdates() {
        super.finalizeCollectionViewUpdates()
        self.deleteIndexPaths = nil
        self.insertIndexPaths = nil
    }
    
    public override func initialLayoutAttributesForAppearingItem(at itemIndexPath: IndexPath) -> UICollectionViewLayoutAttributes? {
        //Appear animation
        var attributes = super.initialLayoutAttributesForAppearingItem(at: itemIndexPath)
        
        if self.insertIndexPaths?.contains(itemIndexPath) ?? false {
            if attributes != nil {
                attributes = self.layoutAttributesForItem(at: itemIndexPath)
                attributes?.alpha = 0.0
                attributes?.center = CGPoint(x: self.center.x, y: self.center.y)
            }
        }
        
        
        return attributes
    }
    
    public override func finalLayoutAttributesForDisappearingItem(at itemIndexPath: IndexPath) -> UICollectionViewLayoutAttributes? {
        // Disappear animation
        var attributes = super.finalLayoutAttributesForDisappearingItem(at: itemIndexPath)
        
        if self.deleteIndexPaths?.contains(itemIndexPath) ?? false {
            if attributes != nil {
                attributes = self.layoutAttributesForItem(at: itemIndexPath)
                
                attributes?.alpha = 0.0
                attributes?.center = CGPoint(x: self.center.x, y: self.center.y)
                attributes?.transform3D = CATransform3DMakeScale(0.1, 0.1, 1.0)
            }
        }
        
        return attributes
    }
    
    

}

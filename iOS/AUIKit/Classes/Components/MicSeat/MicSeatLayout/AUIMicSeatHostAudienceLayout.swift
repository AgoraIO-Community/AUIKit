//
//  AUIMicSeatHostAudienceLayout.swift
//  AUIKit
//
//  Created by 朱继超 on 2023/6/1.
//

import UIKit


/// Description 9麦位布局数据源
@objc public protocol AUIMicSeatHostAudienceLayoutDataSource:AnyObject,NSObjectProtocol {
    func hostSize() -> CGSize
    func otherSize() -> CGSize
    func rowSpace() -> CGFloat
}

fileprivate let columnSpace = 6

/// Description 9麦位布局
@objcMembers public final class AUIMicSeatHostAudienceLayout: UICollectionViewLayout {
    
    internal var center: CGPoint!
    internal var rows: Int!
    internal var hostSize: CGSize!
    internal var otherSize: CGSize!
    internal var rowSpace: CGFloat = 10
    
    public weak var dataSource: AUIMicSeatHostAudienceLayoutDataSource?
    
    public override func prepare() {
        let size = self.collectionView?.frame.size ?? .zero
        self.rows = self.collectionView?.numberOfItems(inSection: 0) ?? 0
        self.center = CGPoint(x: size.width / 2, y: size.height / 2)
        self.hostSize = self.dataSource?.hostSize() ?? CGSize(width: 102, height: 120)
        self.otherSize = self.dataSource?.otherSize() ?? CGSize(width: 80, height: 92)
        self.rowSpace = self.dataSource?.rowSpace() ?? 10
    }
    
    public override func layoutAttributesForItem(at indexPath: IndexPath) -> UICollectionViewLayoutAttributes? {
        //Calculate per item center
        let attributes = UICollectionViewLayoutAttributes(forCellWith: indexPath)
        if indexPath.row == 0 {
            attributes.frame = CGRect(x: 0, y: 0, width: self.hostSize.width, height: self.hostSize.height)
            attributes.center = CGPoint(x: self.center.x, y: self.hostSize.height/2.0)
        } else {
            let maxWidth = 4*Int(self.otherSize.width)+columnSpace*3
            if maxWidth > Int(self.collectionView!.frame.width) {
                assert(false,"The width of the second row cannot exceed the total width of the collectionView minus 30.")
            }
            let sidesSpace = (Int(self.collectionView!.frame.width) - maxWidth)/2
            let x = sidesSpace+((indexPath.row-1)%4)*Int(self.otherSize.width)+columnSpace*((indexPath.row-1)%4)
            let y = ((indexPath.row-1)/4)*Int(self.otherSize.height)+Int(self.hostSize.height)+((indexPath.row-1)/4)*Int(self.rowSpace)
            attributes.frame = CGRect(x: x, y: y, width: Int(self.otherSize.width), height: Int(self.otherSize.height))
            print("indexPath:\(indexPath.row) frame:\(attributes.frame)")
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
    

}

//
//  AUITabsPageContainer.swift
//  AUiKit
//
//  Created by 朱继超 on 2023/5/22.
//

import UIKit

public protocol AUITabsPageContainerCellDelegate: NSObjectProtocol {
        
//    associatedtype RawDataClass: NSObject
    
    /// Description 获取当前容器视图类名作为视图唯一标识
    /// - Returns: 类名
    func viewIdentity() -> String
    
    /// Description 根据协议类名创建用户容器类
    /// - Returns: 继承UIView的容器
    func create(frame: CGRect,datas: [NSObject]) -> UIView?
    
    /// Description 容器类的布局信息
    /// - Returns: frame
    func rawFrame() -> CGRect
    
    /// Description 容器类的数据源数组
    /// - Returns: 数据源数组
    func rawDatas() -> [NSObject]
}



public class AUITabsPageContainer: UIView {
    
    private var titles = [String]()
    
    private var tabStyle = AUiTabsStyle()
    
    private var containers = [AUITabsPageContainerCellDelegate]()
            
    private lazy var tabs: AUiTabs = {
        AUiTabs(frame: CGRect(x: 15, y: 24, width: self.frame.width-30, height: 44), segmentStyle: self.tabStyle, titles: self.titles)
    }()
    
    private lazy var layout: UICollectionViewFlowLayout = {
        let flow = UICollectionViewFlowLayout()
        flow.scrollDirection = .horizontal
        flow.itemSize = CGSize(width: AScreenWidth, height: self.frame.height - self.tabs.frame.maxY - 5 - CGFloat(ABottomBarHeight))
        flow.minimumLineSpacing = 0
        flow.minimumInteritemSpacing = 0
        return flow
    }()
    
    private lazy var container: UICollectionView = {
        UICollectionView(frame: CGRect(x: 0, y: self.tabs.frame.maxY, width: AScreenWidth, height: self.frame.height - self.tabs.frame.maxY - 5 - CGFloat(ABottomBarHeight)), collectionViewLayout: self.layout).registerCell(AUITabsPageContainerCell.self, forCellReuseIdentifier: "AUITabsPageContainerCell").backgroundColor(.clear).delegate(self).dataSource(self)
    }()
    
    public convenience init(frame: CGRect,barStyle: AUiTabsStyle,containers: [AUITabsPageContainerCellDelegate],titles: [String]) {
        self.init(frame: frame)
        self.tabStyle = barStyle
        self.containers = containers
        self.titles = titles
        self.addSubview(self.tabs)
        self.addSubview(self.container)
    }
    
    
    
}

extension AUITabsPageContainer: UICollectionViewDataSource,UICollectionViewDelegate {
    public func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        self.containers.count
    }
    
    public func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "AUITabsPageContainerCell", for: indexPath) as? AUITabsPageContainerCell
        cell?.renderContainer()
        return cell ?? AUITabsPageContainerCell()
    }
    
    public func collectionView(_ collectionView: UICollectionView, willDisplay cell: UICollectionViewCell, forItemAt indexPath: IndexPath) {
        guard let container = self.containers[safe: indexPath.row] else { return }
        
//        for (index,container) in self.containers.enumerated() {
//            if index < indexPath.row - 1 || index > indexPath.row + 1 {
//                guard let cell = collectionView.cellForItem(at: IndexPath(row: index, section: indexPath.section)) as? AUITabsPageContainerCell else { continue }
//                cell.view = nil
//            }
//        }
        guard let cell = cell as? AUITabsPageContainerCell else { return }
        let identity = container.viewIdentity()
        if cell.identity != identity {
            cell.willRenderContainer(displayView: container.create(frame: container.rawFrame(), datas: container.rawDatas()), identity: identity)
        }
    }
    
    
}


class AUITabsPageContainerCell: UICollectionViewCell {
    
    
    var identity: String = ""
    
    private var view: UIView?
    
    override init(frame: CGRect) {
        super.init(frame: frame)
    }
    
    required init?(coder: NSCoder) {
        super.init(coder: coder)
    }
    
    func willRenderContainer(displayView: UIView?,identity: String) {
        self.identity = identity
        self.view?.removeFromSuperview()
        self.view = nil
        self.view = displayView
    }
    
    func renderContainer() {
        guard let container = self.view else { return }
        self.contentView.addSubview(container)
        
        self.view?.bottomAnchor.constraint(equalTo: self.contentView.bottomAnchor).isActive = true
        self.view?.leftAnchor.constraint(equalTo: self.contentView.leftAnchor).isActive = true
        self.view?.rightAnchor.constraint(equalTo: self.contentView.rightAnchor).isActive = true
        self.view?.topAnchor.constraint(equalTo: self.contentView.topAnchor).isActive = true
    }
    
    
    override func prepareForReuse() {
        super.prepareForReuse()
        self.view?.removeFromSuperview()
        self.view = nil
    }
    

}

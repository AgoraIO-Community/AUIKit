//
//  AUIReceiveGiftsView.swift
//  AUiKit
//
//  Created by 朱继超 on 2023/5/17.
//

import UIKit
/*!
 *  \~Chinese
 *  AUIGiftBarrageView抽象而来的view协议用于跟scene层view binder绑定进行数据UI交互
 *
 *  \~English
 *  The view protocol abstracted from AUIGiftBarrageView is used to bind with the scene layer view binder for data UI interaction
 *
 */
@objc public protocol IAUIGiftBarrageView: NSObjectProtocol {
    
    /// Description 收到礼物后刷新UI
    /// - Parameter gift: 礼物模型
    /// Description Refresh the UI after receiving the gift
    /// - Parameter gift: entity
    func receiveGift(gift: AUIGiftEntity)
}
/*!
 *  \~Chinese
 *  收礼物cell的数据源协议包含cell高度以及X、Y方向的缩放比例
 *
 *  \~English
 *  The data source protocol of the gift receiving cell includes the cell height and the scaling ratio in the X and Y directions
 *
 */
@objc public protocol AUIGiftBarrageViewDataSource: NSObjectProtocol {
    @objc optional func rowHeight() -> CGFloat
    @objc optional func zoomScaleX() -> CGFloat
    @objc optional func zoomScaleY() -> CGFloat
}
/*!
 *  \~Chinese
 *  收礼物view
 *
 *  \~English
 *  The receive gift view.
 *
 */
public class AUIGiftBarrageView: UIView, UITableViewDelegate, UITableViewDataSource,IAUIGiftBarrageView {
    
    public var dataSource: AUIGiftBarrageViewDataSource?
    
    public var gifts = [AUIGiftEntity]() {
        didSet {
            if self.gifts.count > 0 {
                self.isHidden = false
                self.cellAnimation()
            }
        }
    }

    private var lastOffsetY = CGFloat(0)

    public lazy var giftList: UITableView = {
        UITableView(frame: CGRect(x: 5, y: 0, width: self.frame.width - 20, height: self.frame.height), style: .plain).tableFooterView(UIView()).separatorStyle(.none).showsVerticalScrollIndicator(false).showsHorizontalScrollIndicator(false).delegate(self).dataSource(self).backgroundColor(.clear).isUserInteractionEnabled(false)
    }()

    override public init(frame: CGRect) {
        super.init(frame: frame)
    }
    
    /// Description 初始化方法需要传入布局的坐标以及对cell一些形变参数的数据源设置
    /// - Parameters:
    ///   - frame: 坐标
    ///   - source: cell形变数据源
    @objc public convenience init(frame: CGRect, source: AUIGiftBarrageViewDataSource?) {
        self.init(frame: frame)
        self.dataSource = source
        self.addSubview(self.giftList)
        self.giftList.isScrollEnabled = false
        self.giftList.isUserInteractionEnabled = false
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public func receiveGift(gift: AUIGiftEntity) {
        self.gifts.append(gift)
    }
}

public extension AUIGiftBarrageView {
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        self.gifts.count
    }

    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        self.dataSource?.rowHeight?() ?? 64
    }

    func tableView(_ tableView: UITableView, didEndDisplaying cell: UITableViewCell, forRowAt indexPath: IndexPath) {
        cell.contentView.transform = CGAffineTransform(scaleX: 0.75, y: 0.75)
        cell.alpha = 0
        cell.isUserInteractionEnabled = false
    }

    func tableView(_ tableView: UITableView, willDisplay cell: UITableViewCell, forRowAt indexPath: IndexPath) {
        cell.contentView.transform = CGAffineTransform(scaleX: 1, y: 1)
    }

    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        var cell = tableView.dequeueReusableCell(withIdentifier: "AUIReceiveGiftCell") as? AUIReceiveGiftCell
        if cell == nil {
            cell = AUIReceiveGiftCell(reuseIdentifier: "AUIReceiveGiftCell",config: AUIReceiveGiftCellConfig())
        }
        cell?.refresh(item: self.gifts[safe: indexPath.row] ?? AUIGiftEntity())
        return cell ?? AUIReceiveGiftCell()
    }

    internal func cellAnimation() {
        self.alpha = 1
        self.giftList.reloadData()
        var indexPath = IndexPath(row: 0, section: 0)
        if self.gifts.count >= 2 {
            indexPath = IndexPath(row: self.gifts.count - 2, section: 0)
        }
        if self.gifts.count > 1{
            let cell = self.giftList.cellForRow(at: indexPath) as? AUIReceiveGiftCell
            guard let gift = self.gifts[safe: indexPath.row] else { return }
            cell?.refresh(item: gift)
            UIView.animate(withDuration: 0.3) {
                cell?.alpha = 0.35
                cell?.contentView.transform = CGAffineTransform(scaleX: self.dataSource?.zoomScaleX?() ?? 0.75, y: self.dataSource?.zoomScaleY?() ?? 0.75)
                self.giftList.scrollToRow(at: IndexPath(row: self.gifts.count - 1, section: 0), at: .top, animated: false)
            }
        }
        DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
            UIView.animate(withDuration: 0.3, delay: 1, options: .curveEaseInOut) {
                self.alpha = 0
                self.isHidden = true
            } completion: { _ in
                self.gifts.removeAll()
            }
        }
    }
}


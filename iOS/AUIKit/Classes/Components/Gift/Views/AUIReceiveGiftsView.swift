//
//  AUIReceiveGiftsView.swift
//  AUiKit
//
//  Created by 朱继超 on 2023/5/17.
//

import UIKit

@objc public protocol AUIReceiveGiftsViewDataSource: NSObjectProtocol {
    @objc optional func rowHeight() -> CGFloat
    @objc optional func zoomScaleX() -> CGFloat
    @objc optional func zoomScaleY() -> CGFloat
}

public class AUIReceiveGiftsView: UIView, UITableViewDelegate, UITableViewDataSource {
    
    private var dataSource: AUIReceiveGiftsViewDataSource?
    
    public var gifts = [AUIGiftEntity]() {
        didSet {
            if self.gifts.count > 0 {
                self.viewWithTag(11)?.isHidden = false
                self.cellAnimation()
            }
        }
    }

    private var lastOffsetY = CGFloat(0)

    public lazy var giftList: UITableView = {
        UITableView(frame: CGRect(x: 5, y: 0, width: self.frame.width - 20, height: self.frame.height), style: .plain).tableFooterView(UIView()).separatorStyle(.none).showsVerticalScrollIndicator(false).showsHorizontalScrollIndicator(false).delegate(self).dataSource(self).backgroundColor(.clear)
    }()

    override public init(frame: CGRect) {
        super.init(frame: frame)
    }
    
    @objc public convenience init(frame: CGRect, source: AUIReceiveGiftsViewDataSource?) {
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
}

public extension AUIReceiveGiftsView {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        self.gifts.count
    }

    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        self.dataSource?.rowHeight?() ?? 54
    }

    func tableView(_ tableView: UITableView, didEndDisplaying cell: UITableViewCell, forRowAt indexPath: IndexPath) {
        cell.contentView.transform = CGAffineTransform(scaleX: 0.75, y: 0.75)
        cell.alpha = 0
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
        let indexPath = IndexPath(row: self.gifts.count - 2, section: 0)
        let cell = self.giftList.cellForRow(at: indexPath) as? AUIReceiveGiftCell
        guard let gift = self.gifts[safe: indexPath.row] else { return }
        cell?.refresh(item: gift)
        UIView.animate(withDuration: 0.3) {
            cell?.alpha = 0.35
            cell?.contentView.transform = CGAffineTransform(scaleX: self.dataSource?.zoomScaleX?() ?? 0.75, y: self.dataSource?.zoomScaleY?() ?? 0.75)
            self.giftList.scrollToRow(at: IndexPath(row: self.gifts.count - 1, section: 0), at: .top, animated: false)
        }
        DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
            UIView.animate(withDuration: 0.3, delay: 1, options: .curveEaseInOut) {
                self.alpha = 0
            } completion: { finished in
                if finished {
                    self.gifts.removeAll()
                    self.alpha = 0
                }
            }
        }
    }
}


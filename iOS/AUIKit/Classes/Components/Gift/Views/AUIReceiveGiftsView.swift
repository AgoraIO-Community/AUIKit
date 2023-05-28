//
//  AUIReceiveGiftsView.swift
//  AUIKit
//
//  Created by 朱继超 on 2023/5/17.
//

import UIKit

public class AUIReceiveGiftsView: UIView, UITableViewDelegate, UITableViewDataSource {
    
    public var gifts = [AUIGiftEntity]() {
        didSet {
            if self.gifts.count > 0 {
                self.cellAnimation()
            }
        }
    }

    private var lastOffsetY = CGFloat(0)

    public lazy var giftList: UITableView = {
        UITableView(frame: CGRect(x: 5, y: 0, width: self.frame.width - 20, height: self.frame.height), style: .plain).tableFooterView(UIView()).separatorStyle(.none).registerCell(AUIReceiveGiftCell.self, forCellReuseIdentifier: "AUIReceiveGiftCell").showsVerticalScrollIndicator(false).showsHorizontalScrollIndicator(false).delegate(self).dataSource(self).backgroundColor(.clear)
    }()

    override public init(frame: CGRect) {
        super.init(frame: frame)
        self.addSubview(giftList)
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
        44
    }

    func tableView(_ tableView: UITableView, didEndDisplaying cell: UITableViewCell, forRowAt indexPath: IndexPath) {
        cell.contentView.transform = CGAffineTransform(scaleX: 0.75, y: 0.75)
        cell.alpha = 0
    }

    func tableView(_ tableView: UITableView, willDisplay cell: UITableViewCell, forRowAt indexPath: IndexPath) {
        cell.contentView.transform = CGAffineTransform(scaleX: 1, y: 1)
    }

    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        var cell = tableView.dequeueReusableCell(withIdentifier: "AUIReceiveGiftCell", for: indexPath) as? AUIReceiveGiftCell
        if cell == nil {
            cell = AUIReceiveGiftCell(style: .default, reuseIdentifier: "AUIReceiveGiftCell")
        }
        cell?.refresh(item: self.gifts[safe: indexPath.row] ?? AUIGiftEntity())
        return cell ?? AUIReceiveGiftCell()
    }

    internal func cellAnimation() {
        self.alpha = 1
        self.giftList.reloadData()
        let indexPath = IndexPath(row: self.gifts.count - 2, section: 0)
        let cell = self.giftList.cellForRow(at: indexPath) as? AUIReceiveGiftCell
        cell?.refresh(item: self.gifts[indexPath.row])
        UIView.animate(withDuration: 0.3) {
            cell?.alpha = 0.35
            cell?.contentView.transform = CGAffineTransform(scaleX: 0.75, y: 0.75)
            self.giftList.scrollToRow(at: IndexPath(row: self.gifts.count - 1, section: 0), at: .top, animated: false)
        }
        DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
            UIView.animate(withDuration: 0.3, delay: 1, options: .curveEaseInOut) {
                self.alpha = 0
            } completion: { finished in
                if finished {
                    self.gifts.removeAll()
                    self.removeFromSuperview()
                }
            }
        }
    }
}


//
//  AUIEmptyView.swift
//  AUIKit
//
//  Created by 朱继超 on 2023/6/26.
//

import UIKit

public class AUIEmptyView: UIView {

    var emptyImage = UIImage.aui_Image(named: "empty")

    lazy var image: UIImageView = .init(frame: CGRect(x: 90, y: 60, width: self.frame.width - 180, height: (231 / 397.0) * (self.frame.width - 180))).contentMode(.scaleAspectFit).image(self.emptyImage)

    lazy var text: UILabel = {
        UILabel(frame: CGRect(x: 20, y: self.image.frame.maxY + 10, width: self.frame.width - 40, height: 60)).textAlignment(.center).font(.systemFont(ofSize: 14, weight: .regular)).textColor(UIColor(0x5E686E)).numberOfLines(0)
    }()

    override public init(frame: CGRect) {
        super.init(frame: frame)
    }

    public convenience init(frame: CGRect, title: String, image: UIImage?) {
        self.init(frame: frame)
        if image != nil {
            emptyImage = image!
            self.image.image = image
        }
        self.addSubViews([self.image, self.text])
        self.image.center = self.center
        self.text.frame = CGRect(x: 20, y: self.image.frame.maxY, width: self.frame.width - 40, height: 60)
        self.text.text = title
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    public override func hitTest(_ point: CGPoint, with event: UIEvent?) -> UIView? {
        if let subviews = self.superview?.subviews.reversed() {
            for view in subviews {
                if view is UIButton,view.frame.contains(point) {
                    return view
                }
                if view is UITableView {
                    return view
                }
                if view is UICollectionView {
                    return view
                }
            }
        }
        return super.hitTest(point, with: event)
    }
}

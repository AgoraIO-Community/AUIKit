//
//  AUITableViewSliderCell.swift
//  AUIKit
//
//  Created by wushengtao on 2023/4/2.
//

import Foundation


open class AUITableViewSliderCell: AUITableViewCell {
    public var onSliderValueDidChanged: ((CGFloat)->())?
    public lazy var slider: AUISlider = {
        let slider = AUISlider()
        
        slider.addTarget(self, action: #selector(onSliderChanged(_:)), for: .valueChanged)
        
        return slider
    }()
    
    public override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        contentView.addSubview(slider)
        slider.style = .singleLine
    }
    
    required public init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open override func layoutSubviews() {
        super.layoutSubviews()
        slider.frame = CGRect(x: 95, y: 0, width: aui_width - 95, height: aui_height)
        slider.setNeedsLayout()
        slider.layoutIfNeeded()
    }
    
    open override func setSelected(_ selected: Bool, animated: Bool) {
    }
    
    open override func setHighlighted(_ highlighted: Bool, animated: Bool) {
    }
    
    @objc private func onSliderChanged(_ sender: AUISlider) {
        onSliderValueDidChanged?(slider.currentValue)
    }
}

//
//  AUiSlider.swift
//  AUiKit
//
//  Created by wushengtao on 2023/3/30.
//

import Foundation
import SwiftTheme

public class AUiSliderTheme: NSObject {
    public var backgroundColor: ThemeColorPicker = "CommonColor.black"              //背景色
    public var minimumTrackColor: ThemeColorPicker = "CommonColor.primary"          //滑块左边部分颜色
    public var maximumTrackColor: ThemeColorPicker = "CommonColor.primary35"        //滑块右边部分颜色
    public var thumbColor: ThemeColorPicker = "CommonColor.normalTextColor"         //滑块颜色
    public var thumbBorderColor: ThemeCGColorPicker = "CommonColor.primary"         //滑块边框颜色
    public var trackBigLabelFont: ThemeFontPicker = "Slider.numberBigLabelFont"     //数值描述的字体(文字描述居于左右时)
    public var trackSmallLabelFont: ThemeFontPicker = "Slider.numberSmallLabelFont" //数值描述的字体(文字描述居于底部时)
    public var trackLabelColor: ThemeColorPicker = "CommonColor.normalTextColor"    //数值描述颜色
    public var titleLabelFont: ThemeFontPicker = "Slider.titleLabelFont"            //标题字体
    public var titleLabelColor: ThemeColorPicker = "CommonColor.normalTextColor"    //标题颜色
}

public enum AUiSliderStyle: Int {
    case singleLine = 0            //单滑动条
    case titleAndSingleLine        //标题+滑动条
    case bigNumberAndSingleLine    //左右数字+滑动条
    case smallNumberAndSingleLine  //数字条在下面+滑动条
}

private let kPadding:CGFloat = 16
private let kLineHeight: CGFloat = 2
private let kPaddingBetweenThumbViewAndSmallNumber: CGFloat = 4
private let kThumbViewSize = CGSize(width: 16, height: 16)
private let kSplitLineSize = CGSize(width: 2, height: 8)
open class AUiSlider: UIControl {
    private var touchPrevVal: CGFloat = 0
    open var minimumValue: CGFloat = 0
    open var maximumValue: CGFloat = 100
    open var currentValue: CGFloat = 50 {
        didSet {
            thumbLabel.text = "\(Int(currentValue))"
            let percent = currentValue / (maximumValue - minimumValue)
            minimumTrackLine.aui_width = maximumTrackLine.aui_width * percent
            thumbView.aui_centerX = maximumTrackLine.aui_left + maximumTrackLine.aui_width * percent
            thumbLabel.aui_centerX = thumbView.aui_centerX
        }
    }
    public var style: AUiSliderStyle = .singleLine {
        didSet {
            resetStyle()
        }
    }
    public var theme: AUiSliderTheme = AUiSliderTheme() {
        didSet {
            resetTheme()
        }
    }
    
    //标题
    public lazy var textLabel: UILabel = UILabel()
    
    //头部分割线
    lazy var headSplitLine: UIView = UIView()
    
    //尾部分割线
    lazy var tailSplitLine: UIView = UIView()
    
    //滑块左边部分线
    lazy var minimumTrackLine: UIView = UIView()
    
    //滑块右边部分线
    lazy var maximumTrackLine: UIView = UIView()
    
    //滑块
    lazy var thumbView: UIView = {
        let view = UIView()
        view.aui_size = kThumbViewSize
        view.layer.cornerRadius = kThumbViewSize.width / 2
        view.layer.theme_borderColor = theme.thumbBorderColor
        view.layer.borderWidth = 2
        view.clipsToBounds = true
        
        return view
    }()
    
    //最小数值展示
    lazy var minimumTrackLabel: UILabel = UILabel()
    
    //最大数值展示
    lazy var maximumTrackLabel: UILabel = UILabel()
    
    //当前数值展示
    lazy var thumbLabel: UILabel = UILabel()
    
    public override init(frame: CGRect) {
        super.init(frame: frame)
        _loadSubViews()
    }
    
    required public init?(coder: NSCoder) {
        super.init(coder: coder)
        _loadSubViews()
    }
    
    private func resetTheme() {
        textLabel.theme_font = theme.titleLabelFont
        textLabel.theme_textColor = theme.titleLabelColor
        
        minimumTrackLine.theme_backgroundColor = theme.minimumTrackColor
        maximumTrackLine.theme_backgroundColor = theme.maximumTrackColor
        thumbView.theme_backgroundColor = theme.thumbColor
        
        minimumTrackLabel.theme_textColor = theme.titleLabelColor
        maximumTrackLabel.theme_textColor = theme.titleLabelColor
        thumbLabel.theme_textColor = theme.titleLabelColor
        
        maximumTrackLabel.text = "\(Int(maximumValue))"
        minimumTrackLabel.text = "\(Int(minimumValue))"
        thumbLabel.text = "\(Int(currentValue))"
        
        _resetFont()
    }
    
    private func _resetFont() {
        if style == .smallNumberAndSingleLine {
            minimumTrackLabel.theme_font = theme.trackSmallLabelFont
            maximumTrackLabel.theme_font = theme.trackSmallLabelFont
            thumbLabel.theme_font = theme.trackSmallLabelFont
        } else {
            minimumTrackLabel.theme_font = theme.trackBigLabelFont
            maximumTrackLabel.theme_font = theme.trackBigLabelFont
            thumbLabel.theme_font = theme.trackBigLabelFont
        }
    }
    
    private func _loadSubViews() {
        addSubview(textLabel)
        addSubview(minimumTrackLine)
        addSubview(maximumTrackLine)
        addSubview(headSplitLine)
        addSubview(tailSplitLine)
        addSubview(thumbView)
        addSubview(maximumTrackLabel)
        addSubview(minimumTrackLabel)
        addSubview(thumbLabel)
        resetTheme()
        resetStyle()
        
        let gesture = UIPanGestureRecognizer()
        gesture.addTarget(self, action: #selector(onPanGesture(_:)))
        addGestureRecognizer(gesture)
    }
    
    private func resetStyle() {
        _resetFont()
        switch style {
        case .singleLine:
            textLabel.isHidden = true
            headSplitLine.isHidden = true
            tailSplitLine.isHidden = true
            thumbLabel.isHidden = true
            minimumTrackLabel.isHidden = true
            maximumTrackLabel.isHidden = true
            
            let lineWidth = aui_width - kPadding * 2
            maximumTrackLine.frame = CGRect(x: kPadding, y: (aui_height - kLineHeight) / 2, width: lineWidth, height: kLineHeight)
            let percent = currentValue / (maximumValue - minimumValue)
            minimumTrackLine.frame = CGRect(x: maximumTrackLine.aui_left, y: (aui_height - kLineHeight) / 2, width: lineWidth * percent, height: kLineHeight)
            thumbView.aui_center = CGPoint(x: maximumTrackLine.aui_left + lineWidth * percent,
                                                y: maximumTrackLine.center.y)
            break
        case .titleAndSingleLine:
            textLabel.isHidden = false
            headSplitLine.isHidden = true
            tailSplitLine.isHidden = true
            thumbLabel.isHidden = true
            minimumTrackLabel.isHidden = true
            maximumTrackLabel.isHidden = true
            
            textLabel.sizeToFit()
            let lineWidth = aui_width - kPadding * 3 - textLabel.aui_width
            maximumTrackLine.frame = CGRect(x: textLabel.aui_width + kPadding * 2, y: (aui_height - kLineHeight) / 2, width: lineWidth, height: kLineHeight)
            let percent = currentValue / (maximumValue - minimumValue)
            minimumTrackLine.frame = CGRect(x: maximumTrackLine.aui_left, y: maximumTrackLine.aui_top, width: lineWidth * percent, height: kLineHeight)
            thumbView.aui_center = CGPoint(x: maximumTrackLine.aui_left + lineWidth * percent,
                                                y: maximumTrackLine.center.y)
            textLabel.aui_center = CGPoint(x: kPadding + textLabel.aui_width / 2, y: thumbView.aui_centerY)
            break
        case .smallNumberAndSingleLine:
            textLabel.isHidden = true
            headSplitLine.isHidden = false
            tailSplitLine.isHidden = false
            thumbLabel.isHidden = false
            minimumTrackLabel.isHidden = false
            maximumTrackLabel.isHidden = false
            
            minimumTrackLabel.sizeToFit()
            maximumTrackLabel.sizeToFit()
            thumbLabel.sizeToFit()
            let contentHeight = kPaddingBetweenThumbViewAndSmallNumber + kThumbViewSize.height + minimumTrackLabel.aui_height
            let topBottomPadding = (aui_height - contentHeight) / 2
            let lineWidth = aui_width - kPadding * 2
            maximumTrackLine.frame = CGRect(x: kPadding, y: topBottomPadding, width: lineWidth, height: kLineHeight)
            let percent = currentValue / (maximumValue - minimumValue)
            minimumTrackLine.frame = CGRect(x: maximumTrackLine.aui_left, y: maximumTrackLine.aui_top, width: lineWidth * percent, height: kLineHeight)
            thumbView.aui_center = CGPoint(x: maximumTrackLine.aui_left + lineWidth * percent,
                                                y: maximumTrackLine.center.y)
            minimumTrackLabel.aui_tl = CGPoint(x: maximumTrackLine.aui_left, y: thumbView.aui_bottom + kPaddingBetweenThumbViewAndSmallNumber)
            maximumTrackLabel.aui_tr = CGPoint(x: maximumTrackLine.aui_right, y: minimumTrackLabel.aui_top)
            thumbLabel.aui_center = CGPoint(x: thumbView.aui_centerX, y: maximumTrackLabel.aui_centerY)
            
            headSplitLine.frame = CGRect(x: maximumTrackLine.aui_left,
                                         y: maximumTrackLine.aui_centerY - (maximumTrackLine.aui_height - kSplitLineSize.height) / 2,
                                         width: kSplitLineSize.width,
                                         height: kSplitLineSize.height)
            break
        case .bigNumberAndSingleLine:
            textLabel.isHidden = true
            headSplitLine.isHidden = true
            tailSplitLine.isHidden = true
            thumbLabel.isHidden = true
            minimumTrackLabel.isHidden = false
            maximumTrackLabel.isHidden = false
            
            minimumTrackLabel.sizeToFit()
            maximumTrackLabel.sizeToFit()
            minimumTrackLabel.aui_tl = CGPoint(x: kPadding, y: (aui_height - minimumTrackLabel.aui_height) / 2)
            maximumTrackLabel.aui_tr = CGPoint(x: aui_width - kPadding, y: minimumTrackLabel.aui_top)
            let lineWidth = maximumTrackLabel.aui_left - minimumTrackLabel.aui_right - kPadding * 2
            maximumTrackLine.frame = CGRect(x: minimumTrackLabel.aui_right + kPadding, y: (aui_height - kLineHeight) / 2, width: lineWidth, height: kLineHeight)
            let percent = currentValue / (maximumValue - minimumValue)
            minimumTrackLine.frame = CGRect(x: maximumTrackLine.aui_left, y: maximumTrackLine.aui_top, width: lineWidth * percent, height: kLineHeight)
            thumbView.aui_center = CGPoint(x: maximumTrackLine.aui_left + lineWidth * percent,
                                                y: maximumTrackLine.center.y)
            break
        }
        
        minimumTrackLine.layer.cornerRadius = minimumTrackLine.aui_height / 2
        maximumTrackLine.layer.cornerRadius = maximumTrackLine.aui_height / 2
    }
    
    open override func point(inside point: CGPoint, with event: UIEvent?) -> Bool {
        let isInside = super.point(inside: point, with: event)
        if thumbView.frame.insetBy(dx: -10, dy: -10).contains(point) {
            return true
        }
        return isInside
    }
    
    open override func layoutSubviews() {
        super.layoutSubviews()
        resetStyle()
    }
}


extension AUiSlider {
    @objc func onPanGesture(_ ges: UIPanGestureRecognizer) {
        let state = ges.state
        let val = ges.location(in: self).x
        if state == .began {
            touchPrevVal = val
        } else if state == .changed {
            let offset = val - touchPrevVal
            touchPrevVal = val
            let val = self.currentValue + offset * (maximumValue - minimumValue) / maximumTrackLine.aui_width
            self.currentValue = min(max(val, minimumValue), maximumValue)
            sendActions(for: .valueChanged)
        } else if state == .ended || state == .cancelled {
            
        }
    }
}

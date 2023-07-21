//
//  AUISlider.swift
//  AUIKit
//
//  Created by wushengtao on 2023/3/30.
//

import Foundation
import SwiftTheme

public class AUISliderTheme: NSObject {
    func setup(slider: AUISlider, style: AUISliderStyle){}
    func resetFont(slider: AUISlider, style: AUISliderStyle){}
}

public class AUISliderDynamicTheme: AUISliderTheme {
    
    public var backgroundColor: ThemeColorPicker = "CommonColor.black"              //背景色
    public var minimumTrackColor: ThemeColorPicker = "Slider.minimumTrackColor"          //滑块左边部分颜色
    public var maximumTrackColor: ThemeColorPicker = "Slider.maximumTrackColor"        //滑块右边部分颜色
    public var thumbColor: ThemeColorPicker = "CommonColor.normalTextColor"         //滑块颜色
    public var thumbBorderColor: ThemeCGColorPicker = "CommonColor.primary"         //滑块边框颜色
    public var trackBigLabelFont: ThemeFontPicker = "Slider.numberBigLabelFont"     //数值描述的字体(文字描述居于左右时)
    public var trackSmallLabelFont: ThemeFontPicker = "Slider.numberSmallLabelFont" //数值描述的字体(文字描述居于底部时)
    public var trackLabelColor: ThemeColorPicker = "CommonColor.normalTextColor"    //数值描述颜色
    public var titleLabelFont: ThemeFontPicker = "Slider.titleLabelFont"            //标题字体
    public var titleLabelColor: ThemeColorPicker = "CommonColor.normalTextColor"    //标题颜色
    
    public override func setup(slider: AUISlider, style: AUISliderStyle) {
        
        slider.textLabel.theme_font = titleLabelFont
        slider.textLabel.theme_textColor = titleLabelColor
        
        slider.minimumTrackLine.theme_backgroundColor = "Slider.minimumTrackColor"
        slider.maximumTrackLine.theme_backgroundColor = maximumTrackColor
        slider.thumbView.theme_backgroundColor = thumbColor
        slider.thumbView.layer.theme_borderColor = thumbBorderColor
        
        slider.minimumTrackLabel.theme_textColor = titleLabelColor
        slider.maximumTrackLabel.theme_textColor = titleLabelColor
        slider.thumbLabel.theme_textColor = titleLabelColor
         
        resetFont(slider: slider, style: style)
    }
    
    public override func resetFont(slider: AUISlider, style: AUISliderStyle) {
        if style == .smallNumberAndSingleLine {
            slider.minimumTrackLabel.theme_font = trackSmallLabelFont
            slider.maximumTrackLabel.theme_font = trackSmallLabelFont
            slider.thumbLabel.theme_font = trackSmallLabelFont
        } else {
            slider.minimumTrackLabel.theme_font = trackBigLabelFont
            slider.maximumTrackLabel.theme_font = trackBigLabelFont
            slider.thumbLabel.theme_font = trackBigLabelFont
        }
    }
}

public class AUISliderNativeTheme: AUISliderTheme {
    
    public var backgroundColor: UIColor = .black              //背景色
    public var minimumTrackColor: UIColor = .aui_primary          //滑块左边部分颜色
    public var maximumTrackColor: UIColor = .aui_primary35        //滑块右边部分颜色
    public var thumbColor: UIColor = .aui_normalTextColor        //滑块颜色
    public var thumbBorderColor: UIColor = .aui_primary        //滑块边框颜色
    public var trackBigLabelFont: UIFont = UIFont(name: "PingFangSC-Semibold", size: 17)!     //数值描述的字体(文字描述居于左右时)
    public var trackSmallLabelFont: UIFont = UIFont(name: "PingFangSC-Semibold", size: 14)! //数值描述的字体(文字描述居于底部时)
    public var trackLabelColor: UIColor = .aui_normalTextColor    //数值描述颜色
    public var titleLabelFont: UIFont = UIFont(name: "PingFangSC-Semibold", size: 12)!            //标题字体
    public var titleLabelColor: UIColor = .aui_normalTextColor    //标题颜色
    
    public override func setup(slider: AUISlider, style: AUISliderStyle) {
        
        slider.textLabel.font = titleLabelFont
        slider.textLabel.textColor = titleLabelColor
        
        slider.minimumTrackLine.backgroundColor = minimumTrackColor
        slider.maximumTrackLine.backgroundColor = maximumTrackColor
        slider.thumbView.backgroundColor = thumbColor
        slider.thumbView.layer.borderColor = thumbBorderColor.cgColor
        
        slider.minimumTrackLabel.textColor = titleLabelColor
        slider.maximumTrackLabel.textColor = titleLabelColor
        slider.thumbLabel.textColor = titleLabelColor
         
        resetFont(slider: slider, style: style)
    }
    
    public override func resetFont(slider: AUISlider, style: AUISliderStyle) {
        if style == .smallNumberAndSingleLine {
            slider.minimumTrackLabel.font = trackSmallLabelFont
            slider.maximumTrackLabel.font = trackSmallLabelFont
            slider.thumbLabel.font = trackSmallLabelFont
        } else {
            slider.minimumTrackLabel.font = trackBigLabelFont
            slider.maximumTrackLabel.font = trackBigLabelFont
            slider.thumbLabel.font = trackBigLabelFont
        }
    }
}

public enum AUISliderStyle: Int {
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
open class AUISlider: UIControl {
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
    public var style: AUISliderStyle = .singleLine {
        didSet {
            resetStyle()
        }
    }
    public var theme: AUISliderTheme = AUISliderDynamicTheme() {
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
        theme.setup(slider: self, style: style)
        maximumTrackLabel.text = "\(Int(maximumValue))"
        minimumTrackLabel.text = "\(Int(minimumValue))"
        thumbLabel.text = "\(Int(currentValue))"
    }
    
    private func _loadSubViews() {
        addSubview(textLabel)
        addSubview(maximumTrackLine)
        addSubview(minimumTrackLine)
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
        theme.resetFont(slider: self, style: style)
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


extension AUISlider {
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

//
//  AUIHorizontalTextCarousel.swift
//  AUiKit
//
//  Created by 朱继超 on 2023/5/17.
//

import UIKit
/*!
 *  \~Chinese
 *  跑马灯常用于送礼全局广播
 *
 *  \~English
 *  Marquee is often used for gift giving global broadcast.
 *
 */
@objc public class AUIHorizontalTextCarousel: UIView {
    
    lazy var voiceIcon: UIImageView = {
        UIImageView(frame: CGRect(x: 8, y: (self.frame.height-10)/2.0, width: 10, height: 10)).image(UIImage.aui_Image(named: "speaker"))
    }()
    
    lazy var scroll: UIScrollView = {
        let container = UIScrollView(frame: CGRect(x: self.voiceIcon.frame.maxX+5, y: 0, width: self.frame.width-self.voiceIcon.frame.maxX-15, height: self.frame.height))
        container.showsVerticalScrollIndicator = false
        container.showsHorizontalScrollIndicator = false
        container.isUserInteractionEnabled = false
        container.bounces = false
        return container
    }()
    
    lazy var textCarousel: UILabel = {
        UILabel(frame: CGRect(x: 0, y: 0, width: self.frame.width-self.voiceIcon.frame.maxX-15, height: self.frame.height)).font(.systemFont(ofSize: 12, weight: .semibold)).textColor(UIColor(white: 1, alpha: 0.7)).backgroundColor(.clear)
    }()

    public override init(frame: CGRect) {
        super.init(frame: frame)
        self.addSubViews([self.voiceIcon,self.self.scroll])
        self.scroll.addSubview(self.textCarousel)
    }
    
    /// Description 根据传入文本计算是否 播放滚动动画
    /// - Parameter text: 富文本字符串
    public func textAnimation(text: NSAttributedString) {
        let width = text.string.a.sizeWithText(font: .systemFont(ofSize: 12, weight: .semibold), size: CGSize(width: 999, height: self.frame.height)).width
        if width > AScreenWidth-60 {
            self.frame = CGRect(x: self.frame.minX, y: self.frame.minY, width: AScreenWidth - 60, height: self.frame.height)
            self.scroll.frame = CGRect(x: self.voiceIcon.frame.maxX+5, y: 0, width: self.frame.width-self.voiceIcon.frame.maxX-15, height: self.frame.height)
            self.setGradient([
                UIColor(red: 1, green: 0.545, blue: 0.125, alpha: 1),
                UIColor(red: 0.672, green: 0, blue: 1, alpha: 1)
            ], [CGPoint(x: 0, y: 0.5),CGPoint(x: 1, y: 0.5)])
            self.textCarousel.frame = CGRect(x: 10, y: 0, width: width+33+20, height: self.frame.height)
            self.scroll.contentSize = CGSize(width: width+33+20, height: self.frame.height)
            UIView.animate(withDuration: 6, delay: 0, usingSpringWithDamping: 0.8, initialSpringVelocity: 0.3, options: .curveLinear) {
                self.textCarousel.attributedText = text
                self.scroll.contentOffset = CGPoint(x: self.scroll.contentSize.width/3.0, y: self.scroll.contentOffset.y)
            } completion: { finished in
                if finished {
                    self.removeFromSuperview()
                }
            }
        } else {
            self.frame = CGRect(x: self.frame.minX, y: self.frame.minY, width: width+33+20, height: self.frame.height)
            self.setGradient([
                UIColor(red: 1, green: 0.545, blue: 0.125, alpha: 1),
                UIColor(red: 0.672, green: 0, blue: 1, alpha: 1)
            ], [CGPoint(x: 0, y: 0.5),CGPoint(x: 1, y: 0.5)])
            self.scroll.frame = CGRect(x: 15, y: 0, width: self.frame.width-self.voiceIcon.frame.maxX-15, height: self.frame.height)
            self.textCarousel.frame = CGRect(x: 10, y: 0, width: width, height: self.frame.height)
            UIView.animate(withDuration: 6, delay: 0, usingSpringWithDamping: 0.8, initialSpringVelocity: 0.3, options: .curveLinear) {
                self.textCarousel.attributedText = text
            } completion: { finished in
                if finished {
                    DispatchQueue.main.async {
                        self.perform(#selector(self.remove), with: nil, afterDelay: 5)
                    }
                }
            }
        }
    }
    
    @objc private func remove() {
        UIView.animate(withDuration: 0.3) {
            self.alpha = 0
        } completion: { finished in
            if finished {
                self.removeFromSuperview()
            }
        }
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
}

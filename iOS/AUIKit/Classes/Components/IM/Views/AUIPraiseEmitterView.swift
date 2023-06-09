//
//  AUIPraiseEmitterView.swift
//  AgoraLyricsScore
//
//  Created by 朱继超 on 2023/5/15.
//

import UIKit


public class AUIPraiseEmitterView: UIView, CAAnimationDelegate {
    var images = [UIImage("finger_heart",.voiceRoom), UIImage("thunder",.voiceRoom), UIImage("thumbs_up",.voiceRoom), UIImage("No_of_the_beast",.voiceRoom), UIImage("lips",.voiceRoom), UIImage("heart",.voiceRoom)]

    var liveLayers = [CALayer]()

    var deleteLayers = [CALayer]()

    var count = UInt(0)

    override public init(frame: CGRect) {
        super.init(frame: frame)
    }
    
    convenience init(frame: CGRect,images: [UIImage]) {
        self.init(frame: frame)
        self.images = images
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    func setupEmitter() {
        self.count += 1
        if self.count == UInt.max {
            self.count = 0
        }
        var shipLayer: CALayer?
        if self.deleteLayers.count > 0 {
            shipLayer = self.deleteLayers.first
            self.deleteLayers.removeAll { $0 == shipLayer }
        } else {
            shipLayer = CALayer()
            let image = self.images[safe: Int(count) % images.count] ?? UIImage()
            shipLayer?.contents = image?.cgImage
            shipLayer?.contentsScale = UIScreen.main.scale
            shipLayer?.frame = CGRect(x: bounds.width / 2.0, y: bounds.height, width: 40, height: 40)
            shipLayer?.transform = CATransform3DRotate(CATransform3DIdentity, CGFloat(Double.pi / 2.0), 0, 0, 1)
        }
        shipLayer?.opacity = 1.0
        self.layer.addSublayer(shipLayer!)
        self.liveLayers.append(shipLayer!)
        self.animationKeyFrameWithLayer(layer: shipLayer!)
    }

    func animationKeyFrameWithLayer(layer: CALayer) {
        let width = bounds.width
        let height = bounds.height
        let path = UIBezierPath()
        path.move(to: CGPoint(x: width / 2.0, y: height))
        path.addCurve(to: CGPoint(x: CGFloat(arc4random()).truncatingRemainder(dividingBy: width), y: 0), controlPoint1: CGPoint(x: (CGFloat(arc4random()).truncatingRemainder(dividingBy: width)) / 2.0, y: (CGFloat(arc4random()).truncatingRemainder(dividingBy: height)) / 2.0), controlPoint2: CGPoint(x: (CGFloat(arc4random()).truncatingRemainder(dividingBy: width)) / 2.0 + width / 2.0, y: (CGFloat(arc4random()).truncatingRemainder(dividingBy: height)) / 2.0 + height / 2.0))

        let animation = CAKeyframeAnimation(keyPath: "position")
        animation.duration = 1.2 + (CGFloat(arc4random()).truncatingRemainder(dividingBy: 9) / 10.0)
        animation.rotationMode = .rotateAuto
        animation.timingFunction = CAMediaTimingFunction(name: CAMediaTimingFunctionName.easeIn)
        animation.path = path.cgPath
        animation.fillMode = .forwards

        let scaleAnimation = CABasicAnimation(keyPath: "transform.scale")
        scaleAnimation.fromValue = NSNumber(value: 1.1)
        scaleAnimation.toValue = NSNumber(value: 0.7)
        scaleAnimation.duration = 1 + ((CGFloat(arc4random()).truncatingRemainder(dividingBy: 10)) / 10.0)

        let alphaAnimation = CABasicAnimation(keyPath: "opacity")
        alphaAnimation.fromValue = NSNumber(value: 1.0)
        alphaAnimation.toValue = NSNumber(value: 0)
        alphaAnimation.duration = animation.duration
        alphaAnimation.fillMode = .forwards

        let animationGroup = CAAnimationGroup()
        animationGroup.repeatCount = 1
        animationGroup.isRemovedOnCompletion = false
        animationGroup.duration = animation.duration
        animationGroup.fillMode = .forwards
        animationGroup.animations = [animation, scaleAnimation, alphaAnimation]
        animation.delegate = self
        layer.add(animationGroup, forKey: "likeAnimation\(count)")
    }

    public func animationDidStop(_ anim: CAAnimation, finished flag: Bool) {
        let layer = self.liveLayers.first
        layer?.removeAllAnimations()
        self.deleteLayers.append(layer!)
        layer?.removeFromSuperlayer()
        self.liveLayers.removeAll { $0 == layer! }
    }
}


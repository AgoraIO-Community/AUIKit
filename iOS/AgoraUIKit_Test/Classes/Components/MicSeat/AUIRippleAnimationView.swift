//
//  AUIRippleAnimationImageView.swift
//  AUIKit
//
//  Created by 朱继超 on 2023/6/1.
//
import UIKit
import QuartzCore

// Statics methods for CAAnimation
public struct AUIAnimation {
    
    /// Animation by changing the opacity
    ///
    /// - Parameters:
    ///   - fromValue: the initial value of the animation
    ///   - toValue: the final value of the animation
    /// - Returns: a CABasicAnimation object
    public static func opacity(from fromValue: CGFloat, to toValue: CGFloat) -> CABasicAnimation {
        let opacityAnimation = CABasicAnimation(keyPath: "opacity")
        opacityAnimation.fromValue = fromValue
        opacityAnimation.toValue = toValue
        
        return opacityAnimation
    }
    
    /// Animation by changing the scale using transform
    ///
    /// - Parameters:
    ///   - fromValue: the initial value of the animation
    ///   - toValue: the final value o the animation
    /// - Returns: a CABasicAnimation object
    public static func transform(from fromValue: CGFloat = 1.0, to toValue: CGFloat) -> CABasicAnimation {
        let transformAnimation = CABasicAnimation(keyPath: "transform")
        transformAnimation.fromValue = NSValue(caTransform3D: CATransform3DMakeScale(fromValue, fromValue, fromValue))
        transformAnimation.toValue = NSValue(caTransform3D: CATransform3DMakeScale(toValue, toValue, toValue))
        
        return transformAnimation
    }
    
    /// Animation by changing the color
    ///
    /// - Parameters:
    ///   - fromColor: the initial color of the animation
    ///   - toColor: the final color of the animation
    /// - Returns: a CABasicAnimation
    public static func color(from fromColor: CGColor, to toColor: CGColor) -> CABasicAnimation {
        let colorAnimation = CABasicAnimation(keyPath: "strokeColor")
        colorAnimation.fromValue = fromColor
        colorAnimation.toValue = toColor
        colorAnimation.autoreverses = true
        
        return colorAnimation
    }
    
    /// Animation by changing the scale using transform
    ///
    /// - Parameters:
    ///   - times: An array of NSNumber objects that define the time at which to apply a given keyframe segment.
    ///   - values: An array of objects that specify the keyframe values to use for the animation.
    ///   - duration: the duration of the animation, the default value is 0.3
    /// - Returns: a CAKeyframeAnimation object
    public static func transform(times: [NSNumber] = [0.0, 0.5, 1.0], values: [CGFloat] = [0.0, 1.4, 1.0], duration: CFTimeInterval = 0.7) -> CAKeyframeAnimation {
        var transformValues = [NSValue]()
        values.forEach {
            transformValues.append(NSValue(caTransform3D: CATransform3DMakeScale($0, $0, 1.0)))
        }
        let transformAnimation = CAKeyframeAnimation(keyPath: "transform")
        transformAnimation.duration = duration
        transformAnimation.values = transformValues
        transformAnimation.keyTimes = times
        transformAnimation.fillMode = CAMediaTimingFillMode.forwards
        transformAnimation.isRemovedOnCompletion = false
        
        return transformAnimation
    }
    
    /// Animation to hide views, using transform and changing the scale to 0.0
    ///
    /// - Returns: a CAKeyframeAnimation object
    public static func hide() -> CAKeyframeAnimation {
        let hideAnimation = transform(times: [0.0, 0.3, 1.0], values: [1.0, 1.9, 0.0])
        hideAnimation.duration = 1.2
        return hideAnimation
    }
    
    /// Allows multiple animations to be grouped and run concurrently.
    ///
    /// - Parameters:
    ///   - animations: the list of animations
    ///   - duration: the animation duration
    /// - Returns: a CAAnimationGroup object
    public static func group(animations: CAAnimation..., duration: CFTimeInterval) -> CAAnimationGroup {
        let animationGroup = CAAnimationGroup()
        animationGroup.animations = animations
        animationGroup.duration = duration
        
        return animationGroup
    }
}

extension CGRect {
    var center: CGPoint {
        return CGPoint(x: midX, y: midY)
    }
}



/// A view with ripple animation

@IBDesignable
public class AUIRippleAnimationView: UIView {
    
    public var isAnimating: Bool = false
    
    // MARK: Private Properties
    
    /// the center circle used for the scale animation
    private var centerAnimatedLayer: CAShapeLayer!
    
    /// the center disk point
    private var diskLayer: CAShapeLayer!
    
    /// The duration to animate the central disk
    private var centerAnimationDuration: CFTimeInterval {
        return CFTimeInterval(self.animationDuration) * 0.90
    }
    
    /// The duration to animate one circle
    private var circleAnimationDuration: CFTimeInterval {
        if circlesLayer.count ==  0 {
            return CFTimeInterval(self.animationDuration)
        }
        return CFTimeInterval(self.animationDuration) / CFTimeInterval(self.circlesLayer.count)
    }
    
    /// The timer used to start / stop circles animation
    private var circlesAnimationTimer: Timer?
    
    /// The timer used to start / stop disk animation
    private var diskAnimationTimer: Timer?
    
    public var centerAnimatedLayerLineWidth: CGFloat = 2
    
    // MARK: Internal properties
    
    /// The maximum possible radius of circle
    var maxCircleRadius: CGFloat {
        if self.numberOfCircles == 0 {
            return min(self.bounds.midX, self.bounds.midY)
        }
        return (self.circlesPadding * CGFloat(self.numberOfCircles - 1) + self.minimumCircleRadius)
    }
    
    /// the circles surrounding the disk
    var circlesLayer = [CAShapeLayer]()
    
    /// The padding between circles
    var circlesPadding: CGFloat {
        if self.paddingBetweenCircles != -1 {
            return self.paddingBetweenCircles
        }
        let availableRadius = min(self.bounds.width, self.bounds.height)/2 - (self.minimumCircleRadius)
        return  availableRadius / CGFloat(self.numberOfCircles)
    }
    
    // MARK: Public Properties
    
    /// The radius of the disk in the view center, the default value is 5
    @IBInspectable public var diskRadius: CGFloat = 5 {
        didSet {
            self.redrawDisks()
            self.redrawCircles()
        }
    }
    
    /// The color of the disk in the view center, the default value is ripplePink color
    @IBInspectable public var diskColor: UIColor = UIColor(0x00FF95) {
        didSet {
            self.diskLayer.strokeColor = diskColor.cgColor
            self.centerAnimatedLayer.strokeColor = diskColor.cgColor
        }
    }
    
    /// The number of circles to draw around the disk, the default value is 3, if the forcedMaximumCircleRadius is used the number of drawn circles could be less than numberOfCircles
    @IBInspectable public var numberOfCircles: Int = 3 {
        didSet {
            redrawCircles()
        }
    }
    
    /// The padding between circles
    @IBInspectable public var paddingBetweenCircles: CGFloat = -1 {
        didSet {
            self.redrawCircles()
        }
    }
    
    /// The color of the off status of the circle, used for animation
    @IBInspectable public var circleOffColor: UIColor = .clear {
        didSet {
            self.circlesLayer.forEach {
                $0.strokeColor = self.circleOffColor.cgColor
            }
        }
    }
    
    /// The color of the on status of the circle, used for animation
    @IBInspectable public var circleOnColor: UIColor = .clear
    
    /// The minimum radius of circles, used to make space between the disk and the first circle, the radius must be grather than 5px , because if not the first circle will not be shown, the default value is 10, it's recommanded to use a value grather than the disk radius if you would like to show circles outside disk
    @IBInspectable public var minimumCircleRadius: CGFloat = 10 {
        didSet {
            if self.minimumCircleRadius < 5 {
                self.minimumCircleRadius = 5
            }
            self.redrawCircles()
        }
    }
    
    /// The duration of the animation, the default value is 0.9
    @IBInspectable public var animationDuration: CGFloat = 0.9 {
        didSet {
            self.stopAnimation()
            self.startAnimation()
        }
    }
    
    /// The bounds rectangle, which describes the view’s location and size in its own coordinate system.
    public override var bounds: CGRect {
        didSet {
            // the sublyers are based in the view size, so if the view change the size, we should redraw sublyers
            self.redrawDisks()
            self.redrawCircles()
        }
    }
    // MARK: init methods
    
    /// Initializes and returns a newly allocated view object with the specified frame rectangle.
    ///
    /// - Parameter frame: The frame rectangle for the view, measured in points.
    override public init(frame: CGRect) {
        super.init(frame: frame)
        
        self.setup()
    }
    
    /// Initializes and returns a newly allocated view object from data in a given unarchiver.
    ///
    /// - Parameter aDecoder: An unarchiver object.
    required public init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        
        self.setup()
    }
    
    func setup() {
        
        self.drawSublayers()
        self.animateSublayers()
    }
    
    // MARK: Drawing methods
    
    /// Calculate the radius of a circle by using its index
    ///
    /// - Parameter index: the index of the circle
    /// - Returns: the radius of the circle
    func radiusOfCircle(at index: Int) -> CGFloat {
        return (self.circlesPadding * CGFloat(index)) + self.minimumCircleRadius
    }
    
    /// Lays out subviews.
    override public func layoutSubviews() {
        super.layoutSubviews()
        
        self.diskLayer.position = self.bounds.center
        self.centerAnimatedLayer.position = self.bounds.center
        self.circlesLayer.forEach {
            $0.position = self.bounds.center
        }
    }
    
    /// Draws disks and circles
    private func drawSublayers() {
        self.drawDisks()
        self.redrawCircles()
    }
    
    /// Draw central disk and the disk for the central animation
    private func drawDisks() {
        self.diskLayer = AUILayerDrawer.diskLayer(radius: self.diskRadius, origin: self.bounds.center, color: self.diskColor.cgColor)
        self.layer.insertSublayer(self.diskLayer, at: 0)
        
        self.centerAnimatedLayer = AUILayerDrawer.diskLayer(radius: self.diskRadius, origin: self.bounds.center, color: self.diskColor.cgColor)
        self.centerAnimatedLayer.opacity = 0.3
        self.centerAnimatedLayer.lineWidth = self.centerAnimatedLayerLineWidth
        self.layer.addSublayer(self.centerAnimatedLayer)
    }
    
    /// Redraws disks by deleting the old ones and drawing a new ones, called for example when the radius changed
    private func redrawDisks() {
        self.diskLayer.removeFromSuperlayer()
        self.centerAnimatedLayer.removeFromSuperlayer()
        
        self.drawDisks()
    }

    /// Redraws circles by deleting old ones and drawing new ones, this method is called, for example, when the number of circles changed
     func redrawCircles() {
         self.circlesLayer.forEach {
            $0.removeFromSuperlayer()
        }
         self.circlesLayer.removeAll()
         for i in 0 ..< self.numberOfCircles {
             self.drawCircle(with: i)
        }
    }
    
    /// Draws the circle by using the index to calculate the radius
    ///
    /// - Parameter index: the index of the circle
    private func drawCircle(with index: Int) {
        let radius = self.radiusOfCircle(at: index)
        if radius > self.maxCircleRadius { return }
        
        let circleLayer = AUILayerDrawer.circleLayer(radius: radius, origin: bounds.center, color: circleOffColor.cgColor)
        circleLayer.lineWidth = 2.0
        self.circlesLayer.append(circleLayer)
        self.layer.addSublayer(circleLayer)
    }
    
    // MARK: Animation methods
    
    /// Add animation to central disk and the surrounding circles
    private func animateSublayers() {
        self.animateCentralDisk()
        self.animateCircles()
        
        self.startAnimation()
    }
    
    /// Animates the central disk by changing the opacitiy and the scale
    @objc private func animateCentralDisk() {
        let maxScale = self.maxCircleRadius / self.diskRadius
        let scaleAnimation = AUIAnimation.transform(to: maxScale)
        let alphaAnimation = AUIAnimation.opacity(from: 0.6, to: 0.0)
        let groupAnimation = AUIAnimation.group(animations: scaleAnimation, alphaAnimation, duration: centerAnimationDuration)
        self.centerAnimatedLayer.add(groupAnimation, forKey: nil)
        self.layer.addSublayer(self.centerAnimatedLayer)
    }
    
    /// Animates circles by changing color from off to on color
    @objc private func animateCircles() {
        for index in 0 ..< self.circlesLayer.count {
            let colorAnimation = AUIAnimation.color(from: circleOffColor.cgColor, to: self.circleOnColor.cgColor)
            colorAnimation.duration = self.circleAnimationDuration
            colorAnimation.autoreverses = true
            colorAnimation.beginTime = CACurrentMediaTime() + CFTimeInterval(self.circleAnimationDuration * Double(index))
            self.circlesLayer[index].add(colorAnimation, forKey: "strokeColor")
        }
    }
}

// MARK: Public methods
extension AUIRippleAnimationView {
    
    /// Start the ripple animation
    public func startAnimation() {
        layer.removeAllAnimations()
        self.circlesAnimationTimer?.invalidate()
        self.diskAnimationTimer?.invalidate()
        let timeInterval = CFTimeInterval(self.animationDuration) + self.circleAnimationDuration
        self.circlesAnimationTimer =  Timer.scheduledTimer(timeInterval: timeInterval, target: self, selector: #selector(animateCircles), userInfo: nil, repeats: true)
        self.diskAnimationTimer = Timer.scheduledTimer(timeInterval: timeInterval, target: self, selector: #selector(animateCentralDisk), userInfo: nil, repeats: true)
        self.isAnimating = true
    }
    
    /// Stop the ripple animation
    public func stopAnimation() {
        layer.removeAllAnimations()
        self.circlesAnimationTimer?.invalidate()
        self.diskAnimationTimer?.invalidate()
        self.isAnimating = false
    }
}

struct AUILayerDrawer {

    /// Creates a circular layer
    ///
    /// - Parameters:
    ///   - radius: the radius of the circle
    ///   - origin: the origin of the circle
    /// - Returns: a circular layer
    private static func layer(radius: CGFloat, origin: CGPoint) -> CAShapeLayer {
        let layer = CAShapeLayer()
        layer.bounds = CGRect(x: 0, y: 0, width: radius*2, height: radius*2)
        layer.position = origin
        
        let center = CGPoint(x: radius, y: radius)
        let path = UIBezierPath(arcCenter: center, radius: radius, startAngle: 0, endAngle: 2 * .pi, clockwise: true)
        layer.path = path.cgPath
        
        return layer
    }
    
    /// Creates a disk layer
    ///
    /// - Parameters:
    ///   - radius: the radius of the disk
    ///   - origin: the origin of the disk
    ///   - color: the color of the disk
    /// - Returns: a disk layer
    static func diskLayer(radius: CGFloat, origin: CGPoint, color: CGColor) -> CAShapeLayer {
        let diskLayer = self.layer(radius: radius, origin: origin)
        diskLayer.strokeColor = color
        diskLayer.fillColor = UIColor.clear.cgColor
        return diskLayer
    }
    
    /// Creates a circle layer
    ///
    /// - Parameters:
    ///   - radius: the radius of the circle
    ///   - origin: the origin of the circle
    ///   - color: the color of the circle
    /// - Returns: a circle layer
    static func circleLayer(radius: CGFloat, origin: CGPoint, color: CGColor) -> CAShapeLayer {
        let circleLayer = self.layer(radius: radius, origin: origin)
        circleLayer.fillColor = UIColor.clear.cgColor
        circleLayer.strokeColor = color
        circleLayer.lineWidth = 1.0
        
        return circleLayer
    }
}

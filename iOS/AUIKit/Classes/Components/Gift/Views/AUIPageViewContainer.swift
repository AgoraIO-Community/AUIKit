//
//  AUIPageViewContainer.swift
//  AUIKit
//
//  Created by 朱继超 on 2023/5/19.
//

import UIKit

public class AUIPageViewContainer: UIView, UIPageViewControllerDataSource, UIPageViewControllerDelegate {
    
    public var scrollClosure: ((Int) -> Void)?

    var controllers: [UIViewController]?

    var nextViewController: UIViewController?

    public var index = 0 {
        didSet {
            DispatchQueue.main.async {
                if let vc = self.controllers?[self.index] {
                    self.pageController.setViewControllers([vc], direction: .forward, animated: false)
                }
            }
        }
    }

    lazy var pageController: UIPageViewController = {
        let page = UIPageViewController(transitionStyle: .scroll, navigationOrientation: .horizontal, options: nil)
        page.view.backgroundColor = .clear
        page.dataSource = self
        page.delegate = self
        return page
    }()

    override public init(frame: CGRect) {
        super.init(frame: frame)
    }

    public convenience init(frame: CGRect, viewControllers: [UIViewController]) {
        self.init(frame: frame)
        self.controllers = viewControllers
        self.pageController.setViewControllers([viewControllers[0]], direction: .forward, animated: false)
        self.addSubview(self.pageController.view)
        self.pageController.view.translatesAutoresizingMaskIntoConstraints = false
        self.pageController.view.bottomAnchor.constraint(equalTo: bottomAnchor).isActive = true
        self.pageController.view.leftAnchor.constraint(equalTo: leftAnchor).isActive = true
        self.pageController.view.rightAnchor.constraint(equalTo: rightAnchor).isActive = true
        self.pageController.view.topAnchor.constraint(equalTo: topAnchor).isActive = true
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

public extension AUIPageViewContainer {
    func pageViewController(_ pageViewController: UIPageViewController, viewControllerBefore viewController: UIViewController) -> UIViewController? {
        self.controllers?[safe: index - 1]
    }

    func pageViewController(_ pageViewController: UIPageViewController, viewControllerAfter viewController: UIViewController) -> UIViewController? {
        self.controllers?[safe: index + 1]
    }

    func pageViewController(_ pageViewController: UIPageViewController, didFinishAnimating finished: Bool, previousViewControllers: [UIViewController], transitionCompleted completed: Bool) {
        if finished, self.controllers?.count ?? 0 > 0 {
            for (idx, vc) in self.controllers!.enumerated() {
                if vc == self.nextViewController {
                    self.index = idx
                    break
                }
            }
            if self.scrollClosure != nil {
                self.scrollClosure!(self.index)
            }
        } else {
            self.nextViewController = previousViewControllers.first
        }
    }

    func pageViewController(_ pageViewController: UIPageViewController, willTransitionTo pendingViewControllers: [UIViewController]) {
        self.nextViewController = pendingViewControllers.first
    }
}

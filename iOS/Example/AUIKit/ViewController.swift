//
//  ViewController.swift
//  AUIKit
//
//  Created by wushengtao on 05/26/2023.
//  Copyright (c) 2023 wushengtao. All rights reserved.
//

import UIKit

private let kTabbarNames = ["widgets", "components"]
class ViewController: UITabBarController {

    override func viewDidLoad() {
        super.viewDidLoad()
        
        let vc1 = WidgetsListViewController()
        vc1.view.backgroundColor = UIColor.white
        
        let vc2 = ComponentsListViewController()
        vc2.view.backgroundColor = UIColor.lightGray
        tabBar.backgroundColor = .gray.withAlphaComponent(0.5)
        viewControllers = [vc1, vc2]
        for (index, singleVC) in viewControllers!.enumerated() {
//            singleVC.tabBarItem.image = UIImage(systemName: "multiply.circle.fill")
//            singleVC.tabBarItem.selectedImage = UIImage(named: tabBarImageNames[index] + "_selected")
            singleVC.tabBarItem.title = kTabbarNames[index]
        }
    }
}

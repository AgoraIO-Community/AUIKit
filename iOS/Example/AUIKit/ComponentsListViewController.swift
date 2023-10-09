//
//  ComponentsListViewController.swift
//  AUIKit_Example
//
//  Created by wushengtao on 2023/8/28.
//  Copyright © 2023 CocoaPods. All rights reserved.
//

import UIKit
import AUIKitCore
import SwiftTheme
import SDWebImage

class ComponentsListViewController: UIViewController,UITableViewDelegate,UITableViewDataSource {
    private var datas = ["Chat&Gift"/*,"MicSeat","Service"*/]
    
    private var controllers: [UIViewController] = [ChatListEffectViewController(),
                                                   MicSeatViewController(),
                                                   TestServiceViewController()
    ]
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.view.addSubview(self.functionList)
    }
    
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        self.datas.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "function", for: indexPath)
        cell.textLabel?.text = self.datas[safe:indexPath.row] ?? ""
        cell.selectionStyle = .none
        cell.accessoryType = .disclosureIndicator
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
        if let vc = self.controllers[safe: indexPath.row] {
            vc.title = self.datas[safe: indexPath.row] ?? ""
            self.navigationController?.pushViewController(vc, animated: true)
        }
    }
    
    lazy var functionList: UITableView = {
        UITableView(frame: CGRect(x: 0, y: 0, width: AScreenWidth, height: AScreenHeight), style: .plain).delegate(self).dataSource(self).rowHeight(50).registerCell(UITableViewCell.self, forCellReuseIdentifier: "function").tableFooterView(UIView())
    }()
}

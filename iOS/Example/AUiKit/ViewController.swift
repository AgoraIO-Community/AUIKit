//
//  ViewController.swift
//  AUIKit
//
//  Created by wushengtao on 05/25/2023.
//  Copyright (c) 2023 wushengtao. All rights reserved.
//

import UIKit
import AUIKit
import SwiftTheme

class ViewController: UIViewController {
    
    private let scrollView: UIScrollView = UIScrollView()
    private lazy var rightItem: UIBarButtonItem = {
        let item = UIBarButtonItem(title: "更换主题", style: .plain, target: self, action: #selector(didClickRightBarButtonItem))
        return item
    }()
    
    private let themeNames = ["Bright","Dark"]
    private var currentIndex = 0

    override func viewDidLoad() {
        super.viewDidLoad()
        
        //设置皮肤路径
        if let folderPath = Bundle.main.path(forResource: "exampleTheme", ofType: "bundle") {
            AUIRoomContext.shared.addThemeFolderPath(path: URL(fileURLWithPath: folderPath) )
        }
        self.title = "基础组件"
        self.navigationItem.rightBarButtonItem = rightItem
        scrollView.frame = view.bounds
        view.addSubview(scrollView)
        
        addButtons()
        addDividers()
        addSliders()
        addTextFileds()
        addTabs()
        addAlertViews()
        addToasts()
        
        layoutScrollView()
    }
    
    @objc private func didClickRightBarButtonItem(){
        AUIRoomContext.shared.switchThemeToNext()
//        currentIndex += 1
//        switchTheme(themeName: themeNames[currentIndex % themeNames.count])
    }
    
    private func layoutScrollView(){
        var y: CGFloat = 0
        scrollView.subviews.forEach { subview in
            subview.frame = CGRect(x: 0, y: y, width: subview.bounds.width, height: subview.bounds.height)
            y += subview.bounds.height
        }
        scrollView.contentSize = CGSize(width: view.bounds.width, height: y)
    }
    
    private func _addTitle(_ title: String) {
        let label = UILabel()
        label.text = title
        label.bounds = CGRect(x: 0, y: 0, width: view.bounds.width, height: 30)
        scrollView.addSubview(label)
    }

    private func addButtons(){
        _addTitle("Buttons")
        let button = createButton()
        scrollView.addSubview(button)
    }
    
    private func addDividers(){
        _addTitle("Dividers")
    }
    
    private func addSliders(){
        _addTitle("Sliders")
        let slider = AUISlider()
        let theme = AUISliderNativeTheme()
        theme.backgroundColor = .gray
        theme.minimumTrackColor = .aui_blue
        theme.maximumTrackColor = .aui_grey
        theme.thumbColor = .aui_primary
        theme.thumbBorderColor = .red
        theme.trackBigLabelFont = .aui_big
        theme.trackSmallLabelFont = .aui_small
        theme.trackLabelColor = .aui_black
        theme.titleLabelFont = .aui_big
        theme.titleLabelColor = .aui_black
        scrollView.addSubview(slider)
    }
    
    private func addTextFileds(){
        _addTitle("TextFileds")
        let tf = AUITextField()
        tf.backgroundColor = .purple
        tf.bottomText = "底部文本"
        tf.bottomTextColor = .red
        tf.placeHolder = "请输入"
        tf.bounds = CGRect(x: 0, y: 0, width: 200, height: 80)
        scrollView.addSubview(tf)
    }
    
    private func addTabs(){
        _addTitle("Tabs")
        let tabs = AUITabs(frame: CGRect(x: 0, y: 0, width: view.bounds.width, height: 50), segmentStyle: AUITabsStyle(), titles: ["首页","消息","音乐"])
        scrollView.addSubview(tabs)
        let tabs2 = AUITabs(frame: CGRect(x: 0, y: 0, width: view.bounds.width, height: 50), segmentStyle: AUITabsStyle(), titles: ["首页","消息","音乐"])
        scrollView.addSubview(tabs2)
        let tabs3 = AUITabs(frame: CGRect(x: 0, y: 0, width: view.bounds.width, height: 50), segmentStyle: AUITabsStyle(), titles: ["首页","消息","音乐","我的"])
        scrollView.addSubview(tabs3)
        let tabs4 = AUITabs(frame: CGRect(x: 0, y: 0, width: view.bounds.width, height: 50), segmentStyle: AUITabsStyle(), titles: ["首页","消息","音乐"])
        scrollView.addSubview(tabs4)
    }
    
    private func addAlertViews(){
        _addTitle("AlertViews")
        for i in 0...10 {
            let button = UIButton(type: .system)
            button.bounds = CGRect(x: 0, y: 0, width: 100, height: 50)
            button.setTitle("显示弹窗\(i + 1)", for: .normal)
            scrollView.addSubview(button)
        }
    }
    
    private func addToasts(){
        _addTitle("Toasts")
        for i in 0...5 {
            let button = UIButton(type: .system)
            button.bounds = CGRect(x: 0, y: 0, width: 100, height: 50)
            button.setTitle("显示Toast\(i + 1)", for: .normal)
            scrollView.addSubview(button)
        }
    }
}


extension ViewController {
    override func touchesEnded(_ touches: Set<UITouch>, with event: UIEvent?) {
        view.endEditing(true)
    }
}

extension ViewController {
    func createButton()-> AUIButton {
        let theme = AUIButtonDynamicTheme.appearanceTheme(appearance:"AppearanceButtonMin")
//        theme.buttonWitdth = "SeatItem.micRoleButtonWidth"
//        theme.buttonHeight = "SeatItem.micRoleButtonHeight"
        theme.icon = auiThemeImage("Buttons.user")
//        theme.selectedIcon = "SeatItem.micSeatItemIconCoSinger"
//        theme.titleFont = "CommonFont.small"
//        theme.padding = "SeatItem.padding"
//        theme.iconWidth = "SeatItem.micRoleButtonIconWidth"
//        theme.iconHeight = "SeatItem.micRoleButtonIconHeight"
//        theme.backgroundColor = "CommonColor.primary"
//        theme.cornerRadius = nil
        let button = AUIButton()
        button.textImageAlignment = .imageLeftTextRight
        button.style = theme
        button.setTitle("主唱", for: .normal)
        button.setTitle("副唱", for: .selected)
        return button
    }
}

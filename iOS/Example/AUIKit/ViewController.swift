//
//  ViewController.swift
//  AUIKit
//
//  Created by wushengtao on 05/26/2023.
//  Copyright (c) 2023 wushengtao. All rights reserved.
//

import UIKit
import AUIKit
import SwiftTheme
import SDWebImage

class ViewController: UIViewController {
    
    private let scrollView: UIScrollView = UIScrollView()
    private let titleLabel: UILabel = {
        let label = UILabel()
        label.bounds = CGRectMake(0, 0, 100, 30)
        label.text = "基础组件"
        label.font = UIFont.boldSystemFont(ofSize: 17)
        label.theme_textColor = AUIColor("ExampleMainColor.normalTextColor")
        return label
    }()
    private lazy var rightItem: UIBarButtonItem = {
        let item = UIBarButtonItem(title: "更换主题", style: .plain, target: self, action: #selector(didClickRightBarButtonItem))
        return item
    }()
    

    override func viewDidLoad() {
        super.viewDidLoad()
        //设置皮肤路径
        if let folderPath = Bundle.main.path(forResource: "exampleTheme", ofType: "bundle") {
            AUIRoomContext.shared.addThemeFolderPath(path: URL(fileURLWithPath: folderPath) )
        }
        
        self.navigationItem.titleView = self.titleLabel
        self.navigationItem.rightBarButtonItem = rightItem
        view.theme_backgroundColor = AUIColor("ExampleMainColor.background")
        
        addButtons()
        /*
        addDividers()
        addSliders()
        addTextFileds()
        addTabs()
        addAlertViews()
        addToasts()
        */

        layoutScrollView()
    }
    
    @objc private func didClickRightBarButtonItem(){
        AUIRoomContext.shared.switchThemeToNext()
    }
    
    private func layoutScrollView(){
        scrollView.frame = view.bounds
        view.addSubview(scrollView)
        
        var y: CGFloat = 0
        scrollView.subviews.forEach { subview in
            subview.frame = CGRect(x: 0, y: y, width: subview.bounds.width, height: subview.bounds.height)
            y += subview.bounds.height + 20
        }
        scrollView.contentSize = CGSize(width: view.bounds.width, height: y)
    }
    
    private func _addTitle(_ title: String) {
        let label = UILabel()
        label.text = title
        label.theme_textColor = AUIColor("ExampleMainColor.normalTextColor")
        label.bounds = CGRect(x: 0, y: 0, width: view.bounds.width, height: 30)
        scrollView.addSubview(label)
    }

    private func addButtons(){
        _addTitle("Buttons")
        func addCommonButtons(){
            let searchIcon = auiThemeImageURL("CustomButton.search")
            let userNormalIcon = auiThemeImageURL("CustomButton.user_normal")
            let loadingIcon = auiThemeImageURL("CustomButton.loading")
            let chatIcon = auiThemeImageURL("CustomButton.chat")
            
            let button = createBigButton(icon: searchIcon)
            let normalButton = createBigButton(icon:userNormalIcon)
            let loadingButton = createBigButton(icon: loadingIcon)
            let dangerButton = createBigButton(icon: userNormalIcon, bgColor: "CommonColor.danger")
            let disableButton = createBigButton(icon: userNormalIcon)
            disableButton.isEnabled = false
            addCommonButtonRow(buttons: [button, normalButton,loadingButton,dangerButton, disableButton])
            
            let minibutton = createSmallButton()
            let mininormalButton = createSmallButton(icon: userNormalIcon)
            let miniloadingButton = createSmallButton(icon: loadingIcon)
            let minidangerButton = createSmallButton(icon: userNormalIcon, bgColor: "CommonColor.danger")
            let minidisableButton = createSmallButton(icon: userNormalIcon)
            minidisableButton.isEnabled = false
            addCommonButtonRow(buttons: [minibutton, mininormalButton, miniloadingButton, minidangerButton, minidisableButton])
            
            let circlebutton = createCircleButton(icon: chatIcon)
            let circleloadingButton = createCircleButton(icon: loadingIcon)
            let circleDisableButton = createCircleButton(icon: chatIcon)
            circleDisableButton.isEnabled = false
            addCommonButtonRow(buttons: [circlebutton, circleloadingButton, circleDisableButton])
        }
           
        func addStrokeButtons(){
            let userNormalIconStr = auiThemeImageURL("CustomButton.user_normal_stroke")
            let userDangerIconStr = auiThemeImageURL("CustomButton.user_danger")
            let chatIconStr = auiThemeImageURL("CustomButton.chat_stroke")
            let loadingIcon = auiThemeImageURL("CustomButton.loading_stroke")

            let button = createBigStrokeButton()
            let normalButton = createBigStrokeButton(icon: userNormalIconStr)
            let loadingButton = createBigStrokeButton(icon: loadingIcon)
            let dangerButton = createBigStrokeButton(icon: userDangerIconStr, tintColor: "CommonColor.danger")
            let disableButton = createBigStrokeButton(icon: userNormalIconStr)
            disableButton.isEnabled = false
            addCommonButtonRow(buttons: [button, normalButton,loadingButton, dangerButton, disableButton])
            
            let minibutton = createSmallStrokeButton()
            let mininormalButton = createSmallStrokeButton(icon: userNormalIconStr)
            let miniloadingButton = createSmallStrokeButton(icon: loadingIcon)
            let minidangerButton = createSmallStrokeButton(icon: userDangerIconStr, tintColor: "CommonColor.danger")
            let minidisableButton = createSmallStrokeButton(icon: userNormalIconStr)
            minidisableButton.isEnabled = false
            addCommonButtonRow(buttons: [minibutton, mininormalButton, miniloadingButton, minidangerButton, minidisableButton])
            
            let circlebutton = createCircleStrokeButton(icon: chatIconStr)
            let circleloadingButton = createCircleStrokeButton(icon: loadingIcon)
            let circleDisableButton = createCircleStrokeButton(icon: chatIconStr)
            circleDisableButton.isEnabled = false
            addCommonButtonRow(buttons: [circlebutton, circleloadingButton, circleDisableButton])
        }
        addCommonButtons()
        addStrokeButtons()
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

extension AUIButtonDynamicTheme {
    func setTheme(icon: ThemePicker?, iconWidth: String, iconHeight: String) {
        guard let icon = icon else {
            self.iconWidth = "CustomButton.iconWidthNone"
            self.iconHeight = "CustomButton.iconHeightNone"
            return
        }
        self.iconWidth = ThemeCGFloatPicker(keyPath: iconWidth)
        self.iconHeight = ThemeCGFloatPicker(keyPath: iconHeight)
        self.icon = icon
    }
}

extension ViewController {
    func createButton(title:String? = nil, theme: AUIButtonDynamicTheme)-> AUIButton {
        let button = AUIButton()
        button.textImageAlignment = .imageLeftTextRight
        button.style = theme
        if let title = title, title.count > 0 {
            button.setTitle(title, for: .normal)
        }
        return button
    }
    
    func createBigButton(icon: ThemePicker? = nil, title: String? = nil, bgColor: String? = nil)-> AUIButton {
        let theme = AUIButtonDynamicTheme.appearanceTheme(appearance:"Button")
        theme.backgroundColor = bgColor != nil ? AUIColor(bgColor!) : AUIColor("ExampleMainColor.primary")
        theme.disabledBackgroundColor = AUIColor("ExampleMainColor.disableBackground")
        theme.highlightedBackgroundColor = AUIColor("ExampleMainColor.hightLight")
        theme.setTheme(icon: icon, iconWidth: "CustomButton.iconBigWidth", iconHeight: "CustomButton.iconBigHeight")
        return createButton(title: "Primary",theme: theme)
    }
    
    func createSmallButton(icon: ThemePicker? = nil, title: String? = nil, bgColor: String? = nil)-> AUIButton {
        let theme = AUIButtonDynamicTheme.appearanceTheme(appearance:"AppearanceButtonMin")
        theme.backgroundColor = bgColor != nil ? AUIColor(bgColor!) : AUIColor("ExampleMainColor.primary")
        theme.disabledBackgroundColor = AUIColor("ExampleMainColor.disableBackground")
        theme.highlightedBackgroundColor = AUIColor("ExampleMainColor.hightLight")
        theme.setTheme(icon: icon, iconWidth: "CustomButton.iconSmallWidth", iconHeight: "CustomButton.iconSmallHeight")
        return createButton(title: "Primary",theme: theme)
    }
    
    func createCircleButton(icon: ThemePicker? = nil)-> AUIButton {
        let theme = AUIButtonDynamicTheme.appearanceTheme(appearance:"AppearanceButtonCircle")
        theme.disabledBackgroundColor = AUIColor("ExampleMainColor.primary")
        theme.backgroundColor = AUIColor("ExampleMainColor.primary")
        theme.highlightedBackgroundColor = AUIColor("ExampleMainColor.hightLight")
        theme.setTheme(icon: icon, iconWidth: "CustomButton.iconCircleWidth", iconHeight: "CustomButton.iconCircleHeight")
        return createButton(theme: theme)
    }
    
    func createBigStrokeButton(icon: ThemePicker? = nil, title: String? = nil, tintColor: String? = nil)-> AUIButton {
        let theme = AUIButtonDynamicTheme.appearanceTheme(appearance:"AppearanceButtonStroke")
        theme.backgroundColor = AUIColor("ExampleMainColor.strokeBackgroud")
        theme.disabledBackgroundColor = AUIColor("ExampleMainColor.strokeBackgroud")
        theme.disabledTitleColor = AUIColor("ExampleMainColor.disableBorderColor")
        theme.disabledBorderColor = AUICGColor("ExampleMainColor.disableBorderColor")
        theme.highlightedBorderColor = AUICGColor("ExampleMainColor.hightLight")
        theme.highlightedTitleColor = AUIColor("ExampleMainColor.hightLight")
        if let tintColor = tintColor {
            theme.borderColor = AUICGColor(tintColor)
            theme.titleColor = AUIColor(tintColor)
        }else{
            theme.borderColor = AUICGColor("ExampleMainColor.borderColor")
            theme.titleColor = AUIColor("ExampleMainColor.borderColor")
        }
        theme.setTheme(icon: icon, iconWidth: "CustomButton.iconBigWidth", iconHeight: "CustomButton.iconBigHeight")
        let button = createButton(title: "Primary", theme: theme)
        button.layer.borderWidth = 1
        return button
    }
    
    func createSmallStrokeButton(icon: ThemePicker? = nil, title: String? = nil, tintColor: String? = nil)-> AUIButton {
        let theme = AUIButtonDynamicTheme.appearanceTheme(appearance:"AppearanceButtonMinStroke")
        theme.backgroundColor = AUIColor("ExampleMainColor.strokeBackgroud")
        theme.disabledBackgroundColor = AUIColor("ExampleMainColor.strokeBackgroud")
        theme.disabledTitleColor = AUIColor("ExampleMainColor.disableBorderColor")
        theme.disabledBorderColor = AUICGColor("ExampleMainColor.disableBorderColor")
        theme.setTheme(icon: icon, iconWidth: "CustomButton.iconSmallWidth", iconHeight: "CustomButton.iconSmallHeight")
        theme.highlightedBorderColor = AUICGColor("ExampleMainColor.hightLight")
        theme.highlightedTitleColor = AUIColor("ExampleMainColor.hightLight")
        if let tintColor = tintColor {
            theme.borderColor = AUICGColor(tintColor)
            theme.titleColor = AUIColor(tintColor)
        }else{
            theme.borderColor = AUICGColor("ExampleMainColor.borderColor")
            theme.titleColor = AUIColor("ExampleMainColor.borderColor")
        }
        let button = createButton(title: "Primary", theme: theme)
        button.layer.borderWidth = 1
        return button
    }
    
    func createCircleStrokeButton(icon: ThemePicker? = nil)-> AUIButton {
        let theme = AUIButtonDynamicTheme.appearanceTheme(appearance:"AppearanceButtonCircleStroke")
        theme.iconWidth = "CustomButton.iconCircleWidth"
        theme.iconHeight = "CustomButton.iconCircleHeight"
        theme.disabledBackgroundColor = AUIColor("ExampleMainColor.strokeBackgroud")
        theme.backgroundColor = AUIColor("ExampleMainColor.strokeBackgroud")
        theme.borderColor = AUICGColor("ExampleMainColor.borderColor")
        theme.highlightedBorderColor = AUICGColor("ExampleMainColor.hightLight")
        theme.highlightedTitleColor = AUIColor("ExampleMainColor.hightLight")
        theme.highlightedBackgroundColor = AUIColor("ExampleMainColor.strokeBackgroud")
        theme.setTheme(icon: icon, iconWidth: "CustomButton.iconCircleWidth", iconHeight: "CustomButton.iconCircleHeight")
        let button = createButton(theme: theme)
        button.layer.borderWidth = 1
        return button
    }
    
    
    func addCommonButtonRow(buttons:[AUIButton]) {
        let buttonScrollView = UIScrollView()
        for button in buttons {
            buttonScrollView.addSubview(button)
        }
        
        var x: CGFloat = 0
        var height: CGFloat = 0
        buttonScrollView.subviews.forEach { subview in
            subview.frame = CGRect(x: x, y: 0, width: subview.bounds.width, height: subview.bounds.height)
            x += subview.bounds.width + 5
            height = max(height, subview.bounds.height)
        }
        buttonScrollView.showsHorizontalScrollIndicator = false
        buttonScrollView.contentSize = CGSize(width: x, height: height)
        buttonScrollView.bounds = CGRect(x: 0, y: 0, width: view.bounds.width, height: height)
        scrollView.addSubview(buttonScrollView)
    }
}

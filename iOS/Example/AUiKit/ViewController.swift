//
//  ViewController.swift
//  AUiKit
//
//  Created by wushengtao on 05/04/2023.
//  Copyright (c) 2023 wushengtao. All rights reserved.
//

import UIKit
import AUiKit

class ViewController: UIViewController {
    
    lazy var bg: UIImageView = {
        UIImageView(frame: self.view.frame).image(UIImage(named: "lbg"))
    }()
    
    lazy var testIMView: AUIRoomChatView = {
        AUIRoomChatView(frame: CGRect(x: 0, y: AScreenHeight - CGFloat(ABottomBarHeight) - (AScreenHeight / 667) * 210 - 50, width: AScreenWidth, height: (AScreenHeight / 667) * 210))
    }()
    
    var datas: [AUIChatFunctionBottomEntity] {
        var entities = [AUIChatFunctionBottomEntity]()
        let names = ["ellipsis_vertical","mic_slash","gift_color","thumb_up_color"]
        for i in 0...3 {
            let entity = AUIChatFunctionBottomEntity()
            entity.selected = false
            entity.selectedImage = UIImage(named: "")
            entity.normalImage = UIImage(named: names[i])
            entity.index = i
            entities.append(entity)
        }
        return entities
    }
    
    lazy var testBottomBar: AUIRoomBottomFunctionBar = {
        AUIRoomBottomFunctionBar(frame: CGRect(x: 0, y: AScreenHeight - CGFloat(ABottomBarHeight) - 50, width: AScreenWidth, height: 50), datas: self.datas, hiddenChat: false)
    }()
    
    lazy var testInputBar: AUIChatInputBar = {
        AUIChatInputBar(frame: CGRect(x: 0, y: AScreenHeight, width: AScreenWidth, height: 60)).backgroundColor(.white)
    }()

    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view, typically from a nib.
        self.view.addSubViews([self.bg,self.testIMView,self.testBottomBar,self.testInputBar])
        self.testInputBar.isHidden = true
        self.testIMView.messages?.append(self.startMessage(nil))
        self.testIMView.chatView.reloadData()
        self.testBottomBar.actionClosure = { [weak self] in
            guard let `self` = self,let idx = $0.index else { return }
            switch idx {
            case 3:
                self.testIMView.showLikeAnimation()
            default:
                break
            }
        }
        self.testBottomBar.raiseKeyboard = { [weak self] in
            self?.testInputBar.isHidden = false
            self?.testInputBar.inputField.becomeFirstResponder()
        }
        self.testInputBar.sendClosure = { [weak self] in
            self?.testIMView.messages?.append((self?.startMessage($0))!)
            self?.testIMView.chatView.reloadData()
        }
    }
    
    func startMessage(_ text: String?) -> AUIChatEntity {
        let entity = AUIChatEntity()
        entity.userName = "owner"
        entity.content = text == nil ? "Welcome to the voice chat room! Pornography, gambling or violence is strictly prohibited in the room.":text
        entity.attributeContent = entity.attributeContent
        entity.chatId = "123"
        entity.width = entity.width
        entity.height = entity.height
        entity.joined = false
        return entity
    }
    
    func showTabs() {
        
    }
    
    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        self.testInputBar.hiddenInputBar()
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

}


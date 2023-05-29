//
//  ViewController.swift
//  AUIKit
//
//  Created by wushengtao on 05/25/2023.
//  Copyright (c) 2023 wushengtao. All rights reserved.
//

import UIKit
import AUIKit

let giftMap = [["gift_id": "AUIKitGift1", "gift_name": "Sweet Heart", "gift_price": "1", "gift_count": "1", "selected": true], ["gift_id": "AUIKitGift2", "gift_name": "Flower", "gift_price": "5", "gift_count": "1", "selected": false], ["gift_id": "AUIKitGift3", "gift_name": "Crystal Box", "gift_price": "10", "gift_count": "1", "selected": false], ["gift_id": "AUIKitGift4", "gift_name": "Super Agora", "gift_price": "20", "gift_count": "1", "selected": false], ["gift_id": "AUIKitGift5", "gift_name": "Star", "gift_price": "50", "gift_count": "1", "selected": false], ["gift_id": "AUIKitGift6", "gift_name": "Lollipop", "gift_price": "100", "gift_count": "1", "selected": false], ["gift_id": "AUIKitGift7", "gift_name": "Diamond", "gift_price": "500", "gift_count": "1", "selected": false], ["gift_id": "AUIKitGift8", "gift_name": "Crown", "gift_price": "1000", "gift_count": "1", "selected": false], ["gift_id": "AUIKitGift9", "gift_name": "Rocket", "gift_price": "1500", "gift_count": "1", "selected": false]]

class ViewController: UIViewController,AUIMManagerRespDelegate {
    func messageDidReceive(roomId: String, message: AgoraChatTextMessage) {
        
    }
    
    func onUserDidJoinRoom(roomId: String, user: AUIUserThumbnailInfo) {
        
    }
    
    
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
            entity.selectedImage = nil
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
    
    lazy var gifts: AUIGiftsView = {
        AUIGiftsView(frame: CGRect(x: 0, y: 0, width: Int(AScreenWidth), height: 390-ABottomBarHeight-50), gifts: self.giftList()).backgroundColor(.clear)
    }()
    
    lazy var giftsContainer: AUITabsPageContainer = {
        AUITabsPageContainer(frame: CGRect(x: 0, y: 0, width: Int(AScreenWidth), height: 390), barStyle: AUITabsStyle(), containers: [self.gifts], titles: ["Gifts"])
    }()
    
    lazy var response: AUIIMManagerServiceImplement = {
        AUIIMManagerServiceImplement()
    }()
    
    lazy var giftRequest = AUIGiftServiceImplement()

    override func viewDidLoad() {
        super.viewDidLoad()
        AUIRoomContext.shared.switchTheme (themeName: "UIKit" )
        // Do any additional setup after loading the view, typically from a nib.
        self.view.addSubViews([self.bg,self.testIMView,self.testBottomBar,self.testInputBar,])
    
        self.testInputBar.isHidden = true
        self.testIMView.messages?.append(self.startMessage(nil))
        self.testIMView.chatView.reloadData()
        self.testBottomBar.actionClosure = { [weak self] in
            guard let `self` = self,let idx = $0.index else { return }
            switch idx {
            case 3:
                self.testIMView.showLikeAnimation()
            default:
                self.showTabs()
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
        let appId = "8bcda27385ca4eeba3affcae55f55fe4"
        let user = AUIUserThumbnailInfo()
        user.userId = "z18811508778"
        user.userName = "zjc"
//        self.response.configIM(appKey: "1129210531094378#auikit-voiceroom", user: user) { [weak self] error in
//            
//        }
        self.giftRequest.giftsFromService(roomId: "") { tabs, error in
            
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
        let theme = AUICommonDialogTheme()
        theme.contentControlColor = .pickerWithUIColors([UIColor.white])
        AUICommonDialog.show(contentView: self.giftsContainer,theme: theme)
    }
    
    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        self.testInputBar.hiddenInputBar()
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

    
    func giftList() -> [AUIGiftEntity] {
        var gifts = [AUIGiftEntity]()
        for dic in giftMap {
            guard let entity = AUIGiftEntity.yy_model(with: dic) else { continue }
            gifts.append(entity)
        }
        return gifts
    }
}


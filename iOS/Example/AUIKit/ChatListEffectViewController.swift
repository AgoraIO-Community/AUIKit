//
//  ChatListEffectViewController.swift
//  AUIKit_Example
//
//  Created by 朱继超 on 2023/8/15.
//  Copyright © 2023 CocoaPods. All rights reserved.
//

import UIKit
import AUIKitCore

fileprivate let chatMinY = AScreenHeight/2.0

final class ChatListEffectViewController: UIViewController {
    
    
    lazy var background: UIImageView = {
        UIImageView(frame: self.view.bounds).image(UIImage(named: "lbg"))
    }()
    
    lazy var messageView: AUIChatListView = {
        AUIChatListView(frame: CGRect(x: 0, y: chatMinY, width: AScreenWidth, height: self.view.frame.height-65))
    }()
    
    lazy var emitter: AUIPraiseEffectView = {
        AUIPraiseEffectView(frame: CGRect(x: AScreenWidth - 80, y: chatMinY, width: 80, height: self.view.frame.height - 70),images: []).backgroundColor(.clear)
    }()
    
    lazy var bottomBar: AUIRoomBottomFunctionBar = {
        AUIRoomBottomFunctionBar(frame: CGRect(x: 0, y:AScreenHeight-70, width: AScreenWidth, height: 54), datas: self.updateBottomBarDatas(onMic: true), hiddenChat: false)
    }()
    
    lazy var inputBar: AUIChatInputBar = {
        AUIChatInputBar(frame: CGRect(x: 0, y: AScreenHeight, width: AScreenWidth, height: 60),config: AUIChatInputBarConfig()).backgroundColor(.white)
    }()
    
    private lazy var receiveGift: AUIGiftBarrageView = {
        AUIGiftBarrageView(frame: CGRect(x: 10, y: self.messageView.frame.minY - (AScreenWidth / 9.0 * 2.5), width: AScreenWidth / 3.0 * 2 + 20, height: AScreenWidth / 9.0 * 2.5),source: nil).backgroundColor(.clear).tag(1111)
    }()

    private lazy var giftsView: AUIRoomGiftDialog = {
        AUIRoomGiftDialog(frame: CGRect(x: 0, y: 0, width: AScreenWidth, height: 390), tabs: [])
    }()

    private lazy var invitationView: AUIInvitationView = AUIInvitationView(frame: CGRect(x: 0, y: 0, width: AScreenWidth, height: 380))
    
    private lazy var applyView: AUIApplyView = AUIApplyView(frame: CGRect(x: 0, y: 0, width: AScreenWidth, height: 380))
    
    
    private lazy var moreActions: AUIMoreOperationView = AUIMoreOperationView(frame: CGRect(x: 0, y: 50, width: AScreenWidth, height: 360), datas: self.moreDatas)
    
    private var moreDatas: [AUIMoreOperationCellEntity] {
        [AUIMoreOperationCellEntity(),self.inviteEntity]
    }
    
    private var inviteEntity: AUIMoreOperationCellEntity {
        let entity = AUIMoreOperationCellEntity()
        entity.index = 1
        entity.operationName = "邀请列表"
        return entity
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.loadTheme()
        self.view.addSubViews([self.background,self.messageView,self.receiveGift,self.bottomBar,self.emitter,self.inputBar])
//        getWindow()?.addSubview(self.inputBar)
        // Do any additional setup after loading the view.
        self.bottomBarEvents()
        self.messageView.showNewMessage(entity:self.startMessage("欢迎使用AUIKit"))
        self.parserGifts()
        self.giftsView.addActionHandler(actionHandler: self)
        self.moreActions.addActionHandler(actionHandler: self)
    }
    
    
    /*
     // MARK: - Navigation
     
     // In a storyboard-based application, you will often want to do a little preparation before navigation
     override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
     // Get the new view controller using segue.destination.
     // Pass the selected object to the new view controller.
     }
     */
    
}


extension ChatListEffectViewController:AUIRoomGiftDialogEventsDelegate,AUIMoreOperationViewEventsDelegate {
    
    func onItemSelected(entity: AUIKitCore.AUIMoreOperationCellDataProtocol) {
        switch entity.index {
        case 0: self.showApply()
        case 1: self.showInvite()
        default:
            break
        }
    }
    
    
    func showApply() {
        AUICommonDialog.hidden()
        self.applyView.refreshUsers(users: UserInfo.users)
        AUICommonDialog.show(contentView: self.applyView,theme: AUICommonDialogTheme())
    }
    
    func showInvite() {
        AUICommonDialog.hidden()
        self.applyView.refreshUsers(users: UserInfo.users)
        AUICommonDialog.show(contentView: self.applyView,theme: AUICommonDialogTheme())
    }
    
    func showMoreTabs() {
        AUICommonDialog.hidden()
        let theme = AUICommonDialogTheme()
        AUICommonDialog.show(contentView: self.moreActions,theme: theme)
    }
    
    func muteLocal() {
        guard let entity = self.bottomBar.datas[safe: 1] else {
            return
        }
//        entity.showRedDot = false
//        self.updateBottomBarState(onMic: entity.showRedDot)
//        self.bottomBar.refreshToolBar(datas: self.updateBottomBarDatas(onMic: entity.selected))
        entity.selected = !entity.selected
        self.updateBottomBarRedDot(index: 1, show: false)
    }

    @objc public func updateBottomBarRedDot(index: Int,show: Bool) {
        self.bottomBar.datas[safe: index]?.showRedDot = show
        self.bottomBar.toolBar.reloadItems(at: [IndexPath(item: index, section: 0)])
    }
    
    func sendGiftAction(gift: AUIKitCore.AUIGiftEntity) {
        AUICommonDialog.hidden()
        AUIToast.hidden()
        let user = AUIUserThumbnailInfo()
        user.userName =  UserInfo.users.first?.userName ?? ""
        user.userAvatar =  UserInfo.users.first?.userAvatar ?? ""
        gift.sendUser = user
        self.receiveGift.receiveGift(gift: gift)
        if !gift.giftEffect.isEmpty {
            //这里可以判断大面额礼物 根据id显示特效 参考 https://github.com/AgoraIO-Community/AUIKitVoiceRoom/blob/main/iOS/AScenesKit/AScenesKit/Classes/ViewBinder/AUIRoomGiftBinder.swift
        }
    }
    
    private func loadTheme() {
        
        if let folderPath = Bundle.main.path(forResource: "Gift", ofType: "bundle") {
            AUIThemeManager.shared.addThemeFolderPath(path: URL(fileURLWithPath: folderPath) )
        }
        if let folderPath = Bundle.main.path(forResource: "ChatResource", ofType: "bundle") {
            AUIThemeManager.shared.addThemeFolderPath(path: URL(fileURLWithPath: folderPath) )
        }
        if let folderPath = Bundle.main.path(forResource: "Invitation", ofType: "bundle") {
            AUIThemeManager.shared.addThemeFolderPath(path: URL(fileURLWithPath: folderPath) )
        }
    }
    
    @objc func startMessage(_ text: String?) -> AUIChatEntity {
        let entity = AUIChatEntity()
        let user = AUIUserThumbnailInfo()
        user.userName = "owner"
        entity.user = user
        entity.content = text == nil ? aui_localized("startMessage", bundleName: "auiVoiceChatLocalizable"):text
        entity.attributeContent = entity.attributeContent
        entity.width = entity.width
        entity.height = entity.height
        entity.joined = false
        return entity
    }
    
    @objc func updateBottomBarDatas(onMic: Bool) -> [AUIChatFunctionBottomEntity] {
        var entities = [AUIChatFunctionBottomEntity]()
        var names = ["ellipsis_vertical","mic","gift_color","thumb_up_color"]
        var selectedNames = ["ellipsis_vertical","unmic","gift_color","thumb_up_color"]
        if onMic == false {
            names = ["ellipsis_vertical","gift_color","thumb_up_color"]
            selectedNames = ["ellipsis_vertical","gift_color","thumb_up_color"]
        }
        
        for i in 0...names.count-1 {
            let entity = AUIChatFunctionBottomEntity()
            entity.selected = false
            entity.selectedImage = UIImage(named: selectedNames[i])
            entity.normalImage = UIImage(named: names[i])
            switch names[i] {
            case "ellipsis_vertical":
                entity.type = .more
            case "mic","unmic":
                entity.type = .mic
            case "gift_color":
                entity.type = .gift
            case "thumb_up_color":
                entity.type = .like
            default:
                entity.type = .unknown
                break
            }
            entities.append(entity)
        }
        return entities
    }
    
    @objc func bottomBarEvents() {
        self.bottomBar.actionClosure = { [weak self] entity in
            self?.actionEntity(entity: entity)
        }
        self.bottomBar.raiseKeyboard = { [weak self] in
            self?.showInput()
        }
        self.inputBar.sendClosure = { [weak self] in
            self?.showMessage(text: $0)
        }
    }
    
    private func showInput() {
        self.inputBar.isHidden = false
        self.inputBar.inputField.becomeFirstResponder()
    }
    
    private func showMessage(text: String) {
        self.messageView.chatView.reloadData()
        self.inputBar.inputField.text = ""
        self.messageView.showNewMessage(entity: self.startMessage(text))
    }
    
    private func actionEntity(entity: AUIChatFunctionBottomEntity) {
        switch entity.type {
        case .more: self.showMoreTabs()
        case .mic: self.muteLocal()
        case .gift: self.showGiftDialog()
        case .like: self.emitter.setupEmitter()
        default:
            break
        }
    }
    
    private func showGiftDialog() {
        AUICommonDialog.hidden()
        let theme = AUICommonDialogTheme()
        AUICommonDialog.show(contentView: self.giftsView,theme: theme)
    }
    
    @objc public func updateBottomBarState(onMic: Bool) {
        self.bottomBar.refreshToolBar(datas: self.updateBottomBarDatas(onMic: onMic))
    }
    
    private func parserGifts() {
        if let path = Bundle.main.path(forResource: "gifts", ofType: "json") {
            let url = URL(fileURLWithPath: path)
            let data = try? Data(contentsOf: url)
            let json = data?.a.toDictionary()
            let dataArray = json?["data"] as? [[String:Any]]
            if dataArray != nil {
                let tabs = NSArray.yy_modelArray(with: AUIGiftTabEntity.self, json: dataArray!) as? [AUIGiftTabEntity]
                self.giftsView.fillTabs(tabs: tabs ?? [])
            }
        }
    }
    
}

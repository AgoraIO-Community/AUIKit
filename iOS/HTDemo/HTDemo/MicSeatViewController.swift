//
//  MicSeatViewController.swift
//  AUIKit_Example
//
//  Created by FanPengpeng on 2023/8/1.
//  Copyright © 2023 CocoaPods. All rights reserved.
//

import UIKit


private let kSeatRoomPadding: CGFloat = 16

private let avatarArr = [
    "https://img2.baidu.com/it/u=3853345508,384760633&fm=253&app=120&size=w931&n=0&f=JPEG&fmt=auto?sec=1690995600&t=5b42096181b504c6e55a7dc3950879c6",
    "https://img1.baidu.com/it/u=3065838285,2676115581&fm=253&app=138&size=w931&n=0&f=JPEG&fmt=auto?sec=1690995600&t=33317e67c6570c9f6d7cdfd53ee8db57",
    "https://img0.baidu.com/it/u=1604010673,2427861166&fm=253&app=138&size=w931&n=0&f=JPEG&fmt=auto?sec=1690995600&t=6ef89d3e2799dfd6aa589cea1a335cfc"
]


class MicSeatViewController: UIViewController {
  
    
    
    var channelName = "123"
    
    private let rtcManager = RTCManager()
    
    private var micSeatArray: [AUIMicSeatInfo] = []
    
    private var currentIndex: Int = -1 {
        didSet{
            microphoneButton.isHidden = currentIndex == -1
        }
    }

    // 麦克风开关按钮
    private lazy var microphoneButton: UIButton = {
        let button = UIButton()
        button.setImage(UIImage(named: "aui_karaoke_room_microphone_unmute"), for: .normal)
        button.setImage(UIImage(named: "aui_karaoke_room_microphone_mute"), for: .selected)
        button.addTarget(self, action: #selector(didClickVoiceChatButton(_:)), for: .touchUpInside)
        button.isHidden = true
        return button
    }()

    private var micSeatView: AUIMicSeatView!
    
    @objc private func didClickVoiceChatButton(_ button: UIButton){
        guard currentIndex != -1 else {
            return
        }
        button.isSelected = !button.isSelected
        let seatInfo = micSeatArray[currentIndex]
        seatInfo.isMuteAudio = !seatInfo.isMuteAudio
        micSeatView.refresh(index: currentIndex)
        rtcManager.muteSelf(seatInfo.isMuteAudio)
    }

    
    deinit {
        print("-销毁成功-")
        rtcManager.leave()
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        createUI()
        let uid = arc4random() % 100000
        rtcManager.join(channelName: channelName, uid: "\(uid)", videoView:nil , delegate: self)
    }
    
    private func createUI(){
        
        title = channelName
        view.backgroundColor = .gray
        for i in 0...(8 - 1) {
            let seatInfo = AUIMicSeatInfo()
            seatInfo.micSeat = UInt(i)
            if i < 3 {
                seatInfo.seatName = "user\(i)"
                seatInfo.avatarUrl = avatarArr[i]
                seatInfo.onSeat = true
            }
            micSeatArray.append(seatInfo)
        }
        
        let flowLayout = UICollectionViewFlowLayout()
        let width: CGFloat = 80
        let height: CGFloat = 92
        let hPadding = Int((self.view.frame.size.width - 16 * 2 - width * 4) / 3)
        flowLayout.itemSize = CGSize(width: width, height: height)
        flowLayout.minimumLineSpacing = 0
        flowLayout.minimumInteritemSpacing = CGFloat(hPadding)
        micSeatView = AUIMicSeatView(frame: CGRect(x: kSeatRoomPadding, y: 331, width: self.view.bounds.size.width - kSeatRoomPadding * 2, height: 190), layout: flowLayout)
        micSeatView.uiDelegate = self
        view.addSubview(micSeatView)
        

        view.addSubview(microphoneButton)
        microphoneButton.aui_size = CGSize(width: 50, height: 50)
        microphoneButton.aui_centerX = self.view.aui_centerX
        microphoneButton.aui_top = 100
    }

}


//MARK: AUIMicSeatViewDelegate
extension MicSeatViewController: AUIMicSeatViewDelegate {
    public func seatItems(view: AUIMicSeatView) -> [AUIMicSeatCellDataProtocol] {
        return micSeatArray
    }
    
    public func onItemDidClick(view: AUIMicSeatView, seatIndex: Int) {
        let seatInfo = micSeatArray[seatIndex]
        if seatInfo.onSeat == false && currentIndex == -1{
            seatInfo.onSeat = true
            seatInfo.seatName = "user\(seatIndex)"
            seatInfo.avatarUrl = avatarArr[Int(arc4random()) % 3]
            currentIndex = Int(seatInfo.micSeat)
            view.refresh(index: seatIndex)
        }else if seatInfo.onSeat == true && currentIndex == seatInfo.micSeat {
            seatInfo.onSeat = false
            seatInfo.seatName = ""
            seatInfo.avatarUrl = ""
            currentIndex = -1
            view.refresh(index: seatIndex)
        }
        
        print("  onItemDidClick view = \(view), seatIndex = \(seatIndex)")
    }
    
    public func onMuteVideo(view: AUIMicSeatView, seatIndex: Int, canvas: UIView, isMuteVideo: Bool) {

    }

}

extension MicSeatViewController {
    /*
    private func enterDialogItem(seatInfo: AUIMicSeatInfo, callback: @escaping ()->()) -> AUIActionSheetItem {
        let item = AUIActionSheetThemeItem()
        item.title = aui_localized("enterSeat")
        item.titleColor = "ActionSheet.normalColor"
        item.callback = { [weak self] in
//            self?.micSeatDelegate?.enterSeat(seatIndex: Int(seatInfo.seatIndex), callback: { err in
//                guard let err = err else {return}
//                AUIToast.show(text: err.localizedDescription)
//            })
//            callback()
        }
        return item
    }
    
    private func kickDialogItem(seatInfo: AUIMicSeatInfo, callback: @escaping ()->()) -> AUIActionSheetItem {
        let item = AUIActionSheetThemeItem()
        item.title = aui_localized("kickSeat")
        item.titleColor = "ActionSheet.normalColor"
        item.callback = { [weak self] in
//            self?.micSeatDelegate?.kickSeat(seatIndex: Int(seatInfo.seatIndex),
//                                            callback: { error in
//                guard let err = error else {return}
//                AUIToast.show(text: err.localizedDescription)
//            })
//            callback()
        }
        return item
    }
    
    private func leaveDialogItem(seatInfo: AUIMicSeatInfo, callback: @escaping ()->()) -> AUIActionSheetItem {
        let item = AUIActionSheetThemeItem()
        item.title = aui_localized("leaveSeat")
        item.icon = "ActionSheetCell.normalIcon"
        item.titleColor = "ActionSheet.normalColor"
        item.callback = { [weak self] in
//            self?.micSeatDelegate?.leaveSeat(callback: { error in
//                guard let err = error else {return}
//                AUIToast.show(text: err.localizedDescription)
//            })
//            callback()
        }
        return item
    }
    
    private func muteAudioDialogItem(seatInfo: AUIMicSeatInfo, callback: @escaping ()->()) ->AUIActionSheetItem {
        let item = AUIActionSheetThemeItem()
        item.title = seatInfo.muteAudio ? aui_localized("unmuteAudio") : aui_localized("muteAudio")
//        item.icon = "ActionSheetCell.warnIcon"
        item.titleColor = "ActionSheet.normalColor"
        item.callback = { [weak self] in
//            self?.micSeatDelegate?.muteAudioSeat(seatIndex: Int(seatInfo.seatIndex),
//                                                 isMute: !seatInfo.muteAudio,
//                                                 callback: { error in
//                guard let err = error else {return}
//                AUIToast.show(text: err.localizedDescription)
//            })
//            callback()
        }
        
        return item
    }
    
    public func getDialogItems(seatInfo: AUIMicSeatInfo, callback: @escaping ()->()) ->[AUIActionSheetItem] {
        var items = [AUIActionSheetItem]()
        items.append(muteAudioDialogItem(seatInfo: seatInfo, callback: {
            
        }))
        
        items.append(enterDialogItem(seatInfo: seatInfo, callback: {
            
        }))
        
        return items
    }
     */
}


extension MicSeatViewController : RTCManagerDelegate {
    func onJoinedChannel(_ channel: String) {
        
    }
    
    func onUserJoined(_ uid: UInt) {
        
    }
    
    func onReceiveStreamMessageFromUid(_ uid: UInt, streamId: Int, data: Data) {
        
    }
}

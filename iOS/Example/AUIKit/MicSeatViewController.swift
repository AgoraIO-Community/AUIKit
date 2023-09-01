//
//  MicSeatViewController.swift
//  AUIKit_Example
//
//  Created by FanPengpeng on 2023/8/1.
//  Copyright © 2023 CocoaPods. All rights reserved.
//

import UIKit
import AUIKitCore

class MicSeatViewController: UIViewController {
}
/*
private let kSeatRoomPadding: CGFloat = 16

private let avatarArr = [
    "https://img2.baidu.com/it/u=3853345508,384760633&fm=253&app=120&size=w931&n=0&f=JPEG&fmt=auto?sec=1690995600&t=5b42096181b504c6e55a7dc3950879c6",
    "https://img1.baidu.com/it/u=3065838285,2676115581&fm=253&app=138&size=w931&n=0&f=JPEG&fmt=auto?sec=1690995600&t=33317e67c6570c9f6d7cdfd53ee8db57",
    "https://img0.baidu.com/it/u=1604010673,2427861166&fm=253&app=138&size=w931&n=0&f=JPEG&fmt=auto?sec=1690995600&t=6ef89d3e2799dfd6aa589cea1a335cfc"
]

class MicSeatViewController: UIViewController {
    
    private var micSeatArray: [AUIMicSeatInfo] = []


    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .gray
        
        for i in 0...(8 - 1) {
            let seatInfo = AUIMicSeatInfo()
            seatInfo.seatIndex = UInt(i)
            if i < 2 {
                let user = AUIUserThumbnailInfo()
                user.userId = "\(i)"
                user.userName = "user\(i)"
                user.userAvatar = avatarArr[i]
                seatInfo.user = user
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
        let micSeatView = AUIMicSeatView(frame: CGRect(x: kSeatRoomPadding, y: 331, width: self.view.bounds.size.width - kSeatRoomPadding * 2, height: 190), layout: flowLayout)
        micSeatView.uiDelegate = self

        view.addSubview(micSeatView)
    }

}


//MARK: AUIMicSeatViewDelegate
extension MicSeatViewController: AUIMicSeatViewDelegate {
    public func seatItems(view: AUIMicSeatView) -> [AUIMicSeatCellDataProtocol] {
        return micSeatArray
    }
    
    public func onItemDidClick(view: AUIMicSeatView, seatIndex: Int) {
        let micSeat = micSeatArray[seatIndex]
        
        print("  onItemDidClick view = \(view), seatIndex = \(seatIndex)")

        let dialogItems = getDialogItems(seatInfo: micSeat) {
            AUICommonDialog.hidden()
        }
        guard dialogItems.count > 0 else {return}
        var headerInfo: AUIActionSheetHeaderInfo? = nil
        if let user = micSeat.user, user.userId.count > 0 {
            headerInfo = AUIActionSheetHeaderInfo()
            headerInfo?.avatar = user.userAvatar
            headerInfo?.title = user.userName
            headerInfo?.subTitle = micSeat.seatIndexDesc()
        }
        let dialogView = AUIActionSheet(title: aui_localized("managerSeat"),
                                        items: dialogItems,
                                        headerInfo: headerInfo)
        dialogView.setTheme(theme: AUIActionSheetTheme())
        AUICommonDialog.show(contentView: dialogView, theme: AUICommonDialogTheme())
    }
    
    public func onMuteVideo(view: AUIMicSeatView, seatIndex: Int, canvas: UIView, isMuteVideo: Bool) {

    }

}

extension MicSeatViewController {
    
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
}
*/

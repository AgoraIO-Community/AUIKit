//
//  ViewController.swift
//  HTDemo
//
//  Created by FanPengpeng on 2023/8/24.
//

import UIKit

class ViewController: UIViewController {

    @IBOutlet weak var channelTF: UITextField!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        channelTF.text = "123"
    }


    @IBAction func onClickJoinRoomButton(_ sender: Any) {
        
        guard let channelName = channelTF.text?.trimmingCharacters(in: .whitespacesAndNewlines) else {
            return
        }
        
        let vc = MicSeatViewController()
        vc.channelName = channelName
        self.navigationController?.pushViewController(vc, animated: true)
    }
    
    override func touchesEnded(_ touches: Set<UITouch>, with event: UIEvent?) {
        self.view.endEditing(true)
    }
}


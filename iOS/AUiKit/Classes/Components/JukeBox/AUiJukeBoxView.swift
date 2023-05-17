//
//  AUiJukeBoxView.swift
//  AUiKit
//
//  Created by wushengtao on 2023/3/20.
//

import UIKit
import AgoraRtcKit
import MJRefresh
import SwiftTheme

public protocol AUiJukeBoxViewDelegate: NSObjectProtocol {
    //清除搜索框
    func cleanSearchText(view: AUiJukeBoxView)
    
    /// 开始搜索
    /// - Parameter text: 关键字
    func search(view: AUiJukeBoxView, text: String, completion: @escaping ([AUiJukeBoxItemDataProtocol]?)->())
    
    
    /// 切换点歌/已点segmented
    /// - Parameters:
    ///   - view: <#view description#>
    ///   - segmentIndex: <#segmentIndex description#>
    func onSegmentedChanged(view: AUiJukeBoxView, segmentIndex: Int) -> Bool
    
    
    /// 切换榜单列表
    /// - Parameters:
    ///   - view: <#view description#>
    ///   - tabIndex: <#tabIndex description#>
    /// - Returns: true: 已使用 false: 未使用，需要view默认实现
    func onTabsDidChanged(view: AUiJukeBoxView, tabIndex: Int) -> Bool
    
    /// 选中一首歌
    /// - Parameters:
    ///   - view: <#view description#>
    ///   - tabIndex: 分类index
    ///   - index: 歌曲index
    func onSelectSong(view: AUiJukeBoxView, tabIndex: Int, index: Int)
    
    /// 删除一首歌
    /// - Parameters:
    ///   - view: <#view description#>
    ///   - index: <#index description#>
    func onRemoveSong(view: AUiJukeBoxView, index: Int)
    
    /// 切换下一首歌
    /// - Parameters:
    ///   - view: <#view description#>
    ///   - index: <#index description#>
    func onNextSong(view: AUiJukeBoxView, index: Int)
    
    /// 置顶一首歌
    /// - Parameters:
    ///   - view: <#view description#>
    ///   - index: <#index description#>
    func onPinSong(view: AUiJukeBoxView, index: Int)
    
    /// 当前歌曲选中状态
    /// - Parameters:
    ///   - view: <#view description#>
    ///   - songCode: <#songCode description#>
    func songIsSelected(view: AUiJukeBoxView, songCode: String) -> Bool
    
    
    /// 歌曲是否可以置顶
    /// - Parameters:
    ///   - view: <#view description#>
    ///   - songCode: <#songCode description#>
    /// - Returns: <#description#>
    func pingEnable(view: AUiJukeBoxView, songCode: String) -> Bool
    
    
    /// 歌曲是否可以被删除
    /// - Parameters:
    ///   - view: <#view description#>
    ///   - songCode: <#songCode description#>
    /// - Returns: <#description#>
    func deleteEnable(view: AUiJukeBoxView, songCode: String) -> Bool
    
    /// 获取搜索歌曲列表
    /// - Parameter view: <#view description#>
    /// - Returns: <#description#>
    func getSearchMusicList(view: AUiJukeBoxView) -> [AUiJukeBoxItemDataProtocol]?
    
    /// 根据榜单获取歌曲列表
    /// - Parameters:
    ///   - view: <#view description#>
    ///   - tabIndex: <#tabIndex description#>
    /// - Returns: <#description#>
    func getMusicList(view: AUiJukeBoxView, tabIndex: Int) -> [AUiJukeBoxItemDataProtocol]
    
    /// 获取点歌列表
    /// - Parameter view: <#view description#>
    /// - Returns: <#description#>
    func getSelectedSongList(view: AUiJukeBoxView) -> [AUiJukeBoxItemSelectedDataProtocol]
    
    
    /// 下拉刷新歌曲列表
    /// - Parameters:
    ///   - view: <#view description#>
    ///   - tabIndex: <#tabIndex description#>
    func onRefreshMusicList(view: AUiJukeBoxView, tabIndex: Int, completion: @escaping ([AUiJukeBoxItemDataProtocol]?)->())
    
    
    /// 上拉加载更多歌曲
    /// - Parameters:
    ///   - view: <#view description#>
    ///   - tabIndex: <#tabIndex description#>
    func onLoadMoreMusicList(view: AUiJukeBoxView, tabIndex: Int, completion: @escaping ([AUiJukeBoxItemDataProtocol]?)->())
    
    
    /// 下拉刷新点歌列表
    /// - Parameters:
    ///   - view: <#view description#>history
    ///   - completion: <#completion description#>
    func onRefreshAddedMusicList(view: AUiJukeBoxView, completion: @escaping ([AUiJukeBoxItemSelectedDataProtocol]?)->())
    
    
}

private let kJukeBoxCellId = "JukeBoxCellId"
private let kJukeBoxEdge: CGFloat = 16
private let kJukeBoxSegmentSize = CGSize(width: 160, height: 34)
private let kSearchMusicListKey: Int = 10000

/// 点歌器组件
open class AUiJukeBoxView: UIView {
    public var pageSize: Int = 10
    @objc var cellHeight: CGFloat = 84 {
        didSet {
            self.allMusicTableView.reloadData()
            self.addedMusicTableView.reloadData()
        }
    }
    public weak var uiDelegate: AUiJukeBoxViewDelegate? {
        didSet {
            onSegmentAction()
        }
    }
    
    public var selectedSongCount: Int = 0 {
        didSet {
            var countDesc = ""
            if selectedSongCount > 99 {
                countDesc = " 99+"
            } else if selectedSongCount > 0 {
                countDesc = " \(selectedSongCount)"
            }
            
            if let segmentView = self.segmentControl.segments.last?.selectedView as? UILabel {
                segmentView.text = "\(aui_localized("selectedSong")) \(countDesc)"
            }
            if let segmentView = self.segmentControl.segments.last?.normalView as? UILabel {
                segmentView.text = "\(aui_localized("selectedSong")) \(countDesc)"
            }
        }
    }
    
    //点歌/已点
    private lazy var segmentControl: AUiSegmented = {
        let segmentView = AUiSegmented()
        segmentView.aui_size = kJukeBoxSegmentSize
        segmentView.segments = AUiLabelSegment.segments(withTitles: [aui_localized("selectSong"), aui_localized("selectedSong")])
        segmentView.theme_indicatorColor = AUiColor("JukeBox.segmentViewIndicatorColor")
//        segmentView.selectedSegmentIndex = 0
//        segmentView.selectedSegmentTintColor = .red
        segmentView.backgroundColor = .clear
        segmentView.layer.theme_borderColor = AUiCGColor("JukeBox.segmentViewBorderColor")
        segmentView.layer.borderWidth = 2
        segmentView.cornerRadius = kJukeBoxSegmentSize.height / 2
        segmentView.clipsToBounds = true
        segmentView.addTarget(self, action: #selector(onSegmentAction), for: .valueChanged)
        return segmentView
    }()
    
    private lazy var searchTextField: AUiTextField = {
        let textField = AUiTextField()
        textField.layer.theme_cornerRadius = "JukeBox.searchCornerRadius"
        textField.clipsToBounds = true
        textField.returnKeyType = .search
        textField.theme_backgroundColor = AUiColor("JukeBox.searchBackgroundColor")
        textField.placeHolder = aui_localized("searchSong")
        textField.theme_placeHolderColor = AUiColor("JukeBox.searchPlaceHolderColor")
        textField.theme_placeHolderFont = "JukeBox.searchPlaceHolderFont"
        textField.theme_textColor = AUiColor("JukeBox.searchTextColor")
        textField.theme_leftIconImage = "JukeBox.searchIcon"
        textField.theme_rightIconImage = "JukeBox.closeIcon"
        textField.theme_rightSelectedIconImage = "JukeBox.closeIcon"
        textField.theme_height = "JukeBox.searchTextFieldHeight"
        textField.layer.theme_cornerRadius = "JukeBox.searchTextFieldCornerRadius"
        textField.clipsToBounds = true
        textField.clickRightButtonClosure = {[weak self] selected in
            guard let self = self else {return}
            self.searchTextField.text = ""
            self.uiDelegate?.cleanSearchText(view: self)
            self.tabsView.isHidden = false
//            self.onSegmentAction()
            self.setNeedsLayout()
            self.allMusicTableView.reloadData()
        }
        textField.textEditingEndedClosure = { [weak self] key in
            guard let self = self, let key = key else {return}
            self.uiDelegate?.search(view: self, text: key, completion: {[weak self] list in
                guard let self = self else {return}
//                self.onSegmentAction()
                self.tabsView.isHidden = true
                self.setNeedsLayout()
                self.allMusicTableView.reloadData()
            })
        }
        return textField
    }()
    
    private lazy var tabsView: AUiTabs = {
        let tabs = AUiTabs()
        tabs.titles = [aui_localized("songTabName1"), aui_localized("songTabName2"), aui_localized("songTabName3"), aui_localized("songTabName4")]
        tabs.style.indicatorStyle = .line
        tabs.style.indicatorHeight = 3
        tabs.theme_indicatorWidth = "JukeBox.tabsIndicatorWidth"
        tabs.theme_indicatorColor = AUiColor("JukeBox.tabsIndicatorColor")
        tabs.theme_selectedTitleColor = AUiColor("JukeBox.tabsSelectedTextColor")
        tabs.theme_normalTitleColor = AUiColor("JukeBox.tabsNormalTextColor")
        tabs.theme_textFont = "JukeBox.tabsTextFont"
        tabs.valueChange = { [weak self] index in
            guard let self = self else {return}
            if self.uiDelegate?.onTabsDidChanged(view: self, tabIndex: index) ?? false {
                return
            }
            
            self.allMusicTableView.reloadData()
            if self.uiDelegate?.getMusicList(view: self, tabIndex: index).count ?? 0 > 0 {
                return
            }
            self.allMusicTableView.mj_header?.beginRefreshing()
        }
        return tabs
    }()
    
    //点歌tableview
    public lazy var allMusicTableView: UITableView = {
        let tableView = UITableView(frame: .zero, style: .plain)
        tableView.backgroundColor = .clear
        tableView.register(AUiJukeBoxCell.self, forCellReuseIdentifier: kJukeBoxCellId)
        tableView.mj_header = MJRefreshNormalHeader(refreshingBlock: { [weak self] in
            self?.onRefreshMusicList()
        })
        return tableView
    }()
    
    //已点tableview
    public lazy var addedMusicTableView: UITableView = {
        let tableView = UITableView(frame: .zero, style: .plain)
        tableView.backgroundColor = .clear
        tableView.register(AUiJukeBoxCell.self, forCellReuseIdentifier: kJukeBoxCellId)
        tableView.mj_header = MJRefreshNormalHeader(refreshingBlock: { [weak self] in
            self?.onRefreshAddedMusicList()
        })
        return tableView
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        _loadSubViews()
    }
    
    required public init?(coder: NSCoder) {
        super.init(coder: coder)
        _loadSubViews()
    }
    
    private func _loadSubViews() {
        theme_backgroundColor = "JukeBox.backgroundColor"
        theme_cellHeight = "JukeBox.cellHeight"
        
        addSubview(segmentControl)
        addSubview(searchTextField)
        addSubview(tabsView)
        
        allMusicTableView.delegate = self
        allMusicTableView.dataSource = self
        addSubview(allMusicTableView)
        
        addedMusicTableView.delegate = self
        addedMusicTableView.dataSource = self
        addSubview(addedMusicTableView)
    }
    
    open override func layoutSubviews() {
        super.layoutSubviews()
        segmentControl.aui_center = CGPoint(x: aui_width / 2, y: 34 + kJukeBoxSegmentSize.height / 2)
        searchTextField.frame = CGRect(x: kJukeBoxEdge, y: segmentControl.aui_bottom + 24,
                                       width: aui_width - kJukeBoxEdge * 2, height: searchTextField.aui_height)
        tabsView.frame = CGRect(x: 0, y: searchTextField.aui_bottom + kJukeBoxEdge, width: aui_width, height: 52)
        let allMusicTableViewTop = (tabsView.isHidden ? searchTextField.aui_bottom : tabsView.aui_bottom) + 4
        allMusicTableView.frame = CGRect(x: 0, y: allMusicTableViewTop, width: aui_width, height: aui_height - allMusicTableViewTop)
        addedMusicTableView.frame = CGRect(x: 0, y: segmentControl.aui_bottom, width: aui_width, height: aui_height - segmentControl.aui_bottom)
    }
}

extension AUiJukeBoxView {
     
    private func didClickAddButton(_ index: Int){
        uiDelegate?.onSelectSong(view: self, tabIndex: tabsView.selectIndex, index: index)
    }
    
    private func didClickPinButton(_ index: Int){
        uiDelegate?.onPinSong(view: self, index: index)
    }
    
    private func didClickRemoveButton(_ index: Int){
        uiDelegate?.onRemoveSong(view: self, index: index)
    }
    
    private func didClickNextButton(_ index: Int){
        uiDelegate?.onNextSong(view: self, index: index)
    }
}

extension AUiJukeBoxView {
    private func onRefreshMusicList() {
        aui_info("onRefreshMusicList", tag: "AUiJukeBoxView")
        let idx = tabsView.selectIndex
        allMusicTableView.mj_footer = nil
        uiDelegate?.onRefreshMusicList(view: self,
                                       tabIndex: idx,
                                       completion: {[weak self] list in
            guard let self = self else {return}
            self.allMusicTableView.mj_header?.endRefreshing()
            self.allMusicTableView.reloadData()
            
            if list?.count ?? 0 < self.pageSize {
                return
            }
            
            self.allMusicTableView.mj_footer = MJRefreshAutoNormalFooter(refreshingBlock: { [weak self] in
                self?.onLoadMoreMusicList()
            })
        })
    }
    
    private func onLoadMoreMusicList() {
        aui_info("onLoadMoreMusicList", tag: "AUiJukeBoxView")
        let idx = tabsView.selectIndex
        uiDelegate?.onLoadMoreMusicList(view: self,
                                        tabIndex: idx,
                                        completion: {[weak self] list in
            guard let self = self else {return}
            self.allMusicTableView.mj_footer?.endRefreshing()
            self.allMusicTableView.reloadData()
            
            guard list?.count ?? 0 < self.pageSize else {
                return
            }
            
            self.allMusicTableView.mj_footer?.endRefreshingWithNoMoreData()
        })
    }
    
    private func onRefreshAddedMusicList() {
        aui_info("onRefreshAddedMusicList", tag: "AUiJukeBoxView")
        uiDelegate?.onRefreshAddedMusicList(view: self, completion: {[weak self] list in
            guard let self = self else {return}
            self.addedMusicTableView.mj_header?.endRefreshing()
            self.addedMusicTableView.reloadData()
        })
    }
    
    @objc func onSegmentAction() {
        aui_info("onSegmentAction: \(segmentControl.index)", tag: "AUiJukeBoxView")
        if self.uiDelegate?.onSegmentedChanged(view: self, segmentIndex: self.segmentControl.index) ?? false {
            return
        }
        if self.segmentControl.index == 0 {
            self.tabsView.isHidden = uiDelegate?.getSearchMusicList(view: self) == nil ? false : true
            self.searchTextField.isHidden = false
            self.addedMusicTableView.isHidden = true
            self.allMusicTableView.isHidden = false
            setNeedsLayout()
            if uiDelegate?.getMusicList(view: self, tabIndex: tabsView.selectIndex).count ?? 0 > 0 {
                return
            }
            self.allMusicTableView.mj_header?.beginRefreshing()
        } else {
            self.tabsView.isHidden = true
            self.searchTextField.isHidden = true
            self.addedMusicTableView.isHidden = false
            self.allMusicTableView.isHidden = true
            if uiDelegate?.getSelectedSongList(view: self).count ?? 0 > 0 {
                return
            }
            self.addedMusicTableView.mj_header?.beginRefreshing()
        }
    }
}


extension AUiJukeBoxView: UITableViewDelegate, UITableViewDataSource {

    public func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return cellHeight
    }
    
    public func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if tableView == allMusicTableView {
            if let searchList = uiDelegate?.getSearchMusicList(view: self) {
                return searchList.count
            }
            return uiDelegate?.getMusicList(view: self, tabIndex: self.tabsView.selectIndex).count ?? 0
        }
        
        return uiDelegate?.getSelectedSongList(view: self).count ?? 0
    }
    
    public func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        var cell: AUiJukeBoxCell? = tableView.dequeueReusableCell(withIdentifier: kJukeBoxCellId, for: indexPath) as? AUiJukeBoxCell
        if cell == nil {
            cell = AUiJukeBoxCell(style: .default, reuseIdentifier: kJukeBoxCellId)
        }
        
        guard let cell = cell else {
            return UITableViewCell(style: .default, reuseIdentifier: kJukeBoxCellId)
        }
        
        if tableView == allMusicTableView {
            if let music = uiDelegate?.getSearchMusicList(view: self)?[indexPath.row] ?? uiDelegate?.getMusicList(view: self, tabIndex: self.tabsView.selectIndex)[indexPath.row] {
                cell.aui_style = .selectSong
                cell.music = music
                cell.isSelectedSong = uiDelegate?.songIsSelected(view: self, songCode: music.songCode) ?? false
            }
        } else if let music = uiDelegate?.getSelectedSongList(view: self)[indexPath.row] {
            cell.aui_style = .selectedSong
            cell.music = music
            if music.isPlaying {
                cell.rangeLabel.isHidden = true
                cell.rangeImageView.isHidden = false
            } else {
                cell.rangeLabel.isHidden = false
                cell.rangeImageView.isHidden = true
                cell.rangeLabel.text = "\(indexPath.row + 1)"
            }
            
            cell.nextButton.isHidden = music.isPlaying ? false : true
            let removeEnable = uiDelegate?.deleteEnable(view: self, songCode: music.songCode) ?? false
            if removeEnable {
                cell.removeButton.isHidden = !cell.nextButton.isHidden
            } else {
                cell.removeButton.isHidden = true
            }
            let pinEnable = uiDelegate?.pingEnable(view: self, songCode: music.songCode) ?? false
            if pinEnable {
                cell.pinButton.isHidden = indexPath.row < 2 ? true : false
            } else {
                cell.pinButton.isHidden = true
            }
        } else {
            return cell
        }
        
        cell.selectSongClosure = { [weak self] in
            self?.didClickAddButton(indexPath.row)
        }
        
        cell.pinClosure = { [weak self] in
            self?.didClickPinButton(indexPath.row)
        }
        
        cell.deleteClosure = { [weak self] in
            self?.didClickRemoveButton(indexPath.row)
        }
        
        cell.nextClosure = { [weak self] in
            self?.didClickNextButton(indexPath.row)
        }
        
        return cell
    }
    
    public func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        
    }
}

extension AUiJukeBoxView {
    var theme_cellHeight: ThemeCGFloatPicker? {
        get { return aui_getThemePicker(self, "setCellHeight:") as? ThemeCGFloatPicker }
        set { aui_setThemePicker(self, "setCellHeight:", newValue) }
    }
}

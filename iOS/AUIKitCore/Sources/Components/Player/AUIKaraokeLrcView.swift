//
//  AUIKaraokeLrcView.swift
//  AUIKit
//
//  Created by CP on 2023/4/11.
//

import UIKit
import AgoraLyricsScore
import ScoreEffectUI

@objc public protocol AUILrcViewDelegate: NSObjectProtocol {
    func onKaraokeView( didDragTo position: Int)
    func onKaraokeView(score: Int, totalScore: Int, lineScore: Int, lineIndex: Int)
}

public class AUIKaraokeLrcView: UIView {

    var lrcView: KaraokeView!
    var incentiveView: IncentiveView!
    var lineScoreView: LineScoreView!
    var gradeView: GradeView!
    private var currentLoadLrcPath: String?
    private var lyricModel: LyricModel?

    
    lazy var skipView: AUIKaraokeSkipView = {
        let skipView: AUIKaraokeSkipView = AUIKaraokeSkipView()
        return skipView
    }()
    
    private var totalLines: Int = 0
    private var totalScore: Int = 0
    private var totalCount: Int = 0
    private var progress: Int = 0
    private var hasShowPreludeEndPosition = false
    private var hasShowEndPosition = false
    private var hasShowPreludeEndOnce: Bool = false
    private var hasShowEpilogueOnce: Bool = false
    public var skipCallBack: ((Int, Bool) -> Void)?
    public var showSkipCallBack: ((SkipType) -> Void)?
    public weak var delegate: AUILrcViewDelegate?
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        layoutUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func layoutUI() {
        let margin = 15
        
        gradeView = GradeView(frame: CGRect(x: margin, y: 15, width: Int(self.bounds.width) - margin * 2, height: margin * 2))
        addSubview(gradeView!)
        
        lrcView = KaraokeView(frame: CGRect(x: 0, y: 30, width: self.width, height: self.bounds.height - 20), loggers: [FileLogger()])
        lrcView.scoringView.viewHeight = 60
        lrcView.scoringView.topSpaces = 5
//        lrcView.lyricsView.textNormalColor = UIColor(hex: "#FFFFFF",alpha: 0.5)
//        lrcView.lyricsView.textSelectedColor = UIColor(hex: "#FFFFFF")
//        lrcView.lyricsView.textHighlightedColor = UIColor(hex: "#FF8AB4")
        lrcView.lyricsView.lyricLineSpacing = 6
        lrcView.lyricsView.draggable = false
        lrcView.delegate = self
        addSubview(lrcView!)

        incentiveView = IncentiveView(frame: CGRect(x: 15, y: 55, width: 192, height: 45))
        lrcView.addSubview(incentiveView!)

        lineScoreView = LineScoreView()
        lineScoreView.frame = CGRect(x: lrcView?.scoringView.defaultPitchCursorX ?? 0, y: (lrcView?.scoringView.topSpaces ?? 0) + (lrcView?.bounds.minY ?? 0), width: 50, height: lrcView.scoringView.viewHeight)
        addSubview(self.lineScoreView!)
        
        skipView = AUIKaraokeSkipView(frame: CGRect(x: aui_width / 2.0 - 54, y: 155, width: 108, height: 32))
        skipView.completion = {[weak self] type in
            guard let self = self,
                  let duration = self.lyricModel?.duration,
                  let preludeEndPosition = self.lyricModel?.preludeEndPosition else {
                return
            }
            var pos = preludeEndPosition - 2000
            if self.progress >= duration - 500 {
                pos = duration - 500
                self.skipCallBack?(pos, true)
                self.hasShowEpilogueOnce = true
            } else {
                self.skipCallBack?(pos, false)
                self.hasShowPreludeEndOnce = true
            }
            
            self.skipView.isHidden = true
        }
        addSubview(skipView)
        skipView.isHidden = true
    }
    
    @objc public func getAvgScore() -> Int {
        return totalLines > 0 ? totalScore / totalLines : 0
    }
    
    public func updateScore(with lineScore: Int, cumulativeScore: Int, totalScore: Int) {
        lineScoreView.showScoreView(score: lineScore)
        gradeView.setScore(cumulativeScore: cumulativeScore, totalScore: totalScore)
        incentiveView.show(score: lineScore)
    }
    
    public func resetScore() {
        gradeView.reset()
    }
    
    @objc public func resetLrc() {
        lrcView?.reset()
        currentLoadLrcPath = nil
    }
    
    @objc public func hideSkipView(flag: Bool) {
        skipView.isHidden = flag
    }
    
    @objc public func showPreludeEnd(enable: Bool) {
        if enable {
            if hasShowPreludeEndOnce {return}
        } else {
            hasShowPreludeEndOnce = true
        }
        //显示跳过前奏
        skipView.setSkipType(type: .prelude)
        skipView.isHidden = !enable
        hasShowPreludeEndPosition = !enable
    }
    
    
    @objc public func showEpilogue(enable: Bool) {
        if enable {
            if hasShowEpilogueOnce {return}
        } else {
            hasShowEpilogueOnce = true
        }
        //显示跳过尾奏
        skipView.setSkipType(type: .epilogue)
        skipView.isHidden = !enable
        hasShowEndPosition = !enable
    }
    
    @objc public func resetShowOnce() {
        hasShowPreludeEndOnce = false
        hasShowEpilogueOnce = false
    }
}

extension AUIKaraokeLrcView: KaraokeDelegate {
    public func onKaraokeView(view: KaraokeView, didDragTo position: Int) {
        //歌词组件的滚动
        totalScore = view.scoringView.getCumulativeScore()
        gradeView.setScore(cumulativeScore: totalScore, totalScore: totalCount * 100)
        guard let delegate = self.delegate else {return}
        delegate.onKaraokeView(didDragTo: position)
    }
    
    public func onKaraokeView(view: KaraokeView, didFinishLineWith model: LyricLineModel, score: Int, cumulativeScore: Int, lineIndex: Int, lineCount: Int) {
        //歌词打分
        totalLines = lineCount
        totalScore += score
        guard let delegate = self.delegate else {
            return
        }
        delegate.onKaraokeView(score: totalScore, totalScore: lineCount * 100, lineScore: score, lineIndex: lineIndex)
    }
}

extension AUIKaraokeLrcView {
    
    public func updatePitch(pitch: Float) {
        //pitch 更新
        lrcView?.setPitch(pitch: Double(pitch))
    }
    
    public func updateProgress(progress: Int) {
        self.progress = progress
        //进度更新
        lrcView?.setProgress(progress: progress)
        guard let model = self.lyricModel else {
            return
        }
        let preludeEndPosition = model.preludeEndPosition
        let duration = model.duration - 500
        if progress > model.duration {
            return
        }
//        print("duration:\(duration), pro:\(progress)")
        guard let callback = showSkipCallBack else {return}
        if preludeEndPosition < progress && !hasShowPreludeEndPosition {
            callback(.prelude)
        } else if duration < progress && !hasShowEndPosition {
            callback(.epilogue)
        }
    }
    
    public func resetLrcData(with url: String) {
        guard currentLoadLrcPath != url else {
            return
        }
        let musicUrl = URL(fileURLWithPath: url)
        guard let data = try? Data(contentsOf: musicUrl),
              let model = KaraokeView.parseLyricData(data: data) else {
            return
        }
        currentLoadLrcPath = url
        lyricModel = model
        totalCount = model.lines.count
        totalLines = 0
        totalScore = 0
        lrcView?.setLyricData(data: model)
    }
}


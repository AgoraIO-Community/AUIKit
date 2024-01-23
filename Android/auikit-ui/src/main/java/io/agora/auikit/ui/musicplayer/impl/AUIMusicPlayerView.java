package io.agora.auikit.ui.musicplayer.impl;

import android.animation.Animator;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.Group;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.agora.auikit.ui.R;
import io.agora.auikit.ui.basic.AUIAlertDialog;
import io.agora.auikit.ui.basic.AUIBottomDialog;
import io.agora.auikit.ui.musicplayer.ControllerEffectInfo;
import io.agora.auikit.ui.musicplayer.IMusicPlayerView;
import io.agora.auikit.ui.musicplayer.MusicSettingInfo;
import io.agora.auikit.ui.musicplayer.listener.IMusicPlayerActionListener;
import io.agora.auikit.ui.musicplayer.listener.IMusicPlayerEffectActionListener;
import io.agora.auikit.ui.musicplayer.utils.DownloadUtils;
import io.agora.auikit.ui.musicplayer.utils.ZipUtils;
import io.agora.karaoke_view.v11.KaraokeEvent;
import io.agora.karaoke_view.v11.KaraokeView;
import io.agora.karaoke_view.v11.LyricsView;
import io.agora.karaoke_view.v11.ScoringView;
import io.agora.karaoke_view.v11.model.LyricsLineModel;
import io.agora.karaoke_view.v11.model.LyricsModel;

public class AUIMusicPlayerView extends FrameLayout implements IMusicPlayerView {
    private IMusicPlayerActionListener mOnKaraokeActionListener;
    private ActionDelegate mActionDelegate;

    private TextView tvMusicName;
    private Button btChooseSong;
    private View musicPlayerIdleView;
    private View mMusicPlayerActiveView;
    private View mActiveView;

    private KaraokeView mKaraokeView;
    private LyricsView mLrcView;
    private ScoringView mScoringView;
    private AUIMusicPlayerGradeView mGradeView;

    private MaterialButton mMusicStartBtn;
    private MaterialButton mSwitchSongBtn;
    private MaterialButton mActiveChooseSongBtn;
    private MaterialButton mActiveOriginalBtn;
    private MaterialButton mLeaveChorus;
    private MaterialButton mControllerBtn;
    private MaterialButton mPresetView;
    private Group mMusicControllerGroup;

    private AppCompatButton mJoinChorusView;
    private AppCompatButton mJoinChorusLoadingView;
    private View mPrepareView;
    private TextView mPreparePrecent;

    private ImageView mCumulativeScoreGrade;
    private TextView mCumulativeScore;
    private TextView mLineScore;

    private AUIAlertDialog mSwitchSongDialog;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final Map<Integer,Integer> effectProperties =  new HashMap<>();

    private final List<ControllerEffectInfo> voiceConversionList = new ArrayList<>();

    private MusicSettingInfo musicSettingInfo = new MusicSettingInfo();

    public AUIMusicPlayerView(@NonNull Context context) {
        super(context);
    }

    public AUIMusicPlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AUIMusicPlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View.inflate(context, R.layout.aui_musicplayer_view, this);
        tvMusicName = findViewById(R.id.tv_music_name);
        musicPlayerIdleView = findViewById(R.id.ilIDLE);
        mActiveView = findViewById(R.id.clActive);
        mMusicPlayerActiveView = findViewById(R.id.il_musicplayer_active_view);
        mLrcView = mMusicPlayerActiveView.findViewById(R.id.lyricsView);
        mScoringView = mMusicPlayerActiveView.findViewById(R.id.scoringView);
        mGradeView = findViewById(R.id.grade_view);
        btChooseSong = musicPlayerIdleView.findViewById(R.id.bt_choose_song);
        mMusicStartBtn = mMusicPlayerActiveView.findViewById(R.id.ivMusicStart);
        mSwitchSongBtn = mMusicPlayerActiveView.findViewById(R.id.ivChangeSong);
        mActiveChooseSongBtn = mMusicPlayerActiveView.findViewById(R.id.ivChooseSong);
        mActiveOriginalBtn = mMusicPlayerActiveView.findViewById(R.id.switchOriginal);
        mKaraokeView = new KaraokeView(mLrcView, mScoringView);
        mMusicControllerGroup = mMusicPlayerActiveView.findViewById(R.id.rlMusicControlMenu);
        mJoinChorusView = mMusicPlayerActiveView.findViewById(R.id.btnJoinChorus);
        mJoinChorusLoadingView = mMusicPlayerActiveView.findViewById(R.id.btnJoinChorusLoading);
        mLeaveChorus = mMusicPlayerActiveView.findViewById(R.id.ivLeaveChorus);
        mCumulativeScoreGrade = findViewById(R.id.ivCumulativeScoreGrade);
        mCumulativeScore = findViewById(R.id.tvCumulativeScore);
        mControllerBtn = findViewById(R.id.ivMusicVoiceSettings);
        mPresetView = findViewById(R.id.ivMusicPreset);
        mLineScore = findViewById(R.id.tvLineScore);
        mPrepareView = findViewById(R.id.il_musicplayer_prepare_view);
        mPreparePrecent = mPrepareView.findViewById(R.id.ivPrepareView);
        initListener();
    }

    private void initListener() {
        mKaraokeView.setKaraokeEvent(new KaraokeEvent() {
            @Override
            public void onDragTo(KaraokeView view, long position) {
            }

            @Override
            public void onRefPitchUpdate(float refPitch, int numberOfRefPitches) {
                if (mOnKaraokeActionListener != null) {
                    mOnKaraokeActionListener.onRefPitchUpdate(refPitch, numberOfRefPitches);
                }
            }

            @Override
            public void onLineFinished(KaraokeView view, LyricsLineModel line, int score, int cumulativeScore, int index, int total) {
                if (mOnKaraokeActionListener != null) {
                    mOnKaraokeActionListener.onLineFinished(line, score, cumulativeScore, index, total);
                }
                updateScore(score, cumulativeScore, total * 100);
            }
        });

        // 点歌
        btChooseSong.setOnClickListener(view -> {
            if (mOnKaraokeActionListener != null) {
                mOnKaraokeActionListener.onChooseSongClick();
            }
        });

        // 播放暂停
        mMusicStartBtn.setOnClickListener(view -> {
            mActionDelegate.onClickPlaying();
        });

        // 切歌
        mSwitchSongBtn.setOnClickListener(view -> {
            showSwitchSongDialog();
        });

        // 播放中点歌
        mActiveChooseSongBtn.setOnClickListener(view -> {
            if (mOnKaraokeActionListener != null) {
                mOnKaraokeActionListener.onChooseSongClick();
            }
        });

        // 原唱伴奏
        mActiveOriginalBtn.setOnClickListener(view -> {
            mActionDelegate.onClickOriginal();
        });

        // 加入合唱
        mJoinChorusView.setOnClickListener(view -> {
            mJoinChorusView.setVisibility(View.INVISIBLE);
            mJoinChorusLoadingView.setVisibility(View.VISIBLE);
            mActionDelegate.onClickJoinChorus();
        });

        // 退出合唱
        mLeaveChorus.setOnClickListener(view -> mActionDelegate.onClickLeaveChorus());

        mControllerBtn.setOnClickListener(v -> {
            showAudioControllerDialog();
        });

        mPresetView.setOnClickListener(v -> {
            showPresetDialog();
        });
    }

    @Override
    public void setMusicPlayerActionListener(IMusicPlayerActionListener listener) {
        this.mOnKaraokeActionListener = listener;
    }

    @Override
    public void setMusicPlayerActionDelegate(ActionDelegate actionDelegate) {
        this.mActionDelegate = actionDelegate;
    }

    @Override
    public void setEffectProperties(Map<Integer, Integer> effectProperties) {
        this.effectProperties.clear();
        this.effectProperties.putAll(effectProperties);
    }

    @Override
    public void setVoiceConversionList(List<ControllerEffectInfo> list) {
        this.voiceConversionList.clear();
        this.voiceConversionList.addAll(list);
    }

    public void setMusicSettingInfo(MusicSettingInfo musicSettingInfo){
        this.musicSettingInfo = musicSettingInfo;
    }

    // ---------------- UI ----------------

    public void showSwitchSongDialog() {
        if (mSwitchSongDialog == null) {
            mSwitchSongDialog = new AUIAlertDialog(getContext());
            mSwitchSongDialog.setTitle("切歌");
            mSwitchSongDialog.setMessage("是否终止当前歌曲的演唱？");
            mSwitchSongDialog.setPositiveButton("确认", view -> {
                mActionDelegate.onClickSwitchSong();
                mSwitchSongDialog.dismiss();
            });
            mSwitchSongDialog.setNegativeButton("取消", view -> mSwitchSongDialog.dismiss());
        }
        mSwitchSongDialog.show();
    }

    public void initRoomOwnerUI() {
        btChooseSong.setVisibility(View.VISIBLE);
    }

    // 播放
    public void onPlaying() {
        mainHandler.post(() -> {
            mMusicStartBtn.setActivated(true);
            mMusicStartBtn.setText(R.string.aui_musicplayer_play);
        });
    }

    // 暂停
    public void onPause() {
        mainHandler.post(() -> {
            mMusicStartBtn.setActivated(false);
            mMusicStartBtn.setText(R.string.aui_musicplayer_pause);
        });
    }

    // 原唱
    public void onOriginal() {
        mActiveOriginalBtn.setActivated(true);
    }

    // 伴唱
    public void onAcc() {
        mActiveOriginalBtn.setActivated(false);
    }

    // 设置进程
    public void setProgress(Long progress) {
        mKaraokeView.setProgress(progress);
    }

    // 设置音高
    public void setPitch(Float pitch) {
        mKaraokeView.setPitch(pitch);
    }

    // 设置下载进度
    public void setLoadProgress(int progress) {
        mainHandler.post(() -> mPreparePrecent.setText(progress + "%"));
    }

    // 上麦
    public void onSeat() {
        mainHandler.post(() -> {
            btChooseSong.setVisibility(View.VISIBLE);
            mActiveChooseSongBtn.setVisibility(View.VISIBLE);
        });
    }

    // 下麦
    public void onLeaveSeat() {
        mainHandler.post(() -> {
            btChooseSong.setVisibility(View.INVISIBLE);
            mActiveChooseSongBtn.setVisibility(View.GONE);
        });
    }

    // 未播放状态
    public void onMusicIdle(boolean isOnSeat) {
        mActiveView.setVisibility(View.GONE);
        mMusicPlayerActiveView.setVisibility(View.GONE);

        musicPlayerIdleView.setVisibility(View.VISIBLE);
        if (isOnSeat) {
            btChooseSong.setVisibility(View.VISIBLE);
        } else {
            btChooseSong.setVisibility(View.GONE);
        }
        mLrcView.reset();
        mScoringView.reset();
    }

    // 准备播放状态
    public void onMusicPrepare(String songName, boolean isAudience, boolean isRoomOwner) {
        mainHandler.post(() -> {
            mLrcView.reset();
            mScoringView.reset();
            mPrepareView.setVisibility(View.VISIBLE);
            musicPlayerIdleView.setVisibility(View.GONE);
            mLeaveChorus.setVisibility(View.GONE);
            mActiveView.setVisibility(View.VISIBLE);
            mMusicPlayerActiveView.setVisibility(View.VISIBLE);
            tvMusicName.bringToFront();
            tvMusicName.setText(" | " + songName);
            mCumulativeScore.setText("得分：0分");
            mCumulativeScore.bringToFront();
            mPreparePrecent.setText("0%");
            mActiveOriginalBtn.setActivated(false);

            if (isAudience) {
                mMusicControllerGroup.setVisibility(View.GONE);
                mJoinChorusView.setVisibility(View.VISIBLE);
                mJoinChorusLoadingView.setVisibility(View.INVISIBLE);

                if (isRoomOwner) {
                    // 房主允许切歌
                    mSwitchSongBtn.setVisibility(View.VISIBLE);
                    mActiveChooseSongBtn.setVisibility(View.VISIBLE);
                }
            } else {
                mMusicControllerGroup.setVisibility(View.VISIBLE);
                mJoinChorusView.setVisibility(View.INVISIBLE);
                mJoinChorusLoadingView.setVisibility(View.INVISIBLE);
            }
        });
    }

    // 播放中
    public void onMusicPlaying() {
        mainHandler.post(() -> {
            mGradeView.setScore(0, 0, 0);
            mPrepareView.setVisibility(View.INVISIBLE);
        });
    }

    // 准备失败
    public void onMusicPrepareFailed() {
        mainHandler.post(() -> {
            mPrepareView.setVisibility(View.INVISIBLE);
        });
    }

    // 加入合唱
    public void onJoinChorus(boolean isRoomOwner) {
        mainHandler.post(() -> {
            mJoinChorusLoadingView.setVisibility(View.INVISIBLE);
            mMusicControllerGroup.setVisibility(View.VISIBLE);
            if (!isRoomOwner) {
                mSwitchSongBtn.setVisibility(View.GONE);
            }
            mLeaveChorus.setVisibility(View.VISIBLE);
            mMusicStartBtn.setVisibility(View.GONE);
        });
    }

    // 加入合唱失败
    public void onJoinChorusFailed() {
        mainHandler.post(() -> {
            mJoinChorusLoadingView.setVisibility(View.INVISIBLE);
            mJoinChorusView.setVisibility(View.VISIBLE);
        });
    }

    // 离开合唱
    public void onLeaveChorus(boolean isRoomOwner) {
        mainHandler.post(() -> {
            mJoinChorusView.setVisibility(View.VISIBLE);
            mLeaveChorus.setVisibility(View.GONE);
            mMusicControllerGroup.setVisibility(View.GONE);
            if(isRoomOwner){
                mSwitchSongBtn.setVisibility(View.VISIBLE);
            }
        });
    }

    // ------------ 歌词下载 ----------------
    public void downloadAndSetLrcData(String lrcUrl) {
        DownloadUtils.getInstance().download(getContext(), lrcUrl, file -> {
            if (file.getName().endsWith(".zip")) {
                ZipUtils.unzipOnlyPlainXmlFilesAsync(file.getAbsolutePath(),
                        file.getAbsolutePath().replace(".zip", ""),
                        new ZipUtils.UnZipCallback() {
                            @Override
                            public void onFileUnZipped(List<String> unZipFilePaths) {
                                String xmlPath = "";
                                for (String path : unZipFilePaths) {
                                    if (path.endsWith(".xml")) {
                                        xmlPath = path;
                                        break;
                                    }
                                }

                                if (TextUtils.isEmpty(xmlPath)) {
                                    return;
                                }

                                File xmlFile = new File(xmlPath);
                                LyricsModel lyricsModel = KaraokeView.parseLyricsData(xmlFile);

                                if (lyricsModel == null) {
                                    return;
                                }

                                if (mKaraokeView != null) {
                                    mKaraokeView.setLyricsData(lyricsModel);
                                }
                            }

                            @Override
                            public void onError(Exception e) {

                            }
                        });
            } else {
                LyricsModel lyricsModel = KaraokeView.parseLyricsData(file);

                if (lyricsModel == null) {
                    return;
                }

                if (mKaraokeView != null) {
                    mKaraokeView.setLyricsData(lyricsModel);
                }
            }
        }, exception -> {
        });
    }

    // -------------- 打分 ----------------
    // TODO 预埋
    protected int mCumulativeScoreInPercentage;

    public int getCumulativeScoreInPercentage() {
        return mCumulativeScoreInPercentage;
    }

    public void updateScore(double score, double cumulativeScore, double perfectScore) {
        mCumulativeScoreInPercentage = (int) ((cumulativeScore / perfectScore) * 100);

        mGradeView.setScore((int) score, (int) cumulativeScore, (int) perfectScore);

        mCumulativeScore.setText("得分： " + String.format(getResources().getString(R.string.aui_score_formatter), "" + (int) cumulativeScore));
        int gradeDrawable = mGradeView.getCumulativeDrawable();
        if (gradeDrawable == 0) {
            mCumulativeScoreGrade.setVisibility(INVISIBLE);
        } else {
            mCumulativeScoreGrade.bringToFront();
            mCumulativeScoreGrade.setImageResource(gradeDrawable);
            mCumulativeScoreGrade.setVisibility(VISIBLE);
        }
        showScoreAnimation(score);
    }

    private float mInitialYOfScoreView; // Only for showScoreAnimation

    private void showScoreAnimation(double score) {
        int widthOfParent = ((View) (mLineScore.getParent())).getWidth();
        int marginLeft = (int) (widthOfParent * 0.4);
        ((MarginLayoutParams) (mLineScore.getLayoutParams())).leftMargin = marginLeft;
        ((MarginLayoutParams) (mLineScore.getLayoutParams())).setMarginStart(marginLeft);

        mLineScore.setText("+" + (int) score);
        mLineScore.setAlpha(1.0f);
        mLineScore.setVisibility(VISIBLE);
        if (mInitialYOfScoreView == 0) {
            mInitialYOfScoreView = mLineScore.getY();
        }

        float movingPixels = 200;
        mLineScore.animate().translationY(-movingPixels).setDuration(1000).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mLineScore.animate().alpha(0).setDuration(100).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mLineScore.setY(mInitialYOfScoreView);
                        mLineScore.setVisibility(INVISIBLE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        }).start();

    }

    // ----------------- 变声 ----------------
    private void showPresetDialog() {
        AUIBottomDialog presetDialog = new AUIBottomDialog(getContext());
        AUIMusicPlayerPresetDialogView contentView = new AUIMusicPlayerPresetDialogView(getContext(), voiceConversionList);
        contentView.setActionListener(id -> {
            if (mActionDelegate != null) {
                mActionDelegate.onVoiceConversion(id);
            }
        });
        presetDialog.setCustomView(contentView);
        presetDialog.show();
    }

    // ----------------- 音效设置 -------------
    private void showAudioControllerDialog() {
        AUIBottomDialog controllerDialog = new AUIBottomDialog(getContext());
        AUIMusicPlayerControllerDialogView contentView = new AUIMusicPlayerControllerDialogView(getContext());
        contentView.setEarMonitoring(musicSettingInfo.isEar());
        contentView.setMusicVolume(musicSettingInfo.getMusicVolume());
        contentView.setSignalVolume(musicSettingInfo.getSignalVolume());
        contentView.setMusicPitch(musicSettingInfo.getPitch());
        contentView.setEffectProperties(effectProperties);
        contentView.setCurrentEffect(musicSettingInfo.getEffectId());
        contentView.setEffectActionListener(new IMusicPlayerEffectActionListener() {
            @Override
            public void onEarChanged(boolean enable) {
                musicSettingInfo.setEar(enable);
                if (mActionDelegate != null) {
                    mActionDelegate.onEarMonitoring(enable);
                }
            }

            @Override
            public void onMusicVolChanged(int vol) {
                musicSettingInfo.setMusicVolume(vol);
                if (mActionDelegate != null) {
                    mActionDelegate.onMusicVolume(vol);
                }
            }

            @Override
            public void onSignalVolChanged(int vol) {
                musicSettingInfo.setSignalVolume(vol);
                if (mActionDelegate != null) {
                    mActionDelegate.onSignalVolume(vol);
                }
            }

            @Override
            public void onMusicPitch(int pitch) {
                musicSettingInfo.setPitch(pitch);
                if (mActionDelegate != null) {
                    mActionDelegate.onMusicPitch(pitch);
                }
            }

            @Override
            public void onAudioEffectChanged(int effectId) {
                musicSettingInfo.setEffectId(effectId);
                if (mActionDelegate != null) {
                    mActionDelegate.onAudioEffect(effectId);
                }
            }
        });
        controllerDialog.setCustomView(contentView);
        controllerDialog.show();
    }
}

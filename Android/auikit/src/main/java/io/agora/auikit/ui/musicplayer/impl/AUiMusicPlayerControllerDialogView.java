package io.agora.auikit.ui.musicplayer.impl;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.agora.auikit.R;
import io.agora.auikit.model.AUiEffectVoiceInfo;
import io.agora.auikit.ui.basic.AUIRecyclerView;
import io.agora.auikit.ui.musicplayer.listener.IMusicPlayerEffectActionListener;

/**
 * @author create by zhangwei03
 */
public class AUiMusicPlayerControllerDialogView extends FrameLayout {

    private IMusicPlayerEffectActionListener mEffectActionListener;

    private AppCompatCheckBox checkBoxSwitchInEar;
    private AppCompatSeekBar seekBarMusicVolume;
    private AppCompatSeekBar seekBarSignalVolume;
    private AppCompatSeekBar seekBarPitch;

    //耳返
    private boolean isInEar;
    //人声音量
    private int signalVolume;
    //音乐音量
    private int musicVolume;
    // 升降调
    private int pitch;
    // 音效
    private int currentEffect;

    private final List<AUiEffectVoiceInfo> effectInfoList = new ArrayList<>();

    private final Map<Integer, Pair<Integer, Integer>> effectInfoMap = new HashMap<>();

    private RecyclerView.Adapter<RecyclerView.ViewHolder> mMusicPlayerReverbAdapter;

    public AUiMusicPlayerControllerDialogView(@NonNull Context context) {
        this(context, null);
    }

    public AUiMusicPlayerControllerDialogView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AUiMusicPlayerControllerDialogView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public AUiMusicPlayerControllerDialogView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        buildEffectInfoMap();
        initView(context);
    }

    private void initView(@NonNull Context context) {
        View.inflate(context, R.layout.aui_musicplayer_controller_dialog_view, this);
        checkBoxSwitchInEar = ((AppCompatCheckBox) findViewById(R.id.switch_inear));
        checkBoxSwitchInEar.setOnCheckedChangeListener((buttonView, isChecked) -> {
            this.isInEar = isChecked;
            if (mEffectActionListener != null) {
                mEffectActionListener.onEarChanged(isChecked);
            }
        });
        seekBarMusicVolume = ((AppCompatSeekBar) findViewById(R.id.seek_music_vol));
        seekBarMusicVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                musicVolume = seekBar.getProgress();
                if (mEffectActionListener != null) {
                    mEffectActionListener.onMusicVolChanged(seekBar.getProgress());
                }
            }
        });
        seekBarSignalVolume = ((AppCompatSeekBar) findViewById(R.id.seek_signal_vol));
        seekBarSignalVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                signalVolume = seekBar.getProgress();
                if (mEffectActionListener != null) {
                    mEffectActionListener.onSignalVolChanged(seekBar.getProgress());
                }
            }
        });
        seekBarPitch = ((AppCompatSeekBar) findViewById(R.id.seek_music_pitch));
        seekBarPitch.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int pitchProgress = seekBar.getProgress();
                pitch = pitchProgress - 12;
                if (pitch > 12) pitch = 12;
                if (pitch < -12) pitch = -12;
                if (mEffectActionListener != null) {
                    mEffectActionListener.onMusicPitch(pitch);
                }
            }
        });

        AUIRecyclerView rvAudioPreset = findViewById(R.id.rv_audio_reverb);
        mMusicPlayerReverbAdapter = new RecyclerView.Adapter<>() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new RecyclerView.ViewHolder(new AUiMusicPlayerEffectItemView(parent.getContext())) {
                };
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                AUiMusicPlayerEffectItemView itemView = (AUiMusicPlayerEffectItemView) holder.itemView;
                final AUiEffectVoiceInfo effectVoiceInfo = effectInfoList.get(position);
                if (effectVoiceInfo == null) {
                    return;
                }
                if (position == 0) {
                    itemView.setPresetInnerVisibility(View.VISIBLE);
                    itemView.setPresetInnerIcon(R.drawable.aui_musicplayer_preset_none);
                    itemView.setPresetOutIcon(0);
                } else {
                    itemView.setPresetInnerVisibility(View.GONE);
                    itemView.setPresetOutIcon(effectVoiceInfo.getResId());
                }
                itemView.setItemSelected(currentEffect == effectVoiceInfo.getEffectId());
                itemView.setPresetName(itemView.getContext().getString(effectVoiceInfo.getTitle()));
                itemView.setOnClickListener(v -> {
                    currentEffect = effectVoiceInfo.getEffectId();
                    mMusicPlayerReverbAdapter.notifyDataSetChanged();
                    if (mEffectActionListener != null) {
                        mEffectActionListener.onAudioEffectChanged(effectVoiceInfo.getEffectId());
                    }
                });
            }

            @Override
            public int getItemCount() {
                return effectInfoList.size();
            }
        };
        rvAudioPreset.setAdapter(mMusicPlayerReverbAdapter);
    }

    public void setEffectActionListener(IMusicPlayerEffectActionListener listener) {
        this.mEffectActionListener = listener;
    }

    public void setEarMonitoring(boolean isInEar) {
        this.isInEar = isInEar;
        this.checkBoxSwitchInEar.setChecked(isInEar);
    }

    public void setSignalVolume(int signalVolume) {
        this.signalVolume = signalVolume;
        this.seekBarSignalVolume.setProgress(signalVolume);
    }

    public void setMusicVolume(int musicVolume) {
        this.musicVolume = musicVolume;
        this.seekBarMusicVolume.setProgress(musicVolume);
    }

    public void setMusicPitch(int pitch) {
        this.pitch = pitch;
        this.seekBarPitch.setProgress(this.pitch + 12);
    }

    public void setEffectProperties(Map<Integer, Integer> effectProperties) {
        effectInfoList.clear();
        for (int i = 0; i < effectProperties.size(); i++) {
            Pair<Integer, Integer> pair = effectInfoMap.get(i);
            Integer effectId = effectProperties.get(i);
            if (pair != null && effectId != null) {
                AUiEffectVoiceInfo info = new AUiEffectVoiceInfo(i, effectId, pair.first, pair.second);
                effectInfoList.add(info);
            }
        }
        mMusicPlayerReverbAdapter.notifyDataSetChanged();
    }


    public void setCurrentEffect(int effect) {
        this.currentEffect = effect;
        mMusicPlayerReverbAdapter.notifyDataSetChanged();
    }

    private void buildEffectInfoMap() {
        effectInfoMap.clear();
        effectInfoMap.put(0, Pair.create(R.drawable.aui_musicplayer_preset_none, R.string.aui_musicplayer_reverb_none));
        effectInfoMap.put(1, Pair.create(R.drawable.aui_musicplayer_reverb_effect1, R.string.aui_musicplayer_reverb_karaoke));
        effectInfoMap.put(2, Pair.create(R.drawable.aui_musicplayer_reverb_effect2, R.string.aui_musicplayer_reverb_concert));
        effectInfoMap.put(3, Pair.create(R.drawable.aui_musicplayer_reverb_effect3, R.string.aui_musicplayer_reverb_studio));
        effectInfoMap.put(4, Pair.create(R.drawable.aui_musicplayer_reverb_effect4, R.string.aui_musicplayer_reverb_phonograph));
        effectInfoMap.put(5, Pair.create(R.drawable.aui_musicplayer_reverb_effect5, R.string.aui_musicplayer_reverb_spacial));
        effectInfoMap.put(6, Pair.create(R.drawable.aui_musicplayer_reverb_effect6, R.string.aui_musicplayer_reverb_ethereal));
        effectInfoMap.put(7, Pair.create(R.drawable.aui_musicplayer_reverb_effect7, R.string.aui_musicplayer_reverb_popular));
        effectInfoMap.put(8, Pair.create(R.drawable.aui_musicplayer_reverb_effect8, R.string.aui_musicplayer_reverb_rnb));
    }
}

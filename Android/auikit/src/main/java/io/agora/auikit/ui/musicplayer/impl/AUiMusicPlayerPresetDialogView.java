package io.agora.auikit.ui.musicplayer.impl;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.agora.auikit.R;
import io.agora.auikit.model.AUiEffectVoiceInfo;
import io.agora.auikit.ui.basic.AUIRecyclerView;

public class AUiMusicPlayerPresetDialogView extends FrameLayout {

    private RecyclerView.Adapter<RecyclerView.ViewHolder> mMusicPlayerPresetAdapter;

    private int currentId;

    public AUiMusicPlayerPresetDialogView(@NonNull Context context) {
        this(context, null);
    }

    public AUiMusicPlayerPresetDialogView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AUiMusicPlayerPresetDialogView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public AUiMusicPlayerPresetDialogView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    private void initView(@NonNull Context context) {
        View.inflate(context, R.layout.aui_musicplayer_preset_dialog_view, this);
        AUIRecyclerView rvAudioPreset = findViewById(R.id.rv_audio_preset);
        List<AUiEffectVoiceInfo> presetList = buildPresetData();
        mMusicPlayerPresetAdapter = new RecyclerView.Adapter<>() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new RecyclerView.ViewHolder(new AUiMusicPlayerEffectItemView(parent.getContext())) {
                };
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                AUiMusicPlayerEffectItemView itemView = (AUiMusicPlayerEffectItemView) holder.itemView;
                final AUiEffectVoiceInfo effectVoiceInfo = presetList.get(position);
                if (effectVoiceInfo == null) {
                    return;
                }
                itemView.setPresetInnerIcon(effectVoiceInfo.getResId());
                itemView.setItemSelected(currentId == effectVoiceInfo.getId());
                itemView.setPresetName(itemView.getContext().getString(effectVoiceInfo.getTitle()));
                itemView.setOnClickListener(v -> {
                    currentId = effectVoiceInfo.getId();
                    mMusicPlayerPresetAdapter.notifyDataSetChanged();
                });
            }

            @Override
            public int getItemCount() {
                return presetList.size();
            }
        };
        rvAudioPreset.setAdapter(mMusicPlayerPresetAdapter);
    }

    private List<AUiEffectVoiceInfo> buildPresetData() {
        List<AUiEffectVoiceInfo> list = new ArrayList<>();
        list.add(new AUiEffectVoiceInfo(0, R.drawable.aui_musicplayer_preset_none, R.string.aui_musicplayer_preset_original));
        list.add(new AUiEffectVoiceInfo(1, R.drawable.aui_musicplayer_preset_child, R.string.aui_musicplayer_preset_child));
        list.add(new AUiEffectVoiceInfo(2, R.drawable.aui_musicplayer_preset_lolita, R.string.aui_musicplayer_preset_lolita));
        list.add(new AUiEffectVoiceInfo(3, R.drawable.aui_musicplayer_preset_uncle, R.string.aui_musicplayer_preset_uncle));
        list.add(new AUiEffectVoiceInfo(4, R.drawable.aui_musicplayer_preset_airy, R.string.aui_musicplayer_preset_airy));
        return list;
    }
}

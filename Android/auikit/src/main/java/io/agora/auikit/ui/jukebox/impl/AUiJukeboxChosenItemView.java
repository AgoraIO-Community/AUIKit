package io.agora.auikit.ui.jukebox.impl;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

import io.agora.auikit.R;
import io.agora.auikit.ui.jukebox.IAUiJukeboxChosenItemView;

public class AUiJukeboxChosenItemView extends FrameLayout implements IAUiJukeboxChosenItemView {
    public static final int PlayingTagLocation_aboveOrder = 1;
    public static final int PlayingTagLocation_toTextStart = 2;
    private TextView tvSongName;
    private TextView tvSingerName;
    private TextView tvOrder;
    private ImageView ivPlayingTag;
    private TextView tvPlaying;
    private TextView tvDelete;
    private TextView tvTop;

    private ShapeableImageView shapeableIvCover;

    private int playingTagLocation;

    public AUiJukeboxChosenItemView(@NonNull Context context) {
        this(context, null);
    }

    public AUiJukeboxChosenItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AUiJukeboxChosenItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray themeTa = context.obtainStyledAttributes(attrs, R.styleable.AUiJukeboxChosenItemView, defStyleAttr, 0);
        int appearanceId = themeTa.getResourceId(R.styleable.AUiJukeboxChosenItemView_aui_jukeboxChosenItem_appearance, 0);
        themeTa.recycle();
        initView(context, appearanceId);
    }

    private void initView(@NonNull Context context, @StyleRes  int appearanceId){
        TypedArray typedArray = context.obtainStyledAttributes(appearanceId, R.styleable.AUiJukeboxChosenItemView);
        playingTagLocation = typedArray.getInt(R.styleable.AUiJukeboxChosenItemView_aui_jukeboxChosenItem_playingTagLocation, PlayingTagLocation_aboveOrder);
        typedArray.recycle();

        View.inflate(context, R.layout.aui_jukebox_chosen_item, this);

        tvSongName = findViewById(R.id.tv_song_name);
        tvSingerName = findViewById(R.id.tv_singer_name);
        tvOrder = findViewById(R.id.tv_order);
        ivPlayingTag = findViewById(R.id.iv_playing_tag);
        tvPlaying = findViewById(R.id.tv_playing);
        tvTop = findViewById(R.id.tv_top);
        tvDelete = findViewById(R.id.tv_delete);
        shapeableIvCover = findViewById(R.id.iv_cover);

        if (playingTagLocation == PlayingTagLocation_toTextStart) {
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) ivPlayingTag.getLayoutParams();
            layoutParams.endToStart = R.id.tv_playing;
            ivPlayingTag.setLayoutParams(layoutParams);
        }
    }

    public void setSongName(CharSequence name){
        tvSongName.setText(name);
    }

    public void setSingerName(CharSequence name){
        tvSingerName.setText(name);
    }

    public void setOrder(CharSequence order){
        tvOrder.setText(order);
    }

    public void setSongCover(@Nullable CharSequence songCover){
        Glide.with(shapeableIvCover).load(songCover).error(R.drawable.aui_jukebox_music_icon).into(shapeableIvCover);
    }

    /** 设置视图状态
     *
     * @param isCurrent 该条目是顺序首尾
     * @param ctrlAble 是否可以进行控制（切歌/置顶）
     * @param deleteAble 是否可以进行删除
     */
    @Override
    public void setViewStatus(boolean isCurrent, boolean ctrlAble, boolean deleteAble){
        if (isCurrent) {
            // 演唱中
            if(playingTagLocation == PlayingTagLocation_aboveOrder){
                tvOrder.setVisibility(View.INVISIBLE);
            } else {
                tvOrder.setVisibility(View.VISIBLE);
            }
            if (ctrlAble) {
                tvPlaying.setVisibility(View.VISIBLE);
            } else {
                tvPlaying.setVisibility(View.GONE);
            }
            ivPlayingTag.setVisibility(View.VISIBLE);
            tvDelete.setVisibility(View.GONE);
            tvTop.setVisibility(View.GONE);
        } else {
            if (ctrlAble) {
                tvTop.setVisibility(View.VISIBLE);
            } else {
                tvTop.setVisibility(View.GONE);
            }
            if (deleteAble) {
                tvDelete.setVisibility(View.VISIBLE);
            } else {
                tvDelete.setVisibility(View.GONE);
            }
            ivPlayingTag.setVisibility(View.GONE);
            tvPlaying.setVisibility(View.GONE);
            tvOrder.setVisibility(View.VISIBLE);
        }
    }

    public void setOnDeleteClickListener(View.OnClickListener listener) {
        tvDelete.setOnClickListener(listener);
    }

    public void setOnTopClickListener(View.OnClickListener listener) {
        tvTop.setOnClickListener(listener);
    }

    public void setOnPlayingClickListener(View.OnClickListener listener) {
        tvPlaying.setOnClickListener(listener);
    }



}

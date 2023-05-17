package io.agora.auikit.ui.jukebox.impl;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

import io.agora.auikit.R;

public class AUiJukeboxChooseItemView extends FrameLayout {

    private CheckBox cbChoose;
    private TextView tvSongName;
    private TextView tvSingerName;
    private ShapeableImageView shapeableIvCover;

    public AUiJukeboxChooseItemView(@NonNull Context context) {
        this(context, null);
    }

    public AUiJukeboxChooseItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AUiJukeboxChooseItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(@NonNull Context context){
        View.inflate(context, R.layout.aui_jukebox_choose_item, this);
        cbChoose = findViewById(R.id.cb_choose);
        tvSongName = findViewById(R.id.tv_song_name);
        tvSingerName = findViewById(R.id.tv_singer_name);
        shapeableIvCover = findViewById(R.id.iv_cover);
    }

    public void setSongName(@Nullable CharSequence songName){
        tvSongName.setText(songName);
    }

    public void setSingerName(@Nullable CharSequence singerName){
        tvSingerName.setText(singerName);
    }

    public void setChooseCheck(boolean check) {
        cbChoose.setChecked(check);
        cbChoose.setClickable(!check);
    }

    public void setOnChooseChangeListener(@Nullable CompoundButton.OnCheckedChangeListener listener){
        cbChoose.setOnCheckedChangeListener(listener);
    }

    public void setSongCover(@Nullable CharSequence songCover){
        Glide.with(shapeableIvCover).load(songCover).error(R.drawable.aui_jukebox_music_icon).into(shapeableIvCover);
    }
}

package io.agora.auikit.ui.musicplayer.impl;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.google.android.material.imageview.ShapeableImageView;

import io.agora.auikit.R;

public class AUiMusicPlayerEffectItemView extends FrameLayout {

    private TextView tvPresetName;
    private ShapeableImageView effectPresetOut;
    private AppCompatImageView effectPresetInner;

    private int outStokeColor;

    public AUiMusicPlayerEffectItemView(@NonNull Context context) {
        this(context, null);
    }

    public AUiMusicPlayerEffectItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AUiMusicPlayerEffectItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, R.layout.aui_musicplayer_effect_item_view, this);
        TypedArray themeTa = context.obtainStyledAttributes(attrs, R.styleable.AUiMusicPlayerEffectPresetItem, defStyleAttr, 0);
        int appearanceId = themeTa.getResourceId(R.styleable.AUiMusicPlayerEffectPresetItem_aui_musicPlayerEffectPresetItem_appearance, 0);
        themeTa.recycle();
        TypedArray typedArray = context.obtainStyledAttributes(appearanceId, R.styleable.AUiMusicPlayerEffectPresetItem);
        outStokeColor = typedArray.getColor(R.styleable.AUiMusicPlayerEffectPresetItem_aui_musicPlayerEffectPresetItem_outStokeColor, Color.BLACK);
        typedArray.recycle();

        tvPresetName = findViewById(R.id.tv_reverb_name);
        effectPresetOut = findViewById(R.id.iv_effect_out);
        effectPresetInner = findViewById(R.id.iv_effect_inner);
    }

    public void setPresetName(@Nullable CharSequence songName) {
        tvPresetName.setText(songName);
    }

    public void setItemSelected(boolean isSelected) {
        if (isSelected) {
            effectPresetOut.setStrokeColor(ColorStateList.valueOf(outStokeColor));
        } else {
            effectPresetOut.setStrokeColorResource(android.R.color.transparent);
        }
    }

    public void setPresetOutIcon(@DrawableRes int resId) {
        effectPresetOut.setImageResource(resId);
    }

    public void setPresetInnerIcon(@DrawableRes int resId) {
        effectPresetInner.setImageResource(resId);
    }

    public void setPresetInnerVisibility(@DrawableRes int visibility) {
        effectPresetInner.setVisibility(visibility);
    }
}

package io.agora.auikit.ui.basic;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.google.android.material.imageview.ShapeableImageView;

public class AUIImageView extends ShapeableImageView {
    public AUIImageView(Context context) {
        this(context, null);
    }

    public AUIImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public AUIImageView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
}

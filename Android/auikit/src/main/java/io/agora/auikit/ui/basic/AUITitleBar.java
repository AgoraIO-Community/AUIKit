package io.agora.auikit.ui.basic;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import io.agora.auikit.R;

public class AUITitleBar extends ConstraintLayout {

    public static final int MODE_HOME = 0x01;
    public static final int MODE_BACK = 0x02;
    public static final int MODE_STEP = 0x03;

    private TextView tvTitle;
    private TextView tvStepPrevious;
    private TextView tvStepNext;
    private ImageView ivBack;


    public AUITitleBar(@NonNull Context context) {
        this(context, null);
    }

    public AUITitleBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AUITitleBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.style.AUITitleBar);
        initView();
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AUITitleBar, defStyleAttr, R.style.AUITitleBar);

        // 标题
        String titleText = typedArray.getString(R.styleable.AUITitleBar_aui_titleBar_titleText);
        int titleColor = typedArray.getColor(R.styleable.AUITitleBar_aui_titleBar_titleTextColor, Color.BLACK);
        int titleSize = typedArray.getDimensionPixelSize(R.styleable.AUITitleBar_aui_titleBar_titleTextSize, 0);
        int titleMaxWidth = typedArray.getDimensionPixelSize(R.styleable.AUITitleBar_aui_titleBar_titleMaxWidth, 0);
        tvTitle.setText(titleText);
        tvTitle.setTextColor(titleColor);
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleSize);
        tvTitle.setSingleLine();
        tvTitle.setEllipsize(TextUtils.TruncateAt.END);
        if (titleMaxWidth > 0) {
            tvTitle.setMaxWidth(titleMaxWidth);
        }

        Drawable backDrawable = typedArray.getDrawable(R.styleable.AUITitleBar_aui_titleBar_backDrawable);
        int backWidth = typedArray.getLayoutDimension(R.styleable.AUITitleBar_aui_titleBar_backWidth, LayoutParams.WRAP_CONTENT);
        int backHeight = typedArray.getLayoutDimension(R.styleable.AUITitleBar_aui_titleBar_backHeight, LayoutParams.WRAP_CONTENT);
        ViewGroup.LayoutParams backParams = ivBack.getLayoutParams();
        backParams.width = backWidth;
        backParams.height = backHeight;
        ivBack.setLayoutParams(backParams);
        ivBack.setImageDrawable(backDrawable);

        int stepTextSize = typedArray.getDimensionPixelSize(R.styleable.AUITitleBar_aui_titleBar_stepTextSize, 0);
        int stepTextColor = typedArray.getColor(R.styleable.AUITitleBar_aui_titleBar_stepTextColor, 0);
        String stepPreviousText = typedArray.getString(R.styleable.AUITitleBar_aui_titleBar_stepPreviousText);
        String stepNextText = typedArray.getString(R.styleable.AUITitleBar_aui_titleBar_stepNextText);
        tvStepPrevious.setTextSize(TypedValue.COMPLEX_UNIT_PX, stepTextSize);
        tvStepNext.setTextSize(TypedValue.COMPLEX_UNIT_PX, stepTextSize);
        tvStepPrevious.setTextColor(stepTextColor);
        tvStepNext.setTextColor(stepTextColor);
        tvStepPrevious.setText(stepPreviousText);
        tvStepNext.setText(stepNextText);

        // 显示模式
        int mode = typedArray.getInt(R.styleable.AUITitleBar_aui_titleBar_mode, MODE_HOME);
        setMode(mode);


        typedArray.recycle();
    }

    public void setMode(int mode) {
        if (mode == MODE_BACK) {
            tvStepPrevious.setVisibility(View.GONE);
            tvStepNext.setVisibility(View.GONE);
            ivBack.setVisibility(View.VISIBLE);
        } else if (mode == MODE_STEP) {
            tvStepPrevious.setVisibility(View.VISIBLE);
            tvStepNext.setVisibility(View.VISIBLE);
            ivBack.setVisibility(View.GONE);
        } else {
            tvStepPrevious.setVisibility(View.GONE);
            tvStepNext.setVisibility(View.GONE);
            ivBack.setVisibility(View.GONE);
        }
    }

    public void setOnBackClickListener(View.OnClickListener listener) {
        ivBack.setOnClickListener(listener);
    }

    public void setOnStepClickListener(View.OnClickListener previousListener, View.OnClickListener nextListener) {
        tvStepPrevious.setOnClickListener(previousListener);
        tvStepNext.setOnClickListener(nextListener);
    }

    private void initView() {
        View.inflate(getContext(), R.layout.aui_title_bar_layout, this);
        tvTitle = findViewById(R.id.tvTitle);
        ivBack = findViewById(R.id.ivBack);
        tvStepPrevious = findViewById(R.id.tvStepPrevious);
        tvStepNext = findViewById(R.id.tvStepNext);
    }
}

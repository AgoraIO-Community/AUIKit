package io.agora.auikit.ui.basic;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import io.agora.auikit.R;

public class AUIButton extends ConstraintLayout {

    private TextView tvText;
    private ImageView ivDrawableStart, ivDrawableEnd, ivDrawableTop, ivDrawableBottom, ivDrawableCenter;

    enum BackgroundMode {
        Solid(0x01),
        Stroke(0x02);

        private final int value;

        BackgroundMode(int value) {
            this.value = value;
        }

    }

    public AUIButton(Context context) {
        this(context, null);
    }

    public AUIButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AUIButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.style.AUIButton);

        initView();


        int apResId = R.style.AUIButton;
        TypedValue outValue = new TypedValue();
        if (context.getTheme().resolveAttribute(R.attr.aui_button_appearance, outValue, true)) {
            apResId = outValue.resourceId;
        }

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AUIButton, defStyleAttr, apResId);
        float dimensionNone = -2f;

        // Background
        Drawable bgDrawable = typedArray.getDrawable(R.styleable.AUIButton_aui_button_background);
        setBackground(bgDrawable);

        // Text style
        String text = typedArray.getString(R.styleable.AUIButton_aui_button_text);
        float textSize = typedArray.getDimension(R.styleable.AUIButton_aui_button_textSize, 0);
        int textNormalColor = typedArray.getColor(R.styleable.AUIButton_aui_button_textNormalColor, Color.BLACK);
        int textPressedColor = typedArray.getColor(R.styleable.AUIButton_aui_button_textPressedColor, Color.BLACK);
        int textDisableColor = typedArray.getColor(R.styleable.AUIButton_aui_button_textDisableColor, Color.BLACK);
        tvText.setText(text);
        tvText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        tvText.setTextColor(new ColorStateList(
                new int[][]{{android.R.attr.state_pressed}, {-android.R.attr.state_enabled}, {}}, new int[]{textPressedColor, textDisableColor, textNormalColor}
        ));

        // Drawables
        int drawablePadding = typedArray.getDimensionPixelOffset(R.styleable.AUIButton_aui_button_drawablePadding, 0);
        int drawablePaddingStart = typedArray.getDimensionPixelOffset(R.styleable.AUIButton_aui_button_drawablePaddingStart, 0);
        int drawablePaddingEnd = typedArray.getDimensionPixelOffset(R.styleable.AUIButton_aui_button_drawablePaddingEnd, 0);
        int drawablePaddingTop = typedArray.getDimensionPixelOffset(R.styleable.AUIButton_aui_button_drawablePaddingTop, 0);
        int drawablePaddingBottom = typedArray.getDimensionPixelOffset(R.styleable.AUIButton_aui_button_drawablePaddingBottom, 0);

        int drawableMargin = typedArray.getDimensionPixelOffset(R.styleable.AUIButton_aui_button_drawableMargin, 0);
        int drawableMarginStart = typedArray.getDimensionPixelOffset(R.styleable.AUIButton_aui_button_drawableMarginStart, 0);
        int drawableMarginEnd = typedArray.getDimensionPixelOffset(R.styleable.AUIButton_aui_button_drawableMarginEnd, 0);
        int drawableMarginTop = typedArray.getDimensionPixelOffset(R.styleable.AUIButton_aui_button_drawableMarginTop, 0);
        int drawableMarginBottom = typedArray.getDimensionPixelOffset(R.styleable.AUIButton_aui_button_drawableMarginBottom, 0);

        int drawableWidth = typedArray.getLayoutDimension(R.styleable.AUIButton_aui_button_drawableWidth, LayoutParams.WRAP_CONTENT);
        int drawableHeight = typedArray.getLayoutDimension(R.styleable.AUIButton_aui_button_drawableHeight, LayoutParams.WRAP_CONTENT);

        boolean drawableToEdge = typedArray.getBoolean(R.styleable.AUIButton_aui_button_drawableToEdge, false);

        LayoutParams drawableLayoutParams = new LayoutParams(drawableWidth, drawableHeight);
        if (drawableMargin > 0) {
            drawableLayoutParams.leftMargin
                    = drawableLayoutParams.topMargin
                    = drawableLayoutParams.rightMargin
                    = drawableLayoutParams.bottomMargin
                    = drawableMargin;
        } else {
            drawableLayoutParams.leftMargin = drawableMarginStart;
            drawableLayoutParams.topMargin = drawableMarginTop;
            drawableLayoutParams.rightMargin = drawableMarginEnd;
            drawableLayoutParams.bottomMargin = drawableMarginBottom;
        }
        drawableLayoutParams.verticalChainStyle = drawableToEdge? LayoutParams.CHAIN_SPREAD_INSIDE: LayoutParams.CHAIN_PACKED;
        drawableLayoutParams.horizontalChainStyle = drawableToEdge? LayoutParams.CHAIN_SPREAD_INSIDE: LayoutParams.CHAIN_PACKED;

        boolean drawableRotateEnable = typedArray.getBoolean(R.styleable.AUIButton_aui_button_drawableRotateAnimEnable, false);
        int drawableRotateDuration = typedArray.getInt(R.styleable.AUIButton_aui_button_drawableRotateAnimDuration, 200);

        int drawableTint = typedArray.getInt(R.styleable.AUIButton_aui_button_drawableTint, -1);
        int drawablePressedTint = typedArray.getInt(R.styleable.AUIButton_aui_button_drawablePressedTint, -1);
        int drawableDisableTint = typedArray.getInt(R.styleable.AUIButton_aui_button_drawableDisableTint, -1);
        ColorStateList drawableTintList = new ColorStateList(new int[][]{{android.R.attr.state_pressed}, {-android.R.attr.state_enabled}, {}}, new int[]{drawablePressedTint, drawableDisableTint, drawableTint});
        //      DrawableStart
        setupDrawable(drawablePadding,
                drawablePaddingStart,
                drawablePaddingEnd,
                drawablePaddingTop,
                drawablePaddingBottom,
                drawableLayoutParams,
                drawableRotateEnable,
                drawableRotateDuration,
                typedArray.getDrawable(R.styleable.AUIButton_aui_button_drawableStart),
                drawableTintList,
                ivDrawableStart);
        //      DrawableTop
        setupDrawable(drawablePadding,
                drawablePaddingStart,
                drawablePaddingEnd,
                drawablePaddingTop,
                drawablePaddingBottom,
                drawableLayoutParams,
                drawableRotateEnable,
                drawableRotateDuration,
                typedArray.getDrawable(R.styleable.AUIButton_aui_button_drawableTop),
                drawableTintList,
                ivDrawableTop);
        //      DrawableEnd
        setupDrawable(drawablePadding,
                drawablePaddingStart,
                drawablePaddingEnd,
                drawablePaddingTop,
                drawablePaddingBottom,
                drawableLayoutParams,
                drawableRotateEnable,
                drawableRotateDuration,
                typedArray.getDrawable(R.styleable.AUIButton_aui_button_drawableEnd),
                drawableTintList,
                ivDrawableEnd);
        //      DrawableBottom
        setupDrawable(drawablePadding,
                drawablePaddingStart,
                drawablePaddingEnd,
                drawablePaddingTop,
                drawablePaddingBottom,
                drawableLayoutParams,
                drawableRotateEnable,
                drawableRotateDuration,
                typedArray.getDrawable(R.styleable.AUIButton_aui_button_drawableBottom),
                drawableTintList,
                ivDrawableBottom);
        //      DrawableCenter
        setupDrawable(drawablePadding,
                drawablePaddingStart,
                drawablePaddingEnd,
                drawablePaddingTop,
                drawablePaddingBottom,
                drawableLayoutParams,
                drawableRotateEnable,
                drawableRotateDuration,
                typedArray.getDrawable(R.styleable.AUIButton_aui_button_drawableCenter),
                drawableTintList,
                ivDrawableCenter);

        // enable
        boolean enable = typedArray.getBoolean(R.styleable.AUIButton_android_enabled, true);
        setEnabled(enable);

        typedArray.recycle();

    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        ivDrawableStart.setEnabled(enabled);
        ivDrawableTop.setEnabled(enabled);
        ivDrawableEnd.setEnabled(enabled);
        ivDrawableBottom.setEnabled(enabled);
        ivDrawableCenter.setEnabled(enabled);
        tvText.setEnabled(enabled);
        setClickable(enabled);
        setFocusable(enabled);
    }

    private void setupDrawable(int drawablePadding,
                               int drawablePaddingStart,
                               int drawablePaddingEnd,
                               int drawablePaddingTop,
                               int drawablePaddingBottom,
                               LayoutParams drawableLayoutParams,
                               boolean drawableRotateEnable,
                               int drawableRotateDuration,
                               Drawable drawableStart,
                               ColorStateList tintList,
                               ImageView imageView) {
        if (drawableStart != null) {
            imageView.setVisibility(View.VISIBLE);
            if (drawablePadding > 0) {
                imageView.setPadding(drawablePadding, drawablePadding, drawablePadding, drawablePadding);
            } else {
                imageView.setPadding(drawablePaddingStart, drawablePaddingTop, drawablePaddingEnd, drawablePaddingBottom);
            }
            LayoutParams layoutParams = (LayoutParams) imageView.getLayoutParams();
            layoutParams.width = drawableLayoutParams.width;
            layoutParams.height = drawableLayoutParams.height;
            layoutParams.leftMargin = drawableLayoutParams.leftMargin;
            layoutParams.topMargin = drawableLayoutParams.topMargin;
            layoutParams.rightMargin = drawableLayoutParams.rightMargin;
            layoutParams.bottomMargin = drawableLayoutParams.bottomMargin;
            layoutParams.horizontalChainStyle = drawableLayoutParams.horizontalChainStyle;
            layoutParams.verticalChainStyle = drawableLayoutParams.verticalChainStyle;
            imageView.setLayoutParams(layoutParams);
            imageView.setImageDrawable(drawableStart);

            imageView.setImageTintList(tintList);

            if (drawableRotateEnable) {
                RotateAnimation animation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                animation.setDuration(drawableRotateDuration);
                animation.setRepeatCount(RotateAnimation.INFINITE);
                animation.setRepeatMode(RotateAnimation.RESTART);
                animation.setInterpolator(new LinearInterpolator());
                imageView.startAnimation(animation);
            } else {
                imageView.clearAnimation();
            }
        } else {
            LayoutParams layoutParams = (LayoutParams) imageView.getLayoutParams();
            layoutParams.width = layoutParams.height = LayoutParams.WRAP_CONTENT;
            layoutParams.leftMargin = layoutParams.topMargin = layoutParams.rightMargin = layoutParams.bottomMargin = 0;
            imageView.setLayoutParams(layoutParams);
            imageView.setVisibility(View.INVISIBLE);
            imageView.setPadding(0, 0, 0, 0);
        }
    }

    private void initView() {
        View.inflate(getContext(), R.layout.aui_button_layout, this);
        tvText = findViewById(R.id.text);
        ivDrawableStart = findViewById(R.id.drawableStart);
        ivDrawableEnd = findViewById(R.id.drawableEnd);
        ivDrawableTop = findViewById(R.id.drawableTop);
        ivDrawableBottom = findViewById(R.id.drawableBottom);
        ivDrawableCenter = findViewById(R.id.drawableCenter);
    }

    private GradientDrawable createGradientDrawable(
            float cornersRadius,
            float cornersTopLeftRadius,
            float cornersTopRightRadius,
            float cornersBottomLeftRadius,
            float cornersBottomRightRadius,
            int color,
            int bgMode
    ) {
        GradientDrawable drawable = new GradientDrawable();
        if (cornersRadius > 0) {
            drawable.setCornerRadius(cornersRadius);
        } else {
            drawable.setCornerRadii(new float[]{
                    cornersTopLeftRadius, cornersTopLeftRadius,
                    cornersTopRightRadius, cornersTopRightRadius,
                    cornersBottomLeftRadius, cornersBottomLeftRadius,
                    cornersBottomRightRadius, cornersBottomRightRadius
            });
        }

        if (bgMode == BackgroundMode.Stroke.value) {
            drawable.setStroke(2, color);
        } else {
            drawable.setColor(color);
        }
        return drawable;
    }

    public void setText(String text) {
        tvText.setText(text);
    }

    public void setCenterDrawable(Drawable drawable) {
        ivDrawableCenter.setImageDrawable(drawable);
    }


}

package io.agora.auikit.ui.basic;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;
import java.util.List;

import io.agora.auikit.R;

public class AUiEditText extends FrameLayout {

    private final List<Runnable> normalChangeUiRunList = new ArrayList<>();
    private final List<Runnable> errorChangeUiRunList = new ArrayList<>();
    private TextView tvTitle;
    private TextView tvPhoneArea;
    private View vPhoneAreaLine;
    private EditText etInput;
    private CheckBox cbPasswordIcon;
    private ImageView ivLeft;
    private ImageView ivRight;
    private TextView tvTip;
    private ConstraintLayout llInput;

    public static final int BackgroundType_Box = 0x01;
    public static final int BackgroundType_Line = 0x02;
    public static final int Status_Normal = 0x01;
    public static final int Status_Error = 0x02;

    public static final int InputType_Text = 0x01;
    public static final int InputType_Password = 0x02;
    public static final int InputType_Phone = 0x03;

    public AUiEditText(@NonNull Context context) {
        this(context, null);
    }

    public AUiEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AUiEditText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();

        int styleId = R.style.AUiEditText;
        TypedValue outValue = new TypedValue();
        if (context.getTheme().resolveAttribute(R.attr.aui_editText_appearance, outValue, true)) {
            styleId = outValue.resourceId;
        }

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AUiEditText, defStyleAttr, styleId);

        // 小标题
        String titleText = typedArray.getString(R.styleable.AUiEditText_aui_edittext_title_text);
        int titleTextColor = typedArray.getColor(R.styleable.AUiEditText_aui_edittext_title_textColor, Color.BLACK);
        int titleTextSize = typedArray.getDimensionPixelSize(R.styleable.AUiEditText_aui_edittext_title_textSize, 0);
        int titlePadding = typedArray.getDimensionPixelSize(R.styleable.AUiEditText_aui_edittext_title_padding, 0);
        int titlePaddingStart = typedArray.getDimensionPixelSize(R.styleable.AUiEditText_aui_edittext_title_paddingStart, 0);
        int titlePaddingTop = typedArray.getDimensionPixelSize(R.styleable.AUiEditText_aui_edittext_title_paddingTop, 0);
        int titlePaddingEnd = typedArray.getDimensionPixelSize(R.styleable.AUiEditText_aui_edittext_title_paddingEnd, 0);
        int titlePaddingBottom = typedArray.getDimensionPixelSize(R.styleable.AUiEditText_aui_edittext_title_paddingBottom, 0);
        if (titlePadding > 0) {
            tvTitle.setPadding(titlePadding, titlePadding, titlePadding, titlePadding);
        } else {
            tvTitle.setPadding(titlePaddingStart, titlePaddingTop, titlePaddingEnd, titlePaddingBottom);
        }
        setTitle(titleText);
        tvTitle.setTextColor(titleTextColor);
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleTextSize);

        // 提示信息
        String tipText = typedArray.getString(R.styleable.AUiEditText_aui_edittext_tip_text);
        int tipTextColor = typedArray.getColor(R.styleable.AUiEditText_aui_edittext_tip_textColor, Color.BLACK);
        int tipTextErrorColor = typedArray.getColor(R.styleable.AUiEditText_aui_edittext_tip_textErrorColor, Color.BLACK);
        int tipTextSize = typedArray.getDimensionPixelSize(R.styleable.AUiEditText_aui_edittext_tip_textSize, 0);
        int tipPadding = typedArray.getDimensionPixelSize(R.styleable.AUiEditText_aui_edittext_tip_padding, 0);
        int tipPaddingStart = typedArray.getDimensionPixelSize(R.styleable.AUiEditText_aui_edittext_tip_paddingStart, 0);
        int tipPaddingTop = typedArray.getDimensionPixelSize(R.styleable.AUiEditText_aui_edittext_tip_paddingTop, 0);
        int tipPaddingEnd = typedArray.getDimensionPixelSize(R.styleable.AUiEditText_aui_edittext_tip_paddingEnd, 0);
        int tipPaddingBottom = typedArray.getDimensionPixelSize(R.styleable.AUiEditText_aui_edittext_tip_paddingBottom, 0);
        if (tipPadding > 0) {
            tvTip.setPadding(tipPadding, tipPadding, tipPadding, tipPadding);
        } else {
            tvTip.setPadding(tipPaddingStart, tipPaddingTop, tipPaddingEnd, tipPaddingBottom);
        }
        setTipText(tipText);
        tvTip.setTextSize(TypedValue.COMPLEX_UNIT_PX, tipTextSize);
        normalChangeUiRunList.add(() -> tvTip.setTextColor(tipTextColor));
        errorChangeUiRunList.add(() -> tvTip.setTextColor(tipTextErrorColor));

        // 输入框字体
        String inputText = typedArray.getString(R.styleable.AUiEditText_aui_edittext_input_text);
        int inputTextColor = typedArray.getColor(R.styleable.AUiEditText_aui_edittext_input_textColor, Color.BLACK);
        int inputTextSize = typedArray.getDimensionPixelSize(R.styleable.AUiEditText_aui_edittext_input_textSize, 0);
        String inputHintText = typedArray.getString(R.styleable.AUiEditText_aui_edittext_input_hintText);
        int inputHintTextColor = typedArray.getColor(R.styleable.AUiEditText_aui_edittext_input_hintTextColor, Color.GRAY);
        etInput.setText(inputText);
        etInput.setTextColor(inputTextColor);
        etInput.setTextSize(TypedValue.COMPLEX_UNIT_PX, inputTextSize);
        etInput.setHint(inputHintText);
        etInput.setHintTextColor(inputHintTextColor);

        // 输入框间距
        int inputPadding = typedArray.getDimensionPixelSize(R.styleable.AUiEditText_aui_edittext_input_padding, 0);
        int inputPaddingStart = typedArray.getDimensionPixelSize(R.styleable.AUiEditText_aui_edittext_input_paddingStart, 0);
        int inputPaddingTop = typedArray.getDimensionPixelSize(R.styleable.AUiEditText_aui_edittext_input_paddingTop, 0);
        int inputPaddingEnd = typedArray.getDimensionPixelSize(R.styleable.AUiEditText_aui_edittext_input_paddingEnd, 0);
        int inputPaddingBottom = typedArray.getDimensionPixelSize(R.styleable.AUiEditText_aui_edittext_input_paddingBottom, 0);
        if (inputPadding > 0) {
            llInput.setPadding(inputPadding, 0, inputPadding, 0);
            etInput.setPadding(0, inputPadding, 0, inputPadding);
        } else {
            llInput.setPadding(inputPaddingStart, 0, inputPaddingEnd, 0);
            etInput.setPadding(0, inputPaddingTop, 0, inputPaddingBottom);
        }

        // 输入框背景
        int backgroundType = typedArray.getInt(R.styleable.AUiEditText_aui_edittext_input_backgroundType, BackgroundType_Box);
        int backgroundColor = typedArray.getColor(R.styleable.AUiEditText_aui_edittext_input_backgroundColor, 0);
        int backgroundHighlightWidth = typedArray.getDimensionPixelSize(R.styleable.AUiEditText_aui_edittext_input_backgroundHighlightWidth, 0);
        int backgroundHighlightColor = typedArray.getColor(R.styleable.AUiEditText_aui_edittext_input_backgroundHighlightColor, 0);
        int backgroundErrorColor = typedArray.getColor(R.styleable.AUiEditText_aui_edittext_input_backgroundErrorColor, 0);
        int backgroundErrorHighlightColor = typedArray.getColor(R.styleable.AUiEditText_aui_edittext_input_backgroundErrorHighlightColor, 0);
        int backgroundBoxRadius = typedArray.getDimensionPixelSize(R.styleable.AUiEditText_aui_edittext_input_backgroundBoxRadius, 0);
        StateListDrawable backgroundDrawable = new StateListDrawable();
        StateListDrawable backgroundErrorDrawable = new StateListDrawable();
        if (backgroundType == BackgroundType_Box) {
            backgroundDrawable.addState(new int[]{android.R.attr.state_activated}, createShapeDrawable(backgroundColor, backgroundBoxRadius, backgroundHighlightWidth, backgroundHighlightColor));
            backgroundDrawable.addState(new int[]{}, createShapeDrawable(backgroundColor, backgroundBoxRadius, 0, 0));
            backgroundErrorDrawable.addState(new int[]{android.R.attr.state_activated}, createShapeDrawable(backgroundErrorColor, backgroundBoxRadius, backgroundHighlightWidth, backgroundErrorHighlightColor));
            backgroundErrorDrawable.addState(new int[]{}, createShapeDrawable(backgroundErrorColor, backgroundBoxRadius, 0, 0));
        } else {
            backgroundDrawable.addState(new int[]{android.R.attr.state_activated}, createBottomLineDrawable(backgroundHighlightColor, backgroundHighlightWidth));
            backgroundDrawable.addState(new int[]{}, createBottomLineDrawable(backgroundColor, backgroundHighlightWidth));
            backgroundErrorDrawable.addState(new int[]{android.R.attr.state_activated}, createBottomLineDrawable(backgroundErrorHighlightColor, backgroundHighlightWidth));
            backgroundErrorDrawable.addState(new int[]{}, createBottomLineDrawable(backgroundErrorColor, backgroundHighlightWidth));
        }
        normalChangeUiRunList.add(() -> {
            llInput.setBackground(backgroundDrawable);
            setInputSelectHandlerColor(backgroundHighlightColor);
        });
        errorChangeUiRunList.add(() -> {
            llInput.setBackground(backgroundErrorDrawable);
            setInputSelectHandlerColor(backgroundErrorHighlightColor);
        });

        // 状态
        int status = typedArray.getInt(R.styleable.AUiEditText_aui_edittext_status, Status_Normal);
        if (status == Status_Normal) {
            for (Runnable runnable : normalChangeUiRunList) {
                runnable.run();
            }
        } else {
            for (Runnable runnable : errorChangeUiRunList) {
                runnable.run();
            }
        }

        // 输入类型
        int inputType = typedArray.getInt(R.styleable.AUiEditText_aui_edittext_input_type, InputType_Text);
        if (inputType == InputType_Phone) {
            // 手机区号
            tvPhoneArea.setVisibility(View.VISIBLE);
            vPhoneAreaLine.setVisibility(View.VISIBLE);
            cbPasswordIcon.setVisibility(View.GONE);

            String inputAreaText = typedArray.getString(R.styleable.AUiEditText_aui_edittext_input_phoneAreaText);
            int inputAreaTextSize = typedArray.getDimensionPixelSize(R.styleable.AUiEditText_aui_edittext_input_phoneAreaTextSize, 0);
            int inputAreaTextColor = typedArray.getColor(R.styleable.AUiEditText_aui_edittext_input_phoneAreaTextColor, 0);
            tvPhoneArea.setText(inputAreaText);
            tvPhoneArea.setTextSize(TypedValue.COMPLEX_UNIT_PX, inputAreaTextSize);
            tvPhoneArea.setTextColor(inputAreaTextColor);

            int inputAreaLineColor = typedArray.getColor(R.styleable.AUiEditText_aui_edittext_input_phoneAreaLineColor, 0);
            int inputAreaLineWidth = typedArray.getDimensionPixelSize(R.styleable.AUiEditText_aui_edittext_input_phoneAreaLineWidth, 0);
            int inputAreaLineInsertLeft = typedArray.getDimensionPixelSize(R.styleable.AUiEditText_aui_edittext_input_phoneAreaLineInsertLeft, 0);
            int inputAreaLineInsertRight = typedArray.getDimensionPixelSize(R.styleable.AUiEditText_aui_edittext_input_phoneAreaLineInsertRight, 0);
            int inputAreaLineInsertTop = typedArray.getDimensionPixelSize(R.styleable.AUiEditText_aui_edittext_input_phoneAreaLineInsertTop, 0);
            int inputAreaLineInsertBottom = typedArray.getDimensionPixelSize(R.styleable.AUiEditText_aui_edittext_input_phoneAreaLineInsertBottom, 0);
            MarginLayoutParams layoutParams = (MarginLayoutParams) vPhoneAreaLine.getLayoutParams();
            layoutParams.width = inputAreaLineWidth;
            layoutParams.leftMargin = inputAreaLineInsertLeft;
            layoutParams.topMargin = inputAreaLineInsertTop;
            layoutParams.rightMargin = inputAreaLineInsertRight;
            layoutParams.bottomMargin = inputAreaLineInsertBottom;
            vPhoneAreaLine.setLayoutParams(layoutParams);
            vPhoneAreaLine.setBackgroundColor(inputAreaLineColor);

            etInput.setInputType(EditorInfo.TYPE_CLASS_PHONE);

        } else if (inputType == InputType_Password) {
            tvPhoneArea.setVisibility(View.GONE);
            vPhoneAreaLine.setVisibility(View.GONE);
            cbPasswordIcon.setVisibility(View.VISIBLE);

            Drawable inputPasswordDrawable = typedArray.getDrawable(R.styleable.AUiEditText_aui_edittext_input_passwordEyeSrc);
            int inputPasswordEyeWidth = typedArray.getLayoutDimension(R.styleable.AUiEditText_aui_edittext_input_passwordEyeWidth, 0);
            int inputPasswordEyeHeight = typedArray.getLayoutDimension(R.styleable.AUiEditText_aui_edittext_input_passwordEyeWidth, 0);
            ViewGroup.LayoutParams inputPasswordEyeParams = cbPasswordIcon.getLayoutParams();
            inputPasswordEyeParams.width = inputPasswordEyeWidth;
            inputPasswordEyeParams.height = inputPasswordEyeHeight;
            cbPasswordIcon.setLayoutParams(inputPasswordEyeParams);
            cbPasswordIcon.setBackground(inputPasswordDrawable);

            etInput.setInputType(EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
            post(() -> etInput.setTransformationMethod(PasswordTransformationMethod.getInstance()));
            cbPasswordIcon.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    // 显示密码
                    etInput.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    etInput.setSelection(etInput.getText().toString().length());
                } else {
                    // 隐藏密码
                    etInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    etInput.setSelection(etInput.getText().toString().length());
                }
            });
            cbPasswordIcon.setChecked(false);

        } else {
            tvPhoneArea.setVisibility(View.GONE);
            vPhoneAreaLine.setVisibility(View.GONE);
            cbPasswordIcon.setVisibility(View.GONE);
        }

        // 输入配置
        int inputLineNum = typedArray.getInt(R.styleable.AUiEditText_aui_edittext_input_lineNum, 1);
        int inputMaxCount = typedArray.getInt(R.styleable.AUiEditText_aui_edittext_input_maxCount, Integer.MAX_VALUE);
        if (inputLineNum == 1) {
            etInput.setSingleLine();
        } else {
            etInput.setMaxLines(inputLineNum);
        }
        etInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(inputMaxCount)});

        // 左右图标
        Drawable iconLeft = typedArray.getDrawable(R.styleable.AUiEditText_aui_edittext_icon_left);
        Drawable iconRight = typedArray.getDrawable(R.styleable.AUiEditText_aui_edittext_icon_right);
        ColorStateList iconLeftTint = typedArray.getColorStateList(R.styleable.AUiEditText_aui_edittext_icon_left_tint);
        ColorStateList iconRightTint = typedArray.getColorStateList(R.styleable.AUiEditText_aui_edittext_icon_right_tint);
        ivLeft.setImageDrawable(iconLeft);
        ivLeft.setImageTintList(iconLeftTint);
        ivRight.setImageDrawable(iconRight);
        ivRight.setImageTintList(iconRightTint);

        typedArray.recycle();
    }

    public void setTitle(String titleText) {
        tvTitle.setText(titleText);
        tvTitle.setVisibility(TextUtils.isEmpty(titleText) ? View.GONE : View.VISIBLE);
    }

    public void setTipText(String tipText) {
        tvTip.setText(tipText);
        tvTip.setVisibility(TextUtils.isEmpty(tipText) ? View.GONE : View.VISIBLE);
    }

    public void setInputSelectHandlerColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            etInput.getTextSelectHandle().setTint(color);
            etInput.getTextSelectHandleLeft().setTint(color);
            etInput.getTextSelectHandleRight().setTint(color);
            etInput.getTextCursorDrawable().setTint(color);
        }
    }

    private void initView() {
        View.inflate(getContext(), R.layout.aui_edittext_layout, this);
        tvTitle = findViewById(R.id.tvTitle);
        tvPhoneArea = findViewById(R.id.tvPhoneArea);
        vPhoneAreaLine = findViewById(R.id.vPhoneAreaLine);
        etInput = findViewById(R.id.etInput);
        ivLeft = findViewById(R.id.ivLeft);
        ivRight = findViewById(R.id.ivRight);
        cbPasswordIcon = findViewById(R.id.cbPasswordIcon);
        tvTip = findViewById(R.id.tvTip);
        llInput = findViewById(R.id.llInput);
        etInput.setOnFocusChangeListener((v, hasFocus) -> {
            llInput.setActivated(hasFocus);
        });
    }

    public void setRightIconClickListener(View.OnClickListener clickListener){
        ivRight.setOnClickListener(clickListener);
    }

    public void setLeftIconClickListener(View.OnClickListener clickListener){
        ivLeft.setOnClickListener(clickListener);
    }

    public void setStatus(int status) {
        if (status == Status_Error) {
            for (Runnable runnable : errorChangeUiRunList) {
                runnable.run();
            }
        } else {
            for (Runnable runnable : normalChangeUiRunList) {
                runnable.run();
            }
        }
    }

    private GradientDrawable createShapeDrawable(
            int solidColor,
            float radius,
            int strokeWidth,
            int strokeColor
    ) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(radius);
        drawable.setColor(solidColor);
        drawable.setStroke(strokeWidth, strokeColor);
        return drawable;
    }

    private LayerDrawable createBottomLineDrawable(int color, int width) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setStroke(width, color);
        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{drawable});
        layerDrawable.setLayerInset(0, -1 * width, -1 * width, -1 * width, 0);
        return layerDrawable;
    }

    public String getText() {
        return etInput.getText().toString();
    }

    public void setHint(String hint) {
        etInput.setHint(hint);
    }

    public void setText(String text) {
        etInput.setText(text);
    }

    public void setAutoFocus(boolean autoFocus) {
        etInput.setFocusable(true);
        etInput.setFocusableInTouchMode(true);
        if (autoFocus) {
            etInput.requestFocus();
        }
    }

}

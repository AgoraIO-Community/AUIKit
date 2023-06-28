package io.agora.auikit.ui.basic;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Space;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialog;

import io.agora.auikit.R;

public class AUIAlertDialog extends AppCompatDialog {

    private TextView tvTitle;
    private View titleLayout;
    private ImageView ivTitleClose;
    private TextView tvMessage;
    private AUIButton btnPositive;
    private AUIButton btnNegative;
    private AUIEditText edtInput;
    private Space btnSpace;
    private Space inputSpaceTop;
    private Space inputSpaceBottom;
    private Space messageSpaceTop;
    private Space messageSpaceBottom;

    private static int getDefaultTheme(Context context, int attrId, int themeId) {
        if(themeId == 0){
            if(attrId == 0){
                attrId = R.attr.aui_alertDialog_appearance;
            }
            TypedValue outValue = new TypedValue();
            if (context.getTheme().resolveAttribute(attrId, outValue, true)) {
                themeId = outValue.resourceId;
            }else{
                themeId = R.style.AUIAlertDialog;
            }
        }
        return themeId;
    }

    public AUIAlertDialog(@NonNull Context context) {
        this(context, 0);
    }

    public AUIAlertDialog(@NonNull Context context, int attrId) {
        this(context, attrId, 0);
    }

    public AUIAlertDialog(@NonNull Context context, int attrId, int themeResId) {
        super(context, getDefaultTheme(context, attrId, themeResId));

        TypedArray typedArray = getContext().obtainStyledAttributes(null, R.styleable.AUIAlertDialog, 0, themeResId);
        // get layout id
        int resourceId = typedArray.getResourceId(R.styleable.AUIAlertDialog_aui_alertDialog_contentLayout, View.NO_ID);
        if (resourceId != View.NO_ID) {
            setContentView(resourceId);
        }
        typedArray.recycle();

    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        initView();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        initView();
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        initView();
    }

    private void initView() {
        View parentPanel = findViewById(R.id.parentPanel);
        tvTitle = findViewById(R.id.tvTitle);
        titleLayout = findViewById(R.id.topPanel);
        ivTitleClose = findViewById(R.id.ivTitleClose);
        tvMessage = findViewById(R.id.tvMessage);
        messageSpaceTop = findViewById(R.id.messageSpaceTop);
        messageSpaceBottom = findViewById(R.id.messageSpaceBottom);
        btnPositive = findViewById(R.id.buttonPositive);
        btnNegative = findViewById(R.id.buttonNegative);
        btnSpace = findViewById(R.id.buttonSpacer);
        inputSpaceTop = findViewById(R.id.inputSpaceTop);
        inputSpaceBottom = findViewById(R.id.inputSpaceBottom);
        edtInput = findViewById(R.id.inputEditText);

        TypedArray typedArray = getContext().obtainStyledAttributes(null, R.styleable.AUIAlertDialog, 0, 0);

        // 背景
        int bgColor = typedArray.getColor(R.styleable.AUIAlertDialog_aui_alertDialog_contentBackgroundColor, Color.WHITE);
        int bgShadow = typedArray.getColor(R.styleable.AUIAlertDialog_aui_alertDialog_shadowColor, Color.GRAY);
        int bgCornerRadius = typedArray.getDimensionPixelSize(R.styleable.AUIAlertDialog_aui_alertDialog_contentRadius, 0);
        if (parentPanel != null) {
            parentPanel.setBackground(new AUIShadowRectDrawable()
                    .setColor(bgColor)
                    .setShadowColor(bgShadow)
                    .setShadowRadius(10f)
                    .setShadowOffsetX(5f)
                    .setShadowOffsetY(5f)
                    .setCornerRadius(bgCornerRadius));
        }

        typedArray.recycle();
    }

    public void setInput(String hint, String text, boolean autoFocus) {
        getWindow().setSoftInputMode(autoFocus ? WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
                : WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        inputSpaceTop.setVisibility(View.VISIBLE);
        inputSpaceBottom.setVisibility(View.VISIBLE);
        edtInput.setVisibility(View.VISIBLE);
        edtInput.setHint(hint);
        edtInput.setText(text);
        edtInput.setAutoFocus(autoFocus);
    }

    public String getInputText() {
        return edtInput.getText();
    }

    public void setPositiveButton(String text, @Nullable View.OnClickListener clickListener) {
        btnPositive.setText(text);
        btnPositive.setOnClickListener(clickListener);
    }

    public void setTitleCloseButton(@Nullable View.OnClickListener clickListener){
        this.setTitleCloseButton(View.NO_ID, clickListener);
    }

    public void setTitleCloseButton(@DrawableRes int icon, @Nullable View.OnClickListener clickListener){
        titleLayout.setVisibility(View.VISIBLE);
        ivTitleClose.setVisibility(View.VISIBLE);
        if(icon != View.NO_ID){
            ivTitleClose.setImageResource(icon);
        }
        ivTitleClose.setOnClickListener(clickListener);
    }

    public void setNegativeButton(String text, @Nullable View.OnClickListener clickListener) {
        btnNegative.setVisibility(View.VISIBLE);
        btnSpace.setVisibility(View.VISIBLE);
        btnNegative.setText(text);
        btnNegative.setOnClickListener(clickListener);
    }

    @Override
    public void setTitle(int titleId) {
        super.setTitle(titleId);
        setTitle(getContext().getString(titleId));
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        titleLayout.setVisibility(View.VISIBLE);
        tvTitle.setText(title);
    }

    public void setMessage(String message) {
        messageSpaceTop.setVisibility(View.VISIBLE);
        messageSpaceBottom.setVisibility(View.VISIBLE);
        tvMessage.setVisibility(View.VISIBLE);
        tvMessage.setText(message);
    }

    /**
     * @see android.view.Gravity
     */
    public void setMessageGravity(int gravity){
        tvMessage.setGravity(gravity);
    }

    /**
     *
     * @param alpha 0 ~ 255
     */
    public void setMessageTextAlpha(int alpha){
        tvMessage.setTextColor(tvMessage.getCurrentTextColor() & 0x00ffffff + alpha * 0x01000000);
    }

}

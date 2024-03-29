package io.agora.auikit.ui.basic;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatCheckBox;

import io.agora.auikit.ui.R;

public class AUICheckBox extends AppCompatCheckBox {

    private String mCheckedText;
    private String mNormalText;

    public AUICheckBox(Context context) {
        this(context, null);
    }

    public AUICheckBox(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AUICheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AUICheckBox, defStyleAttr, 0);
        mCheckedText = typedArray.getString(R.styleable.AUICheckBox_aui_checkedText);
        mNormalText = typedArray.getString(R.styleable.AUICheckBox_android_text);
        typedArray.recycle();

        refreshText(isChecked());
    }

    private void refreshText(boolean Checked) {
        if (!TextUtils.isEmpty(mCheckedText) && Checked) {
            setText(mCheckedText);
        } else {
            setText(mNormalText);
        }
    }

    @Override
    public void setChecked(boolean checked) {
        super.setChecked(checked);
        refreshText(checked);
    }
}

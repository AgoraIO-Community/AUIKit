package io.agora.auikit.ui.micseats.impl;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import io.agora.auikit.model.AUIMicSeatStatus;
import io.agora.auikit.ui.R;
import io.agora.auikit.ui.micseats.IMicSeatItemView;

public class AUIMicSeatItemView extends FrameLayout implements IMicSeatItemView, ViewTreeObserver.OnGlobalLayoutListener {
    private String titleIdleText;
    private ImageView ivStateIdle;
    private ImageView ivStateLock;
    private TextView tvTitle;
    private TextView tvRoomOwner;
    private TextView tvLeadSinger;
    private TextView tvChorus;
    private ImageView ivAudioMute;
    private ImageView ivVideoMute;
    private View bgSeat;
    private ImageView ivUserAvatar;
    private AUIRippleAnimationView ripple;
    private int itemWidth;
    private Context context;

    public AUIMicSeatItemView(@NonNull Context context) {
        this(context, null);
    }

    public AUIMicSeatItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AUIMicSeatItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        TypedArray themeTa = context.obtainStyledAttributes(attrs, R.styleable.AUIMicSeatItemView, defStyleAttr, 0);
        int appearanceId = themeTa.getResourceId(R.styleable.AUIMicSeatItemView_aui_micSeatItem_appearance, 0);
        themeTa.recycle();
        initView(context, appearanceId);
    }

    private void initView(@NonNull Context context, @StyleRes int appearanceId) {
        View.inflate(context, R.layout.aui_micseat_item_view, this);

        TypedArray typedArray = context.obtainStyledAttributes(appearanceId, R.styleable.AUIMicSeatItemView);
        int audioMuteIconGravity = typedArray.getInt(R.styleable.AUIMicSeatItemView_aui_micSeatItem_audioMuteIconGravity, 1);
        titleIdleText = typedArray.getString(R.styleable.AUIMicSeatItemView_aui_micSeatItem_titleIdleText);
        typedArray.recycle();

        bgSeat = findViewById(R.id.bg_seat);
        ivStateIdle = findViewById(R.id.iv_state_idle);
        ivStateLock = findViewById(R.id.iv_state_lock);
        ivUserAvatar = findViewById(R.id.iv_user_avatar);
        tvTitle = findViewById(R.id.tv_title);
        tvRoomOwner = findViewById(R.id.tv_room_owner);
        tvLeadSinger = findViewById(R.id.tv_lead_singer);
        tvChorus = findViewById(R.id.tv_chorus);
        ivAudioMute = findViewById(R.id.iv_audio_mute);
        ivVideoMute = findViewById(R.id.iv_video_mute);
        ripple = findViewById(R.id.iv_ripple);

        // 设置监听器以获取宽度
        getViewTreeObserver().addOnGlobalLayoutListener(this);

        ripple.setInterpolator(new AccelerateInterpolator(1.4f));
        ripple.setStyle(Paint.Style.STROKE);

        // 静音图标位置配置
        FrameLayout.LayoutParams ivAudioMuteParams = (LayoutParams) ivAudioMute.getLayoutParams();
        switch (audioMuteIconGravity) {
            case 1: // bottomEnd
                ivAudioMuteParams.gravity = Gravity.BOTTOM | Gravity.END;
                ivAudioMute.setLayoutParams(ivAudioMuteParams);
                break;
            case 2: // center
                ivAudioMuteParams.gravity = Gravity.CENTER;
                ivAudioMute.setLayoutParams(ivAudioMuteParams);
                break;
        }
        // 关闭视频图标位置配置
        FrameLayout.LayoutParams ivVideoMuteParams = (LayoutParams) ivVideoMute.getLayoutParams();
        switch (audioMuteIconGravity) {
            case 1: // bottomEnd
                ivVideoMuteParams.gravity = Gravity.BOTTOM | Gravity.END;
                ivVideoMute.setLayoutParams(ivVideoMuteParams);
                break;
            case 2: // center
                ivVideoMuteParams.gravity = Gravity.CENTER;
                ivVideoMute.setLayoutParams(ivVideoMuteParams);
                break;
        }
    }

    public void setRippleInitialRadius(float radius){
        ripple.setInitialRadius(radius);
    }

    public void setRippleInterpolator(float value){
        ripple.setInterpolator(new AccelerateInterpolator(value));
    }

    @Override
    public void setRoomOwnerVisibility(int visible) {
        tvRoomOwner.setVisibility(visible);
    }

    @Override
    public void setTitleIndex(int index) {
        if (titleIdleText.contains("%d")) {
            tvTitle.setText(String.format(titleIdleText, index));
        }
    }

    @Override
    public void setTitleText(String text) {
        tvTitle.setText(text);
    }

    public void startRippleAnimation(){
        ripple.startRippleAnimation();
    }

    public void stopRippleAnimation(){
        ripple.stopImmediatelyRippleAnimation();
    }

    @Override
    public void setAudioMuteVisibility(int visible) {
        ivAudioMute.setVisibility(visible);
    }

    @Override
    public void setVideoMuteVisibility(int visible) {
        ivVideoMute.setVisibility(visible);
    }

    @Override
    public void setUserAvatarImageDrawable(Drawable drawable) {
        ivUserAvatar.setImageDrawable(drawable);
    }

    @Override
    public void setMicSeatState(int state) {
        if (state == AUIMicSeatStatus.locked) {
            ivStateLock.setVisibility(View.VISIBLE);
            ivStateIdle.setVisibility(View.INVISIBLE);
        } else {
            ivStateIdle.setVisibility(View.VISIBLE);
            ivStateLock.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void setUserAvatarImageUrl(String url) {
        RequestOptions options = RequestOptions.circleCropTransform();
        Glide.with(ivUserAvatar).load(url).apply(options).into(ivUserAvatar);
    }

    @Override
    public void setChorusMicOwnerType(ChorusType type) {
        if (type == ChorusType.LeadSinger) {
            tvChorus.setVisibility(View.VISIBLE);
            Drawable drawable = getResources().getDrawable(R.drawable.aui_micseat_item_icon_leadsinger);
            tvChorus.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
            tvChorus.setText(R.string.aui_micseat_leadsinger);
        } else if (type == ChorusType.SecondarySinger) {
            tvChorus.setVisibility(View.VISIBLE);
            Drawable drawable = getResources().getDrawable(R.drawable.aui_micseat_item_icon_chorus);
            tvChorus.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
            tvChorus.setText(R.string.aui_micseat_chorus);
        } else {
            tvChorus.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onGlobalLayout() {
        // 当布局完成时，获取 ImageView 的宽度
        if (ivUserAvatar.getWidth() > 0) {
            itemWidth = ivUserAvatar.getWidth();
            // 在获取到宽度后，移除监听器
            getViewTreeObserver().removeOnGlobalLayoutListener(this);
            ripple.setInitialRadius(itemWidth/2);
            ripple.setMaxRadius(itemWidth/2 + 10);
        }
    }
}

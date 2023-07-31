package io.agora.auikit.ui.micseats.impl;

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
import com.bumptech.glide.request.RequestOptions;

import io.agora.auikit.model.AUIUserThumbnailInfo;
import io.agora.auikit.ui.R;
import io.agora.auikit.ui.micseats.IMicSeatDialogView;

public class AUIMicSeatDialogView extends FrameLayout implements IMicSeatDialogView {
    private String userDesIdleText;
    private View vMicInfo;
    private ImageView ivAvatar;
    private TextView tvUserName;
    private TextView tvUserInfo;
    private TextView tvEnterSeat;
    private TextView tvLeaveSeat;
    private TextView tvKickSeat;
    private TextView tvLockSeat;
    private TextView tvMuteAudio;
    private TextView tvMuteVideo;
    private TextView tvInvited;
    private boolean mMuteAudio;
    private boolean mMuteVideo;
    private boolean mSeatClosed;

    public AUIMicSeatDialogView(@NonNull Context context) {
        this(context, null);
    }

    public AUIMicSeatDialogView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AUIMicSeatDialogView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray themeTa = context.obtainStyledAttributes(attrs, R.styleable.AUIMicSeatDialogView, defStyleAttr, 0);
        int appearanceId = themeTa.getResourceId(R.styleable.AUIMicSeatDialogView_aui_micSeatDialog_appearance, 0);
        themeTa.recycle();
        initView(context, appearanceId);
    }

    private void initView(@NonNull Context context, @StyleRes int appearanceId) {
        View.inflate(context, R.layout.aui_micseat_dialog_view, this);

        TypedArray typedArray = context.obtainStyledAttributes(appearanceId, R.styleable.AUIMicSeatDialogView);
        int userGravity = typedArray.getInt(R.styleable.AUIMicSeatDialogView_aui_micSeatDialog_userGravity, 1);
        userDesIdleText = typedArray.getString(R.styleable.AUIMicSeatDialogView_aui_micSeatDialog_userDesText);
        typedArray.recycle();

        vMicInfo = findViewById(R.id.ctl_user);
        ivAvatar = findViewById(R.id.iv_user_avatar);
        tvUserName = findViewById(R.id.tv_user_name);
        tvUserInfo = findViewById(R.id.tv_user_info);
        tvEnterSeat = findViewById(R.id.tv_enter_seat);
        tvLeaveSeat = findViewById(R.id.tv_leave_seat);
        tvKickSeat = findViewById(R.id.tv_kick_seat);
        tvLockSeat = findViewById(R.id.tv_lock_seat);
        tvMuteAudio = findViewById(R.id.tv_mute_audio);
        tvMuteVideo = findViewById(R.id.tv_mute_video);
        tvInvited = findViewById(R.id.tv_invited);

        // 用户信息位置配置
        switch (userGravity) {
            case 1: // start: default ui, do nothing
                break;
            case 2: // center
                ConstraintLayout.LayoutParams avatarParams = (ConstraintLayout.LayoutParams) ivAvatar.getLayoutParams();
                avatarParams.startToStart = 0; // 0： parent
                avatarParams.endToEnd = 0; // 0： parent
                ivAvatar.setLayoutParams(avatarParams);

                ConstraintLayout.LayoutParams userNameParams = (ConstraintLayout.LayoutParams) tvUserName.getLayoutParams();
                userNameParams.startToStart = 0;
                userNameParams.endToEnd = 0;
                userNameParams.topToTop = -1;
                userNameParams.topToBottom = R.id.iv_user_avatar;
                userNameParams.bottomToTop = R.id.tv_user_info;
                tvUserName.setLayoutParams(userNameParams);

                ConstraintLayout.LayoutParams userDesParams = (ConstraintLayout.LayoutParams) tvUserInfo.getLayoutParams();
                userDesParams.startToStart = 0;
                userDesParams.endToEnd = 0;
                userDesParams.topToTop = -1;
                userDesParams.topToBottom = R.id.tv_user_name;
                userDesParams.bottomToBottom = -1;
                tvUserInfo.setLayoutParams(userDesParams);
                break;
        }
    }

    public void addInvite(boolean isShow){
        if (isShow){
            tvInvited.setVisibility(View.VISIBLE);
        }else {
            tvInvited.setVisibility(View.GONE);
        }
    }
    public void addMuteAudio(boolean isMute) {
        mMuteAudio = isMute;
        tvMuteAudio.setVisibility(View.VISIBLE);
        tvMuteAudio.setText(isMute ? R.string.aui_micseat_dialog_unmute_audio : R.string.aui_micseat_dialog_mute_audio);
    }
    public void addMuteVideo(boolean isMute) {
        mMuteVideo = isMute;
        tvMuteVideo.setVisibility(View.VISIBLE);
        tvMuteVideo.setText(isMute ? R.string.aui_micseat_dialog_unmute_video : R.string.aui_micseat_dialog_mute_video);
    }
    public void addCloseSeat(boolean isClosed) {
        mSeatClosed = isClosed;
        tvLockSeat.setVisibility(View.VISIBLE);
        tvLockSeat.setText(isClosed ? R.string.aui_micseat_dialog_open_seat : R.string.aui_micseat_dialog_close_seat);
    }

    public void addKickSeat() {
        tvKickSeat.setVisibility(View.VISIBLE);
    }

    public void addLeaveSeat() {
        tvLeaveSeat.setVisibility(View.VISIBLE);
    }

    public void addEnterSeat(boolean isShow) {
        if (isShow){
            tvEnterSeat.setVisibility(View.VISIBLE);
        }else {
            tvEnterSeat.setVisibility(View.GONE);
        }
    }

    public void setUserInfo(@Nullable AUIUserThumbnailInfo userInfo) {
        if (userInfo == null || userInfo.userId.isEmpty()) {
            vMicInfo.setVisibility(View.GONE);
            return;
        }
        vMicInfo.setVisibility(View.VISIBLE);
        tvUserName.setText(userInfo.userName);
        tvUserInfo.setText(userInfo.userId);
        RequestOptions options = RequestOptions.circleCropTransform();
        Glide.with(ivAvatar).load(userInfo.userAvatar).apply(options).into(ivAvatar);
    }

    public void setEnterSeatClickListener(OnClickListener clickListener){
        tvEnterSeat.setOnClickListener(clickListener);
    }

    public void setLeaveSeatClickListener(OnClickListener clickListener){
        tvLeaveSeat.setOnClickListener(clickListener);
    }

    public void setKickSeatClickListener(OnClickListener clickListener){
        tvKickSeat.setOnClickListener(clickListener);
    }

    public void setMuteAudioClickListener(OnClickListener clickListener){
        tvMuteAudio.setOnClickListener(clickListener);
    }

    public void setMuteVideoClickListener(OnClickListener clickListener){
        tvMuteVideo.setOnClickListener(clickListener);
    }

    public void setCloseSeatClickListener(OnClickListener clickListener){
        tvLockSeat.setOnClickListener(clickListener);
    }

    public void setInvitedClickListener(OnClickListener clickListener){
        tvInvited.setOnClickListener(clickListener);
    }

    public boolean isSeatClosed() {
        return mSeatClosed;
    }

    public boolean isMuteAudio() {
        return mMuteAudio;
    }

    public boolean isMuteVideo() {
        return mMuteVideo;
    }

}
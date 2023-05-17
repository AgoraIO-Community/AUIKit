package io.agora.auikit.binder;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.LinkedList;
import java.util.List;

import io.agora.auikit.model.AUiChooseMusicModel;
import io.agora.auikit.model.AUiChoristerModel;
import io.agora.auikit.model.AUiMicSeatInfo;
import io.agora.auikit.model.AUiMicSeatStatus;
import io.agora.auikit.model.AUiUserInfo;
import io.agora.auikit.model.AUiUserThumbnailInfo;
import io.agora.auikit.service.IAUiChorusService;
import io.agora.auikit.service.IAUiJukeboxService;
import io.agora.auikit.service.IAUiMicSeatService;
import io.agora.auikit.service.IAUiUserService;
import io.agora.auikit.service.callback.AUiChooseSongListCallback;
import io.agora.auikit.service.callback.AUiChoristerListCallback;
import io.agora.auikit.service.callback.AUiException;
import io.agora.auikit.ui.micseats.IMicSeatDialogView;
import io.agora.auikit.ui.micseats.IMicSeatItemView;
import io.agora.auikit.ui.micseats.IMicSeatsView;

public class AUiMicSeatsBinder implements
        IAUiBindable,
        IMicSeatsView.ActionDelegate,
        IAUiMicSeatService.AUiMicSeatRespDelegate,
        IAUiChorusService.AUiChorusRespDelegate,
        IAUiJukeboxService.AUiJukeboxRespDelegate,
        IAUiUserService.AUiUserRespDelegate {
    private final IMicSeatsView micSeatsView;
    private final IAUiUserService userService;
    private final IAUiMicSeatService micSeatService;
    private final IAUiJukeboxService jukeboxService;
    private final IAUiChorusService chorusService;
    private Handler mMainHandler;
    private String mLeadSingerId = "";

    private LinkedList<String> mAccompanySingers = new LinkedList<String>();

    public AUiMicSeatsBinder(
            IMicSeatsView micSeatsView,
            IAUiUserService userService,
            IAUiMicSeatService micSeatService,
            IAUiJukeboxService jukeboxService,
            IAUiChorusService chorusService) {
        this.userService = userService;
        this.micSeatsView = micSeatsView;
        this.micSeatService = micSeatService;
        this.jukeboxService = jukeboxService;
        this.chorusService = chorusService;
    }

    @Override
    public void bind() {
        mMainHandler = new Handler(Looper.getMainLooper());
        userService.bindRespDelegate(this);
        micSeatService.bindRespDelegate(this);
        jukeboxService.bindRespDelegate(this);
        chorusService.bindRespDelegate(this);
        micSeatsView.setMicSeatActionDelegate(this);

        // update view
        IMicSeatItemView[] seatViewList = micSeatsView.getMicSeatItemViewList();
        for (int seatIndex = 0; seatIndex < seatViewList.length; seatIndex++) {
            AUiMicSeatInfo micSeatInfo = micSeatService.getMicSeatInfo(seatIndex);
            updateSeatView(seatIndex, micSeatInfo);
        }

        jukeboxService.getAllChooseSongList(new AUiChooseSongListCallback() {
            @Override
            public void onResult(@Nullable AUiException error, @Nullable List<AUiChooseMusicModel> songList) {
                if (songList != null && songList.size() != 0) {
                    AUiChooseMusicModel song = songList.get(0);
                    mLeadSingerId = song.owner.userId;
                    runOnUiThread(() -> updateChorusTag() );
                }
            }
        });
        chorusService.getChoristersList(new AUiChoristerListCallback() {
            @Override
            public void onResult(@Nullable AUiException error, @Nullable List<AUiChoristerModel> songList) {
                for (AUiChoristerModel song : songList) {
                    mAccompanySingers.add(song.userId);
                }
                if (mAccompanySingers.size() != 0) {
                    runOnUiThread(() -> updateChorusTag() );
                }
            }
        });
    }

    @Override
    public void unBind() {
        mMainHandler.removeCallbacksAndMessages(null);
        mMainHandler = null;
        userService.unbindRespDelegate(this);
        micSeatService.unbindRespDelegate(this);
        jukeboxService.unbindRespDelegate(this);
        chorusService.unbindRespDelegate(this);

        micSeatsView.setMicSeatActionDelegate(null);
    }
    private void runOnUiThread(@NonNull Runnable runnable) {
        if (mMainHandler != null) {
            if (mMainHandler.getLooper().getThread() == Thread.currentThread()) {
                runnable.run();
            } else {
                mMainHandler.post(runnable);
            }
        }
    }
    /** IAUiMicSeatService.AUiMicSeatRespDelegate implements. */
    @Override
    public void onSeatListChange(List<AUiMicSeatInfo> seatInfoList) {
        IAUiMicSeatService.AUiMicSeatRespDelegate.super.onSeatListChange(seatInfoList);
    }

    @Override
    public void onAnchorEnterSeat(int seatIndex, @NonNull AUiUserThumbnailInfo userInfo) {
        IMicSeatItemView seatView = micSeatsView.getMicSeatItemViewList()[seatIndex];
        seatView.setTitleText(userInfo.userName);
        seatView.setUserAvatarImageUrl(userInfo.userAvatar);
    }

    @Override
    public void onAnchorLeaveSeat(int seatIndex, @NonNull AUiUserThumbnailInfo userInfo) {
        updateSeatView(seatIndex, null);
    }

    @Override
    public void onSeatAudioMute(int seatIndex, boolean isMute) {
        IMicSeatItemView seatView = micSeatsView.getMicSeatItemViewList()[seatIndex];
        seatView.setAudioMuteVisibility(isMute ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onSeatVideoMute(int seatIndex, boolean isMute) {
        AUiMicSeatInfo seatInfo = micSeatService.getMicSeatInfo(seatIndex);
        updateSeatView(seatIndex, seatInfo);
    }

    @Override
    public void onSeatClose(int seatIndex, boolean isClose) {
        AUiMicSeatInfo seatInfo = micSeatService.getMicSeatInfo(seatIndex);
        IMicSeatItemView seatView = micSeatsView.getMicSeatItemViewList()[seatIndex];
        seatView.setMicSeatState(seatInfo.seatStatus);
    }

    private void updateSeatView(int seatIndex, @Nullable AUiMicSeatInfo micSeatInfo) {
        IMicSeatItemView seatView = micSeatsView.getMicSeatItemViewList()[seatIndex];
        if (micSeatInfo == null || micSeatInfo.seatStatus == AUiMicSeatStatus.idle) {
            seatView.setTitleIndex(seatIndex + 1);
            seatView.setAudioMuteVisibility(View.GONE);
            seatView.setVideoMuteVisibility(View.GONE);
            seatView.setUserAvatarImageDrawable(null);
            seatView.setChorusMicOwnerType(IMicSeatItemView.ChorusType.None);
            return;
        }
        AUiUserInfo userInfo = null;
        if (micSeatInfo.user != null) {
            userInfo = userService.getUserInfo(micSeatInfo.user.userId);
        }
        seatView.setRoomOwnerVisibility((seatIndex == 0) ? View.VISIBLE : View.GONE);

        seatView.setMicSeatState(AUiMicSeatStatus.locked);

        boolean isAudioMute = (micSeatInfo.muteAudio != 0);
        if (userInfo != null) {
            isAudioMute = isAudioMute || (userInfo.muteAudio == 1);
        }
        seatView.setAudioMuteVisibility(isAudioMute ? View.VISIBLE : View.GONE);

        boolean isVideoMute = (micSeatInfo.muteVideo != 0);
        seatView.setVideoMuteVisibility(isVideoMute ? View.VISIBLE : View.GONE);

        if (micSeatInfo.user != null) {
            seatView.setTitleText(micSeatInfo.user.userName);
            seatView.setUserAvatarImageUrl(micSeatInfo.user.userAvatar);

            if (micSeatInfo.user.userId.equals(mLeadSingerId)) {
                seatView.setChorusMicOwnerType(IMicSeatItemView.ChorusType.LeadSinger);
            } else if (mAccompanySingers.contains(micSeatInfo.user.userId)) {
                seatView.setChorusMicOwnerType(IMicSeatItemView.ChorusType.SecondarySinger);
            } else {
                seatView.setChorusMicOwnerType(IMicSeatItemView.ChorusType.None);
            }
        }
    }

    private void setLeadSingerId(String str) {
        if (str.equals(mLeadSingerId)) {
            return;
        }
        mLeadSingerId = str;
        updateChorusTag();
    }

    private void updateChorusTag() {
        IMicSeatItemView[] seatViewList = micSeatsView.getMicSeatItemViewList();
        for (int i = 0; i < seatViewList.length; i++) {
            AUiMicSeatInfo micSeatInfo = micSeatService.getMicSeatInfo(i);
            IMicSeatItemView itemView = seatViewList[i];
            if (micSeatInfo.user == null || micSeatInfo.user.userId.isEmpty()) {
                itemView.setChorusMicOwnerType(IMicSeatItemView.ChorusType.None);
                continue;
            }
            if (micSeatInfo.user.userId.equals(mLeadSingerId)) {
                itemView.setChorusMicOwnerType(IMicSeatItemView.ChorusType.LeadSinger);
            } else if (mAccompanySingers.contains(micSeatInfo.user.userId)) {
                itemView.setChorusMicOwnerType(IMicSeatItemView.ChorusType.SecondarySinger);
            } else {
                itemView.setChorusMicOwnerType(IMicSeatItemView.ChorusType.None);
            }
        }
    }

    /** AUIMicSeatDialogView.ActionDelegate implements. */
    @Override
    public boolean onClickSeat(int index, IMicSeatDialogView dialogView) {
        AUiMicSeatInfo seatInfo = micSeatService.getMicSeatInfo(index);
        dialogView.setUserInfo(seatInfo.user);
        boolean isEmptySeat = (seatInfo.user == null || seatInfo.user.userId.length() == 0);
        boolean isCurrentUser = seatInfo.user != null && seatInfo.user.userId.equals(micSeatService.getRoomContext().currentUserInfo.userId);
        boolean isRoomOwner = micSeatService.getRoomContext().isRoomOwner(micSeatService.getChannelName());
        boolean inSeat = false;
        for (int i = 0; i <= 7; i++) {
            AUiMicSeatInfo info = micSeatService.getMicSeatInfo(i);
            if (info.user != null && info.user.userId.equals(micSeatService.getRoomContext().currentUserInfo.userId)) {
                inSeat = true;
                break;
            }
        }
        if (isRoomOwner) {
            if (isEmptySeat) {
                dialogView.addMuteAudio((seatInfo.muteAudio != 0));
                dialogView.addCloseSeat((seatInfo.seatStatus == AUiMicSeatStatus.locked));
            } else {
                if (isCurrentUser) {
                    dialogView.addMuteAudio((seatInfo.muteAudio != 0));
                } else {
                    dialogView.addKickSeat();
                    dialogView.addMuteAudio((seatInfo.muteAudio != 0));
                    dialogView.addCloseSeat((seatInfo.seatStatus == AUiMicSeatStatus.locked));
                }
            }
        } else {
            if (isEmptySeat) {
                if (inSeat) {
                    return false;
                } else {
                    dialogView.addEnterSeat();
                }
            } else {
                if (isCurrentUser) {
                    dialogView.addLeaveSeat();
                    dialogView.addMuteAudio((seatInfo.muteAudio != 0));
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onClickEnterSeat(int index) {
        micSeatService.enterSeat(index, null);
    }

    @Override
    public void onClickLeaveSeat(int index) {
        micSeatService.leaveSeat(null);
    }

    @Override
    public void onClickKickSeat(int index) {
        micSeatService.kickSeat(index, null);
    }

    @Override
    public void onClickCloseSeat(int index, boolean isClose) {
        micSeatService.closeSeat(index, isClose, null);
    }

    @Override
    public void onClickMuteAudio(int index, boolean mute) {
        micSeatService.muteAudioSeat(index, mute, null);
    }

    @Override
    public void onClickMuteVideo(int index, boolean mute) {
        micSeatService.muteVideoSeat(index, mute, null);
    }
    /** IAUiMicSeatService.AUiChorusRespDelegate implements. */
    @Override
    public void onChoristerDidEnter(AUiChoristerModel chorister) {
        if (mAccompanySingers.contains(chorister.userId)) {
            return;
        }
        mAccompanySingers.add(chorister.userId);
        updateChorusTag();
    }

    @Override
    public void onChoristerDidLeave(AUiChoristerModel chorister) {
        if (mAccompanySingers.contains(chorister.userId)) {
            mAccompanySingers.remove(chorister.userId);
            updateChorusTag();
        }
    }

    @Override
    public void onSingerRoleChanged(int oldRole, int newRole) {

    }

    @Override
    public void onChoristerDidChanged() {

    }
    /** IAUiMicSeatService.AUiJukeboxRespDelegate implements. */
    @Override
    public void onAddChooseSong(@NonNull AUiChooseMusicModel song) {

    }

    @Override
    public void onRemoveChooseSong(@NonNull AUiChooseMusicModel song) {

    }

    @Override
    public void onUpdateChooseSong(@NonNull AUiChooseMusicModel song) {


    }

    @Override
    public void onUpdateAllChooseSongs(@NonNull List<AUiChooseMusicModel> songs) {
        if (songs.size() != 0) {
            AUiChooseMusicModel song = songs.get(0);
            mAccompanySingers.clear();
            setLeadSingerId(song.owner.userId);
        } else {
            mAccompanySingers.clear();
            setLeadSingerId("");
        }
    }
    /** IAUiUserService.AUiUserRespDelegate implements. */
    @Override
    public void onRoomUserSnapshot(@NonNull String roomId, @Nullable List<AUiUserInfo> userList) {

    }

    @Override
    public void onRoomUserEnter(@NonNull String roomId, @NonNull AUiUserInfo userInfo) {

    }

    @Override
    public void onRoomUserLeave(@NonNull String roomId, @NonNull AUiUserInfo userInfo) {

    }

    @Override
    public void onRoomUserUpdate(@NonNull String roomId, @NonNull AUiUserInfo userInfo) {

    }

    @Override
    public void onUserAudioMute(@NonNull String userId, boolean mute) {
        for (int i = 0; i <= 7; i++) {
            AUiMicSeatInfo seatInfo = micSeatService.getMicSeatInfo(i);
            if (seatInfo != null && seatInfo.user != null && seatInfo.user.userId.equals(userId)) {
                updateSeatView(i, seatInfo);
                break;
            }
        }
    }

    @Override
    public void onUserVideoMute(@NonNull String userId, boolean mute) {

    }
}

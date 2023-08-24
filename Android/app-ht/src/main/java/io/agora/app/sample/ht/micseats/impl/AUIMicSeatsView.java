package io.agora.app.sample.ht.micseats.impl;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.Map;

import io.agora.app.sample.ht.R;
import io.agora.app.sample.ht.micseats.IMicSeatItemView;
import io.agora.app.sample.ht.micseats.IMicSeatsView;

public class AUIMicSeatsView extends FrameLayout implements IMicSeatsView {

    private RecyclerView.Adapter<RecyclerView.ViewHolder> mMicSeatsAdapter;
    private int micSeatCount = 8;
    private MicSeatItemViewWrap[] micSeatViewList = new MicSeatItemViewWrap[micSeatCount];
    private ActionDelegate actionDelegate;

    private final Map<Integer, Integer> userMicSeatIndexMap = new HashMap<>();

    public AUIMicSeatsView(@NonNull Context context) {
        this(context, null);
    }

    public AUIMicSeatsView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AUIMicSeatsView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        for (int i = 0; i < micSeatViewList.length; i++) {
            micSeatViewList[i] = new MicSeatItemViewWrap();
        }
        initView(context);
    }

    private void initView(@NonNull Context context) {
        View.inflate(context, R.layout.aui_micseats_view, this);
        AUIRecyclerView rvMicSeats = findViewById(R.id.rv_mic_seats);
        mMicSeatsAdapter = new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new RecyclerView.ViewHolder(new AUIMicSeatItemView(parent.getContext())) {
                };
            }
            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                AUIMicSeatItemView seatItemView = (AUIMicSeatItemView) holder.itemView;
                MicSeatItemViewWrap seatItemViewWrap = micSeatViewList[position];
                seatItemViewWrap.setView(seatItemView);
                seatItemView.setOnClickListener(view -> {
                    if (actionDelegate != null) {
                        int userId = -1;
                        if (userMicSeatIndexMap.containsValue(position)) {
                            for (Map.Entry<Integer, Integer> entry : userMicSeatIndexMap.entrySet()) {
                                if(entry.getValue() == position){
                                    userId = entry.getKey();
                                }
                            }
                        }
                        actionDelegate.onClickSeat(userId, seatItemViewWrap);
                    }
                });
            }
            @Override
            public int getItemCount() {
                return micSeatCount;
            }
        };
        rvMicSeats.setAdapter(mMicSeatsAdapter);
    }

    @Override
    public IMicSeatItemView[] setMicSeatCount(int count) {
        micSeatCount = count;
        micSeatViewList = new MicSeatItemViewWrap[micSeatCount];
        for (int i = 0; i < micSeatViewList.length; i++) {
            MicSeatItemViewWrap itemView = new MicSeatItemViewWrap();
            itemView.setIndex(i);
            micSeatViewList[i] = itemView;
        }
        mMicSeatsAdapter.notifyDataSetChanged();
        return micSeatViewList;
    }


    @Override
    public IMicSeatItemView findMicSeatItemView(int userId) {
        Integer seatIndex = userMicSeatIndexMap.get(userId);
        if (seatIndex == null) {
            return null;
        }
        return micSeatViewList[seatIndex];
    }

    @Override
    public IMicSeatItemView upMicSeat(int userId, int seatIndex) {
        Integer existSeatIndex = userMicSeatIndexMap.get(userId);
        if (existSeatIndex != null) {
            return micSeatViewList[existSeatIndex];
        }

        IMicSeatItemView itemView = null;
        int retSeatIndex = seatIndex;

        if(retSeatIndex >= 0 && retSeatIndex < micSeatViewList.length){
            if(micSeatViewList[retSeatIndex].getMicSeatState() == IMicSeatItemView.MicSeatState.idle){
                itemView = micSeatViewList[retSeatIndex];
            }
        }
        if(itemView == null){
            for (int i = 0; i < micSeatViewList.length; i++) {
                if(micSeatViewList[i].getMicSeatState() == IMicSeatItemView.MicSeatState.idle){
                    itemView = micSeatViewList[i];
                    retSeatIndex = i;
                    break;
                }
            }
        }

        if(itemView != null){
            itemView.setMicSeatState(IMicSeatItemView.MicSeatState.used);
            userMicSeatIndexMap.put(userId, retSeatIndex);
        }

        return itemView;
    }

    @Override
    public IMicSeatItemView downMicSeat(int userId) {
        Integer seatIndex = userMicSeatIndexMap.get(userId);
        if (seatIndex == null) {
            return null;
        }
        userMicSeatIndexMap.remove(userId);
        MicSeatItemViewWrap itemView = micSeatViewList[seatIndex];
        itemView.setMicSeatState(IMicSeatItemView.MicSeatState.idle);
        return itemView;
    }

    @Override
    public void setMicSeatActionDelegate(ActionDelegate actionDelegate) {
        this.actionDelegate = actionDelegate;
    }

    private static class MicSeatItemViewWrap implements IMicSeatItemView {
        private String titleText;
        private int index;
        private int audioMuteVisibility = View.GONE, videoMuteVisibility = View.GONE;
        private int roomOwnerVisibility = View.GONE;
        private ChorusType chorusType = ChorusType.None;
        private Drawable userAvatarImageDrawable;
        private MicSeatState seatStatus = MicSeatState.idle;
        private String userAvatarImageUrl;
        private IMicSeatItemView view;

        private void setView(IMicSeatItemView view) {
            this.view = view;
            setTitleText(titleText);
            setIndex(index);
            setRoomOwnerVisibility(roomOwnerVisibility);
            setAudioMuteVisibility(audioMuteVisibility);
            setVideoMuteVisibility(videoMuteVisibility);
            setUserAvatarImageDrawable(userAvatarImageDrawable);
            setMicSeatState(seatStatus);
            setUserAvatarImageUrl(userAvatarImageUrl);
            setChorusMicOwnerType(chorusType);
        }

        @Override
        public void setTitleText(String text) {
            this.titleText = text;
            if (view != null) {
                view.setTitleText(text);
            }
        }
        @Override
        public void setRoomOwnerVisibility(int visible) {
            this.roomOwnerVisibility = visible;
            if (view != null) {
                view.setRoomOwnerVisibility(visible);
            }
        }
        @Override
        public void setIndex(int index) {
            this.index = index;
            if (view != null) {
                view.setIndex(index);
            }
        }

        @Override
        public int getIndex() {
            if (view != null) {
                return view.getIndex();
            }
            return index;
        }

        @Override
        public void setAudioMuteVisibility(int visible) {
            this.audioMuteVisibility = visible;
            if (view != null) {
                view.setAudioMuteVisibility(visible);
            }
        }

        @Override
        public void setVideoMuteVisibility(int visible) {
            this.videoMuteVisibility = visible;
            if (view != null) {
                view.setVideoMuteVisibility(visible);
            }
        }

        @Override
        public void setUserAvatarImageDrawable(Drawable drawable) {
            this.userAvatarImageDrawable = drawable;
            if (view != null) {
                view.setUserAvatarImageDrawable(drawable);
            }
        }

        @Override
        public void setMicSeatState(MicSeatState state) {
            this.seatStatus = state;
            if (view != null) {
                view.setMicSeatState(state);
            }
        }

        @Override
        public MicSeatState getMicSeatState() {
            if (view != null) {
                return view.getMicSeatState();
            }
            return seatStatus;
        }

        @Override
        public void setUserAvatarImageUrl(String url) {
            this.userAvatarImageUrl = url;
            if (view != null) {
                view.setUserAvatarImageUrl(url);
            }
        }

        @Override
        public void setChorusMicOwnerType(ChorusType type) {
            this.chorusType = type;
            if (view != null) {
                view.setChorusMicOwnerType(type);
            }
        }

        @Override
        public void startRippleAnimation() {
            if (view != null) {
                view.startRippleAnimation();
            }
        }

        @Override
        public void stopRippleAnimation() {
            if (view != null) {
                view.stopRippleAnimation();
            }
        }

        @Override
        public void setRippleInterpolator(float value) {
            if (view != null) {
                view.setRippleInterpolator(value);
            }
        }
    }
}

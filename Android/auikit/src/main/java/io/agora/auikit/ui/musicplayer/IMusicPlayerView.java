package io.agora.auikit.ui.musicplayer;

import java.util.Map;

import io.agora.auikit.ui.musicplayer.listener.IMusicPlayerActionListener;

public interface IMusicPlayerView {
    void setMusicPlayerActionListener(IMusicPlayerActionListener listener);

    void setMusicPlayerActionDelegate(ActionDelegate actionDelegate);

    void setEffectProperties(Map<Integer,Integer> effectProperties);

    interface ActionDelegate {
        void onClickJoinChorus();
        void onClickLeaveChorus();
        void onClickSwitchSong();
        void onClickPlaying();
        void onClickOriginal();
        void onEarMonitoring(boolean enable);
        void onSignalVolume(int volume);
        void onMusicVolume(int volume);
        void onMusicPitch(int pitch);
        void onAudioEffect(int effectId);
    }
}

package io.agora.auikit.ui.musicplayer.listener;

import io.agora.karaoke_view.v11.model.LyricsLineModel;

public interface IMusicPlayerActionListener {
    default void onChooseSongClick() {
    }

    default void onSwitchOriginalClick() {
    }

    default void onMenuClick() {
    }

    default void onPlayClick() {
    }

    default void onChangeMusicClick() {
    }

    default void onStartSing() {
    }

    default void onJoinChorus() {
    }

    default void onLeaveChorus() {
    }

    default void onDragTo(long position) {
    }

    default void onRefPitchUpdate(float refPitch, int numberOfRefPitches) {
    }

    default void onLineFinished(LyricsLineModel line, int score, int cumulativeScore, int index, int total) {
    }

    default void onSkipPreludeClick() {
    }

    default void onSkipPostludeClick() {
    }

    default void onReGetLrcUrl() {
    }
}

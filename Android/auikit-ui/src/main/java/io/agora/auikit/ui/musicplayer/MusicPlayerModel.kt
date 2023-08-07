package io.agora.auikit.ui.musicplayer

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class ControllerEffectInfo(
    val index: Int,
    val effectId: Int,
    @DrawableRes val icon: Int,
    @StringRes val title: Int,
)

data class MusicSettingInfo(
    var isEar: Boolean = false,
    var signalVolume: Int = 0,
    var musicVolume: Int = 0,
    var pitch: Int = 0,
    var effectId: Int = 0
)
package io.agora.auikit.ui.chatBottomBar

import androidx.annotation.DrawableRes

data class AUIChatBottomBarIcon(
    @DrawableRes val iconRes : Int,
    val iconUrl: String,
    val labelString: String,
    val type: AUIChatBottomBarIconType
)

enum class AUIChatBottomBarIconType {
    /**
     * normal icon, can be input one or more in edit view
     */
    NORMAL
}
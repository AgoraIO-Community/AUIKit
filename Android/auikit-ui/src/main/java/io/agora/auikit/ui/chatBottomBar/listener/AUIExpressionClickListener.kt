package io.agora.auikit.ui.chatBottomBar.listener

import io.agora.auikit.ui.chatBottomBar.AUIChatBottomBarIcon


interface AUIExpressionClickListener {
    fun onDeleteImageClicked()
    fun onExpressionClicked(emojiIcon: AUIChatBottomBarIcon?)
}
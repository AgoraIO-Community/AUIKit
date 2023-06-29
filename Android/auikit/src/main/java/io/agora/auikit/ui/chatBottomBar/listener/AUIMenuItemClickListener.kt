package io.agora.auikit.ui.chatBottomBar.listener

import android.view.View

interface AUIMenuItemClickListener {
    fun onChatExtendMenuItemClick(itemId: Int, view: View?)
    fun onSendMessage(content: String?)

    fun setSoftKeyBoardHeightChangedListener(listener: AUISoftKeyboardHeightChangeListener){}
}
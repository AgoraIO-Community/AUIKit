package io.agora.auikit.ui.chatList.listener

import io.agora.auikit.ui.chatList.AUIChatInfo


interface AUIChatListItemClickListener {
    fun onItemClickListener(message: AUIChatInfo?){}
    fun onChatListViewClickListener(){}
}
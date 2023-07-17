package io.agora.auikit.ui.chatList.listener

import io.agora.auikit.model.AUIChatEntity

interface AUIChatListItemClickListener {
    fun onItemClickListener(message: AUIChatEntity?){}
    fun onChatListViewClickListener(){}
}
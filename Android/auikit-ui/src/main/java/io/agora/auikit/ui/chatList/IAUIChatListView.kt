package io.agora.auikit.ui.chatList

import io.agora.auikit.model.AUIChatEntity
import io.agora.auikit.ui.chatList.impl.AUIBroadcastMessageLayout
import io.agora.auikit.ui.chatList.listener.AUIChatListItemClickListener

interface IAUIChatListView {
    fun setChatListItemClickListener(listener:AUIChatListItemClickListener?){}

    fun refresh(msgList:ArrayList<AUIChatEntity>){}

    fun refreshSelectLast(msgList:ArrayList<AUIChatEntity>?){}

    fun initView(ownerId:String?){}

    // broadcast view
    fun setScrollSpeed(speed: Int){}

    fun showSubtitleView(content:String){}

    fun setSubtitleStatusChangeListener(listener: AUIBroadcastMessageLayout.SubtitleStatusChangeListener){}
}
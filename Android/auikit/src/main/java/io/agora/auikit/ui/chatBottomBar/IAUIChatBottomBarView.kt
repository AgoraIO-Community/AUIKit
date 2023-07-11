package io.agora.auikit.ui.chatBottomBar

import io.agora.auikit.ui.chatBottomBar.listener.AUIMenuItemClickListener

interface IAUIChatBottomBarView {

    fun setMenuItemClickListener(listener: AUIMenuItemClickListener?){}

    fun setSoftKeyListener(){}

    fun hideKeyboard(){}

    fun setEnableMic(isEnable: Boolean){}

    fun setShowMic(isShow:Boolean){}

    fun setShowMoreStatus(isOwner: Boolean?, isShowHandStatus: Boolean){}

}
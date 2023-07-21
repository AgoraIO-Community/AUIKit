package io.agora.auikit.ui.roomInfo.listener

import android.view.View

interface AUIRoomInfoActionListener {
    fun onBackClickListener(view: View){}
    fun onClickUpperLeftAvatar(view: View){}
    fun onLongClickUpperLeftAvatar(view: View): Boolean { return false }
    fun onUpperLeftRightIconClickListener(view: View){}
}
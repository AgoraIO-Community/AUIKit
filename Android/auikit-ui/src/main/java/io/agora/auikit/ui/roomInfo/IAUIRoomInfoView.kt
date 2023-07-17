package io.agora.auikit.ui.roomInfo

import android.net.Uri
import io.agora.auikit.ui.roomInfo.listener.AUIRoomInfoActionListener

interface IAUIRoomInfoView {

    fun setRoomInfoActionListener(listener: AUIRoomInfoActionListener?){}

    fun setVoiceTitle(title:String){}

    fun setVoiceSubTitle(subtitle:String){}

    fun setMemberAvatar(url:String){}

    fun setMemberAvatar(uri: Uri){}

    fun setRightIcon(url:String){}

    fun setRightIcon(uri: Uri){}
}
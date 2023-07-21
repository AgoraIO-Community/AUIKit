package io.agora.auikit.ui.member

import io.agora.auikit.model.AUIUserInfo
import io.agora.auikit.ui.basic.AUIImageView
import io.agora.auikit.ui.member.listener.AUIRoomMembersActionListener

interface IAUIRoomMembersView {

    fun setMemberActionListener(listener: AUIRoomMembersActionListener?){}

    fun setRightIconResources(url:String,view: AUIImageView){}

    fun setMemberData(rankList: List<AUIUserInfo?>){}
}
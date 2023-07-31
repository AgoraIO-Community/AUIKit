package io.agora.auikit.ui.member.listener

import android.view.View

interface AUIRoomMembersActionListener {
    fun onMemberRankClickListener(view: View){}
    fun onMemberRightUserMoreClickListener(view: View){}
    fun onMemberRightShutDownClickListener(view: View){}
}
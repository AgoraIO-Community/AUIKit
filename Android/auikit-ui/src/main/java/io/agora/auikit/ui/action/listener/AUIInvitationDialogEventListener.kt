package io.agora.auikit.ui.action.listener

import android.view.View
import io.agora.auikit.model.AUIUserInfo

interface AUIInvitationDialogEventListener {
    fun onInvitedItemClick(view: View, invitedIndex: Int, user: AUIUserInfo?){}
}
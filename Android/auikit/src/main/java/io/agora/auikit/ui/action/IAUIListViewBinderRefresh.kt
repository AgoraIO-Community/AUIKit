package io.agora.auikit.ui.action

import io.agora.auikit.model.AUIUserInfo
import io.agora.auikit.ui.action.listener.AUIApplyDialogEventListener
import io.agora.auikit.ui.action.listener.AUIInvitationDialogEventListener

interface IAUIListViewBinderRefresh {
    fun refreshApplyData(userList:MutableList<AUIUserInfo>?){}
    fun setApplyDialogListener(listener: AUIApplyDialogEventListener){}
    fun refreshInvitationData(userList:MutableList<AUIUserInfo>?){}
    fun setInvitationDialogListener(listener: AUIInvitationDialogEventListener){}
}
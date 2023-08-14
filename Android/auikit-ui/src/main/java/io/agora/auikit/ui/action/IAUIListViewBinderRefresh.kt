package io.agora.auikit.ui.action

import io.agora.auikit.ui.action.listener.AUIApplyDialogEventListener
import io.agora.auikit.ui.action.listener.AUIInvitationDialogEventListener

interface IAUIListViewBinderRefresh {
    fun refreshApplyData(userList:MutableList<AUIActionUserInfo?>){}
    fun setApplyDialogListener(listener: AUIApplyDialogEventListener){}
    fun refreshInvitationData(userList:MutableList<AUIActionUserInfo?>){}
    fun setInvitationDialogListener(listener: AUIInvitationDialogEventListener){}
}
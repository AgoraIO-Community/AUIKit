package io.agora.auikit.ui.action

import io.agora.auikit.ui.action.listener.AUIApplyDialogEventListener
import io.agora.auikit.ui.action.listener.AUIInvitationDialogEventListener

interface IAUIListViewBinderRefresh {
    fun refreshApplyData(userList:List<AUIActionUserInfo>){}
    fun setApplyDialogListener(listener: AUIApplyDialogEventListener){}
    fun refreshInvitationData(userList:List<AUIActionUserInfo>){}
    fun setInvitationDialogListener(listener: AUIInvitationDialogEventListener){}
}
package io.agora.auikit.ui.action.listener

import android.view.View
import io.agora.auikit.ui.action.AUIActionUserInfo

interface AUIApplyDialogEventListener {
    fun onApplyItemClick(view: View, applyIndex: Int?, user: AUIActionUserInfo?, position:Int){}
}
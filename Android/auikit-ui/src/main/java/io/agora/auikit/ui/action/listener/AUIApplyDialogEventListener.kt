package io.agora.auikit.ui.action.listener

import android.view.View
import io.agora.auikit.model.AUIUserInfo

interface AUIApplyDialogEventListener {
    fun onApplyItemClick(view: View, applyIndex: Int?, user: AUIUserInfo?, position:Int){}
}
package io.agora.auikit.ui.gift

import android.view.View
import io.agora.auikit.ui.gift.impl.dialog.AUiGiftListView

interface IAUIGiftBarrageView {
    fun setDialogActionListener(listener: AUiGiftListView.ActionListener){}

    fun refresh(giftList:List<AUIGiftInfo>?) {}

    fun setEmptyView(view: View?){}
}
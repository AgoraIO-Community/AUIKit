package io.agora.auikit.ui.gift

import android.view.View
import io.agora.auikit.model.AUIGiftEntity
import io.agora.auikit.ui.gift.impl.dialog.AUiGiftListView

interface IAUIGiftBarrageView {
    fun setDialogActionListener(listener: AUiGiftListView.ActionListener){}

    fun refresh(giftList:ArrayList<AUIGiftEntity>?) {}

    fun setEmptyView(view: View?){}
}
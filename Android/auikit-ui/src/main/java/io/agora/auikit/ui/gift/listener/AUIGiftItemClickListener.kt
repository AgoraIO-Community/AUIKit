package io.agora.auikit.ui.gift.listener

import android.view.View
import io.agora.auikit.model.AUIGiftEntity

interface AUIGiftItemClickListener {
    fun sendGift(view: View, position:Int, gift: AUIGiftEntity)
    fun selectGift(view: View, position:Int, gift: AUIGiftEntity)
}
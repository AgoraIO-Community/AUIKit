package io.agora.auikit.ui.gift.listener

import android.view.View
import io.agora.auikit.ui.gift.AUIGiftInfo

interface AUIGiftItemClickListener {
    fun sendGift(view: View, position:Int, gift: AUIGiftInfo)
    fun selectGift(view: View, position:Int, gift: AUIGiftInfo)
}
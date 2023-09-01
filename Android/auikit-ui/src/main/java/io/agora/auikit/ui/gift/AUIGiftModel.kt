package io.agora.auikit.ui.gift

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class AUIGiftInfo(
    val giftId: String,
    val giftName: String,
    val giftIcon: String,
    var giftCount: Int,
    val giftPrice: String,
    val giftEffect: String,
    val giftEffectMD5: String,
    var sendUserId: String,
    var sendUserName: String,
    var sendUserAvatar: String
)

data class AUIGiftTabInfo constructor(
    @SerializedName("tabId") val tabId: Int,
    @SerializedName("displayName") val tabName: String,
    @SerializedName("gifts") val gifts: List<AUIGiftInfo>
): Serializable

private val selectMap = mutableMapOf<String, Boolean>()
internal var AUIGiftInfo.selected: Boolean
    get() = selectMap.getOrDefault(giftId, false)
    set(value) = selectMap.set(giftId, value)
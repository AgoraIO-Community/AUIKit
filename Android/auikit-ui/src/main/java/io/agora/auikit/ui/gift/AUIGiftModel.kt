package io.agora.auikit.ui.gift

data class AUIGiftInfo(
    val giftId: String,
    val giftName: String,
    val giftIcon: String,
    val giftCount: Int,
    val giftPrice: String,
    val giftEffect: String,
    val giftEffectMD5: String,
    val sendUserId: String,
    val sendUserName: String,
    val sendUserAvatar: String
)

data class AUIGiftTabInfo(
    val tabId: Int,
    val tabName: String,
    val gifts: List<AUIGiftInfo>
)

private val selectMap = mutableMapOf<String, Boolean>()
internal var AUIGiftInfo.selected: Boolean
    get() = selectMap.getOrDefault(giftId, false)
    set(value) = selectMap.set(giftId, value)
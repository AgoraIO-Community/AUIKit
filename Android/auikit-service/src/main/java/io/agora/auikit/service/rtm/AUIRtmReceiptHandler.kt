package io.agora.auikit.service.rtm

data class AUIRtmReceiptHandler(
    val uniqueId: String,
    val closure: (AUIRtmException?) -> Unit,
    val runnable: Runnable,
)
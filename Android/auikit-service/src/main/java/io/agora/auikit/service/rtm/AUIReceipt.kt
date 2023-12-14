package io.agora.auikit.service.rtm

data class AUIReceipt(
    val uniqueId: String,
    val closure: (AUIRtmException?) -> Unit,
    val runnable: Runnable,
)
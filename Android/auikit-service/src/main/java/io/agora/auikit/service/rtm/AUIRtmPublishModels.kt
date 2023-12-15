package io.agora.auikit.service.rtm

import java.util.UUID

const val kAUISeatEnterInterface = "/v1/seat/enter"
const val kAUISeatLeaveInterface = "/v1/seat/leave"
const val kAUISeatKickInterface = "/v1/seat/kick"
const val kAUISeatMuteAudioInterface = "/v1/seat/audio/mute"
const val kAUISeatUnmuteAudioInterface = "/v1/seat/audio/unmute"
const val kAUISeatLockInterface = "/v1/seat/lock"
const val kAUISeatUnlockInterface = "/v1/seat/unlock"

data class AUIRtmMicSeatInfo(
    val roomId: String,
    val userId: String,
    val userName: String,
    val userAvatar: String,
    val micSeatNo: Int
)

data class AUIRtmPublishModel<Model>(
    val uniqueId: String = UUID.randomUUID().toString(),
    val interfaceName: String?,
    val data: Model?,
)

data class AUIRtmReceiptModel(
    val uniqueId: String,
    val code: Int,
    val reason: String
)
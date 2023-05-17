package io.agora.auikit.service.http.invitation

data class InvitationCreateReq(
    val channelName: String,
    val fromUserId: String,
    val toUserId: String,
    val payload: InvitationPayload
)
data class InvitationPayload(
    val desc: String,
    val seatNo: Int
)
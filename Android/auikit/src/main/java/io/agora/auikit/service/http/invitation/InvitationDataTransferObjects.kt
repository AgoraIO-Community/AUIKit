package io.agora.auikit.service.http.invitation

data class InvitationCreateReq(
    val roomId: String,
    val fromUserId: String,
    val toUserId:String,
    val payload: InvitationPayload?
)
data class InvitationPayload(
    val desc: String,
    val seatNo: Int
)

data class InvitationAcceptReq(
    val roomId: String,
    val fromUserId: String,
)

data class RejectInvitationAccept(
    val roomId: String,
    val fromUserId: String,
    val toUserId: String,
)
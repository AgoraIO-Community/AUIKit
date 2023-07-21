package io.agora.auikit.service.http.apply

import io.agora.auikit.service.http.invitation.InvitationPayload

data class ApplyCreateReq(
    val roomId: String,
    val fromUserId: String,
    val payload: InvitationPayload
)

data class ApplyAcceptReq(
    val roomId: String,
    val fromUserId: String,
    val toUserId: String,
)

data class ApplyCancelReq(
    val roomId: String,
    val fromUserId: String,
    val toUserId: String,
)


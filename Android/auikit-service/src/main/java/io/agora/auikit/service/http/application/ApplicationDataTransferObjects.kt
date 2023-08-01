package io.agora.auikit.service.http.application

data class ApplicationCreateReq(
    val channelName: String,
    val fromUserId: String,
    val toUserId: String,
    val payload: ApplicationPayload
)
data class ApplicationPayload(
    val desc: String,
    val seatNo: Int
)
data class TokenGenerateReq(
    val channelName: String,
    val userId: String
)
data class TokenGenerateResp(
    val appId: String,
    val rtcToken: String,
    val rtmToken: String
)
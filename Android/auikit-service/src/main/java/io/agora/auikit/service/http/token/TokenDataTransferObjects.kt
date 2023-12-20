package io.agora.auikit.service.http.token

data class TokenGenerateReq(
    val appId: String,
    val appCert: String,
    val channelName: String,
    val userId: String
)
data class TokenGenerateResp(
    val appId: String,
    val rtcToken: String,
    val rtmToken: String
)
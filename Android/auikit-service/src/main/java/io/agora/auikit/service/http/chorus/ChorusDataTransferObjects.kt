package io.agora.auikit.service.http.chorus

data class ChorusReq(
    val roomId: String,
    val songCode: String,
    val userId: String
)
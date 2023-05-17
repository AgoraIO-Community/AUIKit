package io.agora.auikit.service.http.seat

data class SeatEnterReq(
    val roomId: String,
    val userId: String,
    val userName: String,
    val userAvatar: String,
    val micSeatNo: Int
)
data class SeatLeaveReq(
    val roomId: String,
    val userId: String
)
data class SeatPickReq(
    val roomId: String,
    val micSeatUserId: String,
    val micSeatNo: Int
)
data class SeatInfoReq(
    val roomId: String,
    val userId: String,
    val micSeatNo: Int
)
package io.agora.auikit.service.http.song

data class SongAddReq constructor(
    val roomId: String,
    val userId: String,
    val songCode: String,
    val name: String,
    val singer: String,
    val poster: String,
    val releaseTime: String,
    val duration: Int,
    val musicUrl: String,
    val lrcUrl: String,
    val owner: SongOwner
)
data class SongOwner constructor(
    val userId: String,
    val userName: String,
    val userAvatar: String
)
data class SongPinReq constructor(
    val roomId: String,
    val songCode: String,
    val userId: String,
)
data class SongRemoveReq constructor(
    val roomId: String,
    val songCode: String,
    val userId: String,
)
data class SongPlayReq constructor(
    val roomId: String,
    val songCode: String,
    val userId: String,
)

data class SongStopReq constructor(
    val roomId: String,
    val songCode: String,
    val userId: String,
)
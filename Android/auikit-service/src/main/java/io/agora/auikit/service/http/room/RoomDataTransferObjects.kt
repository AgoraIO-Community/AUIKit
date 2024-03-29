package io.agora.auikit.service.http.room

import io.agora.auikit.model.AUIRoomInfo
import io.agora.auikit.service.http.PayloadResp

data class CreateRoomReq(
    val appId: String,
    val sceneId: String,
    val roomId: String,
    val payload: AUIRoomInfo
)
data class CreateRoomResp(
    val roomId: String,
    val payload: AUIRoomInfo,
    val createTime: Long,
    val updateTime: Long
)
data class RoomUserReq(
    val appId: String,
    val sceneId: String,
    val roomId: String
)
data class DestroyRoomResp(
    val roomId: String
)
data class RoomListReq(
    val appId: String,
    val sceneId: String,
    val pageSize: Int,
    val lastCreateTime: Long?
)
data class QueryRoomResp(
    val appId: String,
    val sceneId: String,
    val roomId: String,
    val payload: AUIRoomInfo,
    val createTime: Long,
    val updateTime: Long
)
data class UpdateRoomReq(
    val appId: String,
    val sceneId: String,
    val roomId: String,
    val payload: AUIRoomInfo
)
data class QueryRoomReq(
    val appId: String,
    val sceneId: String,
    val roomId: String
)
data class RoomListResp(
    val pageSize: Int,
    val count: Int,
    val list: List<PayloadResp<AUIRoomInfo>>
){
    fun getRoomList(): List<AUIRoomInfo>{
        val list = mutableListOf<AUIRoomInfo>()
        this.list.forEach {
            list.add(AUIRoomInfo().apply {
                roomId = it.roomId
                roomName = it.payload?.roomName ?: ""
                memberCount = it.payload?.memberCount ?: 0
                owner = it.payload?.owner
                thumbnail = it.payload?.thumbnail ?: ""
                createTime = it.createTime
            })
        }
        return list
    }
}

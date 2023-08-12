package io.agora.auikit.service.http.room

import io.agora.auikit.model.AUIRoomInfo
import io.agora.auikit.service.http.CommonResp
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface RoomInterface {

    @POST("room/create")
    fun createRoom(@Body req: CreateRoomReq): Call<CommonResp<CreateRoomResp>>

    @POST("room/destroy")
    fun destroyRoom(@Body req: RoomUserReq): Call<CommonResp<DestroyRoomResp>>

    @POST("room/query")
    fun fetchRoomInfo(@Body req: RoomReq): Call<CommonResp<AUIRoomInfo>>

    @POST("room/list")
    fun fetchRoomList(@Body req: RoomListReq): Call<CommonResp<RoomListResp>>

    @POST("room/leave")
    fun leaveRoom(@Body req: RoomUserReq): Call<CommonResp<String>>

    @POST("chatRoom/rooms/create")
    fun createChatRoom(@Body req: CreateChatRoomReq): Call<CommonResp<CreateChatRoomRsp>>
}
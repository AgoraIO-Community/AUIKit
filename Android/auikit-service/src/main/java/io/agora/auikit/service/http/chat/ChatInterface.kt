package io.agora.auikit.service.http.chat

import io.agora.auikit.service.http.CommonResp
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ChatInterface {

    @POST("chatRoom/create")
    fun createChatRoom(@Body req: CreateChatRoomReq): Call<CommonResp<CreateChatRoomResp>>

}
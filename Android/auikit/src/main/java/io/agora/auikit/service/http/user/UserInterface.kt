package io.agora.auikit.service.http.user

import io.agora.auikit.service.http.CommonResp
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface UserInterface {
    @POST("chatRoom/users/create")
    fun createUser(@Body req: CreateUserReq): Call<CommonResp<CreateUserRsp>>

    @POST("users/kickOut")
    fun kickOut(@Body req: KickUserReq): Call<CommonResp<KickUserRsp>>
}
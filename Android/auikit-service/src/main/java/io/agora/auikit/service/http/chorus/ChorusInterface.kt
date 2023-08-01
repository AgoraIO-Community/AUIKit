package io.agora.auikit.service.http.chorus

import io.agora.auikit.service.http.CommonResp
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ChorusInterface {

    @POST("chorus/join")
    fun choursJoin(@Body req: ChorusReq): Call<CommonResp<Any>>

    @POST("chorus/leave")
    fun choursLeave(@Body req: ChorusReq): Call<CommonResp<Any>>

}
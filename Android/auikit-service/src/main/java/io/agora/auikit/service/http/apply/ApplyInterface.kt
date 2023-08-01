package io.agora.auikit.service.http.apply

import io.agora.auikit.service.http.CommonResp
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApplyInterface {

    @POST("application/create")
    fun applyCreate(@Body req: ApplyCreateReq): Call<CommonResp<Any>>

    @POST("application/accept")
    fun applyAccept(@Body req: ApplyAcceptReq): Call<CommonResp<Any>>

    @POST("application/cancel")
    fun applyCancel(@Body req: ApplyCancelReq): Call<CommonResp<Any>>
}
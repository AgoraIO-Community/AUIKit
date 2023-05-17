package io.agora.auikit.service.http.invitation

import io.agora.auikit.service.http.CommonResp
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface InvitationInterface {

    @POST("application/create")
    fun createApplication(@Body req: InvitationCreateReq): Call<CommonResp<Any>>

}
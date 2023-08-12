package io.agora.auikit.service.http.invitation

import io.agora.auikit.service.http.CommonResp
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface InvitationInterface {

    @POST("invitation/create")
    fun initiateCreate(@Body req: InvitationCreateReq): Call<CommonResp<Any>>

    @POST("invitation/accept")
    fun acceptInitiate(@Body req: InvitationAcceptReq): Call<CommonResp<Any>>

    @POST("invitation/cancel")
    fun acceptCancel(@Body req: RejectInvitationAccept): Call<CommonResp<Any>>

}
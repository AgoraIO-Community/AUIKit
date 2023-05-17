package io.agora.auikit.service.http.song

import io.agora.auikit.service.http.CommonResp
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface SongInterface {

    @POST("song/add")
    fun songAdd(@Body req: SongAddReq): Call<CommonResp<Any>>

    @POST("song/pin")
    fun songPin(@Body req: SongPinReq): Call<CommonResp<Any>>

    @POST("song/remove")
    fun songRemove(@Body req: SongRemoveReq): Call<CommonResp<Any>>

    @POST("song/play")
    fun songPlay(@Body req: SongPlayReq): Call<CommonResp<Any>>

    @POST("song/stop")
    fun songStop(@Body req: SongStopReq): Call<CommonResp<Any>>
}
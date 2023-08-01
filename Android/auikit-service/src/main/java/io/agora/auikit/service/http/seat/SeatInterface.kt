package io.agora.auikit.service.http.seat

import io.agora.auikit.service.http.CommonResp
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface SeatInterface {

    @POST("seat/enter")
    fun seatEnter(@Body req: SeatEnterReq): Call<CommonResp<Any>>

    @POST("seat/leave")
    fun seatLeave(@Body req: SeatLeaveReq): Call<CommonResp<Any>>

    @POST("seat/pick")
    fun seatPick(@Body req: SeatPickReq): Call<CommonResp<Any>>

    @POST("seat/kick")
    fun seatKick(@Body req: SeatInfoReq): Call<CommonResp<Any>>

    @POST("seat/audio/mute")
    fun seatAudioMute(@Body req: SeatInfoReq): Call<CommonResp<Any>>

    @POST("seat/audio/unmute")
    fun seatAudioUnMute(@Body req: SeatInfoReq): Call<CommonResp<Any>>

    @POST("seat/video/mute")
    fun seatVideoMute(@Body req: SeatInfoReq): Call<CommonResp<Any>>

    @POST("seat/video/unmute")
    fun seatVideoUnMute(@Body req: SeatInfoReq): Call<CommonResp<Any>>
    @POST("seat/lock")
    fun seatLock(@Body req: SeatInfoReq): Call<CommonResp<Any>>
    @POST("seat/unlock")
    fun seatUnLock(@Body req: SeatInfoReq): Call<CommonResp<Any>>
}
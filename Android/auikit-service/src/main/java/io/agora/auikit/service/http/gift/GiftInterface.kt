package io.agora.auikit.service.http.gift

import io.agora.auikit.model.AUIGiftTabEntity
import io.agora.auikit.service.http.CommonResp
import retrofit2.Call
import retrofit2.http.GET

interface GiftInterface {

    @GET("gifts/list")
    fun fetchGiftInfo(): Call<CommonResp<List<AUIGiftTabEntity>>>
}
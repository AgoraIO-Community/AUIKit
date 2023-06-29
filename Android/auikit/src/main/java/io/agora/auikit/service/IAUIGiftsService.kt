package io.agora.auikit.service

import io.agora.auikit.model.AUIGiftEntity
import io.agora.auikit.service.callback.AUICallback
import io.agora.auikit.service.callback.AUIGiftListCallback

interface IAUIGiftsService : IAUICommonService<IAUIGiftsService.AUIGiftRespDelegate>{

    /**
     * - roomId: 房间id
     * - callback: 结果回调(包含礼物数组)
     */
    fun getGiftsFromService(callback:AUIGiftListCallback?)


    /**
     * - gift：礼物信息
     * - callback: 结果回调
     */
    fun sendGift(gift: AUIGiftEntity,callback: AUICallback)

    interface AUIGiftRespDelegate{
        fun onReceiveGiftMsg(channel:String){}
    }
}
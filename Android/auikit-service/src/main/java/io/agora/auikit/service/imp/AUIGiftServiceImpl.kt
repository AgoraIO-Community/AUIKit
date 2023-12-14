package io.agora.auikit.service.imp

import io.agora.auikit.model.AUIGiftEntity
import io.agora.auikit.model.AUIGiftTabEntity
import io.agora.auikit.model.AUIRoomContext
import io.agora.auikit.service.IAUIGiftsService
import io.agora.auikit.service.callback.AUICallback
import io.agora.auikit.service.callback.AUIException
import io.agora.auikit.service.callback.AUIGiftListCallback
import io.agora.auikit.service.http.CommonResp
import io.agora.auikit.service.http.HttpManager
import io.agora.auikit.service.http.Utils
import io.agora.auikit.service.http.gift.GiftInterface
import io.agora.auikit.service.im.AUIChatManager
import io.agora.auikit.service.rtm.AUIRtmManager
import io.agora.auikit.service.rtm.AUIRtmMsgRespObserver
import io.agora.auikit.utils.GsonTools
import io.agora.auikit.utils.ObservableHelper
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response

private const val giftKey = "AUIChatRoomGift"
class AUIGiftServiceImpl constructor(
    private val channelName: String,
    private val rtmManager: AUIRtmManager,
    private val chatManager:AUIChatManager
) : IAUIGiftsService, AUIRtmMsgRespObserver {

    private val observableHelper =
        ObservableHelper<IAUIGiftsService.AUIGiftRespObserver>()
    private var roomContext:AUIRoomContext

    init {
        rtmManager.subscribeMsg(channelName, giftKey, this)
        this.roomContext = AUIRoomContext.shared()
    }

    override fun getGiftsFromService(callback: AUIGiftListCallback?) {
        HttpManager.getService(GiftInterface::class.java)
            .fetchGiftInfo()
            .enqueue(object : retrofit2.Callback<CommonResp<List<AUIGiftTabEntity>>> {
                override fun onResponse(call: Call<CommonResp<List<AUIGiftTabEntity>>>
                , response: Response<CommonResp<List<AUIGiftTabEntity>>>
                ) {
                    val rsp = response.body()?.data
                    if (response.code() == 200 && rsp != null) {
                        callback?.onResult(null,rsp)
                    } else {
                        callback?.onResult(Utils.errorFromResponse(response),mutableListOf<AUIGiftTabEntity>())
                    }
                }
                override fun onFailure(call: Call<CommonResp<List<AUIGiftTabEntity>>>, t: Throwable) {
                    callback?.onResult(AUIException(-1, t.message),mutableListOf<AUIGiftTabEntity>())
                }
            })
    }

    override fun sendGift(gift: AUIGiftEntity, callback: AUICallback) {
        val giftJson = JSONObject()
        giftJson.put("messageType", "AUIChatRoomGift")
        giftJson.put("messageInfo", GsonTools.beanToString(gift))
        rtmManager.publish(channelName, giftJson.toString()){ error ->
            if(error == null ){
                callback.onResult(null)
            }else{
                callback.onResult(AUIException(AUIException.ERROR_CODE_RTM, "error: $error"))
            }
        }
    }

    override fun registerRespObserver(observer: IAUIGiftsService.AUIGiftRespObserver?) {
        observableHelper.subscribeEvent(observer)
    }

    override fun unRegisterRespObserver(observer: IAUIGiftsService.AUIGiftRespObserver?) {
        observableHelper.subscribeEvent(observer)
    }

    override fun getRoomContext() = roomContext

    override fun getChannelName() = channelName

    override fun onMsgDidChanged(channelName: String, key: String, value: Any) {
        if (key == giftKey){
            val gift = JSONObject(value.toString())
            GsonTools.toBean(gift["messageInfo"].toString(), AUIGiftEntity::class.java)?.let { it ->
                chatManager.addGiftList(it)
                this.observableHelper.notifyEventHandlers { it1 ->
                    it1.onReceiveGiftMsg(it)
                }
            }
        }
    }
}
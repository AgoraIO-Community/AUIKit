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
import io.agora.auikit.service.rtm.AUIRtmMsgProxyDelegate
import io.agora.auikit.utils.DelegateHelper
import io.agora.auikit.utils.GsonTools
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response

private const val giftKey = "AUIChatRoomGift"
class AUIGiftServiceImpl constructor(
    private val channelName: String,
    private val rtmManager: AUIRtmManager,
    private val chatManager:AUIChatManager
) : IAUIGiftsService, AUIRtmMsgProxyDelegate {

    private val delegateHelper = DelegateHelper<IAUIGiftsService.AUIGiftRespDelegate>()
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
        rtmManager.sendGiftMetadata(channelName,gift,callback)
    }

    override fun bindRespDelegate(delegate: IAUIGiftsService.AUIGiftRespDelegate?) {
        delegateHelper.bindDelegate(delegate)
    }

    override fun unbindRespDelegate(delegate: IAUIGiftsService.AUIGiftRespDelegate?) {
        delegateHelper.bindDelegate(delegate)
    }

    override fun getRoomContext() = roomContext

    override fun getChannelName() = channelName

    override fun onMsgDidChanged(channelName: String, key: String, value: Any) {
        if (key == giftKey){
            val gift = JSONObject(value.toString())
            GsonTools.toBean(gift["messageInfo"].toString(), AUIGiftEntity::class.java)?.let { it ->
                chatManager.addGiftList(it)
                this.delegateHelper.notifyDelegate { it1 ->
                    it1.onReceiveGiftMsg(it)
                }
            }
        }
    }
}
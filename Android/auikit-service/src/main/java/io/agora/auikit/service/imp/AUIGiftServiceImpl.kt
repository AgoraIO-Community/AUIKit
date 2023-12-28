package io.agora.auikit.service.imp

import io.agora.auikit.model.AUIGiftEntity
import io.agora.auikit.model.AUIGiftTabEntity
import io.agora.auikit.model.AUIRoomContext
import io.agora.auikit.service.IAUIGiftsService
import io.agora.auikit.service.callback.AUICallback
import io.agora.auikit.service.callback.AUIException
import io.agora.auikit.service.callback.AUIGiftListCallback
import io.agora.auikit.service.im.AUIChatManager
import io.agora.auikit.service.rtm.AUIRtmManager
import io.agora.auikit.service.rtm.AUIRtmMessageRespObserver
import io.agora.auikit.utils.GsonTools
import io.agora.auikit.utils.ObservableHelper
import org.json.JSONObject

private const val giftKey = "AUIChatRoomGift"
class AUIGiftServiceImpl constructor(
    private val channelName: String,
    private val rtmManager: AUIRtmManager,
    private val chatManager:AUIChatManager
) : IAUIGiftsService, AUIRtmMessageRespObserver {

    private val observableHelper =
        ObservableHelper<IAUIGiftsService.AUIGiftRespObserver>()
    private var roomContext:AUIRoomContext

    init {
        rtmManager.subscribeMessage(this)
        this.roomContext = AUIRoomContext.shared()
    }

    override fun getGiftsFromService(callback: AUIGiftListCallback?) {
        callback?.onResult(null, listOf(
            AUIGiftTabEntity(
                1,
                "Gifts",
                listOf(
                    AUIGiftEntity(
                        "2665752a-e273-427c-ac5a-4b2a9c82b255",
                        "Sweet Heart",
                        "1",
                        "https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/pictures/gift/AUIKitGift1.png"
                    ),
                    AUIGiftEntity(
                        "ff3bbb9e-ef18-430f-aa61-5bddf75eb722",
                        "Flower",
                        "1",
                        "https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/pictures/gift/AUIKitGift2.png"
                    ),
                    AUIGiftEntity(
                        "94f296fa-86d9-4552-84db-025b05ed9f8d",
                        "Sweet Heart",
                        "1",
                        "https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/pictures/gift/AUIKitGift3.png"
                    ),
                    AUIGiftEntity(
                        "d4cd0526-d8db-4e00-8fc0-d5228907a517",
                        "Super Agora",
                        "1",
                        "https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/pictures/gift/AUIKitGift4.png"
                    ),
                    AUIGiftEntity(
                        "c1997f02-d927-46f5-adda-e6af6714bd75",
                        "Star",
                        "1",
                        "https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/pictures/gift/AUIKitGift5.png"
                    ),
                    AUIGiftEntity(
                        "0c62b402-376f-4fbb-b584-769a8249189e",
                        "Lollipop",
                        "1",
                        "https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/pictures/gift/AUIKitGift6.png"
                    ),AUIGiftEntity(
                        "ce3f8bc3-74d7-43be-a040-c397d5c49f6d",
                        "Diamond",
                        "1",
                        "https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/pictures/gift/AUIKitGift7.png"
                    ),
                    AUIGiftEntity(
                        "948b1a3b-b2c6-41fc-99b7-a5b9457cd159",
                        "Crown",
                        "1",
                        "https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/pictures/gift/AUIKitGift8.png"
                    ),
                    AUIGiftEntity(
                        "f1e12397-feb7-4c01-b834-f11faf321dbf",
                        "Mic",
                        "1",
                        "https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/pictures/gift/AUIKitGift9.png"
                    ),
                    AUIGiftEntity(
                        "e915438c-7fbd-4e03-840f-0036ec97c824",
                        "Balloon",
                        "1",
                        "https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/pictures/gift/AUIKitGift10.png",
                        giftEffect = "https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/pag/ballon.pag",
                        effectMD5 = "141761700268c0290852af8f6a501c10"
                    ),
                    AUIGiftEntity(
                        "0c832b52-8f2e-4202-958b-9410db2d9438",
                        "Plant",
                        "1",
                        "https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/pictures/gift/AUIKitGift11.png",
                        giftEffect = "https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/pag/planet.pag",
                        effectMD5 = "41f3eeff249be268004d82a1d1eaf481"
                    ),
                    AUIGiftEntity(
                        "beada6a3-eae6-450e-869c-743d02fa95e7",
                        "Rocket",
                        "1",
                        "https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/pictures/gift/AUIKitGift12.png",
                        giftEffect = "https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/pag/rocket.pag",
                        effectMD5 = "de5094b30eebeadf8b8f5d8357a19578"
                    )
                )
            )
        ))
    }

    override fun sendGift(gift: AUIGiftEntity, callback: AUICallback) {
        val giftJson = JSONObject()
        giftJson.put("messageType", giftKey)
        giftJson.put("messageInfo", GsonTools.beanToString(gift))
        rtmManager.publish(channelName, lockOwnerId, giftJson.toString()){ error ->
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

    override fun onMessageReceive(channelName: String, publisherId: String, message: String) {
        if(publisherId.isEmpty() && this@AUIGiftServiceImpl.channelName != channelName){
            return
        }
        val json = JSONObject(message)
        if (json.getString("messageType") != giftKey) {
            return
        }
        GsonTools.toBean(json["messageInfo"].toString(), AUIGiftEntity::class.java)?.let { it ->
            chatManager.addGiftList(it)
            this.observableHelper.notifyEventHandlers { it1 ->
                it1.onReceiveGiftMsg(it)
            }
        }
    }
}
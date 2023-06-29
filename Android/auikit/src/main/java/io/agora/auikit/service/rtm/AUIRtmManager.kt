package io.agora.auikit.service.rtm

import android.content.Context
import android.util.Log
import io.agora.auikit.model.AUIGiftEntity
import io.agora.auikit.service.callback.AUICallback
import io.agora.auikit.service.callback.AUIException
import io.agora.auikit.utils.AUILogger
import io.agora.auikit.utils.GsonTools
import io.agora.rtm.ErrorInfo
import io.agora.rtm.JoinChannelOptions
import io.agora.rtm.Metadata
import io.agora.rtm.MetadataItem
import io.agora.rtm.MetadataOptions
import io.agora.rtm.PresenceOptions
import io.agora.rtm.ResultCallback
import io.agora.rtm.RtmClient
import io.agora.rtm.RtmConstants
import io.agora.rtm.RtmConstants.RtmChannelType
import io.agora.rtm.RtmEventListener
import io.agora.rtm.StateItem
import io.agora.rtm.StreamChannel
import io.agora.rtm.SubscribeOptions
import io.agora.rtm.WhoNowResult
import org.json.JSONObject

class AUIRtmManager(
    context: Context,
    private val rtmClient: RtmClient,
) {

    val proxy = AUIRtmMsgProxy()
    private val rtmStreamChannelMap = mutableMapOf<String, StreamChannel>()
    private val logger = AUILogger(AUILogger.Config(context, "AUIRtmManager"))
    @Volatile
    private var isLogin = false

    init {
        hookRtmEventListener()
    }

    private fun hookRtmEventListener() {
        val rtmClientClazz = rtmClient.javaClass
        val rtmEventListenerField = rtmClientClazz.getDeclaredField("mRtmEventListener")
        rtmEventListenerField.isAccessible = true
        val rtmEventListener = rtmEventListenerField.get(rtmClient) as RtmEventListener
        proxy.originEventListeners = rtmEventListener
        rtmEventListenerField.set(rtmClient, proxy)
    }

    // Channel Metadata
    private val kChannelType = RtmChannelType.STREAM


    fun login(token:String, completion: (AUIRtmException?) -> Unit){
        if(isLogin){
            completion.invoke(null)
            return
        }
        rtmClient.login(token, object : ResultCallback<Void>{
            override fun onSuccess(responseInfo: Void?) {
                isLogin = true
                completion.invoke(null)
            }

            override fun onFailure(errorInfo: ErrorInfo?) {
                if(errorInfo?.errorCode == RtmConstants.RTM_ERR_ALREADY_LOGIN){
                    isLogin = true
                    completion.invoke(null)
                }else{
                    completion.invoke(
                        AUIRtmException(
                        errorInfo?.errorCode ?: -1,
                        errorInfo?.errorReason ?: "UnKnow",
                        errorInfo?.operation ?: "UnKnow",
                    ))
                }
            }
        })
    }

    fun logout(){
        rtmClient.logout(object : ResultCallback<Void>{
            override fun onSuccess(responseInfo: Void?) {

            }

            override fun onFailure(errorInfo: ErrorInfo?) {

            }
        })
        isLogin = false
    }

    fun subscribeMsg(channelName: String, itemKey: String, delegate: AUIRtmMsgProxyDelegate) {
        proxy.subscribeMsg(channelName, itemKey, delegate)
    }

    fun unsubscribeMsg(channelName: String, itemKey: String, delegate: AUIRtmMsgProxyDelegate) {
        proxy.unsubscribeMsg(channelName, itemKey, delegate)
    }

    fun subscribeUser(delegate: AUIRtmUserProxyDelegate) {
        proxy.subscribeUser(delegate)
    }

    fun unsubscribeUser(delegate: AUIRtmUserProxyDelegate) {
        proxy.unsubscribeUser(delegate)
    }

    fun subscribe(channelType:RtmChannelType,channelName: String, token: String, completion: (AUIRtmException?) -> Unit) {
        when (channelType) {
            RtmChannelType.MESSAGE -> {
                val option = SubscribeOptions()
                option.withMetadata = true
                option.withPresence = true
                rtmClient.subscribe(channelName, option, object : ResultCallback<Void> {
                    override fun onSuccess(responseInfo: Void?) {
                        completion.invoke(null)
                    }

                    override fun onFailure(errorInfo: ErrorInfo?) {
                        if (errorInfo != null) {
                            completion.invoke(
                                AUIRtmException(errorInfo.errorCode, errorInfo.errorReason, errorInfo.operation)
                            )
                        } else {
                            completion.invoke(AUIRtmException(-1, "error", ""))
                        }
                    }
                })
            }
            RtmChannelType.STREAM -> {
                val option = JoinChannelOptions()
                option.token = token
                option.withMetadata = true
                option.withPresence = true
                if (rtmStreamChannelMap[channelName] == null) {
                    logger.d("EnterRoom", "create and join stream channel ...")
                    val streamChannel = rtmClient.createStreamChannel(channelName)
                    logger.d("StreamChannel", "joining... channelName=$channelName, token=$token")
                    streamChannel.join(option, object : ResultCallback<Void> {
                        override fun onSuccess(responseInfo: Void?) {
                            logger.d("EnterRoom", "create and join the stream channel successfully")
                            logger.d("StreamChannel", "join success channelName=$channelName")
                            completion.invoke(null)
                        }

                        override fun onFailure(errorInfo: ErrorInfo?) {
                            logger.d("EnterRoom", "create and join the stream channel failed for $errorInfo")
                            if (errorInfo != null) {
                                completion.invoke(AUIRtmException(errorInfo.errorCode, errorInfo.errorReason, errorInfo.operation))
                            } else {
                                completion.invoke(AUIRtmException(-1, "error", ""))
                            }
                        }
                    })
                    rtmStreamChannelMap[channelName] = streamChannel
                } else {
                    logger.d("EnterRoom", "create and join the stream channel failed for existing")
                    completion.invoke(
                        AUIRtmException(
                            -999,
                            "error for streamChannel existing",
                            "subscribe join stream channel"
                        )
                    )
                }
            }
            else -> {
                completion.invoke(AUIRtmException(-1, "error", ""))
            }
        }
    }

    fun unSubscribe(channelType:RtmChannelType,channelName: String) {
        proxy.cleanCache(channelName)
        when (channelType) {
            RtmChannelType.MESSAGE -> {
                rtmClient.unsubscribe(channelName)
            }
            RtmChannelType.STREAM -> {
                val streamChannel = rtmStreamChannelMap[channelName] ?: return
                streamChannel.leave(object : ResultCallback<Void> {
                    override fun onSuccess(responseInfo: Void?) {

                    }

                    override fun onFailure(errorInfo: ErrorInfo?) {

                    }
                })
                rtmStreamChannelMap.remove(channelName)
            }
        }
    }

    fun cleanMetadata(channelName: String, completion: (AUIRtmException?) -> Unit) {
        val storage = rtmClient.storage
        val data = storage.createMetadata()
        val options = MetadataOptions()
        options.recordTs = true
        options.recordUserId = true
        storage.removeChannelMetadata(channelName, kChannelType, data, options,
            "", object : ResultCallback<Void> {
                override fun onSuccess(responseInfo: Void?) {
                    completion.invoke(null)
                }

                override fun onFailure(errorInfo: ErrorInfo?) {
                    errorInfo ?: return
                    completion.invoke(
                        AUIRtmException(
                            errorInfo.errorCode,
                            errorInfo.errorReason,
                            errorInfo.operation
                        )
                    )
                }
            })
    }

    fun setMetadata(
        channelName: String,
        metadata: Map<String, String>,
        completion: (AUIRtmException?) -> Unit
    ) {
        val storage = rtmClient.storage
        val data = storage.createMetadata()
        metadata.forEach { entry ->
            val item = MetadataItem()
            item.key = entry.key
            item.value = entry.value
            data.setMetadataItem(item)
        }

        val options = MetadataOptions()
        storage.setChannelMetadata(
            channelName,
            kChannelType,
            data,
            options,
            "",
            object : ResultCallback<Void> {
                override fun onSuccess(responseInfo: Void?) {
                    completion.invoke(null)
                }

                override fun onFailure(errorInfo: ErrorInfo?) {
                    errorInfo ?: return
                    completion.invoke(
                        AUIRtmException(
                            errorInfo.errorCode,
                            errorInfo.errorReason,
                            errorInfo.operation
                        )
                    )
                }
            })
    }

    fun updateMetadata(
        channelName: String,
        metadata: Map<String, String>,
        completion: (AUIRtmException?) -> Unit
    ) {
        val storage = rtmClient.storage
        val data = storage.createMetadata()
        metadata.forEach { entry ->
            val item = MetadataItem()
            item.key = entry.key
            item.value = entry.value
            data.setMetadataItem(item)
        }
        val options = MetadataOptions()
        storage.updateChannelMetadata(
            channelName,
            kChannelType,
            data,
            options,
            "",
            object : ResultCallback<Void> {
                override fun onSuccess(responseInfo: Void?) {
                    completion.invoke(null)
                }

                override fun onFailure(errorInfo: ErrorInfo?) {
                    errorInfo ?: return
                    completion.invoke(
                        AUIRtmException(
                            errorInfo.errorCode,
                            errorInfo.errorReason,
                            errorInfo.operation
                        )
                    )
                }
            })
    }

    fun getMetadata(
        channelName: String,
        completion: (AUIRtmException?, Map<String, String>?) -> Unit
    ) {
        val storage = rtmClient.storage
        storage.getChannelMetadata(channelName, kChannelType, object : ResultCallback<Metadata> {
            override fun onSuccess(responseInfo: Metadata?) {
                responseInfo ?: return
                val map = mutableMapOf<String, String>()
                responseInfo.metadataItems.forEach { item ->
                    map[item.key] = item.value
                }
                completion.invoke(null, map)
            }

            override fun onFailure(errorInfo: ErrorInfo?) {
                errorInfo ?: return
                completion.invoke(
                    AUIRtmException(
                        errorInfo.errorCode,
                        errorInfo.errorReason,
                        errorInfo.operation
                    ), null
                )
            }
        })
    }


    // room list
    private val kRoomListChannelName = "uikitRoomList"
    private val kRoomListKey = "uikitRoomList"

    fun getRoomList(completion: (AUIRtmException?, String?) -> Unit) {
        getMetadata(kRoomListChannelName) { error, resp ->
            val roomListStr = resp?.get(kRoomListKey)
            completion.invoke(error, roomListStr)
        }
    }

    fun updateRoomList(listStr: String) {
        setMetadata(kRoomListChannelName, mapOf(Pair(kRoomListKey, listStr))) {}
    }

    fun whoNow(
        channelName: String,
        completion: (AUIRtmException?, List<Map<String, String>>?) -> Unit
    ) {
        val presence = rtmClient.presence
        val options = PresenceOptions()
        options.includeUserId = true
        options.includeState = true
        presence.whoNow(channelName, kChannelType, options, object: ResultCallback<WhoNowResult>{
            override fun onSuccess(responseInfo: WhoNowResult?) {
                responseInfo?: return
                var userList = arrayListOf<Map<String, String>>()
                responseInfo.userStateList.forEach { user ->
                    val userMap = mutableMapOf<String, String>()
                    userMap["userId"] = user.userId
                    user.states.forEach { item ->
                        userMap[item.key] = item.value
                    }
                    userList.add(userMap)
                }
                completion.invoke(null, userList)
            }

            override fun onFailure(errorInfo: ErrorInfo?) {
                errorInfo ?: return
                completion.invoke(AUIRtmException(errorInfo.errorCode, errorInfo.errorReason, errorInfo.operation), null)
            }
        })
    }

    fun setPresenceState(
        channelName: String,
        attr: Map<String, Any>,
        completion: (AUIRtmException?) -> Unit
    ) {
        val presence = rtmClient.presence
        val items = ArrayList<StateItem>()
        attr.forEach { entry ->
            val item = StateItem()
            item.key = entry.key
            item.value = entry.value.toString()
            items.add(item)
        }
        logger.d("PresenceState", "Setting channelName=$channelName, kChannelType=$kChannelType, items=$items")
        presence.setState(channelName, kChannelType, items, object : ResultCallback<Void>{
            override fun onSuccess(responseInfo: Void?) {
                logger.d("PresenceState", "Setting successfully")
                completion.invoke(null)
            }

            override fun onFailure(errorInfo: ErrorInfo?) {
                logger.d("PresenceState", "Setting failure : $errorInfo")
                completion.invoke(AUIRtmException(errorInfo?.errorCode ?: -1, errorInfo?.errorReason ?: "UnKnow Error", errorInfo?.operation ?: "UnKnow Error"))
            }
        })
    }

    // user metadata
    fun subscribeUser(userId: String){
        val storage = rtmClient.storage
        storage.subscribeUserMetadata(userId, object : ResultCallback<Void>{
            override fun onSuccess(responseInfo: Void?) {

            }

            override fun onFailure(errorInfo: ErrorInfo?) {

            }
        })
    }

    fun unSubscribeUser(userId: String){
        val storage = rtmClient.storage
        storage.unsubscribeUserMetadata(userId)
    }

    fun removeUserMetadata(userId: String){
        val storage = rtmClient.storage
        val data = storage.createMetadata()
        val options = MetadataOptions()
        options.recordTs = true
        options.recordUserId = true
        storage.removeUserMetadata(userId, data, options, object: ResultCallback<Void>{
            override fun onSuccess(responseInfo: Void?) {

            }

            override fun onFailure(errorInfo: ErrorInfo?) {

            }
        })
    }

    fun setUserMetadata(userId: String, metadata: Map<String, String>){
        val storage = rtmClient.storage
        val data = storage.createMetadata()
        val options = MetadataOptions()
        options.recordTs = true
        options.recordUserId = true
        metadata.forEach {entry ->
            val item = MetadataItem()
            item.key = entry.key
            item.value = entry.value
            data.setMetadataItem(item)
        }

        storage.setUserMetadata(userId, data, options, object: ResultCallback<Void>{
            override fun onSuccess(responseInfo: Void?) {

            }

            override fun onFailure(errorInfo: ErrorInfo?) {

            }
        })

    }

    fun updateUserMetadata(userId: String, metadata: Map<String, String>){
        val storage = rtmClient.storage
        val data = storage.createMetadata()
        val options = MetadataOptions()
        options.recordTs = true
        options.recordUserId = true
        metadata.forEach {entry ->
            val item = MetadataItem()
            item.key = entry.key
            item.value = entry.value
            data.setMetadataItem(item)
        }

        storage.updateUserMetadata(userId, data, options, object: ResultCallback<Void>{
            override fun onSuccess(responseInfo: Void?) {

            }

            override fun onFailure(errorInfo: ErrorInfo?) {

            }
        })
    }

    fun getUserMetadata(userId: String){
        val storage = rtmClient.storage
        storage.getUserMetadata(userId, object : ResultCallback<Metadata>{
            override fun onSuccess(responseInfo: Metadata?) {

            }

            override fun onFailure(errorInfo: ErrorInfo?) {

            }
        })
    }

    fun sendGiftMetadata(channelName: String, giftEntity: AUIGiftEntity, callback: AUICallback){
        val gift = GsonTools.beanToString(giftEntity)
        var giftJson = JSONObject()
        giftJson.put("messageType","AUIChatRoomGift")
        giftJson.put("messageInfo",gift)
        rtmClient.publish(channelName,giftJson.toString(),null,object: ResultCallback<Void>{
            override fun onSuccess(responseInfo: Void?) {
                giftJson = JSONObject()
                callback.onResult(null)
            }

            override fun onFailure(errorInfo: ErrorInfo?) {
                callback.onResult(errorInfo?.errorCode?.let { AUIException(it,errorInfo.errorReason) })
            }
        })
    }

}
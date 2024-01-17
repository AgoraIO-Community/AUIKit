package io.agora.auikit.service.collection

import com.google.gson.reflect.TypeToken
import io.agora.auikit.model.AUIRoomContext
import io.agora.auikit.service.callback.AUICallback
import io.agora.auikit.service.callback.AUIException
import io.agora.auikit.service.rtm.AUIRtmAttributeRespObserver
import io.agora.auikit.service.rtm.AUIRtmException
import io.agora.auikit.service.rtm.AUIRtmManager
import io.agora.auikit.service.rtm.AUIRtmMessageRespObserver
import io.agora.auikit.utils.GsonTools
import java.util.UUID

class AUIMapCollection(
    private val channelName: String,
    private val observeKey: String,
    private val rtmManager: AUIRtmManager
) : IAUICollection {

    private val messageRespObserver = object : AUIRtmMessageRespObserver {
        override fun onMessageReceive(channelName: String, publisherId: String, message: String) {
            dealReceiveMessage(publisherId, message)
        }
    }

    private val attributeRespObserver = object : AUIRtmAttributeRespObserver {
        override fun onAttributeChanged(channelName: String, key: String, value: Any) {
            if (this@AUIMapCollection.channelName != channelName || key != observeKey) {
                return
            }
            val strValue = value as? String ?: return

            val map = GsonTools.toBean<Map<String, Any>>(
                strValue,
                object : TypeToken<Map<String, Any>>() {}.type
            ) ?: return

            currentMap = map
        }
    }

    private var metadataWillUpdateClosure: ((
        publisherId: String, valueCmd: String?, newValue: Map<String, Any>, oldValue: Map<String, Any>
    ) -> AUIException?)? = null

    private var metadataWillMergeClosure: ((
        publisherId: String, valueCmd: String?, newValue: Map<String, Any>, oldValue: Map<String, Any>
    ) -> AUIException?)? = null

    private var attributesDidChangedClosure: ((channelName: String, observeKey: String, value: Any) -> Unit)? =
        null

    private var currentMap: Map<String, Any> = mutableMapOf()
        set(value) {
            field = value
            attributesDidChangedClosure?.invoke(channelName, observeKey, value)
        }

    init {
        rtmManager.subscribeMessage(messageRespObserver)
        rtmManager.subscribeAttribute(channelName, observeKey, attributeRespObserver)
    }

    /**
     * 释放资源
     *
     */
    override fun release() {
        rtmManager.unsubscribeMessage(messageRespObserver)
        rtmManager.unsubscribeAttribute(
            channelName, observeKey,
            attributeRespObserver
        )
    }

    /**
     * 订阅metadata在设置给rtm之前的事件，用于提前判断是否满足设置要求
     *
     * @param closure 事件回调，当返回null时表示允许设置metadata，当返回AUIException表示有异常，不允许设置metadata
     */
    override fun subscribeWillUpdate(
        closure: ((
            publisherId: String, valueCmd: String?, newValue: Map<String, Any>, oldValue: Map<String, Any>
        ) -> AUIException?)?
    ) {
        metadataWillUpdateClosure = closure
    }

    override fun subscribeWillMerge(
        closure: ((
            publisherId: String, valueCmd: String?, newValue: Map<String, Any>, oldValue: Map<String, Any>
        ) -> AUIException?)?
    ) {
        metadataWillMergeClosure = closure
    }

    override fun subscribeAttributesDidChanged(closure: ((channelName: String, observeKey: String, value: Any) -> Unit)?) {
        attributesDidChangedClosure = closure
    }

    override fun getMetaData(callback: ((error: AUIException?, value: Any?) -> Unit)?) {
        rtmManager.getMetadata(
            channelName = channelName,
            completion = { error, metaData ->
                if (error != null) {
                    callback?.invoke(AUIException(error.code, error.message), null)
                    return@getMetadata
                }
                val data = metaData?.get(observeKey)
                if (data == null) {
                    callback?.invoke(AUIException(-1, "Key data not exist. key=$observeKey"), null)
                    return@getMetadata
                }

                val map = GsonTools.toBean<Map<String, Any>>(
                    data,
                    object : TypeToken<Map<String, Any>>() {}.type
                )
                if (map == null) {
                    callback?.invoke(
                        AUIException(-1, "Key data parse error. key=$observeKey"),
                        null
                    )
                    return@getMetadata
                }

                callback?.invoke(null, map)
            }
        )
    }

    override fun updateMetaData(
        valueCmd: String?,
        value: Map<String, Any>,
        filter: List<Map<String, Any>>?,
        callback: AUICallback?
    ) {
        if (isArbiter()) {
            rtmUpdateMetaData(localUid(), valueCmd, value, callback)
            return
        }

        val uniqueId = UUID.randomUUID().toString()
        val data = AUICollectionMessage(
            channelName = channelName,
            uniqueId = uniqueId,
            sceneKey = observeKey,
            payload = AUICollectionMessagePayload(
                dataCmd = valueCmd,
                data = value
            )
        )
        val jsonStr = GsonTools.beanToString(data)
        if (jsonStr == null) {
            callback?.onResult(AUIException(-1, "updateMetaData fail"))
            return
        }
        rtmManager.publishAndWaitReceipt(
            channelName = channelName,
            userId = arbiterUid(),
            message = jsonStr,
            uniqueId = uniqueId
        ) { error ->
            if (error != null) {
                callback?.onResult(
                    AUIException(
                        AUIException.ERROR_CODE_RTM,
                        "setBatchMetadata error >> $error"
                    )
                )
            } else {
                callback?.onResult(null)
            }
        }
    }

    override fun mergeMetaData(
        valueCmd: String?,
        value: Map<String, Any>,
        filter: List<Map<String, Any>>?,
        callback: AUICallback?
    ) {
        if (isArbiter()) {
            rtmMergeMetaData(localUid(), valueCmd, value, callback)
            return
        }

        val uniqueId = UUID.randomUUID().toString()
        val data = AUICollectionMessage(
            channelName = channelName,
            uniqueId = uniqueId,
            sceneKey = observeKey,
            payload = AUICollectionMessagePayload(
                type = AUICollectionOperationTypeMerge,
                dataCmd = valueCmd,
                data = value
            )
        )
        val jsonStr = GsonTools.beanToString(data)
        if (jsonStr == null) {
            callback?.onResult(AUIException(-1, "updateMetaData fail"))
            return
        }
        rtmManager.publishAndWaitReceipt(
            channelName = channelName,
            userId = arbiterUid(),
            message = jsonStr,
            uniqueId = uniqueId
        ) { error ->
            if (error != null) {
                callback?.onResult(
                    AUIException(
                        AUIException.ERROR_CODE_RTM,
                        "setBatchMetadata error >> $error"
                    )
                )
            } else {
                callback?.onResult(null)
            }
        }
    }

    /**
     * 添加
     *
     * @param value
     * @param callback
     */
    override fun addMetaData(
        valueCmd: String?,
        value: Map<String, Any>,
        filter: List<Map<String, Any>>?,
        callback: AUICallback?
    ) {
        updateMetaData(valueCmd, value, filter, callback)
    }

    override fun removeMetaData(
        valueCmd: String?,
        filter: List<Map<String, Any>>?,
        callback: AUICallback?
    ) {
        TODO("Not yet implemented")
    }

    override fun cleanMetaData(callback: AUICallback?) {
        if (isArbiter()) {
            rtmCleanMetaData(callback)
            return
        }

        val uniqueId = UUID.randomUUID().toString()
        val data = AUICollectionMessage(
            channelName = channelName,
            uniqueId = uniqueId,
            sceneKey = observeKey,
            payload = AUICollectionMessagePayload(
                type = AUICollectionOperationTypeClean,
                dataCmd = null,
                data = null
            )
        )
        val jsonStr = GsonTools.beanToString(data)
        if (jsonStr == null) {
            callback?.onResult(AUIException(-1, "cleanMetaData fail"))
            return
        }
        rtmManager.publishAndWaitReceipt(
            channelName = channelName,
            userId = arbiterUid(),
            message = jsonStr,
            uniqueId = uniqueId
        ) { error ->
            if (error != null) {
                callback?.onResult(
                    AUIException(
                        AUIException.ERROR_CODE_RTM,
                        "cleanMetaData error >> $error"
                    )
                )
            } else {
                callback?.onResult(null)
            }
        }
    }

    private fun localUid() = AUIRoomContext.shared().currentUserInfo.userId

    private fun arbiterUid() = AUIRoomContext.shared().getArbiter(channelName)?.lockOwnerId() ?: ""

    private fun isArbiter() = AUIRoomContext.shared().getArbiter(channelName)?.isArbiter() ?: false


    private fun rtmUpdateMetaData(
        publisherId: String,
        valueCmd: String?,
        value: Map<String, Any>,
        callback: AUICallback?
    ) {
        val error =
            metadataWillUpdateClosure?.invoke(publisherId, valueCmd, value, HashMap(currentMap))
        if (error != null) {
            callback?.onResult(error)
            return
        }

        val map = HashMap(currentMap)
        value.forEach { (k, v) ->
            map[k] = v
        }
        val data = GsonTools.beanToString(map)
        if (data == null) {
            callback?.onResult(AUIException(-1, "rtmSetMetaData fail"))
            return
        }

        rtmManager.setBatchMetadata(
            channelName,
            metadata = mapOf(Pair(observeKey, data)),
        ) { e ->
            if (e != null) {
                callback?.onResult(
                    AUIException(
                        AUIException.ERROR_CODE_RTM,
                        "setBatchMetadata error >> $e"
                    )
                )
            } else {
                callback?.onResult(null)
            }
        }
    }

    private fun rtmMergeMetaData(
        publisherId: String,
        valueCmd: String?,
        value: Map<String, Any>,
        callback: AUICallback?
    ) {
        val error =
            metadataWillMergeClosure?.invoke(publisherId, valueCmd, value, HashMap(currentMap))
        if (error != null) {
            callback?.onResult(error)
            return
        }

        val map = AUICollectionUtils.mergeMap(currentMap, value)
        val data = GsonTools.beanToString(map)
        if (data == null) {
            callback?.onResult(AUIException(-1, "rtmSetMetaData fail"))
            return
        }

        rtmManager.setBatchMetadata(
            channelName,
            metadata = mapOf(Pair(observeKey, data)),
        ) { e ->
            if (e != null) {
                callback?.onResult(
                    AUIException(
                        AUIException.ERROR_CODE_RTM,
                        "setBatchMetadata error >> $e"
                    )
                )
            } else {
                callback?.onResult(null)
            }
        }
    }

    private fun rtmCleanMetaData(callback: AUICallback?) {
        rtmManager.cleanBatchMetadata(
            channelName = channelName,
            remoteKeys = listOf(observeKey),
            completion = { error ->
                if (error != null) {
                    callback?.onResult(AUIException(error.code, error.message))
                } else {
                    callback?.onResult(null)
                }
            }
        )
    }

    private fun dealReceiveMessage(publisherId: String, message: String) {
        val messageModel = GsonTools.toBean(message, AUICollectionMessage::class.java) ?: return

        val uniqueId = messageModel.uniqueId
        if (uniqueId == null
            || messageModel.channelName != channelName
            || messageModel.sceneKey != observeKey
        ) {
            return
        }

        if (messageModel.messageType == AUICollectionMessageTypeReceipt) {
            // receipt message from arbiter
            val data = messageModel.payload?.data as? Map<*, *>
            if(data == null){
                rtmManager.markReceiptFinished(uniqueId, AUIRtmException(
                    -1, "data is not a map", "receipt message"
                ))
                return
            }
            val code = data["code"] as? Int ?: 0
            val reason = data["reason"] as? String ?: "success"
            if (code == 0) {
                // success
                rtmManager.markReceiptFinished(uniqueId, null)
            } else {
                // failure
                rtmManager.markReceiptFinished(
                    uniqueId, AUIRtmException(
                        code,
                        reason,
                        "receipt message from arbiter"
                    )
                )
            }
            return
        }
        val updateType = messageModel.payload?.type
        if (updateType == null) {
            sendReceipt(publisherId, uniqueId, AUIException(-1, "updateType not found"))
            return
        }
        val valueCmd = messageModel.payload.dataCmd
        var error: AUIException? = null
        when (updateType) {
            AUICollectionOperationTypeAdd, AUICollectionOperationTypeUpdate, AUICollectionOperationTypeMerge -> {
                val data = messageModel.payload.data
                if (data != null) {
                    if (updateType == AUICollectionOperationTypeMerge) {
                        rtmMergeMetaData(publisherId, valueCmd, data) {
                            sendReceipt(publisherId, uniqueId, it)
                        }
                    } else {
                        rtmUpdateMetaData(publisherId, valueCmd, data) {
                            sendReceipt(publisherId, uniqueId, it)
                        }
                    }

                } else {
                    error = AUIException(-1, "payload is null or not a map")
                }
            }

            AUICollectionOperationTypeClean -> {
                rtmCleanMetaData {
                    sendReceipt(publisherId, uniqueId, it)
                }
            }

            AUICollectionOperationTypeRemove -> {
                error = AUIException(-1, "map collection remove type unsupported")
            }

            AUICollectionOperationTypeIncrease -> {
                error = AUIException(-1, "map collection increase type unsupported")
            }

            AUICollectionOperationTypeDecrease -> {
                error = AUIException(-1, "map collection decrease type unsupported")
            }
        }

        if (error != null) {
            sendReceipt(
                publisherId,
                uniqueId,
                error
            )
        }
    }

    private fun sendReceipt(publisherId: String, uniqueId: String, error: AUIException?) {
        val data = mapOf(
            Pair("code", error?.code ?: 0),
            Pair("reason", error?.message ?: "")
        )
        val message = AUICollectionMessage(
            channelName = channelName,
            messageType = AUICollectionMessageTypeReceipt,
            uniqueId = uniqueId,
            sceneKey = observeKey,
            payload = AUICollectionMessagePayload(
                dataCmd = "",
                data = data,
            )
        )
        val jsonStr = GsonTools.beanToString(message) ?: return

        rtmManager.publish(channelName, publisherId, jsonStr) {}
    }
}
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
import org.json.JSONObject
import java.util.UUID

class AUIMapCollection(
    private val channelName: String,
    private val observekey: String,
    private val rtmManager: AUIRtmManager
) {

    private val messageRespObserver = object : AUIRtmMessageRespObserver {
        override fun onMessageReceive(channelName: String, publisherId: String, message: String) {
            dealReceiveMessage(publisherId, message)
        }
    }

    private val attributeRespObserver = object : AUIRtmAttributeRespObserver {
        override fun onAttributeChanged(channelName: String, key: String, value: Any) {
            if (this@AUIMapCollection.channelName != channelName || key != observekey) {
                return
            }
            val strValue = value as? String ?: return
            val json = JSONObject(strValue)
            val keys = json.keys()
            val map = mutableMapOf<String, Any>()
            while (keys.hasNext()) {
                val next = keys.next()
                map[next] = json.get(next)
            }
            currentValue = map
        }
    }

    private var metadataWillUpdateClosure: ((
        publisherId: String, valueCmd: String?, newValue: Map<String, Any>, oldValue: Map<String, Any>
    ) -> AUIException?)? = null

    private var metadataWillMergeClosure: ((
        publisherId: String, valueCmd: String?, newValue: Map<String, Any>, oldValue: Map<String, Any>
    ) -> AUIException?)? = null

    private var attributesDidChangedClosure: ((String, String, Any) -> Unit)? = null

    private var currentValue: Map<String, Any> = mutableMapOf<String, Any>()

    init {
        rtmManager.subscribeMessage(messageRespObserver)
        rtmManager.subscribeAttribute(channelName, observekey, attributeRespObserver)
    }

    /**
     * 释放资源
     *
     */
    fun release() {
        rtmManager.unsubscribeMessage(messageRespObserver)
        rtmManager.unsubscribeAttribute(
            channelName, observekey,
            attributeRespObserver
        )
    }

    /**
     * 订阅metadata在设置给rtm之前的事件，用于提前判断是否满足设置要求
     *
     * @param closure 事件回调，当返回null时表示允许设置metadata，当返回AUIException表示有异常，不允许设置metadata
     */
    fun subscribeWillUpdate(
        closure: (
            publisherId: String, valueCmd: String?, newValue: Map<String, Any>, oldValue: Map<String, Any>
        ) -> AUIException?
    ) {
        metadataWillUpdateClosure = closure
    }

    fun subscribeWillMerge(
        closure: (
            publisherId: String, valueCmd: String?, newValue: Map<String, Any>, oldValue: Map<String, Any>
        ) -> AUIException?
    ) {
        metadataWillMergeClosure = closure
    }

    fun subscribeAttributesDidChanged(closure: (String, String, Any) -> Unit) {
        attributesDidChangedClosure = closure
    }

    fun getMetaData(callback: (AUIException?, Any?) -> Unit) {
        rtmManager.getMetadata(
            channelName = channelName,
            completion = { error, metaData ->
                if (error != null) {
                    callback.invoke(AUIException(error.code, error.message), null)
                    return@getMetadata
                }
                val data = metaData?.get(observekey)
                if (data == null) {
                    callback.invoke(AUIException(-1, "Key data not exist. key=$observekey"), null)
                    return@getMetadata
                }

                val map = GsonTools.toBean<Map<String, Any>>(
                    data,
                    object : TypeToken<Map<String, Any>>() {}.type
                )
                if (map == null) {
                    callback.invoke(AUIException(-1, "Key data parse error. key=$observekey"), null)
                    return@getMetadata
                }

                callback.invoke(null, map)
            }
        )
    }

    /**
     * 更新，替换根节点
     *
     * @param valueCmd 命令类型
     * @param value
     * @param objectId
     * @param callback
     */
    fun updateMetaData(
        valueCmd: String?,
        value: Map<String, Any>,
        filter: List<Map<String, Any>>? = null,
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
            sceneKey = observekey,
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

    /**
     * 合并，替换所有子节点
     *
     * @param valueCmd
     * @param value
     * @param objectId
     * @param callback
     */
    fun mergeMetaData(
        valueCmd: String?,
        value: Map<String, Any>,
        filter: List<Map<String, Any>>? = null,
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
            sceneKey = observekey,
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
    fun addMetaData(
        valueCmd: String?,
        value: Map<String, Any>,
        filter: List<Map<String, Any>>?,
        callback: AUICallback?
    ) {
        updateMetaData(valueCmd, value, filter, callback)
    }

    /**
     * 移除
     *
     * @param valueCmd
     * @param objectId
     * @param callback
     */
    fun removeMetaData(
        valueCmd: String?,
        filter: List<Map<String, Any>>?,
        callback: AUICallback?
    ) {
        throw RuntimeException("unsupport method")
    }

    fun cleanMetaData(callback: AUICallback?) {
        if (isArbiter()) {
            rtmCleanMetaData(callback)
            return
        }

        val uniqueId = UUID.randomUUID().toString()
        val data = AUICollectionMessage(
            channelName = channelName,
            uniqueId = uniqueId,
            sceneKey = observekey,
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
            metadataWillUpdateClosure?.invoke(publisherId, valueCmd, value, HashMap(currentValue))
        if (error != null) {
            callback?.onResult(error)
            return
        }

        val map = HashMap(currentValue)
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
            metadata = mapOf(Pair(observekey, data)),
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
            metadataWillMergeClosure?.invoke(publisherId, valueCmd, value, HashMap(currentValue))
        if (error != null) {
            callback?.onResult(error)
            return
        }

        val map = mergeMap(currentValue, value)
        val data = GsonTools.beanToString(map)
        if (data == null) {
            callback?.onResult(AUIException(-1, "rtmSetMetaData fail"))
            return
        }

        rtmManager.setBatchMetadata(
            channelName,
            metadata = mapOf(Pair(observekey, data)),
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
            remoteKeys = listOf(observekey),
            completion = { error ->
                if (error != null) {
                    callback?.onResult(AUIException(error.code, error.message))
                } else {
                    callback?.onResult(null)
                }
            }
        )
    }

    private fun mergeMap(origMap: Map<String, Any>, newMap: Map<String, Any>): Map<String, Any> {
        val resultMap = HashMap<String, Any>(origMap)
        newMap.forEach { (k, v) ->
            val dic = v as? Map<String, Any>
            if (dic != null) {
                val origDic = mutableMapOf<String, Any>()
                if (resultMap[k] is JSONObject) {
                    val json = resultMap[k] as JSONObject
                    val keys = json.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        origDic[key] = json.get(key)
                    }
                }
                val newDic = mergeMap(origDic, dic)
                resultMap[k] = newDic
            } else {
                resultMap[k] = v
            }
        }
        return resultMap
    }

    private fun dealReceiveMessage(publisherId: String, message: String) {
        val messageModel = GsonTools.toBean(message, AUICollectionMessage::class.java) ?: return

        val uniqueId = messageModel.uniqueId
        if (uniqueId == null || messageModel.channelName != channelName) {
            return
        }

        if (messageModel.messageType == AUICollectionMessageTypeReceipt) {
            // receipt message from arbiter
            val data = JSONObject(messageModel.payload?.data?.toString() ?: return)
            val code = data.get("code") as? Int ?: 0
            val reason = data.get("reason") as? String ?: "success"
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
            sceneKey = observekey,
            payload = AUICollectionMessagePayload(
                dataCmd = "",
                data = data,
            )
        )
        val jsonStr = GsonTools.beanToString(message) ?: return

        rtmManager.publish(channelName, publisherId, jsonStr) {}
    }
}
package io.agora.auikit.service.collection

import com.google.gson.reflect.TypeToken
import io.agora.auikit.service.callback.AUICallback
import io.agora.auikit.service.callback.AUIException
import io.agora.auikit.service.rtm.AUIRtmException
import io.agora.auikit.service.rtm.AUIRtmManager
import io.agora.auikit.utils.GsonTools
import java.util.UUID

class AUIMapCollection(
    private val channelName: String,
    private val observeKey: String,
    private val rtmManager: AUIRtmManager
) : AUIBaseCollection(channelName, observeKey, rtmManager) {

    private var currentMap: Map<String, Any> = mutableMapOf()
        set(value) {
            field = value
            attributesDidChangedClosure?.invoke(channelName, observeKey, AUIAttributesModel(value))
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

    override fun calculateMetaData(
        valueCmd: String?,
        key: List<String>,
        value: Int,
        min: Int,
        max: Int,
        filter: List<Map<String, Any>>?,
        callback: AUICallback?
    ) {
        if (isArbiter()) {
            rtmCalculateMetaData(
                localUid(),
                valueCmd,
                key,
                AUICollectionCalcValue(value, min, max),
                callback
            )
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
                data = GsonTools.beanToMap(
                    AUICollectionCalcData(
                        key,
                        AUICollectionCalcValue(value, max, min)
                    )
                )
            )
        )
        val jsonStr = GsonTools.beanToString(data)
        if (jsonStr == null) {
            callback?.onResult(AUIException(-1, "calculateMetaData fail"))
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
        val retMap =
            attributesWillSetClosure?.invoke(channelName, observeKey, valueCmd, AUIAttributesModel(map))?.getMap()
                ?: map
        val data = GsonTools.beanToString(retMap)
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
        val retMap =
            attributesWillSetClosure?.invoke(channelName, observeKey, valueCmd, AUIAttributesModel(map))?.getMap()
                ?: map
        val data = GsonTools.beanToString(retMap)
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

    private fun rtmCalculateMetaData(
        publisherId: String,
        valueCmd: String?,
        key: List<String>,
        value: AUICollectionCalcValue,
        callback: AUICallback?
    ) {
        val currMap = HashMap(currentMap)
        val err = metadataWillCalculateClosure?.invoke(
            publisherId,
            valueCmd,
            currMap,
            key,
            value.value,
            value.min,
            value.max
        )
        if (err != null) {
            callback?.onResult(err)
            return
        }

        val map = AUICollectionUtils.calculateMap(
            currMap,
            key,
            value.value,
            value.min,
            value.max
        ) ?: mutableMapOf()
        val retMap =
            attributesWillSetClosure?.invoke(channelName, observeKey, valueCmd, AUIAttributesModel(map))?.getMap()
                ?: map
        val data = GsonTools.beanToString(retMap)
        if (data == null) {
            callback?.onResult(AUIException(-1, "rtmCalculateMetaData fail"))
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

    override fun onAttributeChanged(value: Any) {
        val strValue = value as? String ?: return

        val map = GsonTools.toBean<Map<String, Any>>(
            strValue,
            object : TypeToken<Map<String, Any>>() {}.type
        ) ?: return

        currentMap = map
    }

    override fun onMessageReceive(publisherId: String, message: String) {
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
            val collectionError = GsonTools.toBean(
                GsonTools.beanToString(messageModel.payload?.data),
                AUICollectionError::class.java
            )
            if (collectionError == null) {
                rtmManager.markReceiptFinished(
                    uniqueId, AUIRtmException(
                        -1, "data is not a map", "receipt message"
                    )
                )
                return
            }

            val code = collectionError.code
            val reason = collectionError.reason
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

            AUICollectionOperationTypeCalculate -> {
                val calcData = GsonTools.toBean(
                    GsonTools.beanToString(messageModel.payload.data),
                    AUICollectionCalcData::class.java
                )
                if (calcData != null) {
                    rtmCalculateMetaData(
                        publisherId,
                        valueCmd,
                        calcData.key,
                        calcData.value
                    ) {
                        sendReceipt(publisherId, uniqueId, it)
                    }
                } else {
                    error = AUIException(-1, "payload data is not AUICollectionCalcData")
                }
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

}
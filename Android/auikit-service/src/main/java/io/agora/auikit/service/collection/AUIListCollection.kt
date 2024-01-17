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

class AUIListCollection(
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
            if (this@AUIListCollection.channelName != channelName || key != observeKey) {
                return
            }
            val strValue = value as? String ?: return
            val list = GsonTools.toBean<List<Map<String, Any>>>(
                strValue,
                object : TypeToken<List<Map<String, Any>>>() {}.type
            ) ?: return
            currentList = list
        }
    }

    private var metadataWillAddClosure: ((
        publisherId: String, valueCmd: String?, value: Map<String, Any>
    ) -> AUIException?)? = null

    private var metadataWillUpdateClosure: ((
        publisherId: String, valueCmd: String?, newValue: Map<String, Any>, oldValue: Map<String, Any>
    ) -> AUIException?)? = null

    private var metadataWillMergeClosure: ((
        publisherId: String, valueCmd: String?, newValue: Map<String, Any>, oldValue: Map<String, Any>
    ) -> AUIException?)? = null

    private var metadataWillRemoveClosure: ((
        publisherId: String, valueCmd: String?, value: Map<String, Any>
    ) -> AUIException?)? = null

    private var attributesDidChangedClosure: ((
        channelName: String, observeKey: String, value: Any
    ) -> Unit)? = null

    private var currentList: List<Map<String, Any>> = mutableListOf()
        set(value) {
            field = value
            attributesDidChangedClosure?.invoke(channelName, observeKey, value)
        }

    init {
        rtmManager.subscribeMessage(messageRespObserver)
        rtmManager.subscribeAttribute(channelName, observeKey, attributeRespObserver)
    }

    override fun release() {
        rtmManager.unsubscribeMessage(messageRespObserver)
        rtmManager.unsubscribeAttribute(
            channelName, observeKey,
            attributeRespObserver
        )
    }

    override fun subscribeWillAdd(closure: ((publisherId: String, valueCmd: String?, value: Map<String, Any>) -> AUIException?)?) {
        metadataWillAddClosure = closure
    }

    override fun subscribeWillUpdate(closure: ((publisherId: String, valueCmd: String?, newValue: Map<String, Any>, oldValue: Map<String, Any>) -> AUIException?)?) {
        metadataWillUpdateClosure = closure
    }

    override fun subscribeWillMerge(closure: ((publisherId: String, valueCmd: String?, newValue: Map<String, Any>, oldValue: Map<String, Any>) -> AUIException?)?) {
        metadataWillMergeClosure = closure
    }

    override fun subscribeWillRemove(closure: ((publisherId: String, valueCmd: String?, value: Map<String, Any>) -> AUIException?)?) {
        metadataWillRemoveClosure = closure
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

                val list = GsonTools.toBean<List<Map<String, Any>>>(
                    data,
                    object : TypeToken<List<Map<String, Any>>>() {}.type
                )
                if (list == null) {
                    callback?.invoke(
                        AUIException(-1, "Key data parse error. key=$observeKey"),
                        null
                    )
                    return@getMetadata
                }

                callback?.invoke(null, list)
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
            rtmUpdateMetaData(localUid(), valueCmd, value, filter, callback)
            return
        }

        val uniqueId = UUID.randomUUID().toString()
        val data = AUICollectionMessage(
            channelName = channelName,
            uniqueId = uniqueId,
            sceneKey = observeKey,
            payload = AUICollectionMessagePayload(
                dataCmd = valueCmd,
                data = value,
                filter = filter
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
            rtmMergeMetaData(localUid(), valueCmd, value, filter, callback)
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
                data = value,
                filter = filter
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

    override fun addMetaData(
        valueCmd: String?,
        value: Map<String, Any>,
        filter: List<Map<String, Any>>?,
        callback: AUICallback?
    ) {
        if (isArbiter()) {
            rtmAddMetaData(localUid(), valueCmd, value, filter, callback)
            return
        }

        val uniqueId = UUID.randomUUID().toString()
        val data = AUICollectionMessage(
            channelName = channelName,
            uniqueId = uniqueId,
            sceneKey = observeKey,
            payload = AUICollectionMessagePayload(
                type = AUICollectionOperationTypeAdd,
                dataCmd = valueCmd,
                data = value,
                filter = filter
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

    override fun removeMetaData(
        valueCmd: String?,
        filter: List<Map<String, Any>>?,
        callback: AUICallback?
    ) {
        if (isArbiter()) {
            rtmRemoveMetaData(localUid(), valueCmd, filter, callback)
            return
        }

        val uniqueId = UUID.randomUUID().toString()
        val data = AUICollectionMessage(
            channelName = channelName,
            uniqueId = uniqueId,
            sceneKey = observeKey,
            payload = AUICollectionMessagePayload(
                type = AUICollectionOperationTypeRemove,
                dataCmd = valueCmd,
                data = null,
                filter = filter
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
                dataCmd = "",
                data = null
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


    private fun localUid() = AUIRoomContext.shared().currentUserInfo.userId

    private fun arbiterUid() = AUIRoomContext.shared().getArbiter(channelName)?.lockOwnerId() ?: ""

    private fun isArbiter() = AUIRoomContext.shared().getArbiter(channelName)?.isArbiter() ?: false


    private fun rtmAddMetaData(
        publisherId: String,
        valueCmd: String?,
        value: Map<String, Any>,
        filter: List<Map<String, Any>>?,
        callback: AUICallback?
    ) {
        val itemIndexes = AUICollectionUtils.getItemIndexes(currentList, filter)
        if (itemIndexes?.isNotEmpty() == true) {
            callback?.onResult(
                AUIException(
                    -1,
                    "rtmAddMetaData fail, the result was not found in the filter"
                )
            )
            return
        }

        val error = metadataWillAddClosure?.invoke(publisherId, valueCmd, value)
        if (error != null) {
            callback?.onResult(error)
            return
        }

        val list = ArrayList(currentList)
        list.add(value)

        val data = GsonTools.beanToString(list)
        if (data == null) {
            callback?.onResult(AUIException(-1, "rtmUpdateMetaData fail"))
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
                        "rtmUpdateMetaData error >> $e"
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
        filter: List<Map<String, Any>>?,
        callback: AUICallback?
    ) {
        val itemIndexes = AUICollectionUtils.getItemIndexes(currentList, filter)
        if (itemIndexes == null) {
            callback?.onResult(
                AUIException(
                    -1,
                    "rtmUpdateMetaData fail, the result was not found in the filter"
                )
            )
            return
        }
        val list = ArrayList(currentList)
        itemIndexes.forEach { itemIdx ->
            val item = list[itemIdx]
            val error = metadataWillUpdateClosure?.invoke(publisherId, valueCmd, value, item)
            if (error != null) {
                callback?.onResult(error)
                return
            }

            val tempItem = HashMap(item)
            value.forEach { key, value ->
                tempItem[key] = value
            }
            list[itemIdx] = tempItem
        }

        val data = GsonTools.beanToString(list)
        if (data == null) {
            callback?.onResult(AUIException(-1, "rtmUpdateMetaData fail"))
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
                        "rtmUpdateMetaData error >> $e"
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
        filter: List<Map<String, Any>>?,
        callback: AUICallback?
    ) {
        val itemIndexes = AUICollectionUtils.getItemIndexes(currentList, filter)
        if (itemIndexes == null) {
            callback?.onResult(
                AUIException(
                    -1,
                    "rtmMergeMetaData fail, the result was not found in the filter"
                )
            )
            return
        }
        val list = ArrayList(currentList)
        itemIndexes.forEach { itemIdx ->
            val item = list[itemIdx]
            val error = metadataWillMergeClosure?.invoke(publisherId, valueCmd, value, item)
            if (error != null) {
                callback?.onResult(error)
                return
            }

            val tempItem = AUICollectionUtils.mergeMap(item, value)
            list[itemIdx] = tempItem
        }

        val data = GsonTools.beanToString(list)
        if (data == null) {
            callback?.onResult(AUIException(-1, "rtmMergeMetaData fail"))
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
                        "rtmMergeMetaData error >> $e"
                    )
                )
            } else {
                callback?.onResult(null)
            }
        }
    }

    private fun rtmRemoveMetaData(
        publisherId: String,
        valueCmd: String?,
        filter: List<Map<String, Any>>?,
        callback: AUICallback?
    ) {
        val itemIndexes = AUICollectionUtils.getItemIndexes(currentList, filter)
        if (itemIndexes == null) {
            callback?.onResult(
                AUIException(
                    -1,
                    "rtmRemoveMetaData fail, the result was not found in the filter"
                )
            )
            return
        }
        var list = ArrayList(currentList)
        itemIndexes.forEach { itemIdx ->
            val item = list[itemIdx]
            val error = metadataWillRemoveClosure?.invoke(publisherId, valueCmd, item)
            if (error != null) {
                callback?.onResult(error)
                return
            }
        }

        val filterList = list.filter { !itemIndexes.contains(list.indexOf(it)) }
        list = ArrayList(filterList)

        val data = GsonTools.beanToString(list)
        if (data == null) {
            callback?.onResult(AUIException(-1, "rtmRemoveMetaData fail"))
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
                        "rtmRemoveMetaData error >> $e"
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
        val filter = GsonTools.toBean<List<Map<String, Any>>>(
            GsonTools.beanToString(messageModel.payload.filter),
            object : TypeToken<List<Map<String, Any>>>() {}.type
        )
        var error: AUIException? = null
        when (updateType) {
            AUICollectionOperationTypeAdd, AUICollectionOperationTypeUpdate, AUICollectionOperationTypeMerge -> {
                val data = messageModel.payload.data
                if (data != null) {
                    if (updateType == AUICollectionOperationTypeAdd) {
                        rtmAddMetaData(publisherId, valueCmd, data, filter) {
                            sendReceipt(publisherId, uniqueId, it)
                        }
                    } else if (updateType == AUICollectionOperationTypeMerge) {
                        rtmMergeMetaData(publisherId, valueCmd, data, filter) {
                            sendReceipt(publisherId, uniqueId, it)
                        }
                    } else {
                        rtmUpdateMetaData(publisherId, valueCmd, data, filter) {
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
                rtmRemoveMetaData(publisherId, valueCmd, filter) {
                    sendReceipt(publisherId, uniqueId, it)
                }
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
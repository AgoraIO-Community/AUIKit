package io.agora.auikit.service.collection

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
            while (keys.hasNext()){
                val next = keys.next()
                map[next] = json.get(next)
            }
            currentValue = map
        }
    }

    private var metadataWillSetClosure: ((
        publisherId: String, valueCmd: String?, newValue: Map<String, Any>, oldValue: Map<String, Any>
    ) -> AUIException?)? = null

    private var metadataWillMergeClosure: ((
        publisherId: String, valueCmd: String?, newValue: Map<String, Any>, oldValue: Map<String, Any>
    ) -> AUIException?)? = null

    private var metadataWillRemoveClosure: ((
        publisherId: String, valueCmd: String?, oldValue: Map<String, Any>
    ) -> AUIException?)? = null


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
    fun subscribeWillSet(
        closure: (
            publisherId: String, valueCmd: String?, newValue: Map<String, Any>, oldValue: Map<String, Any>
        ) -> AUIException?
    ) {
        metadataWillSetClosure = closure
    }

    fun subscribeWillMerge(
        closure: (
            publisherId: String, valueCmd: String?, newValue: Map<String, Any>, oldValue: Map<String, Any>
        ) -> AUIException?
    ) {
        metadataWillMergeClosure = closure
    }

    fun subscribeWillRemove(
        closure: (
            publisherId: String, valueCmd: String?, oldValue: Map<String, Any>
        ) -> AUIException?
    ) {
        metadataWillRemoveClosure = closure
    }

    /**
     * 更新，替换根节点
     *
     * @param valueCmd 命令类型
     * @param value
     * @param objectId
     * @param callback
     */
    fun setMetaData(
        valueCmd: String?,
        value: Map<String, Any>,
        objectId: String,
        callback: AUICallback?
    ) {
        if (isArbiter()) {
            rtmSetMetaData(localUid(), valueCmd, value, objectId, callback)
            return
        }

        val uniqueId = UUID.randomUUID().toString()
        val data = AUICollectionMessage(
            channelName = channelName,
            uniqueId = uniqueId,
            objectId = objectId,
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
            uniqueId = objectId
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
        objectId: String,
        callback: AUICallback?
    ) {
        if (isArbiter()) {
            rtmMergeMetaData(localUid(), valueCmd, value, objectId, callback)
            return
        }

        val uniqueId = UUID.randomUUID().toString()
        val data = AUICollectionMessage(
            channelName = channelName,
            uniqueId = uniqueId,
            objectId = objectId,
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
            uniqueId = objectId
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
    fun addMetaData(valueCmd: String?, value: Map<String, Any>, callback: AUICallback?) {
        setMetaData(valueCmd, value, "", callback)
    }

    /**
     * 移除
     *
     * @param valueCmd
     * @param objectId
     * @param callback
     */
    fun removeMetaData(valueCmd: String?, objectId: String, callback: AUICallback?) {
        if (isArbiter()) {
            rtmRemoveMetaData(localUid(), valueCmd, objectId, callback)
            return
        }

        val uniqueId = UUID.randomUUID().toString()
        val data = AUICollectionMessage(
            channelName = channelName,
            uniqueId = uniqueId,
            objectId = objectId,
            payload = AUICollectionMessagePayload(
                type = AUICollectionOperationTypeRemove,
                dataCmd = valueCmd,
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
            uniqueId = objectId
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

    private fun rtmRemoveMetaData(
        publisherId: String,
        valueCmd: String?,
        objectId: String,
        callback: AUICallback?
    ) {
        val error = metadataWillRemoveClosure?.invoke(publisherId, valueCmd, HashMap(currentValue))
        if (error != null) {
            callback?.onResult(error)
            return
        }

        val map = HashMap(currentValue)
        map.clear()
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

    private fun rtmSetMetaData(
        publisherId: String,
        valueCmd: String?,
        value: Map<String, Any>,
        objectId: String,
        callback: AUICallback?
    ) {
        val error =
            metadataWillSetClosure?.invoke(publisherId, valueCmd, value, HashMap(currentValue))
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
        objectId: String,
        callback: AUICallback?
    ) {
        val error =
            metadataWillMergeClosure?.invoke(publisherId, valueCmd, value, HashMap(currentValue))
        if (error != null) {
            callback?.onResult(error)
            return
        }

        val map = replaceMap(currentValue, value)
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

    private fun replaceMap(origMap: Map<String, Any>, newMap: Map<String, Any>): Map<String, Any> {
        val resultMap = HashMap<String, Any>(origMap)
        newMap.forEach { (k, v) ->
            val dic = v as? Map<String, Any>
            if (dic != null) {
                val origDic = mutableMapOf<String, Any>()
                if(resultMap[k] is JSONObject){
                    val json = resultMap[k] as JSONObject
                    val keys = json.keys()
                    while (keys.hasNext()){
                        val key = keys.next()
                        origDic[key] = json.get(key)
                    }
                }
                val newDic = replaceMap(origDic, dic)
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
        val objectId = messageModel.objectId ?: ""
        when (updateType) {
            AUICollectionOperationTypeAdd, AUICollectionOperationTypeUpdate, AUICollectionOperationTypeMerge -> {
                val data = messageModel.payload.data
                if (data == null) {
                    sendReceipt(
                        publisherId,
                        uniqueId,
                        AUIException(-1, "payload is null or not a map")
                    )
                } else {
                    if (updateType == AUICollectionOperationTypeMerge) {
                        rtmMergeMetaData(publisherId, valueCmd, data, objectId) {
                            sendReceipt(publisherId, uniqueId, it)
                        }
                    } else {
                        rtmSetMetaData(publisherId, valueCmd, data, objectId) {
                            sendReceipt(publisherId, uniqueId, it)
                        }
                    }
                }
            }

            AUICollectionOperationTypeRemove -> {
                rtmRemoveMetaData(publisherId, valueCmd, objectId) {
                    sendReceipt(publisherId, uniqueId, it)
                }
            }

            AUICollectionOperationTypeIncrease -> {

            }

            AUICollectionOperationTypeDecrease -> {

            }
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
            objectId = "",
            payload = AUICollectionMessagePayload(
                dataCmd = "",
                data = data,
            )
        )
        val jsonStr = GsonTools.beanToString(message) ?: return

        rtmManager.publish(channelName, publisherId, jsonStr) {}
    }
}
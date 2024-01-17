package io.agora.auikit.service.collection

import io.agora.auikit.service.callback.AUICallback
import io.agora.auikit.service.callback.AUIException

interface IAUICollection {

    fun subscribeWillAdd(closure: ((publisherId: String, valueCmd: String?, value: Map<String, Any>) -> AUIException?)?)

    fun subscribeWillUpdate(closure: ((publisherId: String, valueCmd: String?, newValue: Map<String, Any>, oldValue: Map<String, Any>) -> AUIException?)?)

    fun subscribeWillMerge(closure: ((publisherId: String, valueCmd: String?, newValue: Map<String, Any>, oldValue: Map<String, Any>) -> AUIException?)?)

    fun subscribeWillRemove(closure: ((publisherId: String, valueCmd: String?, value: Map<String, Any>) -> AUIException?)?)

    fun subscribeAttributesDidChanged(closure: ((channelName: String, observeKey: String, value: Any) -> Unit)?)

    fun subscribeAttributesWillSet(closure: ((channelName: String, observeKey: String, valueCmd: String?, value: Any) -> Any)?)

    fun getMetaData(callback: ((error: AUIException?, value: Any?) -> Unit)?)

    fun updateMetaData(
        valueCmd: String?,
        value: Map<String, Any>,
        filter: List<Map<String, Any>>? = null,
        callback: AUICallback?
    )

    fun mergeMetaData(
        valueCmd: String?,
        value: Map<String, Any>,
        filter: List<Map<String, Any>>? = null,
        callback: AUICallback?
    )

    fun addMetaData(
        valueCmd: String?,
        value: Map<String, Any>,
        filter: List<Map<String, Any>>? = null,
        callback: AUICallback?
    )

    fun removeMetaData(
        valueCmd: String?,
        filter: List<Map<String, Any>>? = null,
        callback: AUICallback?
    )

    fun cleanMetaData(callback: AUICallback?)

    fun release()
}
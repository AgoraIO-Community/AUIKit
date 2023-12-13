package io.agora.auikit.service.rtm

import android.util.Log
import io.agora.rtm.LockEvent
import io.agora.rtm.MessageEvent
import io.agora.rtm.PresenceEvent
import io.agora.rtm.RtmConstants
import io.agora.rtm.RtmEventListener
import io.agora.rtm.StorageEvent
import io.agora.rtm.TopicEvent
import org.json.JSONObject

interface AUIRtmErrorRespObserver {

    /** token过期
     */
    fun onTokenPrivilegeWillExpire(channelName: String?)

    /** 网络状态变化
     */
    fun onConnectionStateChanged(channelName: String?, state: Int, reason: Int) {}

    /** 收到的KV为空
     */
    fun onMsgReceiveEmpty(channelName: String) {}
}

interface AUIRtmMsgRespObserver {
    fun onMsgDidChanged(channelName: String, key: String, value: Any)
    fun onMsgReceiveEmpty(channelName: String) {}
}

interface AUIRtmUserRespObserver {
    fun onUserSnapshotRecv(channelName: String, userId: String, userList: List<Map<String, Any>>)
    fun onUserDidJoined(channelName: String, userId: String, userInfo: Map<String, Any>)
    fun onUserDidLeaved(channelName: String, userId: String, userInfo: Map<String, Any>)
    fun onUserDidUpdated(channelName: String, userId: String, userInfo: Map<String, Any>)
}

class AUIRtmMsgProxy : RtmEventListener {

    var originEventListeners: RtmEventListener? = null
    private val msgRespObservers: MutableMap<String, ArrayList<AUIRtmMsgRespObserver>> = mutableMapOf()
    private val msgCacheAttr: MutableMap<String, MutableMap<String, String>> = mutableMapOf()
    private val userRespObservers: MutableList<AUIRtmUserRespObserver> = mutableListOf()
    private val errorRespObservers: MutableList<AUIRtmErrorRespObserver> = mutableListOf()
    var skipMetaEmpty = 0

    fun cleanCache(channelName: String) {
        msgCacheAttr.remove(channelName)
    }

    fun registerMsgRespObserver(channelName: String, itemKey: String, observer: AUIRtmMsgRespObserver) {
        val key = "${channelName}__${itemKey}"
        val observers = msgRespObservers[key] ?: ArrayList()
        observers.add(observer)
        msgRespObservers[key] = observers
    }

    fun unRegisterMsgRespObserver(channelName: String, itemKey: String, observer: AUIRtmMsgRespObserver) {
        val key = "${channelName}__${itemKey}"
        val observers = msgRespObservers[key] ?: return
        observers.remove(observer)
    }

    fun registerUserRespObserver(observer: AUIRtmUserRespObserver) {
        if (userRespObservers.contains(observer)) {
            return
        }
        userRespObservers.add(observer)
    }

    fun unRegisterUserRespObserver(observer: AUIRtmUserRespObserver) {
        userRespObservers.remove(observer)
    }

    fun registerErrorRespObserver(observer: AUIRtmErrorRespObserver) {
        if (errorRespObservers.contains(observer)) {
            return
        }
        errorRespObservers.add(observer)
    }

    fun unRegisterErrorRespObserver(observer: AUIRtmErrorRespObserver) {
        errorRespObservers.remove(observer)
    }

    override fun onStorageEvent(event: StorageEvent?) {
        Log.d("rtm_event", "onStorageEvent update: ${event?.target}")
        originEventListeners?.onStorageEvent(event)
        event ?: return
        if (event.data.metadataItems.isEmpty()) {
            if(skipMetaEmpty > 0){
                skipMetaEmpty --
                return
            }
            val handlerKey = "${event.target}__"
            msgRespObservers[handlerKey]?.forEach { handler ->
                handler.onMsgReceiveEmpty(event.target)
            }
            return
        }
        val cacheKey = event.target
        val cache = msgCacheAttr[cacheKey] ?: mutableMapOf()
        event.data.metadataItems.forEach { item ->
            if (cache[item.key] == item.value) {
                return@forEach
            }
            cache[item.key] = item.value
            val handlerKey = "${event.target}__${item.key}"
            Log.d("rtm_event", "onStorageEvent: key event:  ${item.key} \n value: ${item.value}")
            msgRespObservers[handlerKey]?.forEach { handler ->
                handler.onMsgDidChanged(event.target, item.key, item.value)
            }
        }
        msgCacheAttr[cacheKey] = cache
    }

    override fun onPresenceEvent(event: PresenceEvent?) {
        originEventListeners?.onPresenceEvent(event)
        Log.d("rtm_presence_event", "onPresenceEvent Type: ${event?.eventType} Publisher: ${event?.publisherId}")
        event ?: return
        val map = mutableMapOf<String, String>()
        event.stateItems.forEach {item ->
            map[item.key] = item.value
        }
        Log.d("rtm_presence_event", "onPresenceEvent Map: $map")
        when(event.eventType){
            RtmConstants.RtmPresenceEventType.REMOTE_JOIN ->
                userRespObservers.forEach { handler ->
                    handler.onUserDidJoined(event.channelName, event.publisherId ?: "", map)
                }
            RtmConstants.RtmPresenceEventType.REMOTE_LEAVE,
            RtmConstants.RtmPresenceEventType.REMOTE_TIMEOUT ->
                userRespObservers.forEach { handler ->
                    handler.onUserDidLeaved(event.channelName, event.publisherId ?: "", map)
                }
            RtmConstants.RtmPresenceEventType.REMOTE_STATE_CHANGED ->
                userRespObservers.forEach { handler ->
                    handler.onUserDidUpdated(event.channelName, event.publisherId ?: "", map)
                }
            RtmConstants.RtmPresenceEventType.SNAPSHOT -> {
                val userList = arrayListOf<Map<String, String>>()
                Log.d("rtm_presence_event", "event.snapshot.userStateList: ${event.snapshot.userStateList}")
                event.snapshot.userStateList.forEach { user ->
                    Log.d("rtm_presence_event", "----------SNAPSHOT User Start--------")
                    Log.d("rtm_presence_event", "user.states: ${user.states}")
                    Log.d("rtm_presence_event", "user.userId: ${user.userId}")
                    Log.d("rtm_presence_event", "----------SNAPSHOT User End--------")
                    if (user.states.isNotEmpty()) {
                        val userMap = mutableMapOf<String, String>()
                        userMap["userId"] = user.userId
                        user.states.forEach { item ->
                            userMap[item.key] = item.value
                        }
                        userList.add(userMap)
                    }
                }
                Log.d("rtm_presence_event", "onPresenceEvent SNAPSHOT: $userList")
                userRespObservers.forEach { handler ->
                    handler.onUserSnapshotRecv(event.channelName, event.publisherId ?: "", userList)
                }
            }
            else -> {
                // do nothing
            }
        }
    }


    override fun onMessageEvent(event: MessageEvent?) {
        event ?: return
        val str = event.message?.data?.let {
            if (it is ByteArray) {
                String(it)
            } else if (it is String) {
                it
            } else {
                ""
            }
        }
        val json = str?.let { JSONObject(it) }
        val messageType = json?.get("messageType").toString()
        originEventListeners?.onMessageEvent(event)
        val delegateKey = "${event.channelName}__$messageType"
        msgRespObservers[delegateKey]?.forEach { handler ->
            str?.let { handler.onMsgDidChanged(event.channelName, messageType, it) }
        }
    }


    override fun onTopicEvent(event: TopicEvent?) {
        originEventListeners?.onTopicEvent(event)
    }

    override fun onLockEvent(event: LockEvent?) {
        originEventListeners?.onLockEvent(event)
    }


    override fun onConnectionStateChanged(
        channelName: String?,
        state: RtmConstants.RtmConnectionState?,
        reason: RtmConstants.RtmConnectionChangeReason?
    ) {
        super.onConnectionStateChanged(channelName, state, reason)
        Log.d("rtm_event", "rtm -- connect state change: $state, reason: $reason")

        errorRespObservers.forEach {
            it.onConnectionStateChanged(channelName, RtmConstants.RtmConnectionState.getValue(state), RtmConstants.RtmConnectionChangeReason.getValue(reason))
        }
    }

    override fun onTokenPrivilegeWillExpire(channelName: String?) {
        originEventListeners?.onTokenPrivilegeWillExpire(channelName)
        if(channelName?.isNotEmpty() == true){
            errorRespObservers.forEach {
                it.onTokenPrivilegeWillExpire(channelName)
            }
        }
    }


}
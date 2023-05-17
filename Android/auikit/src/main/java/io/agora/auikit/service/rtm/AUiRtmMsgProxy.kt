package io.agora.auikit.service.rtm

import android.util.Log
import io.agora.rtm.LockEvent
import io.agora.rtm.MessageEvent
import io.agora.rtm.PresenceEvent
import io.agora.rtm.RtmConstants
import io.agora.rtm.RtmEventListener
import io.agora.rtm.StorageEvent
import io.agora.rtm.TopicEvent

interface AUiRtmErrorProxyDelegate {

    /** token过期
     */
    fun onTokenPrivilegeWillExpire(channelName: String?)

    /** 网络状态变化
     */
    fun onConnectionStateChanged(channelName: String, state: Int, reason: Int)

    /** 收到的KV为空
     */
    fun onMsgRecvEmpty(channelName: String)
}

interface AUiRtmMsgProxyDelegate {
    fun onMsgDidChanged(channelName: String, key: String, value: Any)
    fun onMsgRecvEmpty(channelName: String) {}
}

interface AUiRtmUserProxyDelegate {
    fun onUserSnapshotRecv(channelName: String, userId: String, userList: List<Map<String, Any>>)
    fun onUserDidJoined(channelName: String, userId: String, userInfo: Map<String, Any>)
    fun onUserDidLeaved(channelName: String, userId: String, userInfo: Map<String, Any>)
    fun onUserDidUpdated(channelName: String, userId: String, userInfo: Map<String, Any>)
}

class AUiRtmMsgProxy : RtmEventListener {

    var originEventListeners: RtmEventListener? = null
    private val msgDelegates: MutableMap<String, ArrayList<AUiRtmMsgProxyDelegate>> = mutableMapOf()
    private val userDelegates: MutableList<AUiRtmUserProxyDelegate> = mutableListOf()
    private val msgCacheAttr: MutableMap<String, MutableMap<String, String>> = mutableMapOf()

    fun cleanCache(channelName: String) {
        msgCacheAttr.remove(channelName)
    }

    fun subscribeMsg(channelName: String, itemKey: String, delegate: AUiRtmMsgProxyDelegate) {
        val key = "${channelName}__${itemKey}"
        val delegates = msgDelegates[key] ?: ArrayList()
        delegates.add(delegate)
        msgDelegates[key] = delegates
    }

    fun unsubscribeMsg(channelName: String, itemKey: String, delegate: AUiRtmMsgProxyDelegate) {
        val key = "${channelName}_${itemKey}"
        val delegates = msgDelegates[key] ?: return
        delegates.remove(delegate)
    }

    fun subscribeUser(delegate: AUiRtmUserProxyDelegate) {
        if (userDelegates.contains(delegate)) {
            return
        }
        userDelegates.add(delegate)
    }

    fun unsubscribeUser(delegate: AUiRtmUserProxyDelegate) {
        userDelegates.remove(delegate)
    }

    override fun onStorageEvent(event: StorageEvent?) {
        Log.d("rtm_event", "onStorageEvent update: ${event?.target}")
        originEventListeners?.onStorageEvent(event)
        event ?: return
        if (event.data.metadataItems.isEmpty()) {
            val delegateKey = "${event.target}__"
            msgDelegates[delegateKey]?.forEach { delegate ->
                delegate.onMsgRecvEmpty(event.target)
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
            val delegateKey = "${event.target}__${item.key}"
            Log.d("rtm_event", "onStorageEvent: key event:  ${item.key} \n value: ${item.value}")
            msgDelegates[delegateKey]?.forEach { delegate ->
                delegate.onMsgDidChanged(event.target, item.key, item.value)
            }
        }
        msgCacheAttr[cacheKey] = cache
    }

    override fun onPresenceEvent(event: PresenceEvent?) {
        originEventListeners?.onPresenceEvent(event)
        Log.d("rtm_presence_event", "onPresenceEvent Type: ${event?.type} Publisher: ${event?.publisher}")
        event ?: return
        val map = mutableMapOf<String, String>()
        event.stateItems.forEach {item ->
            map[item.key] = item.value
        }
        Log.d("rtm_presence_event", "onPresenceEvent Map: $map")
        when(event.type){
            RtmConstants.RtmPresenceEventType.REMOTE_JOIN ->
                userDelegates.forEach { delegate ->
                    delegate.onUserDidJoined(event.channelName, event.publisher ?: "", map)
                }
            RtmConstants.RtmPresenceEventType.REMOTE_LEAVE,
            RtmConstants.RtmPresenceEventType.REMOTE_CONNECTION_TIMEOUT ->
                userDelegates.forEach { delegate ->
                    delegate.onUserDidLeaved(event.channelName, event.publisher ?: "", map)
                }
            RtmConstants.RtmPresenceEventType.REMOTE_STATE_CHANGED ->
                userDelegates.forEach { delegate ->
                    delegate.onUserDidUpdated(event.channelName, event.publisher ?: "", map)
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
                userDelegates.forEach { delegate ->
                    delegate.onUserSnapshotRecv(event.channelName, event.publisher ?: "", userList)
                }
            }
            else -> {
                // do nothing
            }
        }
    }


    override fun onMessageEvent(event: MessageEvent?) {
        originEventListeners?.onMessageEvent(event)
    }


    override fun onTopicEvent(event: TopicEvent?) {
        originEventListeners?.onTopicEvent(event)
    }

    override fun onLockEvent(event: LockEvent?) {
        originEventListeners?.onLockEvent(event)
    }


    override fun onConnectionStateChange(channelName: String?, state: Int, reason: Int) {
        Log.d("rtm_event", "rtm -- connect state change: $state, reason: $reason")
    }

    override fun onTokenPrivilegeWillExpire(channelName: String?) {
        originEventListeners?.onTokenPrivilegeWillExpire(channelName)
    }


}
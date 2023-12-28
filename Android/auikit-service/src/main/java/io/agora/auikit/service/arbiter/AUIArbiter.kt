package io.agora.auikit.service.arbiter

import io.agora.auikit.service.rtm.AUIRtmLockRespObserver
import io.agora.auikit.service.rtm.AUIRtmManager

class AUIArbiter(
    private val channelName: String,
    private val rtmManager: AUIRtmManager,
    private val currentUserId: String
) {

    private var lockOwnerId = ""
    private val rtmLockRespObserver = object : AUIRtmLockRespObserver {
        override fun onReceiveLock(channelName: String, lockName: String, lockOwner: String) {
            lockOwnerId = lockOwner
        }

        override fun onReleaseLock(channelName: String, lockName: String, lockOwner: String) {
            if (channelName == this@AUIArbiter.channelName) {
                acquire()
            }
        }
    }

    init {
        rtmManager.subscribeLock(channelName, observer = rtmLockRespObserver)
    }

    fun deInit(){
        rtmManager.unsubscribeLock(rtmLockRespObserver)
    }

    fun create(){
        rtmManager.setLock(channelName){}
    }

    fun destroy(){
        rtmManager.removeLock(channelName){}
    }

    fun acquire() {
        rtmManager.acquireLock(channelName){}
    }

    fun release() {
        rtmManager.releaseLock(channelName){}
    }

    fun isArbiter() = lockOwnerId == currentUserId

    fun lockOwnerId() = lockOwnerId
}
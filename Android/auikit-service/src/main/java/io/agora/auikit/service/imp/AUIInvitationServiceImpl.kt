package io.agora.auikit.service.imp

import android.os.Handler
import android.os.Looper
import io.agora.auikit.model.AUIInvitationInfo
import io.agora.auikit.model.AUIUserInfo
import io.agora.auikit.service.IAUIInvitationService
import io.agora.auikit.service.callback.AUICallback
import io.agora.auikit.service.callback.AUIException
import io.agora.auikit.service.collection.AUIAttributesModel
import io.agora.auikit.service.collection.AUIListCollection
import io.agora.auikit.service.rtm.AUIRtmManager
import io.agora.auikit.utils.GsonTools
import io.agora.auikit.utils.ObservableHelper

const val kInvitationKey = "invitation"

enum class AUIInvitationCmd {
    sendApply,
    cancelApply,
    acceptApply,
    rejectApply,
}

class AUIInvitationServiceImpl(
    private val channelName: String,
    private val rtmManager: AUIRtmManager
) : IAUIInvitationService {

    private val invitationCollection = AUIListCollection(channelName, kInvitationKey, rtmManager)

    private var invitationList = mutableListOf<AUIInvitationInfo>()

    init {
        invitationCollection.subscribeAttributesDidChanged(this::onAttributeChanged)
    }

    override fun deInitService(completion: AUICallback?) {
        super.deInitService(completion)
        timerHandler.removeCallbacksAndMessages(null)
        timeRunnableList.clear()
        invitationCollection.cleanMetaData {
            completion?.onResult(
                if (it == null) null else AUIException(
                    AUIException.ERROR_CODE_RTM_COLLECTION,
                    " $it"
                )
            )
        }
        invitationCollection.release()
    }

    private val observableHelper =
        ObservableHelper<IAUIInvitationService.AUIInvitationRespObserver>()

    override fun registerRespObserver(observer: IAUIInvitationService.AUIInvitationRespObserver?) {
        observableHelper.subscribeEvent(observer)
    }

    override fun unRegisterRespObserver(observer: IAUIInvitationService.AUIInvitationRespObserver?) {
        observableHelper.unSubscribeEvent(observer)
    }

    override fun sendInvitation(userId: String, seatIndex: Int, callback: AUICallback?) {
        val info = AUIInvitationInfo()
        info.seatNo = seatIndex
        info.userId = userId
        info.type = AUIInvitationInfo.AUIInvitationType.Invite

        invitationCollection.addMetaData(
            AUIInvitationCmd.sendApply.name,
            GsonTools.beanToMap(info),
            listOf(mapOf("userId" to roomContext.currentUserInfo.userId, "type" to info.type))
        ) {
            callback?.onResult(
                if (it == null) null else AUIException(
                    AUIException.ERROR_CODE_RTM_COLLECTION,
                    "$it"
                )
            )
        }
    }

    override fun acceptInvitation(userId: String, seatIndex: Int, callback: AUICallback?) {
        invitationCollection.mergeMetaData(
            AUIInvitationCmd.acceptApply.name,
            mapOf("status" to AUIInvitationInfo.AUIInvitationStatus.Accept),
            listOf(mapOf("userId" to userId, "type" to AUIInvitationInfo.AUIInvitationType.Invite))
        ) {
            callback?.onResult(
                if (it == null) null else AUIException(
                    AUIException.ERROR_CODE_RTM_COLLECTION,
                    "$it"
                )
            )
        }
    }

    override fun rejectInvitation(userId: String, callback: AUICallback?) {
        invitationCollection.mergeMetaData(
            AUIInvitationCmd.acceptApply.name,
            mapOf("status" to AUIInvitationInfo.AUIInvitationStatus.Reject),
            listOf(mapOf("userId" to userId, "type" to AUIInvitationInfo.AUIInvitationType.Invite))
        ) {
            callback?.onResult(
                if (it == null) null else AUIException(
                    AUIException.ERROR_CODE_RTM_COLLECTION,
                    "$it"
                )
            )
        }
    }

    override fun cancelInvitation(userId: String, callback: AUICallback?) {
        invitationCollection.removeMetaData(
            AUIInvitationCmd.cancelApply.name,
            listOf(mapOf("userId" to userId))
        ) {
            callback?.onResult(
                if (it == null) null else AUIException(
                    AUIException.ERROR_CODE_RTM_COLLECTION,
                    "$it"
                )
            )
        }
    }

    override fun sendApply(seatIndex: Int, callback: AUICallback) {
        val info = AUIInvitationInfo()
        info.seatNo = seatIndex
        info.userId = roomContext.currentUserInfo.userId
        info.type = AUIInvitationInfo.AUIInvitationType.Apply

        invitationCollection.addMetaData(
            AUIInvitationCmd.sendApply.name,
            GsonTools.beanToMap(info),
            listOf(mapOf("userId" to info.userId, "type" to info.type))
        ) {
            callback.onResult(
                if (it == null) null else AUIException(
                    AUIException.ERROR_CODE_RTM_COLLECTION,
                    "$it"
                )
            )
        }
    }

    override fun cancelApply(callback: AUICallback) {
        invitationCollection.removeMetaData(
            AUIInvitationCmd.cancelApply.name,
            listOf(
                mapOf(
                    "userId" to roomContext.currentUserInfo.userId,
                    "type" to AUIInvitationInfo.AUIInvitationType.Apply
                )
            )
        ) {
            callback.onResult(
                if (it == null) null else AUIException(
                    AUIException.ERROR_CODE_RTM_COLLECTION,
                    "$it"
                )
            )
        }
    }

    override fun acceptApply(userId: String, seatIndex: Int, callback: AUICallback) {
        invitationCollection.mergeMetaData(
            AUIInvitationCmd.acceptApply.name,
            mapOf("status" to AUIInvitationInfo.AUIInvitationStatus.Accept),
            listOf(mapOf("userId" to userId, "type" to AUIInvitationInfo.AUIInvitationType.Apply))
        ) {
            callback.onResult(
                if (it == null) null else AUIException(
                    AUIException.ERROR_CODE_RTM_COLLECTION,
                    "$it"
                )
            )
        }
    }

    override fun rejectApply(userId: String, callback: AUICallback) {
        invitationCollection.mergeMetaData(
            AUIInvitationCmd.rejectApply.name,
            mapOf("status" to AUIInvitationInfo.AUIInvitationStatus.Reject),
            listOf(mapOf("userId" to userId, "type" to AUIInvitationInfo.AUIInvitationType.Apply))
        ) {
            callback.onResult(
                if (it == null) null else AUIException(
                    AUIException.ERROR_CODE_RTM_COLLECTION,
                    "$it"
                )
            )
        }
    }


    override fun getChannelName() = channelName


    private fun onAttributeChanged(channelName: String, key: String, value: AUIAttributesModel) {
        val list =
            GsonTools.toList(GsonTools.beanToString(value.getList()), AUIInvitationInfo::class.java)
                ?: return

        // 申请列表
        val newApplyList = list.filter { it.type == AUIInvitationInfo.AUIInvitationType.Apply }
        val oldApplyList =
            invitationList.filter { it.type == AUIInvitationInfo.AUIInvitationType.Apply }

        // 邀请列表
        val newInviteList = list.filter { it.type == AUIInvitationInfo.AUIInvitationType.Invite }
        val oldInviteList =
            invitationList.filter { it.type == AUIInvitationInfo.AUIInvitationType.Invite }

        invitationList = ArrayList(list)

        // 处理申请差异列表
        var applyListChanged = false
        newApplyList.forEach { newApply ->
            val oldApply = oldApplyList.find { it.userId == newApply.userId }
            if (oldApply?.status != newApply.status) {
                applyListChanged = true
                when (newApply.status) {
                    AUIInvitationInfo.AUIInvitationStatus.Accept -> {
                        observableHelper.notifyEventHandlers {
                            it.onApplyAccepted(newApply.userId, newApply.seatNo)
                        }
                        removeInvitation(newApply.userId, newApply.type)
                    }

                    AUIInvitationInfo.AUIInvitationStatus.Reject -> {
                        observableHelper.notifyEventHandlers {
                            it.onApplyRejected(newApply.userId)
                        }
                        removeInvitation(newApply.userId, newApply.type)
                    }

                    AUIInvitationInfo.AUIInvitationStatus.Timeout -> {
                        removeInvitation(newApply.userId, newApply.type)
                    }

                    AUIInvitationInfo.AUIInvitationStatus.Waiting -> {
                        observableHelper.notifyEventHandlers {
                            it.onReceiveNewApply(newApply.userId, newApply.seatNo)
                        }
                        startInvitationTimer(userId = newApply.userId, type = newApply.type)
                    }
                }
            }
        }
        oldApplyList.forEach { oldApply ->
            val newApply = newApplyList.find { it.userId == oldApply.userId }
            if (newApply == null ) {
                if(oldApply.status == AUIInvitationInfo.AUIInvitationStatus.Waiting){
                    observableHelper.notifyEventHandlers {
                        it.onApplyCanceled(oldApply.userId)
                    }
                }
                applyListChanged = true
                return@forEach
            }
        }
        if (applyListChanged) {
            observableHelper.notifyEventHandlers {
                it.onApplyListUpdate(newApplyList.map {
                    AUIUserInfo().apply {
                        userId = it.userId
                        micIndex = it.seatNo
                    }
                })
            }
        }

        // 处理邀请差异列表
        newInviteList.forEach { newInvite ->
            val oldInvite = oldApplyList.find { it.userId == newInvite.userId }
            if (oldInvite?.status != newInvite.status) {
                when (newInvite.status) {
                    AUIInvitationInfo.AUIInvitationStatus.Accept -> {
                        observableHelper.notifyEventHandlers {
                            it.onInviteeAccepted(newInvite.userId, newInvite.seatNo)
                        }
                        removeInvitation(newInvite.userId, newInvite.type)
                    }

                    AUIInvitationInfo.AUIInvitationStatus.Reject -> {
                        observableHelper.notifyEventHandlers {
                            it.onInviteeRejected(newInvite.userId)
                        }
                        removeInvitation(newInvite.userId, newInvite.type)
                    }

                    AUIInvitationInfo.AUIInvitationStatus.Timeout -> {
                        removeInvitation(newInvite.userId, newInvite.type)
                    }

                    AUIInvitationInfo.AUIInvitationStatus.Waiting -> {
                        observableHelper.notifyEventHandlers {
                            it.onReceiveInvitation(newInvite.userId, newInvite.seatNo)
                        }
                        startInvitationTimer(userId = newInvite.userId, type = newInvite.type)
                    }
                }
            }
        }
        oldInviteList.forEach { oldInvite ->
            val newInvite = newApplyList.find { it.userId == oldInvite.userId }
            if (newInvite == null && oldInvite.status == AUIInvitationInfo.AUIInvitationStatus.Waiting) {
                observableHelper.notifyEventHandlers {
                    it.onInvitationCancelled(oldInvite.userId)
                }
                return@forEach
            }
        }
    }

    private val timerHandler = Handler(Looper.getMainLooper())
    private val timeRunnableList = mutableListOf<InvitationTimerRun>()

    private fun removeInvitation(userId: String, type: Int) {
        if (roomContext.getArbiter(channelName)?.isArbiter() == true) {
            invitationCollection.removeMetaData(
                AUIInvitationCmd.cancelApply.name,
                listOf(mapOf("userId" to userId, "type" to type))
            ) {}
            timeRunnableList.filter { it.userId == userId && it.type == type }.forEach {
                timerHandler.removeCallbacks(it)
            }
        }
    }

    private fun startInvitationTimer(timeout: Long = 10000, userId: String, type: Int) {
        if (roomContext.getArbiter(channelName)?.isArbiter() == true) {
            timerHandler.postDelayed(InvitationTimerRun(userId, type), timeout)
        }
    }

    inner class InvitationTimerRun(
        val userId: String,
        val type: Int,
    ) : Runnable {
        override fun run() {
            invitationCollection.mergeMetaData(
                AUIInvitationCmd.cancelApply.name,
                mapOf("status" to AUIInvitationInfo.AUIInvitationStatus.Timeout),
                listOf(mapOf("userId" to userId, "type" to type))
            ) {}
        }
    }

}
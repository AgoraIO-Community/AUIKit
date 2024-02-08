package io.agora.auikit.service.imp

import io.agora.auikit.model.AUIInvitationInfo
import io.agora.auikit.model.AUIInvitationInfo.AUIInvitationStatus
import io.agora.auikit.model.AUIUserInfo
import io.agora.auikit.service.IAUIInvitationService
import io.agora.auikit.service.callback.AUICallback
import io.agora.auikit.service.callback.AUIException
import io.agora.auikit.service.collection.AUIAttributesModel
import io.agora.auikit.service.collection.AUICollectionException
import io.agora.auikit.service.collection.AUIListCollection
import io.agora.auikit.service.rtm.AUIRtmManager
import io.agora.auikit.service.rtm.AUIThrottler
import io.agora.auikit.utils.GsonTools
import io.agora.auikit.utils.ObservableHelper
import java.util.Timer
import kotlin.concurrent.scheduleAtFixedRate

const val kInvitationKey = "invitation"

enum class AUIInvitationCmd {
    sendApply,
    cancelApply,
    acceptApply,
    rejectApply,

    sendInvit,
    cancelInvit,
    acceptInvit,
    rejectInvit,
}

class AUIInvitationServiceImpl(
    private val channelName: String,
    private val rtmManager: AUIRtmManager
) : IAUIInvitationService {

    private val invitationCollection = AUIListCollection(channelName, kInvitationKey, rtmManager)

    private val invitationMap = mutableMapOf<String, AUIInvitationInfo>()
    private val checkThrottler by lazy { AUIThrottler() }
    private val observerList = mutableListOf<AUIInvitationInfo>()
    private var timer: Timer? = null
        set(value) {
            value?.cancel()
            field = value
        }

    init {
        invitationCollection.subscribeValueWillChange(this::valueWillChange)
        invitationCollection.subscribeWillAdd(this::metadataWillAdd)
        invitationCollection.subscribeWillMerge(this::metadataWillMerge)
        invitationCollection.subscribeAttributesWillSet(this::metadataWillSet)
        invitationCollection.subscribeAttributesDidChanged(this::onAttributeChanged)
    }


    override fun cleanUserInfo(userId: String, completion: AUICallback?) {
        super.cleanUserInfo(userId, completion)
        if(roomContext.getArbiter(channelName)?.isArbiter() != true){
            return
        }
        invitationCollection.removeMetaData(
            "",
            listOf(
                mapOf("userId" to userId)
            )
        ){
            completion?.onResult(
                if (it == null) null else AUIException(
                    AUIException.ERROR_CODE_RTM_COLLECTION,
                    " $it"
                )
            )
        }
    }

    override fun deInitService(completion: AUICallback?) {
        super.deInitService(completion)
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
            AUIInvitationCmd.sendInvit.name,
            GsonTools.beanToMap(info),
            listOf(
                mapOf("userId" to roomContext.currentUserInfo.userId, "status" to AUIInvitationInfo.AUIInvitationStatus.Waiting),
                mapOf("userId" to roomContext.currentUserInfo.userId, "status" to AUIInvitationInfo.AUIInvitationStatus.Accept),
            )
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
            AUIInvitationCmd.acceptInvit.name,
            mapOf(
                "status" to AUIInvitationInfo.AUIInvitationStatus.Accept,
                "editTime" to roomContext.ntpTime
            ),
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
            AUIInvitationCmd.rejectInvit.name,
            mapOf(
                "status" to AUIInvitationInfo.AUIInvitationStatus.Reject,
                "editTime" to roomContext.ntpTime
            ),
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
        invitationCollection.mergeMetaData(
            AUIInvitationCmd.cancelInvit.name,
            mapOf(
                "status" to AUIInvitationInfo.AUIInvitationStatus.Cancel,
                "editTime" to roomContext.ntpTime
            ),
            listOf(
                mapOf("userId" to userId, "type" to AUIInvitationInfo.AUIInvitationType.Invite)
            )
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
            listOf(
                mapOf("userId" to info.userId, "status" to AUIInvitationStatus.Waiting),
                mapOf("userId" to info.userId, "status" to AUIInvitationStatus.Accept)
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

    override fun cancelApply(callback: AUICallback) {
        cancelApplyByUserId(roomContext.currentUserInfo.userId, callback)
    }

    private fun cancelApplyByUserId(userId: String, callback: AUICallback) {
        invitationCollection.mergeMetaData(
            AUIInvitationCmd.cancelApply.name,
            mapOf(
                "status" to AUIInvitationStatus.Cancel,
                "editTime" to roomContext.ntpTime
            ),
            listOf(
                mapOf(
                    "userId" to userId,
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
            mapOf(
                "status" to AUIInvitationStatus.Accept,
                "editTime" to roomContext.ntpTime
            ),
            listOf(
                mapOf("userId" to userId, "type" to AUIInvitationInfo.AUIInvitationType.Apply)
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

    override fun rejectApply(userId: String, callback: AUICallback) {
        invitationCollection.mergeMetaData(
            AUIInvitationCmd.rejectApply.name,
            mapOf(
                "status" to AUIInvitationInfo.AUIInvitationStatus.Reject,
                "editTime" to roomContext.ntpTime
            ),
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

    private fun valueWillChange(
        publisherId: String,
        valueCmd: String?,
        value: Map<String, Any>
    ): Map<String, Any>? {
        val tempItem = mutableMapOf<String, Any>()
        tempItem.putAll(value)
        val currentTime = roomContext.ntpTime
        tempItem["editTime"] = currentTime
        when (valueCmd) {
            AUIInvitationCmd.sendInvit.name,
            AUIInvitationCmd.sendApply.name -> tempItem["createTime"] = currentTime
        }
        return tempItem
    }

    private fun metadataWillAdd(
        publisherId: String,
        valueCmd: String?,
        value: Map<String, Any>,
        attrs: AUIAttributesModel
    ): AUICollectionException? {
        return when (valueCmd) {
            AUIInvitationCmd.sendApply.name, AUIInvitationCmd.sendInvit.name -> {
                attrs.setList(attrs.getList()?.filter { it["userId"] != value["userId"] })
                null
            }
            else -> {
                AUICollectionException.ErrorCode.unknown.toException("UnKnown cmd : $valueCmd")
            }
        }
    }

    private fun metadataWillMerge(
        publisherId: String,
        valueCmd: String?,
        newValue: Map<String, Any>,
        oldValue: Map<String, Any>
    ): AUICollectionException? {
        return when (valueCmd) {
            AUIInvitationCmd.acceptInvit.name -> {
                val userId = oldValue["userId"] as? String ?: ""
                val seatIndex = (oldValue["seatNo"] as? Long)?.toInt() ?: 0
                var error: AUIException? = null
                observableHelper.notifyEventHandlers {
                    error = it.onInviteWillAccept(userId, seatIndex)
                }
                if (error != null) AUICollectionException.ErrorCode.unknown.toException() else null
            }

            AUIInvitationCmd.acceptApply.name -> {
                val userId = oldValue["userId"] as? String ?: ""
                val seatIndex = (oldValue["seatNo"] as? Long)?.toInt() ?: 0
                var error: AUIException? = null
                observableHelper.notifyEventHandlers {
                    error = it.onApplyWillAccept(userId, seatIndex)
                }
                if (error != null) AUICollectionException.ErrorCode.unknown.toException() else null
            }

            AUIInvitationCmd.cancelInvit.name,
            AUIInvitationCmd.cancelApply.name,
            AUIInvitationCmd.rejectInvit.name,
            AUIInvitationCmd.rejectApply.name -> null
            else -> AUICollectionException.ErrorCode.unknown.toException()
        }
    }

    private fun metadataWillSet(
        channelName: String,
        observeKey: String,
        valueCmd: String?,
        value: AUIAttributesModel
    ) {
        val list = value.getList() ?: return
        val currentTime = roomContext.ntpTime
        val filterList = list.filter { attr ->
            val editTime = attr["editTime"] as? Long ?: 0
            val invalidTs = attr["invalidTs"] as? Long ?: 0
            val status = attr["status"] as? Int ?: 0
            if (status == AUIInvitationInfo.AUIInvitationStatus.Waiting) {
                return@filter true
            }
            if (currentTime - editTime < invalidTs) {
                return@filter true
            }
            return@filter false
        }
        value.setList(filterList)
    }

    private fun onAttributeChanged(channelName: String, key: String, value: AUIAttributesModel) {
        val list =
            GsonTools.toList(GsonTools.beanToString(value.getList()), AUIInvitationInfo::class.java)
                ?: return

        // 申请列表
        val newApplyList = list.filter { it.type == AUIInvitationInfo.AUIInvitationType.Apply }
        val oldApplyList =
            invitationMap.values.filter { it.type == AUIInvitationInfo.AUIInvitationType.Apply }

        // 邀请列表
        val newInviteList = list.filter { it.type == AUIInvitationInfo.AUIInvitationType.Invite }
        val oldInviteList =
            invitationMap.values.filter { it.type == AUIInvitationInfo.AUIInvitationType.Invite }

        invitationMap.clear()
        list.forEach {
            invitationMap[it.userId] = it
        }

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
                    }

                    AUIInvitationInfo.AUIInvitationStatus.Reject -> {
                        observableHelper.notifyEventHandlers {
                            it.onApplyRejected(newApply.userId)
                        }
                    }

                    AUIInvitationInfo.AUIInvitationStatus.Timeout -> {
                    }

                    AUIInvitationInfo.AUIInvitationStatus.Waiting -> {
                        observableHelper.notifyEventHandlers {
                            it.onReceiveNewApply(newApply.userId, newApply.seatNo)
                        }
                    }
                }
            }
        }
        oldApplyList.forEach { oldApply ->
            val newApply = newApplyList.find { it.userId == oldApply.userId }
            if (newApply == null) {
                if (oldApply.status == AUIInvitationInfo.AUIInvitationStatus.Waiting) {
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
                it.onApplyListUpdate(newApplyList.filter { it.status == AUIInvitationStatus.Waiting }.map {
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
                    }

                    AUIInvitationInfo.AUIInvitationStatus.Reject -> {
                        observableHelper.notifyEventHandlers {
                            it.onInviteeRejected(newInvite.userId)
                        }
                    }

                    AUIInvitationInfo.AUIInvitationStatus.Timeout -> {

                    }

                    AUIInvitationInfo.AUIInvitationStatus.Waiting -> {
                        observableHelper.notifyEventHandlers {
                            it.onReceiveInvitation(newInvite.userId, newInvite.seatNo)
                        }
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

        checkThrottler.triggerLastEvent(delay = 300) {
            checkWaitingTimeout()
        }
    }

    private fun checkWaitingTimeout() {
        if (roomContext.getArbiter(channelName)?.isArbiter() != true) {
            return
        }
        val observerList =
            invitationMap.values.filter { it.status == AUIInvitationInfo.AUIInvitationStatus.Waiting }
        if (observerList.isEmpty()) {
            timer = null
            return
        }
        this.observerList.clear()
        this.observerList.addAll(observerList)
        timer = Timer().apply {
            scheduleAtFixedRate(0, 1000) {
                val currentTs = roomContext.ntpTime
                val observerList = ArrayList(this@AUIInvitationServiceImpl.observerList)
                this@AUIInvitationServiceImpl.observerList.clear()
                observerList.forEach { info ->
                    if (currentTs - info.createTime > info.timeoutTs) {
                        when (info.type) {
                            AUIInvitationInfo.AUIInvitationType.Apply -> cancelApplyByUserId(info.userId) {}
                            AUIInvitationInfo.AUIInvitationType.Invite -> cancelInvitation(info.userId) {}
                        }
                    } else {
                        this@AUIInvitationServiceImpl.observerList.add(info)
                    }
                }
                if (this@AUIInvitationServiceImpl.observerList.isEmpty()) {
                    timer = null
                }
            }
        }
    }
}
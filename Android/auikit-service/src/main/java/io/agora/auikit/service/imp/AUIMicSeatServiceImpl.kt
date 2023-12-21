package io.agora.auikit.service.imp

import android.util.Log
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import io.agora.auikit.model.AUIMicSeatInfo
import io.agora.auikit.model.AUIMicSeatStatus
import io.agora.auikit.model.AUIRoomContext
import io.agora.auikit.model.AUIUserThumbnailInfo
import io.agora.auikit.service.IAUIMicSeatService
import io.agora.auikit.service.callback.AUICallback
import io.agora.auikit.service.callback.AUIException
import io.agora.auikit.service.rtm.AUIRtmAttributeRespObserver
import io.agora.auikit.service.rtm.AUIRtmException
import io.agora.auikit.service.rtm.AUIRtmManager
import io.agora.auikit.service.rtm.AUIRtmMessageRespObserver
import io.agora.auikit.service.rtm.AUIRtmMicSeatInfo
import io.agora.auikit.service.rtm.AUIRtmPublishModel
import io.agora.auikit.service.rtm.AUIRtmReceiptModel
import io.agora.auikit.service.rtm.kAUISeatEnterInterface
import io.agora.auikit.service.rtm.kAUISeatKickInterface
import io.agora.auikit.service.rtm.kAUISeatLeaveInterface
import io.agora.auikit.service.rtm.kAUISeatLockInterface
import io.agora.auikit.service.rtm.kAUISeatMuteAudioInterface
import io.agora.auikit.service.rtm.kAUISeatUnlockInterface
import io.agora.auikit.service.rtm.kAUISeatUnmuteAudioInterface
import io.agora.auikit.utils.GsonTools
import io.agora.auikit.utils.ObservableHelper

private const val kSeatAttrKey = "micSeat"

class AUIMicSeatServiceImpl(
    private val channelName: String,
    private val rtmManager: AUIRtmManager
) : IAUIMicSeatService, AUIRtmAttributeRespObserver, AUIRtmMessageRespObserver {

    private val observableHelper =
        ObservableHelper<IAUIMicSeatService.AUIMicSeatRespObserver>()

    private var micSeats = mutableMapOf<Int, AUIMicSeatInfo>()

    init {
        rtmManager.subscribeAttribute(channelName, kSeatAttrKey, this)
        rtmManager.subscribeMessage(this)
    }

    override fun initService(completion: AUICallback?) {
        val roomInfo = roomContext.getRoomInfo(channelName) ?: return
        val seatMap = mutableMapOf<String, Any>()
        for (i in 0 until roomInfo.micSeatCount) {
            val seat = AUIMicSeatInfo()
            seat.seatIndex = i
            if (i == 0) {
                seat.user = roomContext.currentUserInfo
                seat.seatStatus = AUIMicSeatStatus.used
            }
            seatMap.put(i.toString(), seat)
        }

        val metadata = mapOf(Pair(kSeatAttrKey, GsonTools.beanToString(seatMap) ?: ""))
        rtmManager.setBatchMetadata(
            channelName,
            metadata = metadata
        ) { error ->
            if (error == null) {
                completion?.onResult(null)
            } else {
                completion?.onResult(AUIException(AUIException.ERROR_CODE_RTM, "error: $error"))
            }
        }
    }

    override fun deInitService(completion: AUICallback?) {
        if (roomContext.getArbiter(channelName)?.isArbiter() != true) {
            return
        }

        rtmManager.cleanBatchMetadata(
            channelName,
            remoteKeys = listOf(kSeatAttrKey)
        ) { error ->
            if (error == null) {
                completion?.onResult(null)
            } else {
                completion?.onResult(AUIException(AUIException.ERROR_CODE_RTM, "error: $error"))
            }
        }
    }

    override fun registerRespObserver(observer: IAUIMicSeatService.AUIMicSeatRespObserver?) {
        observableHelper.subscribeEvent(observer)
    }

    override fun unRegisterRespObserver(observer: IAUIMicSeatService.AUIMicSeatRespObserver?) {
        observableHelper.unSubscribeEvent(observer)
    }

    override fun enterSeat(seatIndex: Int, callback: AUICallback?) {
        if (AUIRoomContext.shared().getArbiter(channelName)?.isArbiter() == true) {
            rtmEnterSeat(seatIndex, roomContext.currentUserInfo, callback)
        } else {
            val seatInfo = AUIRtmMicSeatInfo(
                channelName,
                roomContext.currentUserInfo.userId,
                roomContext.currentUserInfo.userName,
                roomContext.currentUserInfo.userAvatar,
                seatIndex
            )
            rtmManager.publishAndWaitReceipt(
                channelName,
                AUIRtmPublishModel(interfaceName = kAUISeatEnterInterface, data = seatInfo)
            ) { error ->
                if (error == null) {
                    callback?.onResult(null)
                } else {
                    callback?.onResult(AUIException(AUIException.ERROR_CODE_RTM, "error: $error"))
                }
            }
        }
    }

    override fun leaveSeat(callback: AUICallback?) {
        val userId = roomContext.currentUserInfo.userId
        val micSeat = micSeats.values.find { it.user?.userId == userId }

        if (micSeat == null) {
            callback?.onResult(
                AUIException(
                    AUIException.ERROR_CODE_SEAT_NOT_ENTER,
                    "user not on seat"
                )
            )
            return
        }

        if (AUIRoomContext.shared().getArbiter(channelName)?.isArbiter() == true) {
            rtmLeaveSeat(userId, callback)
        } else {
            val seatInfo = AUIRtmMicSeatInfo(
                channelName,
                userId,
                roomContext.currentUserInfo.userName,
                roomContext.currentUserInfo.userAvatar,
                micSeat.seatIndex
            )
            rtmManager.publishAndWaitReceipt(
                channelName,
                AUIRtmPublishModel(interfaceName = kAUISeatLeaveInterface, data = seatInfo)
            ) { error ->
                if (error == null) {
                    callback?.onResult(null)
                } else {
                    callback?.onResult(AUIException(AUIException.ERROR_CODE_RTM, "error: $error"))
                }
            }
        }
    }

    override fun autoEnterSeat(callback: AUICallback?) {
        var toIndex: Int? = null
        for ((key, value) in micSeats) {
            if (value.seatStatus == AUIMicSeatStatus.idle) {
                toIndex = key
                break
            }
        }
        if (toIndex != null) {
            enterSeat(toIndex, callback)
        } else {
            callback?.onResult(
                AUIException(
                    -1,
                    "can not find empty mic seat"
                )
            )
        }
    }

    override fun kickSeat(seatIndex: Int, callback: AUICallback?) {
        val micSeat = micSeats[seatIndex]
        if (AUIRoomContext.shared().getArbiter(channelName)?.isArbiter() == true) {
            rtmKickSeat(seatIndex, callback)
        } else {
            rtmManager.publishAndWaitReceipt(
                channelName,
                AUIRtmPublishModel(interfaceName = kAUISeatKickInterface, data = micSeat)
            ) { error ->
                if (error == null) {
                    callback?.onResult(null)
                } else {
                    callback?.onResult(AUIException(AUIException.ERROR_CODE_RTM, "error: $error"))
                }
            }
        }
    }


    override fun pickSeat(seatIndex: Int, userId: String, callback: AUICallback?) {
        // do nothing
        throw RuntimeException("Not implement yet.")
    }


    override fun muteAudioSeat(seatIndex: Int, isMute: Boolean, callback: AUICallback?) {
        val micSeat = micSeats[seatIndex]

        if (AUIRoomContext.shared().getArbiter(channelName)?.isArbiter() == true) {
            rtmMuteAudioSeat(seatIndex, isMute, callback)
        } else {
            rtmManager.publishAndWaitReceipt(
                channelName,
                AUIRtmPublishModel(
                    interfaceName = if (isMute) kAUISeatMuteAudioInterface else kAUISeatUnmuteAudioInterface,
                    data = micSeat
                )
            ) { error ->
                if (error == null) {
                    callback?.onResult(null)
                } else {
                    callback?.onResult(AUIException(AUIException.ERROR_CODE_RTM, "error: $error"))
                }
            }
        }
    }

    override fun muteVideoSeat(seatIndex: Int, isMute: Boolean, callback: AUICallback?) {
        // do nothing
        throw RuntimeException("Not implement yet.")
    }

    override fun closeSeat(seatIndex: Int, isClose: Boolean, callback: AUICallback?) {
        val micSeat = micSeats[seatIndex]

        if (AUIRoomContext.shared().getArbiter(channelName)?.isArbiter() == true) {
            rtmCloseSeat(seatIndex, isClose, callback)
        } else {
            rtmManager.publishAndWaitReceipt(
                channelName,
                AUIRtmPublishModel(
                    interfaceName = if (isClose) kAUISeatLockInterface else kAUISeatUnlockInterface,
                    data = micSeat
                )
            ) { error ->
                if (error == null) {
                    callback?.onResult(null)
                } else {
                    callback?.onResult(AUIException(AUIException.ERROR_CODE_RTM, "error: $error"))
                }
            }
        }
    }

    override fun onClickInvited(index: Int) {
        observableHelper.notifyEventHandlers {
            it.onShowInvited(index)
        }
    }

    override fun getMicSeatInfo(seatIndex: Int): AUIMicSeatInfo? {
        return micSeats[seatIndex]
    }

    override fun getMicSeatIndex(userId: String): Int {
        var index = -1
        micSeats.forEach { (key, value) ->
            if (value.user?.userId == userId) {
                index = key
            }
        }
        return index
    }

    override fun getMicSeatSize(): Int {
        return micSeats.size
    }

    override fun getChannelName() = channelName

    override fun onMessageReceive(channelName: String, message: String) {
        if (channelName != this.channelName) {
            return
        }

        val publishModel: AUIRtmPublishModel<JsonObject>? =
            GsonTools.toBean(message, object : TypeToken<AUIRtmPublishModel<JsonObject>>() {}.type)

        if (publishModel?.uniqueId == null) {
            return
        }

        if (publishModel.interfaceName == null) {
            // receipt message from arbiter
            val receiptModel = GsonTools.toBean(message, AUIRtmReceiptModel::class.java) ?: return
            if (receiptModel.code == 0) {
                // success
                rtmManager.markReceiptFinished(receiptModel.uniqueId, null)
            } else {
                // failure
                rtmManager.markReceiptFinished(
                    receiptModel.uniqueId, AUIRtmException(
                        receiptModel.code,
                        receiptModel.reason, "receipt message from arbiter"
                    )
                )
            }
        } else {
            // publish message from non-arbiter
            when (publishModel.interfaceName) {
                kAUISeatEnterInterface -> {
                    val seatInfo =
                        GsonTools.toBean(publishModel.data, AUIRtmMicSeatInfo::class.java)
                    if (seatInfo != null) {
                        rtmEnterSeat(seatInfo.micSeatNo, AUIUserThumbnailInfo().apply {
                            userId = seatInfo.userId
                            userName = seatInfo.userName
                            userAvatar = seatInfo.userAvatar
                        }) { error ->
                            rtmManager.sendReceipt(
                                channelName,
                                AUIRtmReceiptModel(
                                    publishModel.uniqueId,
                                    error?.code ?: 0,
                                    error?.message ?: ""
                                )
                            )
                        }
                    } else {
                        rtmManager.sendReceipt(
                            channelName,
                            AUIRtmReceiptModel(publishModel.uniqueId, -1, "Gson parse failed!")
                        )
                    }
                }

                kAUISeatLeaveInterface -> {
                    val seatInfo =
                        GsonTools.toBean(publishModel.data, AUIRtmMicSeatInfo::class.java)
                    if (seatInfo != null) {
                        rtmLeaveSeat(seatInfo.userId) { error ->
                            rtmManager.sendReceipt(
                                channelName,
                                AUIRtmReceiptModel(
                                    publishModel.uniqueId,
                                    error?.code ?: 0,
                                    error?.message ?: ""
                                )
                            )
                        }
                    } else {
                        rtmManager.sendReceipt(
                            channelName,
                            AUIRtmReceiptModel(publishModel.uniqueId, -1, "Gson parse failed!")
                        )
                    }
                }

                kAUISeatKickInterface -> {
                    val seatInfo =
                        GsonTools.toBean(publishModel.data, AUIRtmMicSeatInfo::class.java)
                    if (seatInfo != null) {
                        rtmKickSeat(seatInfo.micSeatNo) { error ->
                            rtmManager.sendReceipt(
                                channelName,
                                AUIRtmReceiptModel(
                                    publishModel.uniqueId,
                                    error?.code ?: 0,
                                    error?.message ?: ""
                                )
                            )
                        }
                    } else {
                        rtmManager.sendReceipt(
                            channelName,
                            AUIRtmReceiptModel(publishModel.uniqueId, -1, "Gson parse failed!")
                        )
                    }
                }

                kAUISeatMuteAudioInterface -> {
                    val seatInfo =
                        GsonTools.toBean(publishModel.data, AUIRtmMicSeatInfo::class.java)
                    if (seatInfo != null) {
                        rtmMuteAudioSeat(seatInfo.micSeatNo, true) { error ->
                            rtmManager.sendReceipt(
                                channelName,
                                AUIRtmReceiptModel(
                                    publishModel.uniqueId,
                                    error?.code ?: 0,
                                    error?.message ?: ""
                                )
                            )
                        }
                    } else {
                        rtmManager.sendReceipt(
                            channelName,
                            AUIRtmReceiptModel(publishModel.uniqueId, -1, "Gson parse failed!")
                        )
                    }
                }

                kAUISeatUnmuteAudioInterface -> {
                    val seatInfo =
                        GsonTools.toBean(publishModel.data, AUIRtmMicSeatInfo::class.java)
                    if (seatInfo != null) {
                        rtmMuteAudioSeat(seatInfo.micSeatNo, false) { error ->
                            rtmManager.sendReceipt(
                                channelName,
                                AUIRtmReceiptModel(
                                    publishModel.uniqueId,
                                    error?.code ?: 0,
                                    error?.message ?: ""
                                )
                            )
                        }
                    } else {
                        rtmManager.sendReceipt(
                            channelName,
                            AUIRtmReceiptModel(publishModel.uniqueId, -1, "Gson parse failed!")
                        )
                    }
                }

                kAUISeatLockInterface -> {
                    val seatInfo =
                        GsonTools.toBean(publishModel.data, AUIRtmMicSeatInfo::class.java)
                    if (seatInfo != null) {
                        rtmCloseSeat(seatInfo.micSeatNo, true) { error ->
                            rtmManager.sendReceipt(
                                channelName,
                                AUIRtmReceiptModel(
                                    publishModel.uniqueId,
                                    error?.code ?: 0,
                                    error?.message ?: ""
                                )
                            )
                        }
                    } else {
                        rtmManager.sendReceipt(
                            channelName,
                            AUIRtmReceiptModel(publishModel.uniqueId, -1, "Gson parse failed!")
                        )
                    }
                }

                kAUISeatUnlockInterface -> {
                    val seatInfo =
                        GsonTools.toBean(publishModel.data, AUIRtmMicSeatInfo::class.java)
                    if (seatInfo != null) {
                        rtmCloseSeat(seatInfo.micSeatNo, false) { error ->
                            rtmManager.sendReceipt(
                                channelName,
                                AUIRtmReceiptModel(
                                    publishModel.uniqueId,
                                    error?.code ?: 0,
                                    error?.message ?: ""
                                )
                            )
                        }
                    } else {
                        rtmManager.sendReceipt(
                            channelName,
                            AUIRtmReceiptModel(publishModel.uniqueId, -1, "Gson parse failed!")
                        )
                    }
                }
            }
        }
    }

    /** AUiRtmMsgProxyDelegate */
    override fun onAttributeChanged(channelName: String, key: String, value: Any) {
        if (key != kSeatAttrKey) {
            return
        }
        Log.d("mic_seat_update", "class: ${value.javaClass}")
        val map: Map<String, Any> = HashMap()
        val seats = GsonTools.toBean(value as String, map.javaClass)
        Log.d("mic_seat_update", "seats: $seats")
        seats?.values?.forEach {
            val newSeatInfo =
                GsonTools.toBean(GsonTools.beanToString(it), AUIMicSeatInfo::class.java) ?: return
            val index = newSeatInfo.seatIndex
            val oldSeatInfo = micSeats[index]
            micSeats[index] = newSeatInfo
            val newSeatUserId = newSeatInfo.user?.userId ?: ""
            val oldSeatUserId = oldSeatInfo?.user?.userId ?: ""
            if (oldSeatUserId.isEmpty() && newSeatUserId.isNotEmpty()) {
                Log.d("mic_seat_update", "onAnchorEnterSeat: $it")
                val newUser = newSeatInfo.user ?: return
                observableHelper.notifyEventHandlers { delegate ->
                    delegate.onAnchorEnterSeat(index, newUser)
                }
            }
            if (oldSeatUserId.isNotEmpty() && newSeatUserId.isEmpty()) {
                Log.d("mic_seat_update", "onAnchorLeaveSeat: $it")
                val originUser = oldSeatInfo?.user ?: return
                observableHelper.notifyEventHandlers { delegate ->
                    delegate.onAnchorLeaveSeat(index, originUser)
                }
            }
            if ((oldSeatInfo?.seatStatus ?: AUIMicSeatStatus.idle) != newSeatInfo.seatStatus &&
                (oldSeatInfo?.seatStatus == AUIMicSeatStatus.locked || newSeatInfo.seatStatus == AUIMicSeatStatus.locked)
            ) {
                Log.d("mic_seat_update", "onSeatClose: $it")
                observableHelper.notifyEventHandlers { delegate ->
                    delegate.onSeatClose(index, (newSeatInfo.seatStatus == AUIMicSeatStatus.locked))
                }
            }
            if (oldSeatInfo?.muteAudio != newSeatInfo.muteAudio) {
                Log.d("mic_seat_update", "onSeatAudioMute: $it")
                observableHelper.notifyEventHandlers { delegate ->
                    delegate.onSeatAudioMute(index, newSeatInfo.muteAudio)
                }
            }
            if (oldSeatInfo?.muteVideo != newSeatInfo.muteVideo) {
                Log.d("mic_seat_update", "onSeatVideoMute: $it")
                observableHelper.notifyEventHandlers { delegate ->
                    delegate.onSeatVideoMute(index, newSeatInfo.muteVideo)
                }
            }
        }
    }


    // set metadata
    private fun rtmEnterSeat(
        seatIndex: Int,
        userInfo: AUIUserThumbnailInfo,
        callback: AUICallback?
    ) {
        if (micSeats.values.find { it.user?.userId == userInfo.userId } != null) {
            callback?.onResult(
                AUIException(
                    AUIException.ERROR_CODE_SEAT_ALREADY_ENTER,
                    "user already enter seat"
                )
            )
            return
        }
        if (micSeats.containsKey(seatIndex) && micSeats[seatIndex]?.seatStatus != AUIMicSeatStatus.idle) {
            callback?.onResult(
                AUIException(
                    AUIException.ERROR_CODE_SEAT_NOT_IDLE,
                    "mic seat not idle"
                )
            )
            return
        }

        val seatMap = mutableMapOf<String, Any>()
        micSeats.forEach { (key, value) ->
            var seatInfo = value
            if(key == seatIndex){
                seatInfo = AUIMicSeatInfo()
                seatInfo.seatIndex = value.seatIndex
                seatInfo.muteVideo = value.muteVideo
                seatInfo.muteAudio = value.muteAudio
                seatInfo.user = userInfo
                seatInfo.seatStatus = AUIMicSeatStatus.used
            }
            seatMap[key.toString()] = seatInfo
        }

        val metadata = mapOf(Pair(kSeatAttrKey, GsonTools.beanToString(seatMap) ?: ""))
        rtmManager.setBatchMetadata(
            channelName,
            metadata = metadata
        ) { error ->
            if (error == null) {
                callback?.onResult(null)
            } else {
                callback?.onResult(AUIException(AUIException.ERROR_CODE_RTM, "error: $error"))
            }
        }
    }

    private fun rtmLeaveSeat(userId: String, callback: AUICallback?) {
        val seatMap = mutableMapOf<String, Any>()
        micSeats.forEach { (key, value) ->
            var seatInfo = value
            if (seatInfo.user?.userId == userId) {
                seatInfo = AUIMicSeatInfo()
                seatInfo.seatIndex = value.seatIndex
                seatInfo.muteAudio = value.muteAudio
                seatInfo.muteVideo = value.muteVideo
            }
            seatMap[key.toString()] = seatInfo
        }

        val metadata = mapOf(Pair(kSeatAttrKey, GsonTools.beanToString(seatMap) ?: ""))

        var willError: AUIException? = null
        observableHelper.notifyEventHandlers {
            willError = it.onSeatWillLeave(userId, metadata)
            if (willError != null) {
                return@notifyEventHandlers
            }
        }
        if (willError != null) {
            callback?.onResult(AUIException(AUIException.ERROR_CODE_RTM, ""))
            return
        }

        rtmManager.setBatchMetadata(
            channelName,
            metadata = metadata
        ) { error ->
            if (error == null) {
                callback?.onResult(null)
            } else {
                callback?.onResult(AUIException(AUIException.ERROR_CODE_RTM, "error: $error"))
            }
        }
    }

    private fun rtmKickSeat(seatIndex: Int, callback: AUICallback?) {
        val seatMap = mutableMapOf<String, Any>()
        micSeats.forEach { (key, value) ->
            var seatInfo = value
            if (key == seatIndex) {
                seatInfo = AUIMicSeatInfo()
                seatInfo.seatIndex = value.seatIndex
                seatInfo.muteVideo = value.muteVideo
                seatInfo.muteAudio = value.muteAudio
            }
            seatMap[key.toString()] = seatInfo
        }

        val metadata = mapOf(Pair(kSeatAttrKey, GsonTools.beanToString(seatMap) ?: ""))
        rtmManager.setBatchMetadata(
            channelName,
            metadata = metadata
        ) { error ->
            if (error == null) {
                callback?.onResult(null)
            } else {
                callback?.onResult(AUIException(AUIException.ERROR_CODE_RTM, "error: $error"))
            }
        }
    }

    private fun rtmMuteAudioSeat(seatIndex: Int, isMute: Boolean, callback: AUICallback?) {
        val seatMap = mutableMapOf<String, Any>()
        micSeats.forEach { (key, value) ->
            var seatInfo = value
            if (key == seatIndex) {
                seatInfo = AUIMicSeatInfo()
                seatInfo.seatStatus = value.seatStatus
                seatInfo.muteVideo = value.muteVideo
                seatInfo.user = value.user
                seatInfo.seatIndex = value.seatIndex
                seatInfo.muteAudio = isMute
            }
            seatMap[key.toString()] = seatInfo
        }

        val metadata = mapOf(Pair(kSeatAttrKey, GsonTools.beanToString(seatMap) ?: ""))
        rtmManager.setBatchMetadata(
            channelName,
            metadata = metadata
        ) { error ->
            if (error == null) {
                callback?.onResult(null)
            } else {
                callback?.onResult(AUIException(AUIException.ERROR_CODE_RTM, "error: $error"))
            }
        }
    }

    private fun rtmCloseSeat(seatIndex: Int, isClose: Boolean, callback: AUICallback?) {
        val seatMap = mutableMapOf<String, Any>()
        micSeats.forEach { (key, value) ->
            var seatInfo = value
            if (key == seatIndex) {
                seatInfo = AUIMicSeatInfo()
                seatInfo.muteVideo = value.muteVideo
                seatInfo.user = value.user
                seatInfo.seatIndex = value.seatIndex
                seatInfo.muteAudio = value.muteAudio
                if (isClose) {
                    seatInfo.seatStatus = AUIMicSeatStatus.locked
                } else if (seatInfo.user != null) {
                    seatInfo.seatStatus = AUIMicSeatStatus.used
                } else {
                    seatInfo.seatStatus = AUIMicSeatStatus.idle
                }
            }
            seatMap[key.toString()] = seatInfo
        }

        val metadata = mapOf(Pair(kSeatAttrKey, GsonTools.beanToString(seatMap) ?: ""))
        rtmManager.setBatchMetadata(
            channelName,
            metadata = metadata
        ) { error ->
            if (error == null) {
                callback?.onResult(null)
            } else {
                callback?.onResult(AUIException(AUIException.ERROR_CODE_RTM, "error: $error"))
            }
        }
    }
}
package io.agora.auikit.service.imp

import android.util.Log
import io.agora.auikit.model.AUIMicSeatInfo
import io.agora.auikit.model.AUIMicSeatStatus
import io.agora.auikit.model.AUIRoomContext
import io.agora.auikit.model.AUIUserThumbnailInfo
import io.agora.auikit.service.IAUIMicSeatService
import io.agora.auikit.service.callback.AUICallback
import io.agora.auikit.service.callback.AUIException
import io.agora.auikit.service.collection.AUIAttributesModel
import io.agora.auikit.service.collection.AUICollectionException
import io.agora.auikit.service.collection.AUIMapCollection
import io.agora.auikit.service.rtm.AUIRtmManager
import io.agora.auikit.utils.AUILogger
import io.agora.auikit.utils.GsonTools
import io.agora.auikit.utils.ObservableHelper

const val kSeatAttrKey = "micSeat"

enum class AUIMicSeatCmd {
    initSeatCmd,
    leaveSeatCmd,
    enterSeatCmd,
    kickSeatCmd,
    muteAudioCmd,
    closeSeatCmd
}

class AUIMicSeatServiceImpl(
    private val channelName: String,
    private val rtmManager: AUIRtmManager
) : IAUIMicSeatService {

    private val observableHelper =
        ObservableHelper<IAUIMicSeatService.AUIMicSeatRespObserver>()

    private var micSeats = mutableMapOf<Int, AUIMicSeatInfo>()

    private val mapCollection = AUIMapCollection(channelName, kSeatAttrKey, rtmManager)


    init {
        mapCollection.subscribeWillMerge(this::metadataWillMerge)
        mapCollection.subscribeAttributesDidChanged(this::onAttributeChanged)
    }

    override fun initService(completion: AUICallback?) {
        if (!AUIRoomContext.shared().isRoomOwner(channelName)) {
            return
        }
        val roomInfo = roomContext.getRoomInfo(channelName) ?: return
        val seatMap = mutableMapOf<String, Any>()
        for (i in 0 until roomInfo.micSeatCount) {
            val seat = AUIMicSeatInfo()
            seat.seatIndex = i
            if (i == 0) {
                seat.user = roomContext.currentUserInfo
                seat.seatStatus = AUIMicSeatStatus.used
            }else{
                seat.user = AUIUserThumbnailInfo()
            }
            seatMap.put(i.toString(), seat)
        }
        AUILogger.logger().d(
            "AUIMicSeatServiceImp",
            "initService >> currentUid=${roomContext.currentUserInfo.userId} arbiterUid=${
                roomContext.getArbiter(
                    channelName
                )?.lockOwnerId()
            }"
        )
        mapCollection.updateMetaData(
            AUIMicSeatCmd.initSeatCmd.name,
            seatMap,
        ) {
            completion?.onResult(
                if (it == null) null else AUIException(
                    AUIException.ERROR_CODE_RTM_COLLECTION,
                    "$it"
                )
            )
        }
    }

    override fun deInitService(completion: AUICallback?) {
        super.deInitService(completion)
        mapCollection.cleanMetaData {
            completion?.onResult(
                if (it == null) null else AUIException(
                    AUIException.ERROR_CODE_RTM_COLLECTION,
                    "$it"
                )
            )
        }
        mapCollection.release()
    }

    override fun registerRespObserver(observer: IAUIMicSeatService.AUIMicSeatRespObserver?) {
        observableHelper.subscribeEvent(observer)
    }

    override fun unRegisterRespObserver(observer: IAUIMicSeatService.AUIMicSeatRespObserver?) {
        observableHelper.unSubscribeEvent(observer)
    }

    override fun enterSeat(seatIndex: Int, callback: AUICallback?) {
        mapCollection.mergeMetaData(
            valueCmd = AUIMicSeatCmd.enterSeatCmd.name,
            value = mapOf(
                Pair(
                    seatIndex.toString(),
                    mapOf(
                        Pair("owner", GsonTools.beanToMap(roomContext.currentUserInfo)),
                        Pair("micSeatStatus", AUIMicSeatStatus.used)
                    )
                )
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
        micSeat.seatIndex

        mapCollection.mergeMetaData(
            valueCmd = AUIMicSeatCmd.leaveSeatCmd.name,
            value = mapOf(
                Pair(
                    micSeat.seatIndex.toString(),
                    mapOf(
                        Pair("owner", GsonTools.beanToMap(AUIUserThumbnailInfo())),
                        Pair("micSeatStatus", AUIMicSeatStatus.idle)
                    )
                )
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

    override fun autoEnterSeat(callback: AUICallback?) {
        var toIndex: Int? = null
        val sortedKeys = micSeats.keys.sortedBy { key -> key }
        for (key in sortedKeys) {
            val value = micSeats[key]
            if (value?.seatStatus == AUIMicSeatStatus.idle) {
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
        if (!micSeats.containsKey(seatIndex)) {
            callback?.onResult(AUIException(-1, "The $seatIndex seat is not exist."))
            return
        }
        mapCollection.mergeMetaData(
            valueCmd = AUIMicSeatCmd.kickSeatCmd.name,
            value = mapOf(
                Pair(
                    seatIndex.toString(),
                    mapOf(
                        Pair("owner", GsonTools.beanToMap(AUIUserThumbnailInfo())),
                        Pair("micSeatStatus", AUIMicSeatStatus.idle)
                    )
                )
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


    override fun pickSeat(seatIndex: Int, userId: String, callback: AUICallback?) {
        // do nothing
        throw RuntimeException("Not implement yet.")
    }


    override fun muteAudioSeat(seatIndex: Int, isMute: Boolean, callback: AUICallback?) {
        if (!micSeats.containsKey(seatIndex)) {
            callback?.onResult(AUIException(-1, "The $seatIndex seat is not exist."))
            return
        }
        mapCollection.mergeMetaData(
            valueCmd = AUIMicSeatCmd.muteAudioCmd.name,
            value = mapOf(Pair(seatIndex.toString(), mapOf(Pair("isMuteAudio", isMute))))
        ) {
            callback?.onResult(
                if (it == null) null else AUIException(
                    AUIException.ERROR_CODE_RTM_COLLECTION,
                    "$it"
                )
            )
        }
    }

    override fun muteVideoSeat(seatIndex: Int, isMute: Boolean, callback: AUICallback?) {
        // do nothing
        throw RuntimeException("Not implement yet.")
    }

    override fun closeSeat(seatIndex: Int, isClose: Boolean, callback: AUICallback?) {
        if (!micSeats.containsKey(seatIndex)) {
            callback?.onResult(AUIException(-1, "The $seatIndex seat is not exist."))
            return
        }
        val micSeat = micSeats[seatIndex]

        var status = AUIMicSeatStatus.idle
        if (isClose) {
            status = AUIMicSeatStatus.locked
        } else if (micSeat?.user?.userId?.isNotEmpty() == true) {
            status = AUIMicSeatStatus.used
        }
        mapCollection.mergeMetaData(
            valueCmd = AUIMicSeatCmd.closeSeatCmd.name,
            value = mapOf(Pair(seatIndex.toString(), mapOf(Pair("micSeatStatus", status))))
        ) {
            callback?.onResult(
                if (it == null) null else AUIException(
                    AUIException.ERROR_CODE_RTM_COLLECTION,
                    "$it"
                )
            )
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

    private fun onAttributeChanged(channelName: String, key: String, value: AUIAttributesModel) {
        if (key != kSeatAttrKey) {
            return
        }
        Log.d("mic_seat_update", "class: ${value.javaClass}")
        val map: Map<String, Any> = HashMap()
        val seats = value.getMap() ?: GsonTools.toBean(GsonTools.beanToString(value), map.javaClass)
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

    private fun metadataWillMerge(
        publisherId: String,
        valueCmd: String?,
        newValue: Map<String, Any>,
        oldValue: Map<String, Any>
    ): AUICollectionException? {
        val seatInfoPair = newValue.toList()[0]
        val seatIndex = seatInfoPair.first.toInt()
        val seatInfoMap = seatInfoPair.second as? Map<*, *>
        var userId = (seatInfoMap?.get("owner") as? Map<*, *>)?.get("userId") as? String
        when (valueCmd) {
            AUIMicSeatCmd.enterSeatCmd.name -> {
                if (micSeats.values.any { it.user?.userId == userId }) {
                    return AUICollectionException.ErrorCode.unknown.toException("code: ${AUIException.ERROR_CODE_SEAT_ALREADY_ENTER}")
                }
                val seatInfo = micSeats[seatIndex]
                userId = seatInfo?.user?.userId ?: ""
                if (seatInfo?.user != null && seatInfo.seatStatus != AUIMicSeatStatus.idle) {
                    return AUICollectionException.ErrorCode.unknown.toException(
                        "${
                            AUIException(
                                AUIException.ERROR_CODE_SEAT_NOT_IDLE,
                                "mic seat not idle"
                            )
                        }"
                    )
                }
            }

            AUIMicSeatCmd.leaveSeatCmd.name -> {
                if (seatIndex == 0) {
                    return AUICollectionException.ErrorCode.unknown.toException(
                        "${
                            AUIException(
                                AUIException.ERROR_CODE_PERMISSION_LEAK,
                                ""
                            )
                        }"
                    )
                }
                if (micSeats[seatIndex]?.user?.userId != publisherId || roomContext.isRoomOwner(
                        channelName,
                        publisherId
                    )
                ) {
                    return AUICollectionException.ErrorCode.unknown.toException(
                        "${
                            AUIException(
                                AUIException.ERROR_CODE_SEAT_NOT_ENTER,
                                ""
                            )
                        }"
                    )
                }
                userId = micSeats[seatIndex]?.user?.userId ?: ""
                val metadata = mutableMapOf<String, String>()
                var error: AUIException? = null
                observableHelper.notifyEventHandlers {
                    error = it.onSeatWillLeave(userId, metadata)?.let { return@notifyEventHandlers }
                }
                return if (error == null) null else AUICollectionException.ErrorCode.unknown.toException(
                    "$error"
                )
            }

            AUIMicSeatCmd.kickSeatCmd.name -> {
                if (seatIndex == 0) {
                    return AUICollectionException.ErrorCode.unknown.toException("${AUIException(AUIException.ERROR_CODE_PERMISSION_LEAK, "")}")
                }
                userId = micSeats[seatIndex]?.user?.userId ?: ""
                val metadata = mutableMapOf<String, String>()
                var error: AUIException? = null
                observableHelper.notifyEventHandlers {
                    error = it.onSeatWillLeave(userId, metadata)?.let { return@notifyEventHandlers }
                }
                return if (error == null) null else AUICollectionException.ErrorCode.unknown.toException(
                    "$error"
                )
            }
        }
        return null
    }
}
package io.agora.auikit.service.imp

import android.util.Log
import io.agora.auikit.model.AUIMicSeatInfo
import io.agora.auikit.model.AUIMicSeatStatus
import io.agora.auikit.model.AUIRoomContext
import io.agora.auikit.model.AUIUserThumbnailInfo
import io.agora.auikit.service.IAUIMicSeatService
import io.agora.auikit.service.callback.AUICallback
import io.agora.auikit.service.callback.AUIException
import io.agora.auikit.service.collection.AUIMapCollection
import io.agora.auikit.service.rtm.AUIRtmAttributeRespObserver
import io.agora.auikit.service.rtm.AUIRtmManager
import io.agora.auikit.utils.GsonTools
import io.agora.auikit.utils.ObservableHelper
import java.util.UUID

private const val kSeatAttrKey = "micSeat"

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
) : IAUIMicSeatService, AUIRtmAttributeRespObserver {

    private val observableHelper =
        ObservableHelper<IAUIMicSeatService.AUIMicSeatRespObserver>()

    private var micSeats = mutableMapOf<Int, AUIMicSeatInfo>()

    private val mapCollection = AUIMapCollection(channelName, kSeatAttrKey, rtmManager)


    init {
        rtmManager.subscribeAttribute(channelName, kSeatAttrKey, this)
        mapCollection.subscribeWillMerge(this::metadataWillMerge)
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
            }
            seatMap.put(i.toString(), seat)
        }
        mapCollection.setMetaData(
            AUIMicSeatCmd.initSeatCmd.name,
            seatMap,
            UUID.randomUUID().toString()
        ) { error ->
            completion?.onResult(error)
        }
    }

    override fun deInitService(completion: AUICallback?) {

        mapCollection.release()

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
        mapCollection.mergeMetaData(
            AUIMicSeatCmd.enterSeatCmd.name,
            mapOf(
                Pair(
                    seatIndex.toString(),
                    mapOf(
                        Pair("owner", roomContext.currentUserInfo),
                        Pair("micSeatStatus", AUIMicSeatStatus.used)
                    )
                )
            ),
            UUID.randomUUID().toString(),
            callback
        )
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
            AUIMicSeatCmd.leaveSeatCmd.name,
            mapOf(
                Pair(
                    micSeat.seatIndex.toString(),
                    mapOf(
                        Pair("owner", AUIUserThumbnailInfo()),
                        Pair("micSeatStatus", AUIMicSeatStatus.idle)
                    )
                )
            ),
            UUID.randomUUID().toString(),
            callback
        )
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
        mapCollection.mergeMetaData(
            AUIMicSeatCmd.kickSeatCmd.name,
            mapOf(
                Pair(
                    seatIndex.toString(),
                    mapOf(
                        Pair("owner", AUIUserThumbnailInfo()),
                        Pair("micSeatStatus", AUIMicSeatStatus.idle)
                    )
                )
            ),
            UUID.randomUUID().toString(),
            callback
        )
    }


    override fun pickSeat(seatIndex: Int, userId: String, callback: AUICallback?) {
        // do nothing
        throw RuntimeException("Not implement yet.")
    }


    override fun muteAudioSeat(seatIndex: Int, isMute: Boolean, callback: AUICallback?) {
        mapCollection.mergeMetaData(
            AUIMicSeatCmd.muteAudioCmd.name,
            mapOf(Pair(seatIndex.toString(), mapOf(Pair("isMuteAudio", isMute)))),
            UUID.randomUUID().toString(),
            callback
        )
    }

    override fun muteVideoSeat(seatIndex: Int, isMute: Boolean, callback: AUICallback?) {
        // do nothing
        throw RuntimeException("Not implement yet.")
    }

    override fun closeSeat(seatIndex: Int, isClose: Boolean, callback: AUICallback?) {
        val micSeat = micSeats[seatIndex]

        var status = AUIMicSeatStatus.idle
        if (isClose) {
            status = AUIMicSeatStatus.locked
        } else if (micSeat?.user != null) {
            status = AUIMicSeatStatus.used
        }
        mapCollection.mergeMetaData(AUIMicSeatCmd.closeSeatCmd.name,
            mapOf(Pair(seatIndex.toString(), mapOf(Pair("micSeatStatus", status)))),
            "",
            callback
        )
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

    private fun metadataWillMerge(
        publisherId: String,
        valueCmd: String?,
        newValue: Map<String, Any>,
        oldValue: Map<String, Any>
    ): AUIException? {
        if (AUIMicSeatCmd.enterSeatCmd.name == valueCmd) {
            newValue.keys.forEach { seatIndex ->
                val index = seatIndex.toInt()
                val seatInfo = micSeats[index]
                if (seatInfo != null && seatInfo.seatStatus != AUIMicSeatStatus.idle) {
                    return AUIException(
                        AUIException.ERROR_CODE_SEAT_NOT_IDLE,
                        "mic seat not idle"
                    )
                }
                // return AUIException(
                //     AUIException.ERROR_CODE_SEAT_ALREADY_ENTER,
                //     "user already enter seat"
                // )
            }
        }
        return null
    }
}
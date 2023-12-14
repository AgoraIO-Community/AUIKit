package io.agora.auikit.service.imp

import android.util.Log
import io.agora.auikit.model.AUIMicSeatInfo
import io.agora.auikit.model.AUIMicSeatStatus
import io.agora.auikit.model.AUIUserThumbnailInfo
import io.agora.auikit.service.IAUIMicSeatService
import io.agora.auikit.service.callback.AUICallback
import io.agora.auikit.service.callback.AUIException
import io.agora.auikit.service.http.CommonResp
import io.agora.auikit.service.http.HttpManager
import io.agora.auikit.service.http.Utils
import io.agora.auikit.service.http.seat.SeatInfoReq
import io.agora.auikit.service.http.seat.SeatInterface
import io.agora.auikit.service.http.seat.SeatLeaveReq
import io.agora.auikit.service.http.seat.SeatPickReq
import io.agora.auikit.service.rtm.AUIRtmManager
import io.agora.auikit.service.rtm.AUIRtmMsgRespObserver
import io.agora.auikit.utils.GsonTools
import io.agora.auikit.utils.ObservableHelper
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response

private const val kSeatAttrKey = "micSeat"
class AUIMicSeatServiceImpl(
    private val channelName: String,
    private val rtmManager: AUIRtmManager
) : IAUIMicSeatService, AUIRtmMsgRespObserver {

    private val observableHelper =
        ObservableHelper<IAUIMicSeatService.AUIMicSeatRespObserver>()

    private var micSeats = mutableMapOf<Int, AUIMicSeatInfo>()

    init {
        rtmManager.subscribeMsg(channelName, kSeatAttrKey, this)
    }

    override fun deInitService(completion: AUICallback?) {
        rtmManager.cleanMetadata(
            channelName,
            removeKeys = listOf(kSeatAttrKry)
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
        rtmEnterSeat(seatIndex, roomContext.currentUserInfo, callback)
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

    override fun leaveSeat(callback: AUICallback?) {
        HttpManager.getService(SeatInterface::class.java)
            .seatLeave(SeatLeaveReq(channelName, roomContext.currentUserInfo.userId))
            .enqueue(object : retrofit2.Callback<CommonResp<Any>> {
                override fun onResponse(call: Call<CommonResp<Any>>, response: Response<CommonResp<Any>>) {
                    if (response.body()?.code == 0) {
                        callback?.onResult(null)
                    } else {
                        callback?.onResult(Utils.errorFromResponse(response))
                    }
                }
                override fun onFailure(call: Call<CommonResp<Any>>, t: Throwable) {
                    callback?.onResult(
                        AUIException(
                            -1,
                            t.message
                        )
                    )
                }
            })
    }

    override fun pickSeat(seatIndex: Int, userId: String, callback: AUICallback?) {
        HttpManager.getService(SeatInterface::class.java)
            .seatPick(SeatPickReq(channelName, userId, seatIndex))
            .enqueue(object : retrofit2.Callback<CommonResp<Any>> {
                override fun onResponse(call: Call<CommonResp<Any>>, response: Response<CommonResp<Any>>) {
                    if (response.body()?.code == 0) {
                        callback?.onResult(null)
                    } else {
                        callback?.onResult(Utils.errorFromResponse(response))
                    }
                }
                override fun onFailure(call: Call<CommonResp<Any>>, t: Throwable) {
                    callback?.onResult(
                        AUIException(
                            -1,
                            t.message
                        )
                    )
                }
            })
    }

    override fun kickSeat(seatIndex: Int, callback: AUICallback?) {
        HttpManager.getService(SeatInterface::class.java)
            .seatKick(SeatInfoReq(channelName, roomContext.currentUserInfo.userId, seatIndex))
            .enqueue(object : retrofit2.Callback<CommonResp<Any>> {
                override fun onResponse(call: Call<CommonResp<Any>>, response: Response<CommonResp<Any>>) {
                    if (response.body()?.code == 0) {
                        callback?.onResult(null)
                    } else {
                        callback?.onResult(Utils.errorFromResponse(response))
                    }
                }
                override fun onFailure(call: Call<CommonResp<Any>>, t: Throwable) {
                    callback?.onResult(
                        AUIException(
                            -1,
                            t.message
                        )
                    )
                }
            })
    }

    override fun muteAudioSeat(seatIndex: Int, isMute: Boolean, callback: AUICallback?) {
        val param = SeatInfoReq(channelName, roomContext.currentUserInfo.userId, seatIndex)
        val service = HttpManager.getService(SeatInterface::class.java)
        val req = if (isMute) {
            service.seatAudioMute(param)
        } else {
            service.seatAudioUnMute(param)
        }
        req.enqueue(object : retrofit2.Callback<CommonResp<Any>> {
            override fun onResponse(call: Call<CommonResp<Any>>, response: Response<CommonResp<Any>>) {
                if (response.body()?.code == 0) {
                    callback?.onResult(null)
                } else {
                    callback?.onResult(Utils.errorFromResponse(response))
                }
            }
            override fun onFailure(call: Call<CommonResp<Any>>, t: Throwable) {
                callback?.onResult(
                    AUIException(
                        -1,
                        t.message
                    )
                )
            }
        })
    }

    override fun muteVideoSeat(seatIndex: Int, isMute: Boolean, callback: AUICallback?) {
        val param = SeatInfoReq(channelName, roomContext.currentUserInfo.userId, seatIndex)
        val service = HttpManager.getService(SeatInterface::class.java)
        val req = if (isMute) {
            service.seatVideoMute(param)
        } else {
            service.seatVideoUnMute(param)
        }
        req.enqueue(object : retrofit2.Callback<CommonResp<Any>> {
            override fun onResponse(call: Call<CommonResp<Any>>, response: Response<CommonResp<Any>>) {
                if (response.body()?.code == 0) {
                    callback?.onResult(null)
                } else {
                    callback?.onResult(Utils.errorFromResponse(response))
                }
            }
            override fun onFailure(call: Call<CommonResp<Any>>, t: Throwable) {
                callback?.onResult(
                    AUIException(
                        -1,
                        t.message
                    )
                )
            }
        })
    }

    override fun closeSeat(seatIndex: Int, isClose: Boolean, callback: AUICallback?) {
        val param = SeatInfoReq(channelName, roomContext.currentUserInfo.userId, seatIndex)
        val service = HttpManager.getService(SeatInterface::class.java)
        val req = if (isClose) {
            service.seatLock(param)
        } else {
            service.seatUnLock(param)
        }
        req.enqueue(object : retrofit2.Callback<CommonResp<Any>> {
            override fun onResponse(call: Call<CommonResp<Any>>, response: Response<CommonResp<Any>>) {
                if (response.body()?.code == 0) {
                    callback?.onResult(null)
                } else {
                    callback?.onResult(Utils.errorFromResponse(response))
                }
            }
            override fun onFailure(call: Call<CommonResp<Any>>, t: Throwable) {
                callback?.onResult(
                    AUIException(
                        -1,
                        t.message
                    )
                )
            }
        })
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
            if(value.user?.userId == userId){
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
    override fun onMsgDidChanged(channelName: String, key: String, value: Any) {
        if (key != kSeatAttrKey) {
            return
        }
        Log.d("mic_seat_update", "class: ${value.javaClass}")
        val map: Map<String, Any> = HashMap()
        val seats = GsonTools.toBean(value as String, map.javaClass)
        Log.d("mic_seat_update", "seats: $seats")
        seats?.values?.forEach {
            val newSeatInfo = GsonTools.toBean(GsonTools.beanToString(it), AUIMicSeatInfo::class.java) ?: return
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
                (oldSeatInfo?.seatStatus == AUIMicSeatStatus.locked || newSeatInfo.seatStatus == AUIMicSeatStatus.locked)) {
                Log.d("mic_seat_update", "onSeatClose: $it")
                observableHelper.notifyEventHandlers { delegate ->
                    delegate.onSeatClose(index, (newSeatInfo.seatStatus == AUIMicSeatStatus.locked))
                }
            }
            if ((oldSeatInfo?.muteAudio ?: 0) != newSeatInfo.muteAudio) {
                Log.d("mic_seat_update", "onSeatAudioMute: $it")
                observableHelper.notifyEventHandlers { delegate ->
                    delegate.onSeatAudioMute(index, (newSeatInfo.muteAudio != 0))
                }
            }
            if ((oldSeatInfo?.muteVideo ?: 0) != newSeatInfo.muteVideo) {
                Log.d("mic_seat_update", "onSeatVideoMute: $it")
                observableHelper.notifyEventHandlers { delegate ->
                    delegate.onSeatVideoMute(index, (newSeatInfo.muteVideo != 0))
                }
            }
        }
    }


    // set metadata
    private val kSeatAttrKry = "micSeat"

    private fun rtmEnterSeat(seatIndex: Int, userInfo: AUIUserThumbnailInfo, callback: AUICallback?) {
        if (micSeats.values.find { it.user?.userId == userInfo.userId } != null) {
            callback?.onResult(AUIException(AUIException.ERROR_CODE_SEAT_ALREADY_ENTER, "user already enter seat"))
            return
        }
        if(micSeats.containsKey(seatIndex)){
            callback?.onResult(AUIException(AUIException.ERROR_CODE_SEAT_NOT_IDLE, "mic seat not idle"))
            return
        }


        val seatMap = JSONObject()
        micSeats.forEach { (key, value) ->
            seatMap.put(key.toString(), GsonTools.beanToString(value))
        }
        seatMap.put(
            seatIndex.toString(), GsonTools.beanToString(
                AUIMicSeatInfo().apply {
                    user = userInfo
                    this.seatIndex = seatIndex
                    seatStatus = AUIMicSeatStatus.used
                })
        )

        val metadata = mapOf(Pair(kSeatAttrKry, seatMap.toString()))
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
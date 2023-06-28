package io.agora.auikit.service.imp

import android.util.Log
import io.agora.auikit.model.AUIMicSeatInfo
import io.agora.auikit.model.AUIMicSeatStatus
import io.agora.auikit.service.IAUIMicSeatService
import io.agora.auikit.service.callback.AUICallback
import io.agora.auikit.service.callback.AUIException
import io.agora.auikit.service.http.CommonResp
import io.agora.auikit.service.http.HttpManager
import io.agora.auikit.service.http.Utils
import io.agora.auikit.service.http.seat.SeatEnterReq
import io.agora.auikit.service.http.seat.SeatInfoReq
import io.agora.auikit.service.http.seat.SeatInterface
import io.agora.auikit.service.http.seat.SeatLeaveReq
import io.agora.auikit.service.http.seat.SeatPickReq
import io.agora.auikit.service.rtm.AUIRtmManager
import io.agora.auikit.service.rtm.AUIRtmMsgProxyDelegate
import io.agora.auikit.utils.DelegateHelper
import io.agora.auikit.utils.GsonTools
import retrofit2.Call
import retrofit2.Response

private const val kSeatAttrKey = "micSeat"
class AUIMicSeatServiceImpl(
    private val channelName: String,
    private val rtmManager: AUIRtmManager
) : IAUIMicSeatService, AUIRtmMsgProxyDelegate {

    private val delegateHelper = DelegateHelper<IAUIMicSeatService.AUIMicSeatRespDelegate>()

    private var micSeats = mutableMapOf<Int, AUIMicSeatInfo>()

    init {
        rtmManager.subscribeMsg(channelName, kSeatAttrKey, this)
    }

    override fun bindRespDelegate(delegate: IAUIMicSeatService.AUIMicSeatRespDelegate?) {
        delegateHelper.bindDelegate(delegate)
    }

    override fun unbindRespDelegate(delegate: IAUIMicSeatService.AUIMicSeatRespDelegate?) {
        delegateHelper.unBindDelegate(delegate)
    }

    override fun enterSeat(seatIndex: Int, callback: AUICallback?) {
        val param = SeatEnterReq(
            channelName,
            roomContext.currentUserInfo.userId,
            roomContext.currentUserInfo.userName,
            roomContext.currentUserInfo.userAvatar,
            seatIndex)
        HttpManager.getService(SeatInterface::class.java)
            .seatEnter(param)
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

    override fun getMicSeatInfo(seatIndex: Int): AUIMicSeatInfo? {
        return micSeats[seatIndex]
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
                delegateHelper.notifyDelegate { delegate ->
                    delegate.onAnchorEnterSeat(index, newUser)
                }
            }
            if (oldSeatUserId.isNotEmpty() && newSeatUserId.isEmpty()) {
                Log.d("mic_seat_update", "onAnchorLeaveSeat: $it")
                val originUser = oldSeatInfo?.user ?: return
                delegateHelper.notifyDelegate { delegate ->
                    delegate.onAnchorLeaveSeat(index, originUser)
                }
            }
            if ((oldSeatInfo?.seatStatus ?: AUIMicSeatStatus.idle) != newSeatInfo.seatStatus &&
                (oldSeatInfo?.seatStatus == AUIMicSeatStatus.locked || newSeatInfo.seatStatus == AUIMicSeatStatus.locked)) {
                Log.d("mic_seat_update", "onSeatClose: $it")
                delegateHelper.notifyDelegate { delegate ->
                    delegate.onSeatClose(index, (newSeatInfo.seatStatus == AUIMicSeatStatus.locked))
                }
            }
            if ((oldSeatInfo?.muteAudio ?: 0) != newSeatInfo.muteAudio) {
                Log.d("mic_seat_update", "onSeatAudioMute: $it")
                delegateHelper.notifyDelegate { delegate ->
                    delegate.onSeatAudioMute(index, (newSeatInfo.muteAudio != 0))
                }
            }
            if ((oldSeatInfo?.muteVideo ?: 0) != newSeatInfo.muteVideo) {
                Log.d("mic_seat_update", "onSeatVideoMute: $it")
                delegateHelper.notifyDelegate { delegate ->
                    delegate.onSeatVideoMute(index, (newSeatInfo.muteVideo != 0))
                }
            }
        }
    }
}
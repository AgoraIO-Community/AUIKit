package io.agora.auikit.service.imp

import android.util.Log
import io.agora.auikit.model.AUIRoomContext
import io.agora.auikit.model.AUIUserInfo
import io.agora.auikit.service.IAUIInvitationService
import io.agora.auikit.service.callback.AUICallback
import io.agora.auikit.service.callback.AUIException
import io.agora.auikit.service.http.CommonResp
import io.agora.auikit.service.http.HttpManager
import io.agora.auikit.service.http.apply.ApplyAcceptReq
import io.agora.auikit.service.http.apply.ApplyCancelReq
import io.agora.auikit.service.http.apply.ApplyCreateReq
import io.agora.auikit.service.http.apply.ApplyInterface
import io.agora.auikit.service.http.invitation.InvitationAcceptReq
import io.agora.auikit.service.http.invitation.InvitationCreateReq
import io.agora.auikit.service.http.invitation.InvitationInterface
import io.agora.auikit.service.http.invitation.InvitationPayload
import io.agora.auikit.service.http.invitation.RejectInvitationAccept
import io.agora.auikit.service.rtm.AUIRtmManager
import io.agora.auikit.service.rtm.AUIRtmMsgRespObserver
import io.agora.auikit.utils.GsonTools
import io.agora.auikit.utils.ObservableHelper
import io.agora.auikit.utils.ThreadManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val RoomApplyKey = "application"
private const val RoomInvitationKey = "invitation"
class AUIInvitationServiceImpl(
    private val channelName: String,
    private val rtmManager: AUIRtmManager
) : IAUIInvitationService, AUIRtmMsgRespObserver {
    private val roomContext:AUIRoomContext

    init {
        rtmManager.subscribeMsg(channelName, RoomApplyKey,this)
        rtmManager.subscribeMsg(channelName, RoomInvitationKey,this)
        this.roomContext = AUIRoomContext.shared()
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
        HttpManager.getService(InvitationInterface::class.java)
            .initiateCreate(
                InvitationCreateReq(
                    channelName,
                    roomContext.currentUserInfo.userId,
                    userId,
                    InvitationPayload("",seatIndex)
                )
            )
            .enqueue(object : Callback<CommonResp<Any>> {
                override fun onResponse(
                    call: Call<CommonResp<Any>>,
                    response: Response<CommonResp<Any>>
                ) {
                    if (response.body()?.code == 0 && response.body()?.message == "Success"){
                        ThreadManager.getInstance().runOnMainThread{
                            callback?.onResult(null)
                        }
                    }
                }

                override fun onFailure(call: Call<CommonResp<Any>>, t: Throwable) {
                    callback?.onResult(AUIException(-1, t.message))
                }
            })
    }

    override fun acceptInvitation(userId: String, seatIndex: Int, callback: AUICallback?) {
        HttpManager.getService(InvitationInterface::class.java)
            .acceptInitiate(
                InvitationAcceptReq(
                    channelName,
                    userId)
            )
            .enqueue(object : Callback<CommonResp<Any>>{
                override fun onResponse(
                    call: Call<CommonResp<Any>>,
                    response: Response<CommonResp<Any>>
                ) {
                    if (response.body()?.code == 0 && response.body()?.message == "Success"){
                        ThreadManager.getInstance().runOnMainThread {
                            callback?.onResult(null)
                        }
                    }
                }

                override fun onFailure(call: Call<CommonResp<Any>>, t: Throwable) {
                    callback?.onResult(AUIException(-1, t.message))
                }
            })
    }

    override fun rejectInvitation(userId: String, callback: AUICallback?) {
        HttpManager.getService(InvitationInterface::class.java)
            .acceptCancel(
                RejectInvitationAccept(
                    channelName,
                    userId,
                    ""
                )
            )
            .enqueue(object : Callback<CommonResp<Any>>{
                override fun onResponse(
                    call: Call<CommonResp<Any>>,
                    response: Response<CommonResp<Any>>
                ) {
                    if (response.body()?.code == 0 && response.body()?.message == "Success"){
                        ThreadManager.getInstance().runOnMainThread {
                            callback?.onResult(null)
                        }
                    }
                }

                override fun onFailure(call: Call<CommonResp<Any>>, t: Throwable) {
                    callback?.onResult(AUIException(-1, t.message))
                }
            })
    }

    override fun cancelInvitation(userId: String, callback: AUICallback?) {
        HttpManager.getService(InvitationInterface::class.java)
            .acceptCancel(
                RejectInvitationAccept(
                    channelName,
                    roomContext.currentUserInfo.userId,
                    userId
                )
            )
            .enqueue(object : Callback<CommonResp<Any>>{
                override fun onResponse(
                    call: Call<CommonResp<Any>>,
                    response: Response<CommonResp<Any>>
                ) {
                    if (response.body()?.code == 0 && response.body()?.message == "Success"){
                        ThreadManager.getInstance().runOnMainThread {
                            callback?.onResult(null)
                        }
                    }
                }

                override fun onFailure(call: Call<CommonResp<Any>>, t: Throwable) {
                    callback?.onResult(AUIException(-1, t.message))
                }
            })
    }

    override fun sendApply(seatIndex: Int, callback: AUICallback) {
        HttpManager.getService(ApplyInterface::class.java)
            .applyCreate(
                ApplyCreateReq(
                    channelName,
                    roomContext.currentUserInfo.userId,
                    InvitationPayload("",seatIndex))
            )
            .enqueue(object : Callback<CommonResp<Any>>{
                override fun onResponse(
                    call: Call<CommonResp<Any>>,
                    response: Response<CommonResp<Any>>
                ) {
                    if (response.body()?.code == 0 && response.body()?.message == "Success"){
                        ThreadManager.getInstance().runOnMainThread{
                            callback.onResult(null)
                        }
                    }
                }

                override fun onFailure(call: Call<CommonResp<Any>>, t: Throwable) {
                    callback.onResult(AUIException(-1, t.message))
                }
            })
    }

    override fun cancelApply(callback: AUICallback) {
        HttpManager.getService(ApplyInterface::class.java)
            .applyCancel(
                ApplyCancelReq(
                    channelName,
                    roomContext.currentUserInfo.userId,
                    roomContext.currentUserInfo.userId)
            )
            .enqueue(object : Callback<CommonResp<Any>>{
                override fun onResponse(
                    call: Call<CommonResp<Any>>,
                    response: Response<CommonResp<Any>>
                ) {
                    if (response.body()?.code == 0 && response.body()?.message == "Success"){
                        ThreadManager.getInstance().runOnMainThread{
                            callback.onResult(null)
                        }
                    }
                }

                override fun onFailure(call: Call<CommonResp<Any>>, t: Throwable) {
                    callback.onResult(AUIException(-1, t.message))
                }
            })
    }

    override fun acceptApply(userId: String, seatIndex: Int, callback: AUICallback) {
        HttpManager.getService(ApplyInterface::class.java)
            .applyAccept(
                ApplyAcceptReq(
                    channelName,
                    roomContext.currentUserInfo.userId,
                    userId)
            )
            .enqueue(object : Callback<CommonResp<Any>>{
                override fun onResponse(
                    call: Call<CommonResp<Any>>,
                    response: Response<CommonResp<Any>>
                ) {
                    if (response.body()?.code == 0 && response.body()?.message == "Success"){
                        ThreadManager.getInstance().runOnMainThread {
                            callback.onResult(null)
                        }
                    }
                }

                override fun onFailure(call: Call<CommonResp<Any>>, t: Throwable) {
                    callback.onResult(AUIException(-1, t.message))
                }
            })
    }

    override fun rejectApply(userId: String, callback: AUICallback) {
        HttpManager.getService(ApplyInterface::class.java)
            .applyCancel(
                ApplyCancelReq(
                    channelName,
                    roomContext.currentUserInfo.userId,
                    userId)
            )
            .enqueue(object : Callback<CommonResp<Any>>{
                override fun onResponse(
                    call: Call<CommonResp<Any>>,
                    response: Response<CommonResp<Any>>
                ) {
                    if (response.body()?.code == 0 && response.body()?.message == "Success"){
                        ThreadManager.getInstance().runOnMainThread {
                            callback.onResult(null)
                        }
                    }
                }

                override fun onFailure(call: Call<CommonResp<Any>>, t: Throwable) {
                    callback.onResult(AUIException(-1, t.message))
                }
            })
    }


    override fun getChannelName() = channelName

    override fun getRoomContext() = roomContext

    override fun onMsgDidChanged(channelName: String, key: String, value: Any) {
        Log.e("apex","AUiServiceImpl key: $key")
        if (key == RoomApplyKey){ //申请
            observableHelper.notifyEventHandlers {
                val list = paresData(value)
                if (list.size > 0){
                    it.onApplyListUpdate(paresData(value))
                }
            }
        }else if (key == RoomInvitationKey){//邀请
            observableHelper.notifyEventHandlers {
                val list = paresData(value)
                if (list.size > 0 && list[list.lastIndex].userId == roomContext.currentUserInfo.userId){
                    it.onReceiveInvitation(list[list.lastIndex].userId,list[list.lastIndex].micIndex)
                }
            }
        }
    }

    private fun paresData(value: Any):ArrayList<AUIUserInfo>{
        val userList = ArrayList<AUIUserInfo>()
        val map: Map<String, Any> = HashMap()
        val micSeat = GsonTools.toBean(value as String, map.javaClass)
        val s = micSeat?.get("micSeat") as Map<String,Any>
        val queue = s["queue"] as ArrayList<Map<String,Any>>
        queue.forEach {
            val payload = it["payload"] as Map<String,Any>
            val seatNo = payload["seatNo"] as Long
            val applyBean = AUIUserInfo()
            applyBean.userId = it["userId"].toString()
            applyBean.micIndex = seatNo.toInt()
            Log.d("apex","${it["userId"]} - ${seatNo.toInt()} -- $it")
            userList.add(applyBean)
        }
        return userList
    }
}
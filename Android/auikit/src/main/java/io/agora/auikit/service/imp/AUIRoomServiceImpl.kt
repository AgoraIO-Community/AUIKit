package io.agora.auikit.service.imp

import io.agora.auikit.model.AUICommonConfig
import io.agora.auikit.model.AUICreateRoomInfo
import io.agora.auikit.model.AUIRoomContext
import io.agora.auikit.model.AUIRoomInfo
import io.agora.auikit.service.IAUIRoomManager
import io.agora.auikit.service.IAUIRoomManager.AUIRoomManagerRespDelegate
import io.agora.auikit.service.callback.AUICallback
import io.agora.auikit.service.callback.AUICreateRoomCallback
import io.agora.auikit.service.callback.AUIException
import io.agora.auikit.service.callback.AUIRoomListCallback
import io.agora.auikit.service.http.CommonResp
import io.agora.auikit.service.http.HttpManager
import io.agora.auikit.service.http.Utils
import io.agora.auikit.service.http.room.CreateRoomReq
import io.agora.auikit.service.http.room.CreateRoomResp
import io.agora.auikit.service.http.room.DestroyRoomResp
import io.agora.auikit.service.http.room.RoomInterface
import io.agora.auikit.service.http.room.RoomListReq
import io.agora.auikit.service.http.room.RoomListResp
import io.agora.auikit.service.http.room.RoomUserReq
import io.agora.auikit.service.rtm.AUIRtmManager
import io.agora.auikit.service.rtm.AUIRtmMsgProxyDelegate
import io.agora.auikit.utils.AUILogger
import io.agora.auikit.utils.AgoraEngineCreator
import io.agora.auikit.utils.DelegateHelper
import io.agora.auikit.utils.MapperUtils
import io.agora.rtm.RtmClient
import retrofit2.Call
import retrofit2.Response

private const val kRoomAttrKey = "room"
class AUIRoomManagerImpl(
    private val commonConfig: AUICommonConfig,
    private val rtmClient: RtmClient? = null
) : IAUIRoomManager, AUIRtmMsgProxyDelegate {

    val rtmManager by lazy {
        val rtm = rtmClient ?: AgoraEngineCreator.createRtmClient(
            commonConfig.context,
            commonConfig.appId,
            commonConfig.userId
        )
        AUIRtmManager(commonConfig.context, rtm)
    }

    private val TAG = "AUiRoomManagerImpl"

    private val delegateHelper = DelegateHelper<AUIRoomManagerRespDelegate>()

    private var mChannelName: String? = null
    protected fun finalize() {
        rtmManager.logout()
    }
    init {
        AUIRoomContext.shared().commonConfig = commonConfig
    }
    override fun bindRespDelegate(delegate: AUIRoomManagerRespDelegate?) {
        delegateHelper.bindDelegate(delegate)
    }

    override fun unbindRespDelegate(delegate: AUIRoomManagerRespDelegate?) {
        delegateHelper.unBindDelegate(delegate)
    }

    override fun createRoom(
        createRoomInfo: AUICreateRoomInfo,
        callback: AUICreateRoomCallback?
    ) {
        val micSeatCount = 8
        HttpManager.getService(RoomInterface::class.java)
            .createRoom(CreateRoomReq(createRoomInfo.roomName,
                roomContext.currentUserInfo.userId,
                roomContext.currentUserInfo.userName,
                roomContext.currentUserInfo.userAvatar,
                micSeatCount))
            .enqueue(object : retrofit2.Callback<CommonResp<CreateRoomResp>> {
                override fun onResponse(call: Call<CommonResp<CreateRoomResp>>, response: Response<CommonResp<CreateRoomResp>>) {
                    val rsp = response.body()?.data
                    if (response.body()?.code == 0 && rsp != null) {
                        val info = AUIRoomInfo().apply {
                            this.roomId = rsp.roomId
                            this.roomName = rsp.roomName
                            this.roomOwner = roomContext.currentUserInfo
                            this.seatCount = micSeatCount
                        }
                        roomContext.insertRoomInfo(info)
                        // success
                        callback?.onResult(null, info)
                    } else {
                        callback?.onResult(Utils.errorFromResponse(response), null)
                    }
                }
                override fun onFailure(call: Call<CommonResp<CreateRoomResp>>, t: Throwable) {
                    callback?.onResult(
                        AUIException(
                            -1,
                            t.message
                        ), null)
                }
            })
    }

    override fun destroyRoom(roomId: String, callback: AUICallback?) {
        rtmManager.unSubscribe(roomId)
        HttpManager.getService(RoomInterface::class.java)
            .destroyRoom(RoomUserReq(roomId, roomContext.currentUserInfo.userId))
            .enqueue(object : retrofit2.Callback<CommonResp<DestroyRoomResp>> {
                override fun onResponse(call: Call<CommonResp<DestroyRoomResp>>, response: Response<CommonResp<DestroyRoomResp>>) {
                    if (response.code() == 200) {
                        // success
                        callback?.onResult(null)
                    } else {
                        callback?.onResult(Utils.errorFromResponse(response))
                    }
                }
                override fun onFailure(call: Call<CommonResp<DestroyRoomResp>>, t: Throwable) {
                    callback?.onResult(
                        AUIException(
                            -1,
                            t.message
                        )
                    )
                }
            })
    }
    override fun enterRoom(roomId: String, token: String, callback: AUICallback?) {
        val user = MapperUtils.model2Map(roomContext.currentUserInfo) as? Map<String, String>
        if (user == null) {
            AUILogger.logger().d("EnterRoom", "user == null")
            callback?.onResult(
                AUIException(
                    -1,
                    ""
                )
            )
            return
        }
        val rtmToken = AUIRoomContext.shared().roomConfig.rtmToken007
        rtmManager.login(rtmToken) { error ->
            if (error != null) {
                callback?.onResult(
                    AUIException(
                        error.code,
                        error.message
                    )
                )
            } else {
                AUILogger.logger().d("EnterRoom", "subscribe room roomId=$roomId token=$token")
                rtmManager.subscribe(roomId, token) { subscribeError ->
                    AUILogger.logger().d("EnterRoom", "subscribe room result : $subscribeError")
                    if (subscribeError != null) {
                        callback?.onResult(
                            AUIException(
                                subscribeError.code,
                                subscribeError.message
                            )
                        )
                    } else {
                        mChannelName = roomId
                        rtmManager.subscribeMsg(roomId, "", this)
                        callback?.onResult(null)
                    }
                }
            }
        }
    }
    override fun exitRoom(roomId: String, callback: AUICallback?) {
        rtmManager.unSubscribe(roomId)
        callback?.onResult(null)
        HttpManager.getService(RoomInterface::class.java)
            .leaveRoom(RoomUserReq(roomId, roomContext.currentUserInfo.userId))
            .enqueue(object : retrofit2.Callback<CommonResp<String>> {
                override fun onResponse(call: Call<CommonResp<String>>, response: Response<CommonResp<String>>) {
                }
                override fun onFailure(call: Call<CommonResp<String>>, t: Throwable) {
                }
            })
    }
    override fun getRoomInfoList(lastCreateTime: Long?, pageSize: Int, callback: AUIRoomListCallback?) {
        HttpManager.getService(RoomInterface::class.java)
            .fetchRoomList(RoomListReq(pageSize, lastCreateTime))
            .enqueue(object : retrofit2.Callback<CommonResp<RoomListResp>> {
                override fun onResponse(call: Call<CommonResp<RoomListResp>>, response: Response<CommonResp<RoomListResp>>) {
                    val roomList = response.body()?.data?.list
                    if (roomList != null) {
                        roomContext.resetRoomMap(roomList)
                        callback?.onResult(null, roomList)
                    } else {
                        callback?.onResult(Utils.errorFromResponse(response), null)
                    }
                }

                override fun onFailure(call: Call<CommonResp<RoomListResp>>, t: Throwable) {
                    callback?.onResult(
                        AUIException(
                            -1,
                            t.message
                        ), null)
                }
            })
    }
    override fun getChannelName() = mChannelName ?: ""
    override fun onMsgDidChanged(channelName: String, key: String, value: Any) {
        if (key != kRoomAttrKey) {
            return
        }
    }

    override fun onMsgRecvEmpty(channelName: String) {
        delegateHelper.notifyDelegate {
            it.onRoomDestroy(channelName)
        }
    }

}
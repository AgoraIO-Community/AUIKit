package io.agora.auikit.service.imp

import io.agora.auikit.model.AUICommonConfig
import io.agora.auikit.model.AUICreateRoomInfo
import io.agora.auikit.model.AUIRoomContext
import io.agora.auikit.model.AUIRoomInfo
import io.agora.auikit.service.IAUIRoomManager
import io.agora.auikit.service.IAUIRoomManager.AUIRoomManagerRespObserver
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
import io.agora.auikit.service.http.user.KickUserReq
import io.agora.auikit.service.http.user.KickUserRsp
import io.agora.auikit.service.http.user.UserInterface
import io.agora.auikit.service.rtm.AUIRtmErrorRespObserver
import io.agora.auikit.service.rtm.AUIRtmManager
import io.agora.auikit.service.rtm.AUIRtmMsgRespObserver
import io.agora.auikit.utils.AUILogger
import io.agora.auikit.utils.AgoraEngineCreator
import io.agora.auikit.utils.MapperUtils
import io.agora.auikit.utils.ObservableHelper
import io.agora.auikit.utils.ThreadManager
import io.agora.rtm2.RtmClient
import io.agora.rtm2.RtmConstants
import retrofit2.Call
import retrofit2.Response
import java.util.concurrent.atomic.AtomicBoolean

private const val kRoomAttrKey = "room"
class AUIRoomManagerImplRespResp(
    private val commonConfig: AUICommonConfig,
    private val rtmClient: RtmClient? = null,
) : IAUIRoomManager, AUIRtmMsgRespObserver, AUIRtmErrorRespObserver {

    private val subChannelMsg = AtomicBoolean(false)
    private val subChannelStream = AtomicBoolean(false)

    val rtmManager by lazy {
        val rtm = rtmClient ?: AgoraEngineCreator.createRtmClient(
            commonConfig.context,
            AUIRoomContext.shared().appId,
            commonConfig.userId
        )
        AUIRtmManager(commonConfig.context, rtm)
    }

    private val TAG = "AUiRoomManagerImpl"

    private val observableHelper =
        ObservableHelper<AUIRoomManagerRespObserver>()

    private var mChannelName: String? = null

    init {
        AUIRoomContext.shared().commonConfig = commonConfig
    }
    override fun registerRespObserver(observer: AUIRoomManagerRespObserver?) {
        observableHelper.subscribeEvent(observer)
    }

    override fun unRegisterRespObserver(observer: AUIRoomManagerRespObserver?) {
        observableHelper.unSubscribeEvent(observer)
    }

    override fun createRoom(
        createRoomInfo: AUICreateRoomInfo,
        callback: AUICreateRoomCallback?
    ) {
        HttpManager.getService(RoomInterface::class.java)
            .createRoom(CreateRoomReq(createRoomInfo.roomName,
                roomContext.currentUserInfo.userId,
                roomContext.currentUserInfo.userName,
                roomContext.currentUserInfo.userAvatar,
                createRoomInfo.micSeatCount,
                createRoomInfo.micSeatStyle
            ))
            .enqueue(object : retrofit2.Callback<CommonResp<CreateRoomResp>> {
                override fun onResponse(call: Call<CommonResp<CreateRoomResp>>, response: Response<CommonResp<CreateRoomResp>>) {
                    val rsp = response.body()?.data
                    if (response.body()?.code == 0 && rsp != null) {
                        val info = AUIRoomInfo().apply {
                            this.roomId = rsp.roomId
                            this.roomName = rsp.roomName
                            this.roomOwner = roomContext.currentUserInfo
                            this.micSeatCount = createRoomInfo.micSeatCount
                            this.micSeatStyle = createRoomInfo.micSeatStyle
                        }
                        roomContext.insertRoomInfo(info)
                        // success
                        callback?.onResult(null, info)
                    } else {
                        callback?.onResult(Utils.errorFromResponse(response), null)
                    }
                }
                override fun onFailure(call: Call<CommonResp<CreateRoomResp>>, t: Throwable) {
                    callback?.onResult(AUIException(-1, t.message), null)
                }
            })
    }

    override fun destroyRoom(roomId: String, callback: AUICallback?) {
        rtmManager.unSubscribe(RtmConstants.RtmChannelType.STREAM,roomId)
        rtmManager.unSubscribe(RtmConstants.RtmChannelType.MESSAGE,roomId)
        rtmManager.unsubscribeMsg(roomId,"",this)
        rtmManager.proxy.unRegisterErrorRespObserver(this)
        rtmManager.logout()
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
        mChannelName = roomId
        subChannelStream.set(false)
        subChannelMsg.set(false)
        val user = MapperUtils.model2Map(roomContext.currentUserInfo) as? Map<String, Any>
        if (user == null) {
            AUILogger.logger().d(TAG, "EnterRoom user == null")
            callback?.onResult(
                AUIException(
                    -1,
                    ""
                )
            )
            return
        }
        val rtmToken = AUIRoomContext.shared().roomConfigMap[roomId]?.rtmToken ?: ""
        AUILogger.logger().d(TAG, "EnterRoom rtmManager login start ...")
        rtmManager.login(rtmToken) { error ->
            if (error != null) {
                AUILogger.logger().d(TAG, "EnterRoom rtmManager login fail ${error.code} ${error.message}")
                callback?.onResult(
                    AUIException(
                        error.code,
                        error.message
                    )
                )
            } else {
                AUILogger.logger().d(TAG, "EnterRoom rtmManager login success")
                AUILogger.logger().d(TAG, "EnterRoom subscribeMsg RtmChannelType.MESSAGE room roomId=$roomId token=$token")
                rtmManager.proxy.registerErrorRespObserver(this)
                rtmManager.subscribeMsg(roomId, "", this)
                rtmManager.subscribe(RtmConstants.RtmChannelType.MESSAGE,roomId, token) { subscribeError ->
                    if (subscribeError != null) {
                        AUILogger.logger().d(TAG, "EnterRoom subscribeMsg RtmChannelType.MESSAGE fail ${subscribeError.code} ${subscribeError.message}")
                        callback?.onResult(
                            AUIException(
                                subscribeError.code,
                                subscribeError.message
                            )
                        )
                    }else{
                        AUILogger.logger().d(TAG, "EnterRoom subscribeMsg RtmChannelType.MESSAGE success")
                        subChannelMsg.set(true)
                        checkSubChannel(roomId,callback)
                    }
                }

                AUILogger.logger().d(TAG, "EnterRoom subscribeMsg RtmChannelType.STREAM room roomId=$roomId token=$token")
                rtmManager.subscribe(RtmConstants.RtmChannelType.STREAM,roomId, token) { subscribeError ->
                    if (subscribeError != null) {
                        AUILogger.logger().d(TAG, "EnterRoom subscribeMsg RtmChannelType.STREAM fail ${subscribeError.code} ${subscribeError.message}")
                        callback?.onResult(
                            AUIException(
                                subscribeError.code,
                                subscribeError.message
                            )
                        )
                    } else {
                        AUILogger.logger().d(TAG, "EnterRoom subscribeMsg RtmChannelType.STREAM RtmChannelType.MESSAGE success")
                        subChannelStream.set(true)
                        checkSubChannel(roomId,callback)
                    }
                }
            }
        }
    }

    private fun checkSubChannel(roomId:String,callback: AUICallback?){
        if (subChannelMsg.get()  && subChannelStream.get()){
            ThreadManager.getInstance().runOnMainThread {
                callback?.onResult(null)
            }
        }
    }

    override fun exitRoom(roomId: String, callback: AUICallback?) {
        rtmManager.unSubscribe(RtmConstants.RtmChannelType.STREAM,roomId)
        rtmManager.unSubscribe(RtmConstants.RtmChannelType.MESSAGE,roomId)
        rtmManager.unsubscribeMsg(roomId,"",this)
        rtmManager.logout()
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

    override fun updateAnnouncementInfo(roomId: String?, content: String?, callback: AUICallback?) {

    }

    override fun kickUser(roomId: String, userId: Int, callback: AUICallback?) {
        HttpManager.getService(UserInterface::class.java)
            .kickOut(
                KickUserReq(
                    roomContext.currentUserInfo.userId,
                    roomId,
                    userId
                )
            )
            .enqueue(object : retrofit2.Callback<CommonResp<KickUserRsp>> {
                override fun onResponse(call: Call<CommonResp<KickUserRsp>>, response: Response<CommonResp<KickUserRsp>>) {
                    val rsp = response.body()?.data
                    if (response.body()?.code == 0 && rsp != null) {
                        ThreadManager.getInstance().runOnMainThread{
                            callback?.onResult(null)
                        }
                    } else {
                        ThreadManager.getInstance().runOnMainThread{
                            callback?.onResult(Utils.errorFromResponse(response))
                        }
                    }
                }
                override fun onFailure(call: Call<CommonResp<KickUserRsp>>, t: Throwable) {
                    ThreadManager.getInstance().runOnMainThread{
                        callback?.onResult(AUIException(-1, t.message))
                    }
                }
            })
    }

    override fun getChannelName() = mChannelName ?: ""

    override fun onMsgDidChanged(channelName: String, key: String, value: Any) {
        if (key != kRoomAttrKey) {
            return
        }
    }

    override fun onTokenPrivilegeWillExpire(channelName: String?) {

    }

    override fun onConnectionStateChanged(channelName: String?, state: Int, reason: Int) {
        if (state == 5 && reason == 3){
            observableHelper.notifyEventHandlers {
                it.onRoomUserBeKicked(channelName,AUIRoomContext.shared().currentUserInfo.userId)
            }
        }
    }

    override fun onMsgReceiveEmpty(channelName: String) {
        if (channelName == getChannelName()) {
            observableHelper.notifyEventHandlers {
                it.onRoomDestroy(channelName)
            }
        }
    }

}
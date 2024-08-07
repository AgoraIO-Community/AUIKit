package io.agora.auikit.service.room

import io.agora.auikit.model.AUIRoomContext
import io.agora.auikit.model.AUIRoomInfo
import io.agora.auikit.service.callback.AUICallback
import io.agora.auikit.service.callback.AUIException
import io.agora.auikit.service.callback.AUIRoomCallback
import io.agora.auikit.service.callback.AUIRoomListCallback
import io.agora.auikit.service.http.CommonResp
import io.agora.auikit.service.http.HttpManager
import io.agora.auikit.service.http.Utils
import io.agora.auikit.service.http.room.CreateRoomReq
import io.agora.auikit.service.http.room.CreateRoomResp
import io.agora.auikit.service.http.room.DestroyRoomResp
import io.agora.auikit.service.http.room.QueryRoomReq
import io.agora.auikit.service.http.room.QueryRoomResp
import io.agora.auikit.service.http.room.RoomInterface
import io.agora.auikit.service.http.room.RoomListReq
import io.agora.auikit.service.http.room.RoomListResp
import io.agora.auikit.service.http.room.RoomUserReq
import io.agora.auikit.service.http.room.UpdateRoomReq
import retrofit2.Call
import retrofit2.Response

class AUIRoomManager(
    private val appId: String = AUIRoomContext.shared().mCommonConfig?.appId ?: "",
    private val sceneId: String
) {

    private val roomInterface by lazy {
        HttpManager.getService(RoomInterface::class.java)
    }

    fun createRoom(
        roomInfo: AUIRoomInfo,
        callback: AUIRoomCallback?
    ) {
        val roomId = roomInfo.roomId
        roomInterface.createRoom(CreateRoomReq(
            appId,
            sceneId,
            roomId,
            roomInfo
        )).enqueue(object : retrofit2.Callback<CommonResp<CreateRoomResp>> {
                override fun onResponse(
                    call: Call<CommonResp<CreateRoomResp>>,
                    response: Response<CommonResp<CreateRoomResp>>
                ) {
                    val rsp = response.body()?.data
                    if (response.body()?.code == 0 && rsp != null) {
                        AUIRoomContext.shared().insertRoomInfo(rsp.payload)
                        // success
                        callback?.onResult(null, rsp.payload)
                    } else {
                        callback?.onResult(Utils.errorFromResponse(response), null)
                    }
                }

                override fun onFailure(call: Call<CommonResp<CreateRoomResp>>, t: Throwable) {
                    callback?.onResult(AUIException(-1, t.message), null)
                }
            })
    }

    fun destroyRoom(
        roomId: String,
        callback: AUICallback?
    ) {
        roomInterface.destroyRoom(RoomUserReq(appId, sceneId, roomId))
            .enqueue(object : retrofit2.Callback<CommonResp<DestroyRoomResp>> {
                override fun onResponse(
                    call: Call<CommonResp<DestroyRoomResp>>,
                    response: Response<CommonResp<DestroyRoomResp>>
                ) {
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

    fun getRoomInfoList(
        lastCreateTime: Long?,
        pageSize: Int,
        callback: AUIRoomListCallback?
    ) {
        roomInterface.fetchRoomList(RoomListReq(appId, sceneId, pageSize, lastCreateTime))
            .enqueue(object : retrofit2.Callback<CommonResp<RoomListResp>> {
                override fun onResponse(
                    call: Call<CommonResp<RoomListResp>>,
                    response: Response<CommonResp<RoomListResp>>
                ) {
                    val roomList = response.body()?.data?.getRoomList()
                    if (roomList != null) {
                        AUIRoomContext.shared().resetRoomMap(roomList)
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
                        ), null
                    )
                }
            })
    }

    fun getRoomInfo(
        roomId: String,
        callback: AUIRoomCallback?
    ) {
        roomInterface.queryRoomInfo(QueryRoomReq(appId, sceneId, roomId))
            .enqueue(object : retrofit2.Callback<CommonResp<QueryRoomResp>> {
                override fun onResponse(
                    call: Call<CommonResp<QueryRoomResp>>,
                    response: Response<CommonResp<QueryRoomResp>>
                ) {
                    val rsp = response.body()?.data
                    if (response.body()?.code == 0 && rsp != null) {
                        AUIRoomContext.shared().insertRoomInfo(rsp.payload)
                        // success
                        callback?.onResult(null, rsp.payload)
                    } else {
                        callback?.onResult(Utils.errorFromResponse(response), null)
                    }
                }

                override fun onFailure(call: Call<CommonResp<QueryRoomResp>>, t: Throwable) {
                    callback?.onResult(AUIException(-1, t.message), null)
                }
            })
    }

    fun updateRoomInfo(
        roomInfo: AUIRoomInfo,
        callback: AUIRoomCallback?
    ) {
        roomInterface.updateRoomInfo(UpdateRoomReq(appId, sceneId, roomInfo.roomId, roomInfo))
            .enqueue(object : retrofit2.Callback<CommonResp<String>> {
                override fun onResponse(
                    call: Call<CommonResp<String>>,
                    response: Response<CommonResp<String>>
                ) {
                    val rsp = response.body()?.data
                    if (response.body()?.code == 0 && rsp != null) {
                        // success
                        callback?.onResult(null, roomInfo)
                    } else {
                        callback?.onResult(Utils.errorFromResponse(response), null)
                    }
                }

                override fun onFailure(call: Call<CommonResp<String>>, t: Throwable) {
                    callback?.onResult(AUIException(-1, t.message), null)
                }
            })
    }
}
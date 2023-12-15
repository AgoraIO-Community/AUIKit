package io.agora.auikit.service.imp

import io.agora.CallBack
import io.agora.auikit.model.AUIRoomContext
import io.agora.auikit.model.AgoraChatMessage
import io.agora.auikit.service.IAUIIMManagerService
import io.agora.auikit.service.callback.AUICallback
import io.agora.auikit.service.callback.AUIChatMsgCallback
import io.agora.auikit.service.callback.AUICreateChatRoomCallback
import io.agora.auikit.service.callback.AUIException
import io.agora.auikit.service.http.CommonResp
import io.agora.auikit.service.http.HttpManager
import io.agora.auikit.service.http.Utils
import io.agora.auikit.service.http.room.CreateChatRoomReq
import io.agora.auikit.service.http.room.CreateChatRoomRsp
import io.agora.auikit.service.http.room.RoomInterface
import io.agora.auikit.service.im.AUIChatEventHandler
import io.agora.auikit.service.im.AUIChatManager
import io.agora.auikit.service.rtm.AUIRtmAttributeRespObserver
import io.agora.auikit.service.rtm.AUIRtmManager
import io.agora.auikit.utils.AUILogger
import io.agora.auikit.utils.AgoraEngineCreator
import io.agora.auikit.utils.ObservableHelper
import io.agora.auikit.utils.ThreadManager
import io.agora.chat.ChatMessage
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response

private const val chatRoomIdKey = "chatRoom"

class AUIIMManagerServiceImpl constructor(
    private val channelName: String,
    private val rtmManager: AUIRtmManager,
    private val chatManager: AUIChatManager
) : IAUIIMManagerService, AUIRtmAttributeRespObserver, AUIChatEventHandler {
    private val roomContext = AUIRoomContext.shared()
    private val mChatRoomIdMap = mutableMapOf<String, String?>()
    private val observableHelper =
        ObservableHelper<IAUIIMManagerService.AUIIMManagerRespObserver>()

    init {
        rtmManager.subscribeAttribute(channelName, chatRoomIdKey, this)

        if (roomContext.isRoomOwner(channelName)) {
            initChatRoom()
        }
    }

    private fun initChatRoom(chatRoomId: String? = null) {
        configIM { configError ->
            if (configError != null) {
                AUILogger.logger().e(message = "IM initialize failed! -- $configError")
                return@configIM
            }

            AgoraEngineCreator.createChatClient(
                roomContext.requireCommonConfig().context,
                chatManager.getAppKey()
            )
            chatManager.initManager()

            login { loginError ->
                if (loginError != null) {
                    AUILogger.logger().e(message = "IM login failed! -- $loginError")
                    return@login
                }
                val userName = chatManager.userName
                if (userName == null) {
                    AUILogger.logger().e(message = "IM create chat room  userName is null!")
                    return@login
                }

                chatManager.subscribeChatMsg(this)
                if(chatRoomId == null){
                    this.createChatRoom(channelName, userName, object : AUICreateChatRoomCallback {
                        override fun onResult(error: AUIException?, chatRoomId: String?) {
                            if (error != null) {
                                AUILogger.logger()
                                    .e(message = "IM create chat room failed! -- $error")
                                return
                            }
                            mChatRoomIdMap[channelName] = chatRoomId
                            chatManager.joinRoom(chatRoomId, object : AUIChatMsgCallback {
                                override fun onOriginalResult(
                                    error: AUIException?,
                                    message: ChatMessage?
                                ) {
                                    super.onOriginalResult(error, message)
                                    if (error != null) {
                                        AUILogger.logger()
                                            .e(message = "IM join chat room failed! -- $error")
                                        return
                                    }
                                    observableHelper.notifyEventHandlers {
                                        it.onUserDidJoinRoom(
                                            chatRoomId!!, IAUIIMManagerService.AgoraChatTextMessage(
                                                message?.msgId, message?.body?.toString(), null
                                            )
                                        )
                                    }
                                }
                            })
                        }
                    })
                }
                else {
                    chatManager.joinRoom(chatRoomId, object : AUIChatMsgCallback {
                        override fun onOriginalResult(
                            error: AUIException?,
                            message: ChatMessage?
                        ) {
                            super.onOriginalResult(error, message)
                            if (error != null) {
                                AUILogger.logger()
                                    .e(message = "IM join chat room failed! -- $error")
                                return
                            }
                            observableHelper.notifyEventHandlers {
                                it.onUserDidJoinRoom(
                                    chatRoomId, IAUIIMManagerService.AgoraChatTextMessage(
                                        message?.msgId, message?.body?.toString(), null
                                    )
                                )
                            }
                        }
                    })
                }

            }
        }
    }


    override fun registerRespObserver(observer: IAUIIMManagerService.AUIIMManagerRespObserver?) {
        observableHelper.subscribeEvent(observer)
    }

    override fun unRegisterRespObserver(observer: IAUIIMManagerService.AUIIMManagerRespObserver?) {
        observableHelper.unSubscribeEvent(observer)
    }

    override fun getRoomContext(): AUIRoomContext = roomContext

    override fun getChannelName() = channelName

    override fun sendMessage(
        roomId: String,
        text: String,
        completion: (IAUIIMManagerService.AgoraChatTextMessage?, AUIException?) -> Unit
    ) {
        chatManager.sendTxtMsg(
            mChatRoomIdMap[roomId],
            text,
            roomContext.currentUserInfo,
            object : AUIChatMsgCallback {
                override fun onResult(error: AUIException?, message: AgoraChatMessage?) {
                    super.onResult(error, message)
                    if (error != null) {
                        completion.invoke(null, error)
                        return
                    }

                    completion.invoke(
                        IAUIIMManagerService.AgoraChatTextMessage(
                            message?.messageId,
                            message?.content, roomContext.currentUserInfo
                        ), null
                    )
                }
            })
    }

    override fun userQuitRoom(completion: ((error: AUIException?) -> Unit)?) {
        rtmManager.unsubscribeAttribute(channelName, chatRoomIdKey, this)
        chatManager.leaveChatRoom()
        chatManager.logoutChat()
        chatManager.unsubscribeChatMsg(this)
        chatManager.clear()
        mChatRoomIdMap.clear()
        completion?.invoke(null)
    }

    override fun userDestroyedChatroom() {
        rtmManager.unsubscribeAttribute(channelName, chatRoomIdKey, this)
        chatManager.asyncDestroyChatRoom(object : CallBack {
            override fun onSuccess() {

            }

            override fun onError(code: Int, error: String?) {
                AUILogger.logger()
                    .e(message = "userDestroyedChatroom error -- code=$code, message=$error")
            }
        })
        chatManager.logoutChat()
        chatManager.unsubscribeChatMsg(this)
        chatManager.clear()
        mChatRoomIdMap.clear()
    }

    private fun configIM(completion: (AUIException?) -> Unit) {
        chatManager.getChatUser(roomContext.currentUserInfo.userId, object : AUICallback {
            override fun onResult(error: AUIException?) {
                completion.invoke(error)
            }
        })
    }

    private fun login(completion: (AUIException?) -> Unit) {
        chatManager.loginChat(object : CallBack {
            override fun onSuccess() {
                completion.invoke(null)
            }

            override fun onError(code: Int, error: String?) {
                completion.invoke(AUIException(code, error))
            }
        })
    }

    private fun createChatRoom(
        roomId: String,
        userName: String,
        callback: AUICreateChatRoomCallback
    ) {
        HttpManager.getService(RoomInterface::class.java)
            .createChatRoom(
                CreateChatRoomReq(
                    roomId,
                    roomContext.currentUserInfo.userId,
                    userName,
                    description = "",
                    custom = ""
                )
            )
            .enqueue(object : retrofit2.Callback<CommonResp<CreateChatRoomRsp>> {
                override fun onResponse(
                    call: Call<CommonResp<CreateChatRoomRsp>>,
                    response: Response<CommonResp<CreateChatRoomRsp>>
                ) {
                    val rsp = response.body()?.data
                    if (response.body()?.code == 0 && rsp != null) {
                        val chatRoomId = rsp.chatRoomId
                        // success
                        chatManager.setChatRoom(chatRoomId)
                        ThreadManager.getInstance().runOnMainThread {
                            callback.onResult(null, chatRoomId)
                        }
                    } else {
                        callback.onResult(Utils.errorFromResponse(response), null)
                    }
                }

                override fun onFailure(call: Call<CommonResp<CreateChatRoomRsp>>, t: Throwable) {
                    callback.onResult(AUIException(-1, t.message), "")
                }
            })
    }

    override fun onAttributeChanged(channelName: String, key: String, value: Any) {
        // 解析数据 获取环信聊天室id
        if (key == chatRoomIdKey) {
            val room = JSONObject(value.toString())
            val chatroomId = room.getString("chatRoomId")
            if (!mChatRoomIdMap.containsKey(channelName)) {
                mChatRoomIdMap[channelName] = chatroomId
                if (!roomContext.isRoomOwner(channelName)) {
                    initChatRoom(chatroomId)
                }
            }
        }
    }

    override fun onReceiveMemberJoinedMsg(roomId: String?, message: AgoraChatMessage?) {
        super.onReceiveMemberJoinedMsg(roomId, message)
        message ?: return
        roomId ?: return
        observableHelper.notifyEventHandlers {
            it.onUserDidJoinRoom(
                roomId, IAUIIMManagerService.AgoraChatTextMessage(
                    message.messageId,
                    message.content,
                    message.user
                )
            )
        }
    }

    override fun onReceiveTextMsg(roomId: String?, message: AgoraChatMessage?) {
        super.onReceiveTextMsg(roomId, message)
        message ?: return
        roomId ?: return
        observableHelper.notifyEventHandlers {
            it.messageDidReceive(
                roomId,
                IAUIIMManagerService.AgoraChatTextMessage(
                    message.messageId,
                    message.content,
                    message.user
                )
            )
        }
    }
}
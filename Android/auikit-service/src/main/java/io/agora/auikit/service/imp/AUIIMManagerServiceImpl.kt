package io.agora.auikit.service.imp

import io.agora.CallBack
import io.agora.auikit.model.AUIRoomContext
import io.agora.auikit.model.AgoraChatMessage
import io.agora.auikit.service.IAUIIMManagerService
import io.agora.auikit.service.callback.AUICallback
import io.agora.auikit.service.callback.AUIChatMsgCallback
import io.agora.auikit.service.callback.AUIException
import io.agora.auikit.service.collection.AUIAttributesModel
import io.agora.auikit.service.collection.AUIMapCollection
import io.agora.auikit.service.http.CommonResp
import io.agora.auikit.service.http.HttpManager
import io.agora.auikit.service.http.chat.CHATROOM_CREATE_TYPE_USER
import io.agora.auikit.service.http.chat.CHATROOM_CREATE_TYPE_USER_ROOM
import io.agora.auikit.service.http.chat.ChatIMConfig
import io.agora.auikit.service.http.chat.ChatInterface
import io.agora.auikit.service.http.chat.ChatRoomConfig
import io.agora.auikit.service.http.chat.ChatUser
import io.agora.auikit.service.http.chat.CreateChatRoomResp
import io.agora.auikit.service.im.AUIChatEventHandler
import io.agora.auikit.service.im.AUIChatManager
import io.agora.auikit.service.rtm.AUIRtmManager
import io.agora.auikit.utils.AUILogger
import io.agora.auikit.utils.AgoraEngineCreator
import io.agora.auikit.utils.ObservableHelper
import io.agora.chat.ChatMessage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val kChatAttrKey = "chatRoom"
private const val kChatIdKey = "chatRoomId"

class AUIIMManagerServiceImpl constructor(
    private val channelName: String,
    private val rtmManager: AUIRtmManager,
    private val chatManager: AUIChatManager
) : IAUIIMManagerService, AUIChatEventHandler {
    private val roomContext = AUIRoomContext.shared()

    private val observableHelper = ObservableHelper<IAUIIMManagerService.AUIIMManagerRespObserver>()

    private val mapCollection = AUIMapCollection(channelName, kChatAttrKey, rtmManager)

    private var chatRoomId = ""
    private var chatUserId = ""
    private var chatUserToken = ""

    init {
        mapCollection.subscribeAttributesDidChanged(this::onAttributeChanged)
    }

    override fun serviceDidLoad() {
        super.serviceDidLoad()
        initChatRoom()
    }

    override fun deInitService(completion: AUICallback?) {
        super.deInitService(completion)

        mapCollection.cleanMetaData(completion)
        mapCollection.release()
    }

    private fun initChatRoom() {
        configIM { configError ->
            if (configError != null) {
                AUILogger.logger().e(message = "IM initialize failed! -- $configError")
                return@configIM
            }

            loginAndJoinChatRoom()


        }
    }

    private fun loginAndJoinChatRoom() {
        if (chatRoomId.isEmpty()) {
            AUILogger.logger().d(message = "loginAndJoinChatRoom >> chatRoomId is empty")
            return
        }
        login { loginError ->
            if (loginError != null) {
                AUILogger.logger().e(message = "loginAndJoinChatRoom >> IM login failed! -- $loginError")
                return@login
            }
            val userName = chatManager.userName
            if (userName == null) {
                AUILogger.logger().e(message = "loginAndJoinChatRoom >> IM login userName is null!")
                return@login
            }

            joinChatRoom(chatRoomId) { message, error ->
                AUILogger.logger()
                    .d(message = "loginAndJoinChatRoom >> joinChatRoom result: message=$message, error=$error")
            }
        }
    }

    private fun joinChatRoom(
        roomId: String?,
        callback: (message: IAUIIMManagerService.AgoraChatTextMessage?, error: AUIException?) -> Unit
    ) {
        chatManager.initManager()
        chatManager.subscribeChatMsg(this)
        chatManager.joinRoom(roomId, object : AUIChatMsgCallback {
            override fun onOriginalResult(
                error: AUIException?,
                message: ChatMessage?
            ) {
                super.onOriginalResult(error, message)
                if (error != null) {
                    AUILogger.logger()
                        .e(message = "IM join chat room failed! -- $error")
                    callback.invoke(
                        null,
                        AUIException(-1, "joinChatRoom >> IM join chat room failed! -- $error")
                    )
                    return
                }

                val textMsg = IAUIIMManagerService.AgoraChatTextMessage(
                    message?.msgId, message?.body?.toString(), null
                )
                observableHelper.notifyEventHandlers {
                    it.onUserDidJoinRoom(roomId!!, textMsg)
                }
                callback.invoke(textMsg, null)
            }
        })
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
            chatRoomId,
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
        mapCollection.subscribeAttributesDidChanged(null)
        chatManager.leaveChatRoom()
        chatManager.logoutChat()
        chatManager.unsubscribeChatMsg(this)
        chatManager.clear()
        chatRoomId = ""
        chatUserToken = ""
        chatUserId = ""
        completion?.invoke(null)
    }

    override fun userDestroyedChatroom() {
        mapCollection.subscribeAttributesDidChanged(null)
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
        chatRoomId = ""
        chatUserId = ""
        chatUserToken = ""
    }

    private fun configIM(completion: (AUIException?) -> Unit) {
        // request restful api to create user and room
        val isRoomOwner = roomContext.isRoomOwner(channelName)
        val commonConfig = roomContext.mCommonConfig ?: return
        HttpManager.getService(ChatInterface::class.java).createChatRoom(
            io.agora.auikit.service.http.chat.CreateChatRoomReq(
                appId = commonConfig.appId,
                type = if (isRoomOwner) CHATROOM_CREATE_TYPE_USER_ROOM else CHATROOM_CREATE_TYPE_USER,
                chatRoomConfig = if (isRoomOwner) ChatRoomConfig(channelName) else null,
                imConfig = ChatIMConfig(
                    commonConfig.imAppKey.split("#").getOrNull(0),
                    commonConfig.imAppKey.split("#").getOrNull(1),
                    commonConfig.imClientId,
                    commonConfig.imClientSecret
                ),
                user = ChatUser(roomContext.currentUserInfo.userId)
            )
        ).enqueue(object : Callback<CommonResp<CreateChatRoomResp>> {
            override fun onResponse(
                call: Call<CommonResp<CreateChatRoomResp>>,
                response: Response<CommonResp<CreateChatRoomResp>>
            ) {
                val resp = response.body()?.data
                if (resp == null) {
                    completion.invoke(AUIException(-1, "configIM >> createChatRoom resp null."))
                    return
                }
                resp.chatId?.let {
                    mapCollection.addMetaData(null, mapOf(Pair(kChatIdKey, it)), null) {}
                    chatRoomId = it
                }

                chatUserId = roomContext.currentUserInfo.userId ?: ""
                chatUserToken = resp.userToken ?: ""

                AgoraEngineCreator.createChatClient(
                    roomContext.requireCommonConfig().context,
                    resp.appKey
                )

                completion.invoke(null)
            }

            override fun onFailure(call: Call<CommonResp<CreateChatRoomResp>>, t: Throwable) {
                completion.invoke(AUIException(-1, t.message))
            }
        })
    }

    private fun login(completion: (AUIException?) -> Unit) {
        if(chatUserId.isEmpty() || chatUserToken.isEmpty()){
            AUILogger.logger().d(message = "login >> parameters are empty. chatUserId=$chatUserId, chatUserToken=$chatUserToken")
            return
        }
        chatManager.userName = chatUserId
        chatManager.accessToken = chatUserToken
        chatManager.loginChat(object : CallBack {
            override fun onSuccess() {
                completion.invoke(null)
            }

            override fun onError(code: Int, error: String?) {
                completion.invoke(AUIException(code, error))
            }
        })
    }

    private fun onAttributeChanged(channelName: String, key: String, value: AUIAttributesModel) {
        // 解析数据 获取环信聊天室id
        if (key == kChatAttrKey) {
            val attributes = value.getMap()
            chatRoomId = attributes?.get(kChatIdKey) as? String ?: ""

            if(!roomContext.isRoomOwner(channelName)){
                loginAndJoinChatRoom()
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
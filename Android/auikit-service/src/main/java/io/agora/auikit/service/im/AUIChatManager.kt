package io.agora.auikit.service.im

import android.text.TextUtils
import android.util.Log
import com.orhanobut.logger.Logger
import io.agora.CallBack
import io.agora.ChatRoomChangeListener
import io.agora.MessageListener
import io.agora.ValueCallBack
import io.agora.auikit.model.AUIChatEntity
import io.agora.auikit.model.AUICustomMsgType
import io.agora.auikit.model.AUIGiftEntity
import io.agora.auikit.model.AUIRoomContext
import io.agora.auikit.model.AUIUserThumbnailInfo
import io.agora.auikit.model.AgoraChatMessage
import io.agora.auikit.service.callback.AUICallback
import io.agora.auikit.service.callback.AUIChatMsgCallback
import io.agora.auikit.service.callback.AUIException
import io.agora.auikit.service.http.CommonResp
import io.agora.auikit.service.http.HttpManager
import io.agora.auikit.service.http.Utils
import io.agora.auikit.service.http.user.CreateUserReq
import io.agora.auikit.service.http.user.CreateUserRsp
import io.agora.auikit.service.http.user.UserInterface
import io.agora.auikit.utils.GsonTools
import io.agora.auikit.utils.ThreadManager
import io.agora.chat.ChatClient
import io.agora.chat.ChatMessage
import io.agora.chat.ChatRoom
import io.agora.chat.CustomMessageBody
import io.agora.chat.TextMessageBody
import io.agora.chat.adapter.EMAChatRoomManagerListener
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response

class AUIChatManager(
    channelName: String,
    roomContext:AUIRoomContext,
): MessageListener, ChatRoomChangeListener {
    private val chatEvnetHandlers = mutableListOf<AUIChatEventHandler>()
    private var chatRoomId:String? = ""
    private var channelId:String? = ""
    private var appKey:String? =""

    var userName:String?=""
    var userId:String?=""
    var accessToken:String?=""

    private var roomContext:AUIRoomContext? = null

    private val currentMsgList: ArrayList<AUIChatEntity> = ArrayList<AUIChatEntity>()
    private val currentGiftList: ArrayList<AUIGiftEntity> = ArrayList<AUIGiftEntity>()

    init {
        this.channelId = channelName
        this.roomContext = roomContext
    }

    fun subscribeChatMsg(delegate: AUIChatEventHandler) {
        chatEvnetHandlers.add(delegate)
    }

    fun unsubscribeChatMsg(delegate: AUIChatEventHandler?) {
        chatEvnetHandlers.remove(delegate)
    }

    fun initManager() {
        Logger.t("AUIChatManager").d("initManager")
        ChatClient.getInstance().chatManager().addMessageListener(this)
        ChatClient.getInstance().chatroomManager().addChatRoomChangeListener(this)
    }

    fun setChatRoom(chatRoomId: String?){
        this.chatRoomId = chatRoomId
        Log.e("apex","setChatRoomID $chatRoomId")
    }

    fun getCurrentRoom():String?{
        return chatRoomId
    }

    fun getCurrentRoomOwnerId():String?{
        return roomContext?.getRoomOwner(channelId)
    }

    fun isOwner():Boolean{
        return roomContext?.isRoomOwner(channelId) == true
    }

    fun getAppKey():String?{
        return appKey
    }

    fun clear(){
        chatRoomId = ""
        channelId = ""
        userId = ""
        userName = ""
        accessToken = ""
        currentMsgList.clear()
        currentGiftList.clear()
        if (ChatClient.getInstance().isSdkInited) {
            ChatClient.getInstance().chatManager().removeMessageListener(this)
            ChatClient.getInstance().chatroomManager().removeChatRoomListener(this)
        }
    }

    fun loginChat(userName:String,userToken:String,callback:CallBack){
        Logger.t("AUIChatManager").d("loginChat")
        ChatClient.getInstance().loginWithToken(userName,userToken,object : CallBack{
            override fun onSuccess() {
                callback.onSuccess()
            }

            override fun onError(code: Int, error: String?) {
                callback.onError(code,error)
            }
        })
    }

    fun loginChat(callback:CallBack){
        Logger.t("AUIChatManager").d("loginChat")
        ChatClient.getInstance().loginWithToken(userName,accessToken,object : CallBack{
            override fun onSuccess() {
                ThreadManager.getInstance().runOnMainThread{
                    callback.onSuccess()
                }
                Log.d("apex","loginChat suc")
            }

            override fun onError(code: Int, error: String?) {
                ThreadManager.getInstance().runOnMainThread{
                    callback.onError(code,error)
                }
                Log.e("apex","loginChat error $code  $error")
            }
        })
    }

    fun logoutChat(callback: CallBack){
        Logger.t("AUIChatManager").d("logoutChat")
        ChatClient.getInstance().logout(false,object:CallBack{
            override fun onSuccess() {
                callback.onSuccess()
            }

            override fun onError(code: Int, error: String?) {
                callback.onError(code, error)
            }
        })
    }

    fun logoutChat() {
        if (ChatClient.getInstance().isSdkInited) {
            ChatClient.getInstance().logout(false)
        }
    }

    /**
     * 加入房间
     */
    fun joinRoom(roomId: String?,callback: AUIChatMsgCallback){
        ChatClient.getInstance().chatroomManager().joinChatRoom(roomId,object:
            ValueCallBack<ChatRoom> {
            override fun onSuccess(value: ChatRoom?) {
                Log.e("apex","joinRoom onSuccess $roomId")
                //加入成功后 返回成员加入消息
                sendJoinMsg(roomId,roomContext?.currentUserInfo,object : AUIChatMsgCallback{
                    override fun onOriginalResult(error: AUIException?, message: ChatMessage?) {
                        if (error == null){
                            ThreadManager.getInstance().runOnMainThreadDelay({callback.onOriginalResult(null,message)},300)
                        }
                    }
                })
            }

            override fun onError(code: Int, errorMsg: String?) {
                Log.e("apex","joinRoom onError $roomId $code $errorMsg")
                ThreadManager.getInstance().runOnMainThread { callback.onOriginalResult(AUIException(code,errorMsg),null) }
            }
        })
    }

    /**
     * 离开房间
     */
    fun leaveChatRoom() {
        if(ChatClient.getInstance().isSdkInited){
            ChatClient.getInstance().chatroomManager().leaveChatRoom(chatRoomId)
        }
    }

    /**
     * 销毁房间
     */
    fun asyncDestroyChatRoom(callBack: CallBack) {
        ChatClient.getInstance().chatroomManager()
            .asyncDestroyChatRoom(chatRoomId, object : CallBack {
                override fun onSuccess() {
                    ThreadManager.getInstance().runOnMainThread(Runnable { callBack.onSuccess() })
                }

                override fun onError(code: Int, error: String) {
                    ThreadManager.getInstance()
                        .runOnMainThread(Runnable { callBack.onError(code, error) })
                }
            })
    }


    fun isLoggedIn():Boolean{
        return ChatClient.getInstance().isSdkInited && ChatClient.getInstance().isLoggedIn
    }

    override fun onMessageReceived(messages: MutableList<ChatMessage>?) {
        messages?.forEach {
            if (it.type == ChatMessage.Type.TXT) {
                parseMsgChatEntity(it)
                try {
                    for (listener in chatEvnetHandlers) {
                        listener.onReceiveTextMsg(channelId,parseChatMessage(it))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            // 先判断是否自定义消息
            if (it.type == ChatMessage.Type.CUSTOM) {
                val body = it.body as CustomMessageBody
                val event = body.event()
                val msgType: AUICustomMsgType? = getCustomMsgType(event)

                // 再排除单聊
                if (it.chatType != ChatMessage.ChatType.Chat){
                    val username: String = it.to
                    // 判断是否同一个聊天室或者群组 并且 event不为空
                    if (TextUtils.equals(username,chatRoomId) && !TextUtils.isEmpty(event)) {
                        when (msgType) {
                            AUICustomMsgType.AUIChatRoomJoinedMember -> {
                                parseMsgChatEntity(it)
                                try {
                                    for (listener in chatEvnetHandlers) {
                                        listener.onReceiveMemberJoinedMsg(channelId,parseChatMessage(it))
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    /**
     *  解析消息 AgoraChatMessage（原消息）
     */
    private fun parseChatMessage(chatMessage: ChatMessage): AgoraChatMessage {
        var chatId:String? = ""
        var messageId:String? = ""
        var content:String? = ""
        var user:AUIUserThumbnailInfo? = AUIUserThumbnailInfo()

        val attr = chatMessage.getStringAttribute("user","")
        if (!attr.isNullOrEmpty()){
            GsonTools.toBean(attr, AgoraChatMessage::class.java)?.let { it ->
                user = it.user
            }
        }
        chatId = chatMessage.conversationId()
        messageId = chatMessage.msgId
        if (chatMessage.body is TextMessageBody) {
            content = (chatMessage.body as TextMessageBody).message
        }
        return AgoraChatMessage(
            chatId,
            messageId,
            content,
            user
        )
    }

    /**
     * 解析消息 AUIChatEntity （ui渲染消息）
     */
    fun parseMsgChatEntity(chatMessage: ChatMessage){
        var content:String?=""
        var joined = false
        val user = AUIUserThumbnailInfo()
        if (chatMessage.body is TextMessageBody) {
            content = (chatMessage.body as TextMessageBody).message
            joined = false
            val attr = chatMessage.getStringAttribute("user","")
            if (!attr.isNullOrEmpty()){
                val json = JSONObject(attr)
                user.userId = json.get("userId") as String
                user.userName = json.get("userName") as String
                user.userAvatar = json.get("userAvatar") as String
            }
        } else if (chatMessage.body is CustomMessageBody) {
            joined = true
            val params = (chatMessage.body as CustomMessageBody).params
            val attr = params["user"]
            if (!attr.isNullOrEmpty()){
                val json = JSONObject(attr)
                user.userId = json.get("userId") as String
                user.userName = json.get("userName") as String
                user.userAvatar = json.get("userAvatar") as String
            }
        }
        currentMsgList.add(AUIChatEntity(user,content,joined))
    }


    fun getGiftList(): ArrayList<AUIGiftEntity>{
        return currentGiftList
    }

    fun addGiftList(gift:AUIGiftEntity){
        currentGiftList.clear()
        currentGiftList.add(gift)
    }

    fun getMsgList(): ArrayList<AUIChatEntity>{
        return currentMsgList
    }

    fun getChatUser(user:String,callback:AUICallback){
        HttpManager.getService(UserInterface::class.java)
            .createUser(CreateUserReq(user))
            .enqueue(object : retrofit2.Callback<CommonResp<CreateUserRsp>> {
                override fun onResponse(call: Call<CommonResp<CreateUserRsp>>, response: Response<CommonResp<CreateUserRsp>>) {
                    val rsp = response.body()?.data
                    if (response.body()?.code == 0 && rsp != null) {
                        userId = rsp.userUuid
                        userName = rsp.userName
                        accessToken = rsp.accessToken
                        appKey = rsp.appKey
                        ThreadManager.getInstance().runOnMainThread{
                            callback.onResult(null)
                        }
                    } else {
                        callback.onResult(AUIException(-1,"getChatUser not ready"))
                        Log.e("UserInterface","onFailure" + Utils.errorFromResponse(response))
                    }
                }
                override fun onFailure(call: Call<CommonResp<CreateUserRsp>>, t: Throwable) {
                    Log.e("UserInterface","onFailure" + AUIException(-1, t.message))
                    callback.onResult(AUIException(-1,t.message))
                }
            })
    }

    /**
     * 发送文本消息
     * @param content
     * @param callBack
     */
    fun sendTxtMsg(roomId: String?, content: String?, userInfo: AUIUserThumbnailInfo?, callBack: AUIChatMsgCallback) {
        if (!isLoggedIn()) {
            return
        }
        val message = ChatMessage.createTextSendMessage(content, roomId)
        message?.let {
            it.setAttribute("user",GsonTools.beanToString(userInfo))
            it.chatType = ChatMessage.ChatType.ChatRoom
            it.setMessageStatusCallback(object : CallBack {
                override fun onSuccess() {
                    parseMsgChatEntity(it)
                    callBack.onResult(null,parseChatMessage(it))
                }

                override fun onError(i: Int, s: String) {
                    callBack.onResult(AUIException(i, s),null)
                }

                override fun onProgress(i: Int, s: String) {

                }
            })
            ChatClient.getInstance().chatManager().sendMessage(it)
        }
    }

    fun sendJoinMsg(roomId: String?,userInfo: AUIUserThumbnailInfo?,callBack: AUIChatMsgCallback){
        if (!isLoggedIn()) {
            return
        }
        val messages = ChatMessage.createSendMessage(ChatMessage.Type.CUSTOM)
        messages.to = roomId
        val customBody = CustomMessageBody( AUICustomMsgType.AUIChatRoomJoinedMember.name)
        val ext = mutableMapOf<String,String>()
        ext["user"] = GsonTools.beanToString(userInfo).toString()
        customBody.params = ext
        messages.let {
            it.body = customBody
            it?.chatType = ChatMessage.ChatType.ChatRoom
            it?.setMessageStatusCallback(object : CallBack {
                override fun onSuccess() {
                    parseMsgChatEntity(it)
                    callBack.onOriginalResult(null,it)
                }

                override fun onError(i: Int, s: String) {
                    callBack.onOriginalResult(AUIException(i, s),null)
                }

                override fun onProgress(i: Int, s: String) {

                }
            })
            ChatClient.getInstance().chatManager().sendMessage(it)
        }
    }

    /**
     * 插入欢迎消息
     * @param content
     */
    fun saveWelcomeMsg(content: String?) {
        currentMsgList.clear()
        val auiChatEntity = AUIChatEntity(
            roomContext?.currentUserInfo,
            content,false
        )
        currentMsgList.add(auiChatEntity)
    }

    /**
     * 获取自定义消息类型
     * @param event
     * @return
     */
    private fun getCustomMsgType(event: String?): AUICustomMsgType? {
        return if (TextUtils.isEmpty(event)) {
            null
        } else AUICustomMsgType.fromName(event)
    }


    /**
     * 获取成员非主动退出房间原因
     * @param reason
     * @return
     */
    private fun getKickReason(reason: Int): VoiceRoomServiceKickedReason? {
        return when (reason) {
            EMAChatRoomManagerListener.BE_KICKED -> VoiceRoomServiceKickedReason.removed
            EMAChatRoomManagerListener.DESTROYED -> VoiceRoomServiceKickedReason.destroyed
            EMAChatRoomManagerListener.BE_KICKED_FOR_OFFLINE -> VoiceRoomServiceKickedReason.offLined
            else -> null
        }
    }

    override fun onAnnouncementChanged(chatRoomId: String?, announcement: String?) {
    }

    override fun onRemovedFromChatRoom(
        reason: Int,
        chatRoomId: String?,
        roomName: String?,
        participant: String?
    ) {
        try {
            for (listener in chatEvnetHandlers) {
                listener.onUserBeKicked(chatRoomId,getKickReason(reason))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onChatRoomDestroyed(chatRoomId: String?, roomName: String?) {
        try {
            for (listener in chatEvnetHandlers) {
                listener.onRoomDestroyed(chatRoomId)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onMemberJoined(roomId: String?, participant: String?) {}

    override fun onMemberExited(roomId: String?, roomName: String?, participant: String?) {}

    override fun onMuteListAdded(
        chatRoomId: String?,
        mutes: MutableList<String>?,
        expireTime: Long
    ) {}

    override fun onMuteListRemoved(chatRoomId: String?, mutes: MutableList<String>?) {}

    override fun onWhiteListAdded(chatRoomId: String?, whitelist: MutableList<String>?) {}

    override fun onWhiteListRemoved(chatRoomId: String?, whitelist: MutableList<String>?) {}

    override fun onAllMemberMuteStateChanged(chatRoomId: String?, isMuted: Boolean) {}

    override fun onAdminAdded(chatRoomId: String?, admin: String?) {}

    override fun onAdminRemoved(chatRoomId: String?, admin: String?) {}

    override fun onOwnerChanged(chatRoomId: String?, newOwner: String?, oldOwner: String?) {}

}
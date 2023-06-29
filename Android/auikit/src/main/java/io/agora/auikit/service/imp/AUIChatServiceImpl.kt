package io.agora.auikit.service.imp

import android.util.Log
import io.agora.CallBack
import io.agora.auikit.model.*
import io.agora.auikit.service.IAUIChatService
import io.agora.auikit.service.callback.*
import io.agora.auikit.service.http.CommonResp
import io.agora.auikit.service.http.HttpManager
import io.agora.auikit.service.http.Utils
import io.agora.auikit.service.http.room.CreateChatRoomReq
import io.agora.auikit.service.http.room.CreateChatRoomRsp
import io.agora.auikit.service.http.room.RoomInterface
import io.agora.auikit.service.im.AUIChatManager
import io.agora.auikit.service.im.AUIChatSubscribeDelegate
import io.agora.auikit.service.rtm.AUIRtmManager
import io.agora.auikit.service.rtm.AUIRtmMsgProxyDelegate
import io.agora.auikit.utils.AgoraEngineCreator
import io.agora.auikit.utils.DelegateHelper
import io.agora.auikit.utils.ThreadManager
import io.agora.chat.ChatMessage
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response

private const val chatRoomIdKey = "chatRoom"
class AUIChatServiceImpl(
    private val channelName: String,
    private val rtmManager: AUIRtmManager
) : IAUIChatService, AUIRtmMsgProxyDelegate {
    private val roomContext:AUIRoomContext
    private var chatManager:AUIChatManager?

    init {
        rtmManager.subscribeMsg(channelName,chatRoomIdKey,this)
        this.roomContext = AUIRoomContext.shared()
        this.chatManager = AUIChatManager(channelName,roomContext)
        Log.e("apex-wt","op2")
    }

    fun initChatService(){
        Log.d("apex","initChatService ${chatManager?.getAppKey()}")
        AgoraEngineCreator.createChatClient(
            roomContext.commonConfig.context,
            chatManager?.getAppKey()
        )
        chatManager?.initManager()
    }

    fun subscribeChatMsg(delegate: AUIChatSubscribeDelegate){
        chatManager?.subscribeChatMsg(delegate)
    }

    fun unsubscribeChatMsg(delegate: AUIChatSubscribeDelegate){
        chatManager?.unsubscribeChatMsg(delegate)
    }

    private val delegateHelper = DelegateHelper<IAUIChatService.AUIChatRespDelegate>()

    override fun bindRespDelegate(delegate: IAUIChatService.AUIChatRespDelegate?) {
        delegateHelper.bindDelegate(delegate)
    }

    override fun unbindRespDelegate(delegate: IAUIChatService.AUIChatRespDelegate?) {
        delegateHelper.unBindDelegate(delegate)
    }

    override fun getRoomContext() = roomContext

    override fun getChannelName() = channelName

    override fun createChatRoom(roomId: String, callback: AUICreateChatRoomCallback) {
        val userName = chatManager?.userName
        Log.e("apex-wt","op6 $userName")
        userName?.let { createRoom(roomId,it,callback) }
    }

    override fun sendMessage(
        roomId: String?,
        text: String?,
        userInfo: AUIUserThumbnailInfo?,
        callback: AUIChatMsgCallback
    ) {
        chatManager?.sendTxtMsg(roomId,text,userInfo,callback)
    }

    override fun joinedChatRoom(roomId: String?, callback: AUIChatMsgCallback) {
        chatManager?.joinRoom(roomId,callback)
    }

    override fun userQuitRoom(callback: CallBack) {
        //区分是房主还是成员 房主调用解散聊天室api 成员调用退出聊天室api
        if ( roomContext.isRoomOwner(channelName) ){
            chatManager?.asyncDestroyChatRoom(callback)
        }else{
            chatManager?.leaveChatRoom()
            callback.onSuccess()
        }
    }

    private fun createRoom(roomId: String,userName:String, callback: AUICreateChatRoomCallback){
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
                override fun onResponse(call: Call<CommonResp<CreateChatRoomRsp>>, response: Response<CommonResp<CreateChatRoomRsp>>) {
                    val rsp = response.body()?.data
                    if (response.body()?.code == 0 && rsp != null) {
                        val chatRoomId = rsp.chatRoomId
                        // success
                        chatManager?.setChatRoom(chatRoomId)
                        Log.e("apex","createChatRoom suc")
                        callback.onResult(null,chatRoomId)
                    } else {
                        callback.onResult(Utils.errorFromResponse(response),null)
                    }
                }
                override fun onFailure(call: Call<CommonResp<CreateChatRoomRsp>>, t: Throwable) {
                    callback.onResult(AUIException(-1, t.message),"")
                    Log.e("apex","createChatRoom onFailure")
                }
            })
    }

    override fun onMsgDidChanged(channelName: String, key: String, value: Any) {
        // 解析数据 获取环信聊天室id
        if (key == chatRoomIdKey){
            delegateHelper.notifyDelegate {
                val room = JSONObject(value.toString())
                it.onUpdateChatRoomId(room.getString("chatRoomId"))
            }
        }
    }

    fun getChatUser(callback:AUICallback){
        chatManager?.getChatUser(roomContext.currentUserInfo.userId,callback)
    }

    fun setChatRoomId(chatRoomId: String?){
        chatManager?.setChatRoom(chatRoomId)
    }

    fun loginChat(userName:String,userToken:String,callback:CallBack){
        chatManager?.loginChat(userName,userToken,callback)
    }

    fun loginChat(callback:CallBack){
        chatManager?.loginChat(callback)
    }

    fun logoutChat(callback:CallBack){
        chatManager?.logoutChat(callback)
    }

    fun logoutChat(){
        chatManager?.logoutChat()
    }

    fun isLoggedIn():Boolean{
        return chatManager?.isLoggedIn() == true
    }

    fun getCurrentRoom():String?{
        return chatManager?.getCurrentRoom()
    }

    fun getCurrentRoomOwnerId():String?{
        return chatManager?.getCurrentRoomOwnerId()
    }

    fun isOwner():Boolean{
        return chatManager?.isOwner() == true
    }

    fun getGiftList(): ArrayList<AUIGiftEntity>?{
        return chatManager?.getGiftList()
    }

    fun addGiftList(gift:AUIGiftEntity){
        chatManager?.addGiftList(gift)
    }

    fun getMsgList(): ArrayList<AUIChatEntity>?{
        return chatManager?.getMsgList()
    }

    fun saveWelcomeMsg(content: String?){
        chatManager?.saveWelcomeMsg(content)
    }

    fun clearChatService(){
        chatManager?.clear()
    }

    fun parseMsgChatEntity(chatMessage: ChatMessage){
        chatManager?.parseMsgChatEntity(chatMessage)
    }

}
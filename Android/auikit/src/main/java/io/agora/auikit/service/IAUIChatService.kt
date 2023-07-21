package io.agora.auikit.service

import io.agora.CallBack
import io.agora.auikit.model.AUIUserThumbnailInfo
import io.agora.auikit.service.callback.AUIChatMsgCallback
import io.agora.auikit.service.callback.AUICreateChatRoomCallback

interface IAUIChatService: IAUICommonService<IAUIChatService.AUIChatRespDelegate>{

    /**
     * 创建聊天室
     * - roomId: 语聊房id
     */
    fun createChatRoom(roomId: String,callback: AUICreateChatRoomCallback){}


    /**
     *  发送聊天室消息
     *  - roomId: 聊天室id
     *  - text: 文本内容
     *  - userInfo: 用户信息
     */
    fun sendMessage(roomId: String?, text: String?, userInfo: AUIUserThumbnailInfo?,callback:AUIChatMsgCallback){}

    /**
     * 加入聊天室
     * - roomId: 聊天室id
     */
    fun joinedChatRoom(roomId: String,callback:AUIChatMsgCallback){}

    /**
     * 退出聊天室
     */
    fun userQuitRoom(callback:CallBack){}

    /**
     * 销毁聊天室
     */
    fun userDestroyedChatroom(){}


    interface AUIChatRespDelegate{


        /**
         * 销毁聊天室
         */
        fun userDestroyedChatroom(){}

        /**
         * 房间Id更新
         *
         * @param roomId   房间唯一id
         */
        fun onUpdateChatRoomId(roomId: String) {}


    }


}
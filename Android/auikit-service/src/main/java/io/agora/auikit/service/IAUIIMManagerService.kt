package io.agora.auikit.service

import io.agora.auikit.model.AUIUserThumbnailInfo
import io.agora.auikit.service.callback.AUIException

interface IAUIIMManagerService: IAUICommonService<IAUIIMManagerService.AUIIMManagerRespObserver>{


    /**
     *  发送聊天室消息
     *  - roomId: 聊天室id
     *  - text: 文本内容
     *  - userInfo: 用户信息
     */
    fun sendMessage(roomId: String, text: String, completion: (AgoraChatTextMessage?, AUIException?) -> Unit)

    /**
     * 退出聊天室
     */
    fun userQuitRoom(completion: ((error: AUIException?)->Unit)? = null)

    /**
     * 销毁聊天室
     */
    fun userDestroyedChatroom()

    data class AgoraChatTextMessage(
        val messageId: String?,
        val content: String?,
        val user: AUIUserThumbnailInfo?
    )

    interface AUIIMManagerRespObserver{

        /**
         * 接收到消息
         */
        fun messageDidReceive(roomId: String, message: AgoraChatTextMessage)

        /**
         * 用户加入聊天室
         */
        fun onUserDidJoinRoom(roomId: String, message: AgoraChatTextMessage)

    }


}
package io.agora.auikit.service.http.chat

import android.support.annotation.IntDef

const val CHATROOM_CREATE_TYPE_USER_ROOM = 0
const val CHATROOM_CREATE_TYPE_USER = 1
const val CHATROOM_CREATE_TYPE_ROOM = 2

@IntDef(
    CHATROOM_CREATE_TYPE_USER_ROOM,
    CHATROOM_CREATE_TYPE_USER,
    CHATROOM_CREATE_TYPE_ROOM
)
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ChatRoomCreateType

data class CreateChatRoomReq(
    val appId: String,
    @ChatRoomCreateType val type: Int = CHATROOM_CREATE_TYPE_USER_ROOM, // 0：创建用户和房间, 1：仅创建用户, 2：仅创建房间
    val chatRoomConfig: ChatRoomConfig?,
    val imConfig: ChatIMConfig?,
    val user: ChatUser
)

data class ChatRoomConfig(
    val name: String?,
    val description: String? = null,
    val maxUsers: Int? = 1000,
    val custom: String? = null
)

data class ChatIMConfig(
    val orgName: String?,
    val appName: String?,
    val clientId: String?,
    val clientSecret: String?
)

data class ChatUser(
    val username: String,
    val password: String? = null
)

data class CreateChatRoomResp(
    val chatId: String?,
    val userToken: String?,
    val userUuid: String?,
    val appKey: String
)

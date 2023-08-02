package io.agora.auikit.ui.chatList

import java.io.Serializable

data class AUIChatInfo (
    val userId: String,
    val userName: String,
    val content: String?,
    val joined: Boolean
): Serializable
package io.agora.auikit.ui.chatList

import java.io.Serializable

data class AUIChatInfo (
    var userId: String,
    var userName: String,
    var content: String?,
    var joined: Boolean
): Serializable
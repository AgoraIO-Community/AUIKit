package io.agora.auikit.service.callback


interface AUICreateChatRoomCallback {
    fun onResult(error: AUIException?, chatRoomId: String?)
}
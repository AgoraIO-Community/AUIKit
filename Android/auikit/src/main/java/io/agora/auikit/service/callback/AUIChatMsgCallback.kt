package io.agora.auikit.service.callback

import io.agora.auikit.model.AUIChatEntity
import io.agora.auikit.model.AgoraChatMessage
import io.agora.chat.ChatMessage

interface AUIChatMsgCallback {
    fun onResult(error: AUIException?,message: AgoraChatMessage?){}
    fun onEntityResult(error: AUIException?,message: AUIChatEntity?){}
    fun onOriginalResult(error: AUIException?,message: ChatMessage?){}
}
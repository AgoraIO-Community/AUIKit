package io.agora.auikit.service.imp

import io.agora.auikit.model.AUIRoomContext
import io.agora.auikit.service.IAUIInvitationService
import io.agora.auikit.service.callback.AUICallback
import io.agora.auikit.service.rtm.AUIRtmManager
import io.agora.auikit.utils.DelegateHelper

class AUIInvitationServiceImpl(
    private val roomContext: AUIRoomContext,
    private val channelName: String,
    private val rtmManager: AUIRtmManager
) : IAUIInvitationService {

    private val delegateHelper = DelegateHelper<IAUIInvitationService.AUIInvitationRespDelegate>()

    override fun bindRespDelegate(delegate: IAUIInvitationService.AUIInvitationRespDelegate?) {
        delegateHelper.bindDelegate(delegate)
    }

    override fun unbindRespDelegate(delegate: IAUIInvitationService.AUIInvitationRespDelegate?) {
        delegateHelper.unBindDelegate(delegate)
    }

    override fun sendInvitation(
        cmd: String?,
        userId: String,
        content: String?,
        callback: AUICallback?
    ) {
        TODO("Not yet implemented")
    }

    override fun acceptInvitation(id: String, callback: AUICallback?) {
        TODO("Not yet implemented")
    }

    override fun rejectInvitation(id: String, callback: AUICallback?) {
        TODO("Not yet implemented")
    }

    override fun cancelInvitation(id: String, callback: AUICallback?) {
        TODO("Not yet implemented")
    }

    override fun getChannelName() = channelName
}
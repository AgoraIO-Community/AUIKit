package io.agora.auikit.service.imp

import io.agora.auikit.model.AUiRoomContext
import io.agora.auikit.service.IAUiInvitationService
import io.agora.auikit.service.callback.AUiCallback
import io.agora.auikit.service.rtm.AUiRtmManager
import io.agora.auikit.utils.DelegateHelper

class AUiInvitationServiceImpl(
    private val roomContext: AUiRoomContext,
    private val channelName: String,
    private val rtmManager: AUiRtmManager
) : IAUiInvitationService {

    private val delegateHelper = DelegateHelper<IAUiInvitationService.AUiInvitationRespDelegate>()

    override fun bindRespDelegate(delegate: IAUiInvitationService.AUiInvitationRespDelegate?) {
        delegateHelper.bindDelegate(delegate)
    }

    override fun unbindRespDelegate(delegate: IAUiInvitationService.AUiInvitationRespDelegate?) {
        delegateHelper.unBindDelegate(delegate)
    }

    override fun sendInvitation(
        cmd: String?,
        userId: String,
        content: String?,
        callback: AUiCallback?
    ) {
        TODO("Not yet implemented")
    }

    override fun acceptInvitation(id: String, callback: AUiCallback?) {
        TODO("Not yet implemented")
    }

    override fun rejectInvitation(id: String, callback: AUiCallback?) {
        TODO("Not yet implemented")
    }

    override fun cancelInvitation(id: String, callback: AUiCallback?) {
        TODO("Not yet implemented")
    }

    override fun getChannelName() = channelName
}
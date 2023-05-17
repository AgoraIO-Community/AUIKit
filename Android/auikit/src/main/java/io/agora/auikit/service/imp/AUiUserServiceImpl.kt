package io.agora.auikit.service.imp

import io.agora.auikit.model.AUiRoomContext
import io.agora.auikit.model.AUiUserInfo
import io.agora.auikit.service.IAUiUserService
import io.agora.auikit.service.callback.AUiCallback
import io.agora.auikit.service.callback.AUiException
import io.agora.auikit.service.callback.AUiUserListCallback
import io.agora.auikit.service.rtm.AUiRtmManager
import io.agora.auikit.service.rtm.AUiRtmUserProxyDelegate
import io.agora.auikit.utils.AUiLogger
import io.agora.auikit.utils.DelegateHelper
import io.agora.auikit.utils.GsonTools

class AUiUserServiceImpl constructor(
    private val channelName: String,
    private val rtmManager: AUiRtmManager
) : IAUiUserService, AUiRtmUserProxyDelegate {

    private val TAG = "AUiUserServiceImpl"

    private var mUserList = mutableListOf<AUiUserInfo>()

    private val delegateHelper = DelegateHelper<IAUiUserService.AUiUserRespDelegate>()

    init {
        rtmManager.subscribeUser(this)
    }

    override fun bindRespDelegate(delegate: IAUiUserService.AUiUserRespDelegate?) {
        delegateHelper.bindDelegate(delegate)
    }

    override fun unbindRespDelegate(delegate: IAUiUserService.AUiUserRespDelegate?) {
        delegateHelper.unBindDelegate(delegate)
    }

    override fun getUserInfoList(
        roomId: String,
        userIdList: MutableList<String>?,
        callback: AUiUserListCallback?
    ) {
        rtmManager.whoNow(roomId) { error, userList ->
            if (error != null) {
                callback?.onResult(AUiException(error.code, error.message), null)
                return@whoNow
            }
            if (userList == null) {
                callback?.onResult(AUiException(-1, ""), null)
                return@whoNow
            }
            val users = mutableListOf<AUiUserInfo>()
            userList.forEach { userMap ->
                GsonTools.toBean(GsonTools.beanToString(userMap), AUiUserInfo::class.java)?.let {
                    users.add(it)
                }
            }
            callback?.onResult(null, users)
        }
    }

    override fun muteUserAudio(isMute: Boolean, callback: AUiCallback?) {
        val currentUserId = roomContext.currentUserInfo.userId
        val user = mUserList.first { it.userId == currentUserId }
        user.muteAudio = if (isMute) 1 else 0
        val map = GsonTools.beanToMap(user)
        rtmManager.setPresenceState(channelName, map.mapValues { it.value.toString() }) { error ->
            if (error != null) {
                callback?.onResult(AUiException(error.code, error.message))
            } else {
                this.delegateHelper.notifyDelegate {
                    it.onUserAudioMute(currentUserId, isMute)
                }
                callback?.onResult(null)
            }
        }
    }

    override fun muteUserVideo(isMute: Boolean, callback: AUiCallback?) {
        val currentUserId = roomContext.currentUserInfo.userId
        val user = mUserList.firstOrNull { it.userId == currentUserId }
        if (user == null) {
            callback?.onResult(AUiException(-1, "can't find current user from users"))
            return
        }
        user.muteVideo = if (isMute) 1 else 0
        val map = GsonTools.beanToMap(user) as Map<String, String>
        rtmManager.setPresenceState(channelName, map) { error ->
            if (error != null) {
                callback?.onResult(AUiException(error.code, error.message))
            } else {
                callback?.onResult(null)
            }
        }
    }

    override fun getUserInfo(userId: String): AUiUserInfo? {
        return mUserList.firstOrNull { it.userId == userId }
    }

    override fun getRoomContext(): AUiRoomContext { return AUiRoomContext.shared() }

    override fun getChannelName() = channelName

    /** AUiRtmUserProxyDelegate */
    override fun onUserSnapshotRecv(
        channelName: String,
        userId: String,
        userList: List<Map<String, Any>>
    ) {
        val users = mutableListOf<AUiUserInfo>()
        userList.forEach { userMap ->
            GsonTools.toBean(GsonTools.beanToString(userMap), AUiUserInfo::class.java)?.let {
                users.add(it)
            }
        }
        mUserList = users
        this.delegateHelper.notifyDelegate {
            it.onRoomUserSnapshot(channelName, mUserList)
        }
        setupUserAttr(channelName)
    }

    override fun onUserDidJoined(
        channelName: String,
        userId: String,
        userInfo: Map<String, Any>
    ) {
        GsonTools.toBean(GsonTools.beanToString(userInfo), AUiUserInfo::class.java)?.let { info ->
            info.userId = userId
            mUserList.add(info)
            this.delegateHelper.notifyDelegate {
                it.onRoomUserEnter(channelName, info)
            }
        }
    }

    override fun onUserDidLeaved(
        channelName: String,
        userId: String,
        userInfo: Map<String, Any>
    ) {
        val index = mUserList.indexOfFirst{ it.userId == userId }
        val info = mUserList.removeAt(index)
        this.delegateHelper.notifyDelegate {
            it.onRoomUserLeave(channelName, info)
        }
    }

    override fun onUserDidUpdated(
        channelName: String,
        userId: String,
        userInfo: Map<String, Any>
    ) {
        if (userInfo.isEmpty()) {
            return
        }
        GsonTools.toBean(GsonTools.beanToString(userInfo), AUiUserInfo::class.java)?.let { info ->
            val index = mUserList.indexOfFirst{ it.userId == info.userId }
            if (index == -1) { // 不存在该用户
                mUserList.add(info)
            } else {
                val oldInfo = mUserList[index]
                mUserList[index] = info
                // 单独更新语音被禁用回调
                if (oldInfo.muteAudio != info.muteAudio) {
                    this.delegateHelper.notifyDelegate {
                        it.onUserAudioMute(info.userId, (info.muteAudio == 1))
                    }
                }
            }
            this.delegateHelper.notifyDelegate {
                it.onRoomUserUpdate(channelName, info)
            }
        }
    }

    private fun setupUserAttr(roomId: String){
        val userId = AUiRoomContext.shared().currentUserInfo.userId
        val userInfo = mUserList.firstOrNull { it.userId == userId } ?: AUiUserInfo()
        userInfo.userId = AUiRoomContext.shared().currentUserInfo.userId
        userInfo.userName = AUiRoomContext.shared().currentUserInfo.userName
        userInfo.userAvatar = AUiRoomContext.shared().currentUserInfo.userAvatar

        val userAttr = GsonTools.beanToMap(userInfo)
        AUiLogger.logger().d(TAG, "setupUserAttr: $roomId : $userAttr")
        rtmManager.setPresenceState(roomId, userAttr) { error ->
            if(error != null){
                AUiLogger.logger().d(TAG, "setupUserAttr: $roomId fail: ${error.reason}")
            }else{
                //rtm不会返回自己更新的数据，需要手动处理
                onUserDidUpdated(roomId, userId, userAttr)
            }
        }
    }
}
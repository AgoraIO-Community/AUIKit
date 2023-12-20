package io.agora.auikit.service.imp

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.ToNumberPolicy
import com.google.gson.reflect.TypeToken
import io.agora.auikit.model.AUIChoristerModel
import io.agora.auikit.service.IAUIChorusService
import io.agora.auikit.service.callback.AUICallback
import io.agora.auikit.service.callback.AUIChoristerListCallback
import io.agora.auikit.service.callback.AUIException
import io.agora.auikit.service.callback.AUISwitchSingerRoleCallback
import io.agora.auikit.service.ktv.ISwitchRoleStateListener
import io.agora.auikit.service.ktv.KTVApi
import io.agora.auikit.service.ktv.KTVSingRole
import io.agora.auikit.service.ktv.SwitchRoleFailReason
import io.agora.auikit.service.rtm.AUIRtmAttributeRespObserver
import io.agora.auikit.service.rtm.AUIRtmException
import io.agora.auikit.service.rtm.AUIRtmManager
import io.agora.auikit.service.rtm.AUIRtmMessageRespObserver
import io.agora.auikit.service.rtm.AUIRtmPlayerInfo
import io.agora.auikit.service.rtm.AUIRtmPublishModel
import io.agora.auikit.service.rtm.AUIRtmReceiptModel
import io.agora.auikit.service.rtm.kAUIPlayerJoinInterface
import io.agora.auikit.service.rtm.kAUIPlayerLeaveInterface
import io.agora.auikit.utils.GsonTools
import io.agora.auikit.utils.ObservableHelper

const val kChorusKey = "chorus"

class AUIChorusServiceImpl constructor(
    private val channelName: String,
    private val ktvApi: KTVApi,
    private val rtmManager: AUIRtmManager
) : IAUIChorusService, AUIRtmAttributeRespObserver, AUIRtmMessageRespObserver {
    private val TAG: String = "Chorus_LOG"

    init {
        rtmManager.subscribeAttribute(channelName, kChorusKey, this)
        rtmManager.subscribeMessage(this)
    }

    private val gson: Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd HH:mm:ss")
        .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
        .create()

    private var chorusList = mutableListOf<AUIChoristerModel>() // 合唱者列表

    private val observableHelper =
        ObservableHelper<IAUIChorusService.AUIChorusRespObserver>()
    override fun registerRespObserver(observer: IAUIChorusService.AUIChorusRespObserver?) {
        observableHelper.subscribeEvent(observer)
    }

    override fun unRegisterRespObserver(observer: IAUIChorusService.AUIChorusRespObserver?) {
        observableHelper.unSubscribeEvent(observer)
    }

    override fun getChannelName() = channelName

    override fun getChoristersList(callback: AUIChoristerListCallback?) {
        callback?.onResult(AUIException(0, ""), chorusList)
    }

    override fun joinChorus(songCode: String?, userId: String?, callback: AUICallback?) {
        songCode ?: return
        userId ?: return

        if (roomContext.getArbiter(channelName)?.isArbiter() == true) {
            rtmJoinChorus(songCode, userId, callback)
            return
        }

        val info = AUIRtmPlayerInfo(
            songCode,
            userId,
            channelName
        )

        rtmManager.publishAndWaitReceipt(
            channelName,
            AUIRtmPublishModel(
                interfaceName = kAUIPlayerJoinInterface,
                data = info
            )
        ) { error ->
            if (error != null) {
                callback?.onResult(AUIException(AUIException.ERROR_CODE_RTM, "error=$error"))
            } else {
                callback?.onResult(null)
            }
        }
    }

    override fun leaveChorus(songCode: String?, userId: String?, callback: AUICallback?) {
        songCode ?: return
        userId ?: return
        if (roomContext.getArbiter(channelName)?.isArbiter() == true) {
            rtmLeaveChorus(songCode, userId, callback)
            return
        }

        val info = AUIRtmPlayerInfo(
            songCode,
            userId,
            channelName
        )

        rtmManager.publishAndWaitReceipt(
            channelName,
            AUIRtmPublishModel(
                interfaceName = kAUIPlayerLeaveInterface,
                data = info
            )
        ) { error ->
            if (error != null) {
                callback?.onResult(AUIException(AUIException.ERROR_CODE_RTM, "error=$error"))
            } else {
                callback?.onResult(null)
            }
        }
    }

    override fun switchSingerRole(newRole: Int, callback: AUISwitchSingerRoleCallback?) {
        ktvApi.switchSingerRole(KTVSingRole.values().firstOrNull { it.value == newRole } ?: KTVSingRole.Audience, object :
            ISwitchRoleStateListener {
            override fun onSwitchRoleSuccess() {
                callback?.onSwitchRoleSuccess()
            }

            override fun onSwitchRoleFail(reason: SwitchRoleFailReason) {
                callback?.onSwitchRoleFail(reason.value)
            }
        })
    }

    override fun cleanUserInfo(userId: String, completion: AUICallback?) {
        super.cleanUserInfo(userId, completion)
        val userList = chorusList.filter { it.userId != userId }
        if(userList.size != chorusList.size){
            val metaData = mutableMapOf<String, String>()
            metaData[kChorusKey] = GsonTools.beanToString(userId) ?: ""
            rtmManager.setBatchMetadata(
                channelName,
                metadata = metaData
            ){ error ->
                if(error != null){
                    completion?.onResult(AUIException(AUIException.ERROR_CODE_RTM, ""))
                }else{
                    completion?.onResult(null)
                }
            }
        }
    }

    override fun onAttributeChanged(channelName: String, key: String, value: Any) {
        if (key != kChorusKey) {
            return
        }
        Log.d(TAG, "channelName:$channelName,key:$key,value:$value")
        val chorusLists: List<AUIChoristerModel> =
            gson.fromJson(value as String, object : TypeToken<List<AUIChoristerModel>>() {}.type) ?: mutableListOf()
        chorusLists.forEach { newChorister ->
            var hasChorister = false
            this.chorusList.forEach {
                if (it.userId == newChorister.userId) {
                    hasChorister = true
                }
            }
            if (!hasChorister) {
                observableHelper.notifyEventHandlers { delegate: IAUIChorusService.AUIChorusRespObserver ->
                    delegate.onChoristerDidEnter(newChorister)
                }
            }
        }
        this.chorusList.forEach { oldChorister ->
            var hasChorister = false
            chorusLists.forEach {
                if (it.userId == oldChorister.userId) {
                    hasChorister = true
                }
            }
            if (!hasChorister) {
                observableHelper.notifyEventHandlers { delegate: IAUIChorusService.AUIChorusRespObserver ->
                    delegate.onChoristerDidLeave(oldChorister)
                }
            }
        }
        this.chorusList.clear()
        this.chorusList.addAll(chorusLists)

        observableHelper.notifyEventHandlers { delegate: IAUIChorusService.AUIChorusRespObserver ->
            //delegate
            delegate.onChoristerDidChanged()
        }
    }

    override fun onMessageReceive(channelName: String, message: String) {
        if (channelName != this.channelName) {
            return
        }

        val publishModel: AUIRtmPublishModel<JsonObject>? =
            GsonTools.toBean(message, object : TypeToken<AUIRtmPublishModel<JsonObject>>() {}.type)

        if (publishModel?.uniqueId == null) {
            return
        }

        if (publishModel.interfaceName == null) {
            // receipt message from arbiter
            val receiptModel = GsonTools.toBean(message, AUIRtmReceiptModel::class.java) ?: return
            if (receiptModel.code == 0) {
                // success
                rtmManager.markReceiptFinished(receiptModel.uniqueId, null)
            } else {
                // failure
                rtmManager.markReceiptFinished(
                    receiptModel.uniqueId, AUIRtmException(
                        receiptModel.code,
                        receiptModel.reason, "receipt message from arbiter"
                    )
                )
            }
        } else {
            val info =
                GsonTools.toBean(publishModel.data, AUIRtmPlayerInfo::class.java)
            if (info == null) {
                rtmManager.sendReceipt(
                    channelName,
                    AUIRtmReceiptModel(publishModel.uniqueId, -1, "Gson parse failed!")
                )
                return
            }
            when(publishModel.interfaceName){
                kAUIPlayerJoinInterface -> {
                    rtmJoinChorus(info.songCode, info.userId) { error ->
                        rtmManager.sendReceipt(
                            channelName,
                            AUIRtmReceiptModel(
                                publishModel.uniqueId,
                                error?.code ?: 0,
                                error?.message ?: ""
                            )
                        )
                    }
                }
                kAUIPlayerLeaveInterface -> {
                    rtmLeaveChorus(info.songCode, info.userId) { error ->
                        rtmManager.sendReceipt(
                            channelName,
                            AUIRtmReceiptModel(
                                publishModel.uniqueId,
                                error?.code ?: 0,
                                error?.message ?: ""
                            )
                        )
                    }
                }
            }
        }
    }

    private fun rtmJoinChorus(songCode: String, userId: String, callback: AUICallback?){
        val index = chorusList.indexOfFirst { it.userId == userId }
        if(index >= 0){
            callback?.onResult(AUIException(AUIException.ERROR_CODE_CHORISTER_ALREADY_EXIST, ""))
            return
        }

        val metaData = mutableMapOf<String, String>()
        var willError: AUIException? = null
        observableHelper.notifyEventHandlers {
            willError = it.onWillJoinChorus(songCode, userId, metaData)
            if (willError != null) {
                return@notifyEventHandlers
            }
        }
        if(willError != null){
            callback?.onResult(willError)
            return
        }

        val list = ArrayList(chorusList)
        val model = AUIChoristerModel()
        model.chorusSongNo = songCode
        model.userId = userId
        list.add(model)
        metaData[kChorusKey] = GsonTools.beanToString(list) ?: ""
        rtmManager.setBatchMetadata(channelName, metadata = metaData){ error ->
            if (error == null) {
                callback?.onResult(null)
            } else {
                callback?.onResult(AUIException(AUIException.ERROR_CODE_RTM, "error: $error"))
            }
        }
    }

    private fun rtmLeaveChorus(songCode: String, userId: String, callback: AUICallback?){
        val list = chorusList.filter { it.userId != userId }
        if(list.size == chorusList.size){
            callback?.onResult(AUIException(AUIException.ERROR_CODE_CHORISTER_NOT_EXIST, ""))
            return
        }

        val metaData = mutableMapOf<String, String>()
        metaData[kChorusKey] = GsonTools.beanToString(list) ?: ""
        rtmManager.setBatchMetadata(channelName, metadata = metaData){ error ->
            if (error == null) {
                callback?.onResult(null)
            } else {
                callback?.onResult(AUIException(AUIException.ERROR_CODE_RTM, "error: $error"))
            }
        }
    }

}
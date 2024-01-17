package io.agora.auikit.service.imp

import android.util.Log
import io.agora.auikit.model.AUIChoristerModel
import io.agora.auikit.service.IAUIChorusService
import io.agora.auikit.service.callback.AUICallback
import io.agora.auikit.service.callback.AUIChoristerListCallback
import io.agora.auikit.service.callback.AUIException
import io.agora.auikit.service.callback.AUISwitchSingerRoleCallback
import io.agora.auikit.service.collection.AUIAttributesModel
import io.agora.auikit.service.collection.AUIListCollection
import io.agora.auikit.service.ktv.ISwitchRoleStateListener
import io.agora.auikit.service.ktv.KTVApi
import io.agora.auikit.service.ktv.KTVSingRole
import io.agora.auikit.service.ktv.SwitchRoleFailReason
import io.agora.auikit.service.rtm.AUIRtmManager
import io.agora.auikit.utils.GsonTools
import io.agora.auikit.utils.ObservableHelper

const val kChorusKey = "chorus"

enum class AUIChorusCmd {
    joinChorusCmd,
    leaveChorusCmd
}

class AUIChorusServiceImpl constructor(
    private val channelName: String,
    private val ktvApi: KTVApi,
    private val rtmManager: AUIRtmManager
) : IAUIChorusService {
    private val TAG: String = "Chorus_LOG"

    private var chorusList = mutableListOf<AUIChoristerModel>() // 合唱者列表

    private val observableHelper = ObservableHelper<IAUIChorusService.AUIChorusRespObserver>()

    private val listCollection = AUIListCollection(channelName, kChorusKey, rtmManager)


    init {
        listCollection.subscribeWillAdd(this::metadataWillAdd)
        listCollection.subscribeAttributesDidChanged(this::onAttributeChanged)
    }

    override fun deInitService(completion: AUICallback?) {
        super.deInitService(completion)

        listCollection.cleanMetaData(completion)
    }

    override fun registerRespObserver(observer: IAUIChorusService.AUIChorusRespObserver?) {
        observableHelper.subscribeEvent(observer)
    }

    override fun unRegisterRespObserver(observer: IAUIChorusService.AUIChorusRespObserver?) {
        observableHelper.unSubscribeEvent(observer)
    }

    override fun getChannelName() = channelName

    override fun getChoristersList(callback: AUIChoristerListCallback?) {
        listCollection.getMetaData { error, value ->
            if (error != null) {
                callback?.onResult(error, null)
                return@getMetaData
            }

            val list =
                GsonTools.toList(GsonTools.beanToString(value), AUIChoristerModel::class.java)
                    ?: mutableListOf()
            chorusList.clear()
            chorusList.addAll(list)
            callback?.onResult(null, list)
        }
    }

    override fun joinChorus(songCode: String?, userId: String?, callback: AUICallback?) {
        listCollection.addMetaData(
            AUIChorusCmd.joinChorusCmd.name,
            mapOf(
                "songCode" to (songCode ?: ""),
                "userId" to (userId ?: "")
            ),
            listOf(mapOf("userId" to (userId ?: ""))),
            callback
        )
    }

    override fun leaveChorus(songCode: String?, userId: String?, callback: AUICallback?) {
        listCollection.removeMetaData(
            AUIChorusCmd.leaveChorusCmd.name,
            listOf(mapOf("userId" to (userId ?: ""))),
            callback
        )
    }

    override fun switchSingerRole(newRole: Int, callback: AUISwitchSingerRoleCallback?) {
        ktvApi.switchSingerRole(KTVSingRole.values().firstOrNull { it.value == newRole }
            ?: KTVSingRole.Audience, object :
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
        if (userList.size != chorusList.size) {
            val metaData = mutableMapOf<String, String>()
            metaData[kChorusKey] = GsonTools.beanToString(userId) ?: ""
            rtmManager.setBatchMetadata(
                channelName,
                metadata = metaData
            ) { error ->
                if (error != null) {
                    completion?.onResult(AUIException(AUIException.ERROR_CODE_RTM, ""))
                } else {
                    completion?.onResult(null)
                }
            }
        }
    }

    private fun onAttributeChanged(channelName: String, key: String, value: AUIAttributesModel) {
        if (key != kChorusKey) {
            return
        }
        Log.d(TAG, "channelName:$channelName,key:$key,value:$value")

        val json = GsonTools.beanToString(value.getList()) ?: return
        var chorusLists: List<AUIChoristerModel> = mutableListOf()
        if (json.contains("[")) {
            chorusLists = GsonTools.toList(json, AUIChoristerModel::class.java) ?: mutableListOf()
        }

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

    private fun metadataWillAdd(
        publisherId: String,
        valueCmd: String?,
        value: Map<String, Any>
    ): AUIException? {
        return null
    }

}
package io.agora.auikit.service.imp

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.ToNumberPolicy
import com.google.gson.reflect.TypeToken
import io.agora.auikit.model.AUIChoristerModel
import io.agora.auikit.service.IAUIChorusService
import io.agora.auikit.service.callback.AUICallback
import io.agora.auikit.service.callback.AUIChoristerListCallback
import io.agora.auikit.service.callback.AUIException
import io.agora.auikit.service.callback.AUISwitchSingerRoleCallback
import io.agora.auikit.service.http.CommonResp
import io.agora.auikit.service.http.HttpManager
import io.agora.auikit.service.http.chorus.ChorusInterface
import io.agora.auikit.service.http.chorus.ChorusReq
import io.agora.auikit.service.ktv.ISwitchRoleStateListener
import io.agora.auikit.service.ktv.KTVApi
import io.agora.auikit.service.ktv.KTVSingRole
import io.agora.auikit.service.ktv.SwitchRoleFailReason
import io.agora.auikit.service.rtm.AUIRtmManager
import io.agora.auikit.service.rtm.AUIRtmMsgProxyDelegate
import io.agora.auikit.utils.DelegateHelper
import retrofit2.Call
import retrofit2.Response

class AUIChorusServiceImpl constructor(
    private val channelName: String,
    private val ktvApi: KTVApi,
    private val rtmManager: AUIRtmManager
) : IAUIChorusService, AUIRtmMsgProxyDelegate {
    private val TAG: String = "Chorus_LOG"
    private val kChorusKey = "chorus"

    init {
        rtmManager.subscribeMsg(channelName, kChorusKey, this)
    }

    private val gson: Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd HH:mm:ss")
        .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
        .create()

    private var chorusList = mutableListOf<AUIChoristerModel>() // 合唱者列表

    private val delegateHelper = DelegateHelper<IAUIChorusService.AUIChorusRespDelegate>()
    override fun bindRespDelegate(delegate: IAUIChorusService.AUIChorusRespDelegate?) {
        delegateHelper.bindDelegate(delegate)
    }

    override fun unbindRespDelegate(delegate: IAUIChorusService.AUIChorusRespDelegate?) {
        delegateHelper.unBindDelegate(delegate)
    }

    override fun getChannelName() = channelName

    override fun getChoristersList(callback: AUIChoristerListCallback?) {
        callback?.onResult(AUIException(0, ""), chorusList)
    }

    override fun joinChorus(songCode: String?, userId: String?, callback: AUICallback?) {
        val code = songCode ?: return
        val uid = userId ?: return
        val param = ChorusReq(
            channelName,
            code,
            uid
        )
        Log.d(TAG, "joinChorus called")
        HttpManager.getService(ChorusInterface::class.java).choursJoin(param)
            .enqueue(object : retrofit2.Callback<CommonResp<Any>> {
            override fun onResponse(call: Call<CommonResp<Any>>, response: Response<CommonResp<Any>>) {
                if (response.body()?.code == 0) {
                    Log.d(TAG, "joinChorus success")
                    callback?.onResult(null)
                } else {
                    Log.d(TAG, "joinChorus failed: " + response.body()?.code + " " + response.body()?.message)
                    callback?.onResult(
                        AUIException(
                            response.body()?.code ?: -1,
                            response.body()?.message
                        )
                    )
                }
            }
            override fun onFailure(call: Call<CommonResp<Any>>, t: Throwable) {
                callback?.onResult(
                    AUIException(
                        -1,
                        t.message
                    )
                )
            }
        })
    }

    override fun leaveChorus(songCode: String?, userId: String?, callback: AUICallback?) {
        val code = songCode ?: return
        val uid = userId ?: return
        val param = ChorusReq(
            channelName,
            code,
            uid
        )
        HttpManager.getService(ChorusInterface::class.java).choursLeave(param)
            .enqueue(object : retrofit2.Callback<CommonResp<Any>> {
                override fun onResponse(call: Call<CommonResp<Any>>, response: Response<CommonResp<Any>>) {
                    if (response.body()?.code == 0) {
                        callback?.onResult(null)
                    } else {
                        callback?.onResult(
                            AUIException(
                                response.body()?.code ?: -1,
                                response.body()?.message
                            )
                        )
                    }
                }
                override fun onFailure(call: Call<CommonResp<Any>>, t: Throwable) {
                    callback?.onResult(
                        AUIException(
                            -1,
                            t.message
                        )
                    )
                }
            })
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

    override fun onMsgDidChanged(channelName: String, key: String, value: Any) {
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
                delegateHelper.notifyDelegate { delegate: IAUIChorusService.AUIChorusRespDelegate ->
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
                delegateHelper.notifyDelegate { delegate: IAUIChorusService.AUIChorusRespDelegate ->
                    delegate.onChoristerDidLeave(oldChorister)
                }
            }
        }
        this.chorusList.clear()
        this.chorusList.addAll(chorusLists)

        delegateHelper.notifyDelegate { delegate: IAUIChorusService.AUIChorusRespDelegate ->
            //delegate
            delegate.onChoristerDidChanged()
        }
    }
}
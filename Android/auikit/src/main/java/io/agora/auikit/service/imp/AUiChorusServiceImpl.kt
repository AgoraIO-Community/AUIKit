package io.agora.auikit.service.imp

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.ToNumberPolicy
import com.google.gson.reflect.TypeToken
import io.agora.auikit.model.AUiChoristerModel
import io.agora.auikit.model.AUiRoomContext
import io.agora.auikit.service.IAUiChorusService
import io.agora.auikit.service.callback.AUiCallback
import io.agora.auikit.service.callback.AUiChoristerListCallback
import io.agora.auikit.service.callback.AUiException
import io.agora.auikit.service.callback.AUiSwitchSingerRoleCallback
import io.agora.auikit.service.http.CommonResp
import io.agora.auikit.service.http.HttpManager
import io.agora.auikit.service.http.chorus.ChorusInterface
import io.agora.auikit.service.http.chorus.ChorusReq
import io.agora.auikit.service.ktv.ISwitchRoleStateListener
import io.agora.auikit.service.ktv.KTVApi
import io.agora.auikit.service.ktv.KTVSingRole
import io.agora.auikit.service.ktv.SwitchRoleFailReason
import io.agora.auikit.service.rtm.AUiRtmManager
import io.agora.auikit.service.rtm.AUiRtmMsgProxyDelegate
import io.agora.auikit.utils.DelegateHelper
import retrofit2.Call
import retrofit2.Response

class AUiChorusServiceImpl constructor(
    private val channelName: String,
    private val ktvApi: KTVApi,
    private val rtmManager: AUiRtmManager
) : IAUiChorusService, AUiRtmMsgProxyDelegate {
    private val TAG: String = "Chorus_LOG"
    private val kChorusKey = "chorus"

    init {
        rtmManager.subscribeMsg(channelName, kChorusKey, this)
    }

    private val gson: Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd HH:mm:ss")
        .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
        .create()

    private var chorusList = mutableListOf<AUiChoristerModel>() // 合唱者列表

    private val delegateHelper = DelegateHelper<IAUiChorusService.AUiChorusRespDelegate>()
    override fun bindRespDelegate(delegate: IAUiChorusService.AUiChorusRespDelegate?) {
        delegateHelper.bindDelegate(delegate)
    }

    override fun unbindRespDelegate(delegate: IAUiChorusService.AUiChorusRespDelegate?) {
        delegateHelper.unBindDelegate(delegate)
    }

    override fun getChannelName() = channelName

    override fun getChoristersList(callback: AUiChoristerListCallback?) {
        callback?.onResult(AUiException(0, ""), chorusList)
    }

    override fun joinChorus(songCode: String?, userId: String?, callback: AUiCallback?) {
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
                    callback?.onResult(AUiException(response.body()?.code ?: -1, response.body()?.message))
                }
            }
            override fun onFailure(call: Call<CommonResp<Any>>, t: Throwable) {
                callback?.onResult(AUiException(-1, t.message))
            }
        })
    }

    override fun leaveChorus(songCode: String?, userId: String?, callback: AUiCallback?) {
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
                        callback?.onResult(AUiException(response.body()?.code ?: -1, response.body()?.message))
                    }
                }
                override fun onFailure(call: Call<CommonResp<Any>>, t: Throwable) {
                    callback?.onResult(AUiException(-1, t.message))
                }
            })
    }

    override fun switchSingerRole(newRole: Int, callback: AUiSwitchSingerRoleCallback?) {
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
        val chorusLists: List<AUiChoristerModel> =
            gson.fromJson(value as String, object : TypeToken<List<AUiChoristerModel>>() {}.type) ?: mutableListOf()
        chorusLists.forEach { newChorister ->
            var hasChorister = false
            this.chorusList.forEach {
                if (it.userId == newChorister.userId) {
                    hasChorister = true
                }
            }
            if (!hasChorister) {
                delegateHelper.notifyDelegate { delegate: IAUiChorusService.AUiChorusRespDelegate ->
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
                delegateHelper.notifyDelegate { delegate: IAUiChorusService.AUiChorusRespDelegate ->
                    delegate.onChoristerDidLeave(oldChorister)
                }
            }
        }
        this.chorusList.clear()
        this.chorusList.addAll(chorusLists)

        delegateHelper.notifyDelegate { delegate: IAUiChorusService.AUiChorusRespDelegate ->
            //delegate
            delegate.onChoristerDidChanged()
        }
    }
}
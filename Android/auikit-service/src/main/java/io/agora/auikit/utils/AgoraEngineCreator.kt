package io.agora.auikit.utils

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import io.agora.chat.ChatClient
import io.agora.chat.ChatOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.RtcEngineEx
import io.agora.rtm.RtmClient
import io.agora.rtm.RtmConfig

object AgoraEngineCreator {

    fun createRtmClient(
        context: Context,
        appId: String,
        userId: String
    ): RtmClient {
        val rtmConfig = RtmConfig.Builder(appId, userId)
            .build()
        return RtmClient.create(rtmConfig)
    }

    fun createRtcEngine(context: Context, appId: String): RtcEngineEx {
        // ------------------ 初始化RTC ------------------
        var rtcEngine: RtcEngineEx? = null
        val config = RtcEngineConfig()
        config.mContext = context
        config.mAppId = appId
        config.mEventHandler = object : IRtcEngineEventHandler() {
            override fun onError(err: Int) {
                super.onError(err)
                AUILogger.logger().e("createRtcEngine", "mEventHandler onError $err")
            }
        }
        config.mChannelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
        config.mAudioScenario = Constants.AUDIO_SCENARIO_CHORUS
        try {
            rtcEngine = RtcEngine.create(config) as RtcEngineEx
        } catch (e: Exception) {
            e.printStackTrace()
            AUILogger.logger().e("createRtcEngine", "RtcEngine.create() called error: $e")
        }
        rtcEngine?.loadExtensionProvider("agora_drm_loader")
        return rtcEngine ?: throw RuntimeException("RtcEngine create failed!")
    }

    fun createChatClient(
        context: Context,
        appKey: String?,
    ){
        // ------------------ 初始化IM ------------------
        val options = ChatOptions()
        options.appKey = appKey
        options.autoLogin = false
        if (!isMainProcess(context)) {
            return
        }
        ChatClient.getInstance().init(context, options)
    }

    private fun isMainProcess(context: Context): Boolean {
        val processName: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getProcessNameByApplication()
        } else {
            getProcessNameByReflection()
        }
        return context.applicationInfo.packageName == processName
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private fun getProcessNameByApplication(): String {
        return Application.getProcessName()
    }

    private fun getProcessNameByReflection(): String {
        var processName: String = ""
        try {
            val declaredMethod = Class.forName(
                "android.app.ActivityThread", false,
                Application::class.java.classLoader
            )
                .getDeclaredMethod("currentProcessName", *arrayOfNulls<Class<*>?>(0))
            declaredMethod.isAccessible = true
            val invoke = declaredMethod.invoke(null, *arrayOfNulls(0))
            if (invoke is String) {
                processName = invoke
            }
        } catch (e: Throwable) {
        }
        return processName
    }
}
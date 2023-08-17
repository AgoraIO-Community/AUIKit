package io.agora.app.sample

import android.app.Application
import io.agora.auikit.model.AUICommonConfig
import io.agora.auikit.model.AUIRoomContext
import io.agora.auikit.utils.AUILogger
import java.util.*

class SampleApplication: Application() {
    private val mUserId = Random().nextInt(99999999).toString()

    override fun onCreate() {
        super.onCreate()
//        HttpManager.setBaseURL(BuildConfig.SERVER_HOST)
        AUILogger.initLogger(AUILogger.Config(applicationContext, "Voice"))

        val config = AUICommonConfig()
        config.context = applicationContext
//        config.appId = BuildConfig.AGORA_APP_ID
        config.userId = mUserId
        config.userName = randomUserName()
        config.userAvatar = randomAvatar()

        AUIRoomContext.shared().commonConfig = config

    }

    private fun randomAvatar(): String {
        val randomValue = Random().nextInt(8) + 1
        return "https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/sample_avatar/sample_avatar_${randomValue}.png"
    }

    private fun randomUserName(): String {
        val userNames = arrayListOf(
            "安迪",
            "路易",
            "汤姆",
            "杰瑞",
            "杰森",
            "布朗",
            "吉姆",
            "露西",
            "莉莉",
            "韩梅梅",
            "李雷",
            "张三",
            "李四",
            "小红",
            "小明",
            "小刚",
            "小霞",
            "小智",
        )
        val randomValue = Random().nextInt(userNames.size) + 1
        return userNames[randomValue % userNames.size]
    }
}
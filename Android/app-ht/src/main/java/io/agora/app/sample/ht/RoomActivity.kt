package io.agora.app.sample.ht

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import io.agora.app.sample.ht.databinding.RoomActivityBinding
import io.agora.app.sample.ht.micseats.IMicSeatsView
import io.agora.rtc.Constants
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.RtcEngineConfig
import java.util.Random

class RoomActivity : AppCompatActivity() {

    companion object{
        private val EXTRA_CHANNEL_NAME = "ChannelName";

        fun start(context: Context, channelName: String){
            val intent = Intent(context, RoomActivity::class.java)
            intent.putExtra(EXTRA_CHANNEL_NAME, channelName)
            context.startActivity(intent)
        }
    }

    private val mBinding by lazy {
        RoomActivityBinding.inflate(LayoutInflater.from(this))
    }

    private val mChannelName by lazy {
        intent.getStringExtra(EXTRA_CHANNEL_NAME)
    }

    private val mRtcEngine by lazy {
        val config = RtcEngineConfig()
        config.mContext = applicationContext
        config.mAppId = getString(R.string.AGORA_APP_ID)
        config.mEventHandler = object: IRtcEngineEventHandler(){

            override fun onUserJoined(uid: Int, elapsed: Int) {
                super.onUserJoined(uid, elapsed)
                runOnUiThread {
                    // 远端用户上麦
                    mBinding.micSteasView.upMicSeat(uid, -1)?.let {
                        it.setTitleText(randomUserName())
                        it.setUserAvatarImageUrl(randomAvatar())
                    }
                }
            }

            override fun onUserOffline(uid: Int, reason: Int) {
                super.onUserOffline(uid, reason)
                runOnUiThread {
                    // 远端用户下麦
                    mBinding.micSteasView.downMicSeat(uid)?.let {
                        it.setTitleText(getString(R.string.aui_micseat_item_title_idle, it.index + 1))
                        it.setUserAvatarImageDrawable(null)
                    }
                }
            }

        }
        RtcEngine.create(config)
    }

    private val localUserId = Random().nextInt(10000) + 100000



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)

        initRtcEngine()
        initMicSeatsView()
        mBinding.stAudio.setOnCheckedChangeListener { buttonView, isChecked ->
            mRtcEngine.enableLocalAudio(isChecked)

            // 本地静音
            mBinding.micSteasView.findMicSeatItemView(localUserId)?.setAudioMuteVisibility(
                if(!isChecked) View.VISIBLE else View.GONE
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mRtcEngine.leaveChannel()
        RtcEngine.destroy()
    }

    private fun initRtcEngine(){
        mRtcEngine.disableVideo()
        mRtcEngine.enableAudio()
        mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)
        mRtcEngine.setClientRole(Constants.CLIENT_ROLE_AUDIENCE)
        mRtcEngine.joinChannel("", mChannelName, "", localUserId)
    }

    private fun initMicSeatsView(){
        val micSeats = mBinding.micSteasView as IMicSeatsView

        micSeats.setMicSeatCount(8).forEach {
            it.setTitleText(getString(R.string.aui_micseat_item_title_idle, it.index + 1))
        }
        micSeats.setMicSeatActionDelegate { userId, itemView ->
            if (userId < 0 && !isLocalUpMicSeat()) {
                // 本地上麦
                mBinding.micSteasView.upMicSeat(localUserId, itemView.index)?.let {
                    it.setTitleText(randomUserName())
                    it.setUserAvatarImageUrl(randomAvatar())

                    mRtcEngine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER)
                }

            } else if (userId == localUserId) {
                // 本地下麦
                mBinding.micSteasView.downMicSeat(localUserId)?.let {
                    it.setTitleText(getString(R.string.aui_micseat_item_title_idle, it.index + 1))
                    it.setUserAvatarImageDrawable(null)

                    mRtcEngine.setClientRole(Constants.CLIENT_ROLE_AUDIENCE)
                }
            }
        }
    }

    private fun isLocalUpMicSeat(): Boolean {
        return mBinding.micSteasView.findMicSeatItemView(localUserId) != null
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

    private fun randomAvatar(): String {
        val randomValue = Random().nextInt(8) + 1
        return "https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/sample_avatar/sample_avatar_${randomValue}.png"
    }
}
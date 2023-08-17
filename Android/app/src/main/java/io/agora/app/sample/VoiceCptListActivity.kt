package io.agora.app.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.agora.app.sample.databinding.VoiceComponentsLayoutBinding
import io.agora.app.sample.dialog.VoiceMoreItemBean
import io.agora.app.sample.dialog.VoiceRoomMoreDialog
import io.agora.auikit.model.AUICommonConfig
import io.agora.auikit.model.AUIRoomContext
import io.agora.auikit.ui.action.AUIActionUserInfo
import io.agora.auikit.ui.action.AUIActionUserInfoList
import io.agora.auikit.ui.action.impI.AUIApplyDialog
import io.agora.auikit.ui.action.impI.AUIInvitationDialog
import io.agora.auikit.ui.action.listener.AUIApplyDialogEventListener
import io.agora.auikit.ui.action.listener.AUIInvitationDialogEventListener
import io.agora.auikit.ui.basic.AUIBottomDialog
import io.agora.auikit.ui.chatBottomBar.impl.AUIKeyboardStatusWatcher
import io.agora.auikit.ui.chatBottomBar.listener.AUIMenuItemClickListener
import io.agora.auikit.ui.chatList.AUIChatInfo
import io.agora.auikit.ui.chatList.impl.AUIBroadcastMessageLayout
import io.agora.auikit.ui.chatList.impl.AUIBroadcastMessageView
import io.agora.auikit.ui.gift.AUIGiftInfo
import io.agora.auikit.ui.gift.AUIGiftTabInfo
import io.agora.auikit.ui.gift.impl.dialog.AUiGiftListView
import io.agora.auikit.ui.member.MemberInfo
import io.agora.auikit.ui.member.impl.AUIRoomMemberListView
import io.agora.auikit.utils.FastClickTools
import io.agora.auikit.utils.GsonTools
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class VoiceCptListActivity : AppCompatActivity() {
    private var themeId = R.style.Theme_Sample_Voice
    private lateinit var mViewBinding:VoiceComponentsLayoutBinding
    private var giftList: List<AUIGiftTabInfo> = mutableListOf()
    private var messageList:ArrayList<AUIChatInfo> = ArrayList<AUIChatInfo>()
    private var isMicEnable = false
    private lateinit var moreDialog:VoiceRoomMoreDialog
    private lateinit var applyDialog:AUIApplyDialog
    private lateinit var invitationDialog:AUIInvitationDialog
    private lateinit var dialogMemberView:AUIRoomMemberListView

    private var memberList:MutableList<MemberInfo?> = mutableListOf()
    private var seatMap: MutableMap<Int, String?> = mutableMapOf()
    private var inviteList: MutableList<AUIActionUserInfo?> = mutableListOf()
    private var applyList: MutableList<AUIActionUserInfo?> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(themeId)
        loadLocalData()
        initView()
    }

    private fun initView(){
        mViewBinding = VoiceComponentsLayoutBinding.inflate(LayoutInflater.from(this))
        setContentView(mViewBinding.root)
        initListener()
        setChatList()
        setBroadcastMessage()

        if (messageList.size > 0){
            mViewBinding.chatListView.refreshSelectLast(messageList)
        }
        //获取软键盘高度
        getSoftKeyboardHeight()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(100, 1003, 0, "改变主题")
        menu.add(100, 1004, 0, "返回上级")
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            1003 -> {
                themeId = if (themeId == R.style.Theme_Sample_Voice) {
                    R.style.Theme_Sample_Voice_Dark
                } else {
                    R.style.Theme_Sample_Voice
                }
                setTheme(themeId)
                initView()
            }
            1004 -> { finish() }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initListener(){
        mViewBinding.chatBottomBar.setMenuItemClickListener(object : AUIMenuItemClickListener{
            override fun onChatExtendMenuItemClick(itemId: Int, view: View?) {
                when(itemId){
                    io.agora.auikit.ui.R.id.voice_extend_item_more ->{
                        if (view?.let { FastClickTools.isFastClick(it) } == true) return
                        showMoreDialog()
                    }
                    io.agora.auikit.ui.R.id.voice_extend_item_mic ->{
                        if (view?.let { FastClickTools.isFastClick(it) } == true) return
                        isMicEnable = !isMicEnable
                        mViewBinding.chatBottomBar.setEnableMic(isMicEnable)
                    }
                    io.agora.auikit.ui.R.id.voice_extend_item_gift ->{
                        if (view?.let { FastClickTools.isFastClick(it) } == true) return
                        showBottomGiftDialog()
                    }
                    io.agora.auikit.ui.R.id.voice_extend_item_like ->{
                        mViewBinding.likeView.addFavor()
                    }
                }
            }

            override fun onSendMessage(content: String?) {
                createTxtMsg(
                    AUIRoomContext.shared().currentUserInfo.userId,
                    AUIRoomContext.shared().currentUserInfo.userName,
                    content
                )
            }
        })
        mViewBinding.broadcast.setSubtitleStatusChangeListener(object :
            AUIBroadcastMessageLayout.SubtitleStatusChangeListener{
            override fun onShortSubtitleShow(textView: TextView) {

            }

            override fun onLongSubtitleRollEnd(textView: TextView) {

            }
        })
    }

    private fun loadLocalData(){
        val config = AUICommonConfig()
        config.context = applicationContext
        config.userId = randomId()
        config.userName = randomUserName()
        config.userAvatar = randomAvatar()
        AUIRoomContext.shared().commonConfig = config

        val jsonFileName = "gift.json" // 指定要读取的文件名
        val json: String
        try {
            val inputStream = this.assets.open(jsonFileName)
            json = inputStream.bufferedReader().use { it.readText() }
            json.let {
                val jsonObject = JSONObject(it)
                giftList = GsonTools.toList(jsonObject.getString("data"),AUIGiftTabInfo::class.java)!!
            }
        } catch (e: Exception) {
            // 处理异常
            e.printStackTrace()
        }

        memberList.add(0,MemberInfo(randomId(),randomUserName(),randomAvatar()))
        memberList.add(1,MemberInfo(randomId(),randomUserName(),randomAvatar()))
        memberList.add(2,MemberInfo(randomId(),randomUserName(),randomAvatar()))

        inviteList.add(AUIActionUserInfo(randomId(),randomUserName(),randomAvatar(),2))
        inviteList.add(AUIActionUserInfo(randomId(),randomUserName(),randomAvatar(),3))
        inviteList.add(AUIActionUserInfo(randomId(),randomUserName(),randomAvatar(),4))

        applyList.add(AUIActionUserInfo(randomId(),randomUserName(),randomAvatar(),5))
        applyList.add(AUIActionUserInfo(randomId(),randomUserName(),randomAvatar(),6))
        applyList.add(AUIActionUserInfo(randomId(),randomUserName(),randomAvatar(),7))

        seatMap[0] = AUIRoomContext.shared().currentUserInfo.userId

        createWelcomeMessage(AUIRoomContext.shared().currentUserInfo.userId,AUIRoomContext.shared().currentUserInfo.userName)
        createSystemMsg(AUIRoomContext.shared().currentUserInfo.userId,AUIRoomContext.shared().currentUserInfo.userName)

    }

    private fun setBroadcastMessage(){
        //设置弹幕滚动速度
        mViewBinding.broadcast.setScrollSpeed(AUIBroadcastMessageView.SPEED_SLOW)
        //设置自动清理时间
        mViewBinding.broadcast.setDelayMillis(3000L)
    }


    private fun setChatList(){
        mViewBinding.chatListView.setOwnerId(AUIRoomContext.shared().currentUserInfo.userId)
    }

    fun setPraiseEffectConfig(){
        //设置图片资源 不设置使用默认数据
        //mViewBinding.likeView.setDrawableIds()
        //更多设置在style中 可以设置贝塞尔曲线、动画持续时长等
    }

    override fun onDestroy() {
        super.onDestroy()
        mViewBinding.chatBottomBar.setMenuItemClickListener(null)
        mViewBinding.broadcast.clearTask()
    }

    private fun getSoftKeyboardHeight(){
        AUIKeyboardStatusWatcher(
            this, this
        ) { isKeyboardShowed: Boolean, keyboardHeight: Int? ->
            mViewBinding.chatBottomBar.onSoftKeyboardHeightChanged(isKeyboardShowed,keyboardHeight)
        }
    }

    private fun showBottomGiftDialog(){
        val dialog = AUiGiftListView(this, giftList)
        dialog.setDialogActionListener(object : AUiGiftListView.ActionListener{
            override fun onGiftSend(bean: AUIGiftInfo?) {
                bean?.let { it ->
                    it.sendUserId = AUIRoomContext.shared().currentUserInfo.userId
                    it.sendUserName = AUIRoomContext.shared().currentUserInfo.userName
                    it.sendUserAvatar = AUIRoomContext.shared().currentUserInfo.userAvatar
                    it.giftCount = 1
                    mViewBinding.giftView.refresh(mutableListOf(it))
                }
            }
        })
        dialog.show(this.supportFragmentManager, "gift_dialog")
    }

    private fun createTxtMsg(userId:String,userName:String,content:String?) {
        val sendChatEntity = AUIChatInfo(
            userId,userName,content,false
        )
        messageList.add(sendChatEntity)
        mViewBinding.chatListView.refreshSelectLast(messageList)
    }

    private fun createWelcomeMessage(userId:String,userName:String){
        val sendChatEntity = AUIChatInfo(
            userId,userName,
            getString(R.string.voice_room_welcome)
            ,false
        )
        messageList.add(sendChatEntity)
    }

    private fun createSystemMsg(userId:String,userName:String){
        val sendChatEntity = AUIChatInfo(
            userId,userName,"",true
        )
        messageList.add(sendChatEntity)
    }

    private fun showMoreDialog(){
        val list = mutableListOf<VoiceMoreItemBean>()
        val applyBean = VoiceMoreItemBean()
        applyBean.ItemTitle = "申请列表"
        list.add(applyBean)

        val inviteBean = VoiceMoreItemBean()
        inviteBean.ItemTitle = "邀请列表"
        list.add(inviteBean)

        val memberBean = VoiceMoreItemBean()
        memberBean.ItemTitle = "成员列表"
        list.add(memberBean)

        val broadcastBean = VoiceMoreItemBean()
        broadcastBean.ItemTitle = "全局广播"
        list.add(broadcastBean)

        moreDialog = VoiceRoomMoreDialog(this,list,object :
            VoiceRoomMoreDialog.GridViewItemClickListener{
            override fun onItemClickListener(position: Int) {
                when(position){
                    0 -> {  showApplyDialog() }
                    1 -> {  showInvitationDialog() }
                    2 -> {  showMemberDialog() }
                    3 -> {  showBroadcastMessage() }
                }
            }
        })
        moreDialog.show(this.supportFragmentManager,"more_dialog")
    }

    private fun showApplyDialog(){
        val applyInfo = AUIActionUserInfoList()
        applyInfo.userList = applyList

        applyDialog = AUIApplyDialog()
        applyDialog.apply {
            arguments = Bundle().apply {
                putSerializable(AUIApplyDialog.KEY_ROOM_APPLY_BEAN, applyInfo)
                putInt(AUIApplyDialog.KEY_CURRENT_ITEM, 0)
            }
            setApplyDialogListener(object : AUIApplyDialogEventListener {
                override fun onApplyItemClick(
                    view: View,
                    applyIndex: Int?,
                    user: AUIActionUserInfo?,
                    position: Int
                ) {
                    Toast.makeText(this@VoiceCptListActivity, "同意 ${user?.userName} 上麦申请", Toast.LENGTH_SHORT).show()
                }
            })
        }
        applyDialog.show(this.supportFragmentManager,"AUIApplyDialog")
    }

    private fun showInvitationDialog(){
        val invitationInfo = AUIActionUserInfoList()
        invitationInfo.userList = inviteList
        invitationInfo.invitedIndex = 2

        invitationDialog = AUIInvitationDialog()
        invitationDialog.apply {
            arguments = Bundle().apply {
                putSerializable(AUIInvitationDialog.KEY_ROOM_INVITED_BEAN, invitationInfo)
                putInt(AUIInvitationDialog.KEY_CURRENT_ITEM, 0)
            }
            setInvitationDialogListener(object : AUIInvitationDialogEventListener {
                override fun onInvitedItemClick(view: View, invitedIndex: Int, user: AUIActionUserInfo?) {
                    Toast.makeText(this@VoiceCptListActivity, "邀请 ${user?.userName} 上$invitedIndex 号麦", Toast.LENGTH_SHORT).show()
                }
            })
        }
        invitationDialog.show(this.supportFragmentManager,"AUIInvitationDialog")
    }

    private fun showMemberDialog(){
        dialogMemberView = AUIRoomMemberListView(this)
        dialogMemberView.setMembers(memberList, seatMap)
        dialogMemberView.setIsOwner(true, AUIRoomContext.shared().currentUserInfo.userId)
        dialogMemberView.setMemberActionListener(object : AUIRoomMemberListView.ActionListener{
            override fun onKickClick(view: View, position: Int, user: MemberInfo?) {
                Toast.makeText(this@VoiceCptListActivity, "onKickClick ${user?.userName}", Toast.LENGTH_SHORT).show()
            }
        })
        dialogMemberView.let {
            AUIBottomDialog(this).apply {
                setCustomView(it)
                show()
            }
        }
    }

    private fun showBroadcastMessage(){
        //设置广播弹幕内容
        mViewBinding.broadcast.showSubtitleView("欢迎来到语聊房!")
        mViewBinding.broadcast.showSubtitleView("测试这是一个超长文本!测试这是一个超长文本!测试这是一个超长文本!测试这是一个超长文本!")
    }


    private fun randomId():String{
        return Random().nextInt(99999999).toString()
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

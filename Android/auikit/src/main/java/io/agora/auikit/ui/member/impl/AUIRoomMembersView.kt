package io.agora.auikit.ui.member.impl

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.agora.auikit.R
import io.agora.auikit.databinding.AuiMemberLayoutBinding
import io.agora.auikit.model.AUIUserInfo
import io.agora.auikit.ui.basic.AUIImageView
import io.agora.auikit.ui.member.IAUIRoomMembersView
import io.agora.auikit.ui.member.listener.AUIRoomMembersActionListener

class AUIRoomMembersView : ConstraintLayout,
    IAUIRoomMembersView {
    private val mRoomViewBinding = AuiMemberLayoutBinding.inflate(LayoutInflater.from(context))
    private var aUpperRightListener: AUIRoomMembersActionListener? = null
    private var aContext: Context? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        this.aContext = context
        addView(mRoomViewBinding.root)
        initListener()
    }

    private fun initListener(){
        mRoomViewBinding.llMemberRank.setOnClickListener{
            aUpperRightListener?.onMemberRankClickListener(it)
        }

        mRoomViewBinding.btnUserMore.setOnClickListener{
            aUpperRightListener?.onMemberRightUserMoreClickListener(it)
        }

        mRoomViewBinding.btnShutDown.setOnClickListener{
            aUpperRightListener?.onMemberRightShutDownClickListener(it)
        }
    }

    override fun setMemberActionListener(listener: AUIRoomMembersActionListener?) {
        this.aUpperRightListener = listener
    }

    /**
     * 设置排行榜前三
     */
    override fun setMemberData(memberList: List<AUIUserInfo?>){
        val size = memberList.size
        size.let {
            if (it > 3){
                setMemberView(3, memberList)
                return
            }
            setMemberView(it, memberList)
        }

    }

    private fun setMemberView(size:Int,rankList:List<AUIUserInfo?>){
        when(size){
            0 -> {
                mRoomViewBinding.ivMember1.visibility = GONE
                mRoomViewBinding.ivMember2.visibility = GONE
                mRoomViewBinding.ivMember3.visibility = GONE
            }
            1 -> {
                mRoomViewBinding.ivMember1.visibility = VISIBLE
                mRoomViewBinding.ivMember2.visibility = GONE
                mRoomViewBinding.ivMember3.visibility = GONE
                rankList[0]?.let { setResources(it.userAvatar,mRoomViewBinding.ivMember1) }
            }
            2 -> {
                mRoomViewBinding.ivMember1.visibility = VISIBLE
                mRoomViewBinding.ivMember2.visibility = VISIBLE
                mRoomViewBinding.ivMember3.visibility = GONE
                rankList[0]?.let { setResources(it.userAvatar,mRoomViewBinding.ivMember1) }
                rankList[1]?.let { setResources(it.userAvatar,mRoomViewBinding.ivMember2) }
            }
            3 -> {
                mRoomViewBinding.ivMember1.visibility = VISIBLE
                mRoomViewBinding.ivMember2.visibility = VISIBLE
                mRoomViewBinding.ivMember3.visibility = VISIBLE
                rankList[0]?.let { setResources(it.userAvatar,mRoomViewBinding.ivMember1) }
                rankList[1]?.let { setResources(it.userAvatar,mRoomViewBinding.ivMember2) }
                rankList[2]?.let { setResources(it.userAvatar,mRoomViewBinding.ivMember3) }
            }
        }
    }

    private fun setResources(url:String,view: AUIImageView){
        aContext?.let {
            Glide.with(it)
                .load(url)
                .error(R.drawable.aui_room_info_avatar)
                .apply(RequestOptions.circleCropTransform())
                .into(view)
        }
    }


    /**
     * 设置右侧icon
     */
    override fun setRightIconResources(url:String,view: AUIImageView){
        setResources(url,view)
    }



}
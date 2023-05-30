package io.agora.auikit.ui.member.impl

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.agora.auikit.databinding.AuiMemberListItemBinding
import io.agora.auikit.databinding.AuiMemberListViewLayoutBinding
import io.agora.auikit.model.AUIUserInfo
import io.agora.auikit.service.IAUIMicSeatService
import io.agora.auikit.service.IAUIUserService

private class MemberItemModel (
    val user: AUIUserInfo,
    val micIndex: Int?){

    fun micString(): String? {
        if (micIndex == null) {
            return null
        }
        return if (micIndex == 0) {
            "房主"
        } else {
            "${micIndex + 1}号麦"
        }
    }
}

class AUIRoomMemberListView : FrameLayout,
    IAUIUserService.AUIUserRespDelegate,
    IAUIMicSeatService.AUIMicSeatRespDelegate {

    private val mBinding by lazy { AuiMemberListViewLayoutBinding.inflate(
        LayoutInflater.from(
            context
        )
    ) }

    private lateinit var listAdapter: ListAdapter<MemberItemModel, io.agora.auikit.utils.BindingViewHolder<AuiMemberListItemBinding>>

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        addView(mBinding.root)
        initView()
    }

    private fun initView() {
        mBinding.rvUserList.layoutManager = LinearLayoutManager(context)
        listAdapter =
            object : ListAdapter<MemberItemModel, io.agora.auikit.utils.BindingViewHolder<AuiMemberListItemBinding>>(object :
                DiffUtil.ItemCallback<MemberItemModel>() {
                override fun areItemsTheSame(oldItem: MemberItemModel, newItem: MemberItemModel) =
                    oldItem.user.userId == newItem.user.userId

                override fun areContentsTheSame(
                    oldItem: MemberItemModel,
                    newItem: MemberItemModel
                ) = false

            }) {
                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ) =
                    io.agora.auikit.utils.BindingViewHolder(
                        AuiMemberListItemBinding.inflate(
                            LayoutInflater.from(parent.context)
                        )
                    )

                override fun onBindViewHolder(
                    holder: io.agora.auikit.utils.BindingViewHolder<AuiMemberListItemBinding>,
                    position: Int
                ) {
                    val item = getItem(position)
                    holder.binding.tvUserName.text = item.user.userName
                    if (item.micIndex != null) {
                        holder.binding.tvUserInfo.visibility = VISIBLE
                        holder.binding.tvUserInfo.text = item.micString()
                    } else {
                        holder.binding.tvUserInfo.visibility = GONE
                    }

                    Glide.with(holder.binding.ivAvatar)
                        .load(item.user.userAvatar)
                        .apply(RequestOptions.circleCropTransform())
                        .into(holder.binding.ivAvatar)
                }
            }
        mBinding.rvUserList.adapter = listAdapter
    }

    fun setMembers(members: List<AUIUserInfo>, seatMap: Map<Int, String>) {
        val temp = mutableListOf<MemberItemModel>()
        members.forEach {  user ->
            val micIndex = seatMap.entries.find { it.value == user.userId }?.key
            val item = MemberItemModel(user, micIndex)
            temp.add(item)
        }
        listAdapter.submitList(temp)
    }
}
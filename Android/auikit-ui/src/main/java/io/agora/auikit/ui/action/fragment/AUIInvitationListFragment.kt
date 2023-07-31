package io.agora.auikit.ui.action.fragment

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.google.android.material.textview.MaterialTextView
import io.agora.auikit.model.AUIActionModel
import io.agora.auikit.model.AUIUserInfo
import io.agora.auikit.ui.R
import io.agora.auikit.ui.basic.AUIImageView
import io.agora.auikit.ui.databinding.AuiInvitationListLayoutBinding
import io.agora.auikit.utils.ResourcesTools

class VoiceRoomInvitedListFragment : Fragment(),
    SwipeRefreshLayout.OnRefreshListener, VoiceInvitedAdapter.InvitedEventListener {

    private var mRoomViewBinding =  AuiInvitationListLayoutBinding.inflate(LayoutInflater.from(
        Companion.activity
    ))

    companion object {

        private const val KEY_ROOM_INFO = "room_info"
        private var activity: FragmentActivity?=null

        fun getInstance(fragmentActivity: FragmentActivity, roomBean: AUIActionModel): VoiceRoomInvitedListFragment {
            activity = fragmentActivity
            return VoiceRoomInvitedListFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(KEY_ROOM_INFO, roomBean)
                }
            }
        }
    }

    private var roomBean: AUIActionModel? = null
    private var invitedIndex:Int? = -1
    private var listener: InviteEventListener?=null

    private var total = 0
        set(value) {
            field = value
            checkEmpty()
        }
    private var members = mutableListOf<AUIUserInfo?>()

    private var invitedAdapter: VoiceInvitedAdapter?=null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return mRoomViewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        roomBean = arguments?.getSerializable(KEY_ROOM_INFO) as AUIActionModel?
        invitedIndex = roomBean?.invitedIndex

        val userList = roomBean?.userList
        userList?.let {
            total = it.size
            checkEmpty()
            members = it
        }

        mRoomViewBinding.apply {
            initAdapter(rvInvitedList)
            slInvitedList.setOnRefreshListener(this@VoiceRoomInvitedListFragment)
        }

    }

    private fun checkEmpty() {
        mRoomViewBinding.apply {
            if (total == 0) {
                ivContributionEmpty.isVisible = true
                mtContributionEmpty.isVisible = true
            } else {
                ivContributionEmpty.isVisible = false
                mtContributionEmpty.isVisible = false
            }
        }
    }

    private fun initAdapter(recyclerView: RecyclerView) {
        activity?.let {
            invitedAdapter = VoiceInvitedAdapter(it,invitedIndex,members)
            recyclerView.layoutManager = LinearLayoutManager(it)
            recyclerView.addItemDecoration(
                DividerItemDecoration(it, DividerItemDecoration.VERTICAL).apply {
                    // dividerThickness = 1.dp.toInt()
                    // dividerInsetStart = 15.dp.toInt()
                    // dividerInsetEnd = 15.dp.toInt()
                    // dividerColor = ResourcesTools.getColor(it.resources, R.color.aui_color_1f979797)
                    setDrawable(ColorDrawable(ResourcesTools.getColor(it.resources, R.color.aui_color_1f979797)))
                }
            )
            recyclerView.adapter = invitedAdapter
            invitedAdapter?.setInvitedEventListener(this)
        }
    }

    override fun onRefresh() {
        invitedAdapter?.notifyDataSetChanged()
        mRoomViewBinding.slInvitedList.isRefreshing = false
    }

    fun refreshData(userList:MutableList<AUIUserInfo?>){
        mRoomViewBinding.root.post {
            userList.let {
                total = it.size
                checkEmpty()
                invitedAdapter?.refresh(it)
            }
        }
    }

    override fun onInvitedClickListener(view: View, invitedIndex: Int, user: AUIUserInfo?) {
        this.listener?.onInviteItemClick(view,invitedIndex,user)
    }

    interface InviteEventListener{
        fun onInviteItemClick(view:View,invitedIndex:Int,user:AUIUserInfo?){}
    }

    fun setInviteEventListener(listener: InviteEventListener){
        this.listener = listener
    }

}

class VoiceInvitedAdapter constructor(
    context: Context,
    index: Int?,
    dataList:MutableList<AUIUserInfo?>
): RecyclerView.Adapter<InvitedViewHolder>()   {
    var dataList:MutableList<AUIUserInfo?> = mutableListOf()
    private var listener: InvitedEventListener?=null
    private var invitedIndex:Int?

    private var mContext:Context?=null
    init {
        this.mContext = context
        this.invitedIndex = index
        if (dataList != null) {
            this.dataList = dataList
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvitedViewHolder {
        return InvitedViewHolder(
            LayoutInflater.from(mContext)
                .inflate(R.layout.aui_invitation_list_item_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: InvitedViewHolder, position: Int) {
        val userInfo = dataList[position]
        holder.name.text = userInfo?.userName
        holder.action.text = mContext?.getString(R.string.aui_room_invited_action)
        holder.action.alpha = 1.0f
        mContext?.let { Glide.with(it).load(userInfo?.userAvatar).into(holder.avatar) }
        holder.action.setOnClickListener{
            invitedIndex?.let { index->
                if (index != -1){
                    listener?.onInvitedClickListener(it,index,userInfo)
                }
            }
            holder.action.alpha = 0.2f
        }
    }

    override fun getItemCount(): Int {
        if (dataList.size > 0){
            return dataList.size
        }
        return 0
    }


    fun refresh(data:MutableList<AUIUserInfo?>){
        data.let {
            dataList = data
        }

        notifyDataSetChanged()
    }

    interface InvitedEventListener{
        fun onInvitedClickListener(view:View,invitedIndex:Int,user:AUIUserInfo?)
    }

    fun setInvitedEventListener(listener: InvitedEventListener){
        this.listener = listener
    }
}

class InvitedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var avatar: AUIImageView
    var name: MaterialTextView
    var action: MaterialTextView

    init {
        avatar = itemView.findViewById<AUIImageView>(R.id.ivInvitedAvatar)
        name = itemView.findViewById<MaterialTextView>(R.id.mtInvitedUsername)
        action = itemView.findViewById<MaterialTextView>(R.id.mtInvitedAction)
    }
}



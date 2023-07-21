package io.agora.auikit.ui.action.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.android.material.textview.MaterialTextView
import io.agora.auikit.R
import io.agora.auikit.databinding.AuiApplyListLayoutBinding
import io.agora.auikit.model.AUIActionModel
import io.agora.auikit.model.AUIUserInfo
import io.agora.auikit.ui.basic.AUIImageView
import io.agora.auikit.utils.DeviceTools.dp
import io.agora.auikit.utils.ResourcesTools

class VoiceRoomApplyListFragment : Fragment(),
    SwipeRefreshLayout.OnRefreshListener, VoiceApplyAdapter.ApplyEventListener {

    private var mRoomViewBinding = AuiApplyListLayoutBinding.inflate(LayoutInflater.from(act))

    companion object {

        private const val KEY_ROOM_INFO = "room_info"
        private var act: FragmentActivity?=null

        fun getInstance(fragmentActivity: FragmentActivity, roomBean: AUIActionModel): VoiceRoomApplyListFragment {
            act = fragmentActivity
            return VoiceRoomApplyListFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(KEY_ROOM_INFO, roomBean)
                }
            }
        }
    }

    private var roomBean: AUIActionModel? = null
    private var listener: ApplyEventListener?=null
    private var invitedIndex:Int? = -1

    private var total = 0
        set(value) {
            field = value
            checkEmpty()
        }
    private var members = mutableListOf<AUIUserInfo?>()

    private var applyAdapter: VoiceApplyAdapter?=null

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
        if (userList != null) {
            members = userList
        }
        userList?.let {
            total = it.size
            checkEmpty()
            applyAdapter?.refresh(it)
        }

        mRoomViewBinding.apply {
            initAdapter(rvApplyList)
            slApplyList.setOnRefreshListener(this@VoiceRoomApplyListFragment)
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
        act?.let {
            applyAdapter = VoiceApplyAdapter(it,members)
            recyclerView.layoutManager = LinearLayoutManager(it)
            recyclerView.addItemDecoration(
                MaterialDividerItemDecoration(it, MaterialDividerItemDecoration.VERTICAL).apply {
                    dividerThickness = 1.dp.toInt()
                    dividerInsetStart = 15.dp.toInt()
                    dividerInsetEnd = 15.dp.toInt()
                    dividerColor = ResourcesTools.getColor(it.resources, R.color.aui_color_1f979797)
                }
            )
            recyclerView.adapter = applyAdapter
            applyAdapter?.setInvitedEventListener(this)
        }
    }

    override fun onRefresh() {
        applyAdapter?.notifyDataSetChanged()
        mRoomViewBinding.slApplyList.isRefreshing = false
    }

    fun refreshData(userList:MutableList<AUIUserInfo?>){
        mRoomViewBinding.root.post {
            userList.let {
                total = it.size
                checkEmpty()
                applyAdapter?.refresh(it)
            }
        }
    }

    interface ApplyEventListener{
        fun onApplyItemClick(view:View,applyIndex:Int?,user:AUIUserInfo?,position: Int){}
    }

    fun setApplyEventListener(listener: ApplyEventListener){
        this.listener = listener
    }

    override fun onApplyClickListener(view: View, user: AUIUserInfo?, position: Int) {
        this.listener?.onApplyItemClick(view, user?.micIndex, user,position)
    }

}

class VoiceApplyAdapter constructor(
    context: Context,
    dataList:MutableList<AUIUserInfo?>
): RecyclerView.Adapter<ApplyViewHolder>()   {
    var dataList:MutableList<AUIUserInfo?> = mutableListOf()
    private var listener: ApplyEventListener?=null

    private var mContext:Context?=null
    init {
        this.mContext = context
        if (dataList != null) {
            this.dataList = dataList
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplyViewHolder {
        return ApplyViewHolder(
            LayoutInflater.from(mContext)
                .inflate(R.layout.aui_apply_list_item_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ApplyViewHolder, position: Int) {
        val userInfo = dataList[position]
        if (userInfo?.userName.isNullOrEmpty()){
            holder.name.text = userInfo?.userId
        }else{
            holder.name.text = userInfo?.userName
        }
        holder.action.text = mContext?.getString(R.string.aui_room_apply_accept)
        holder.action.alpha = 1.0f
        mContext?.let { Glide.with(it).load(userInfo?.userAvatar).into(holder.avatar) }
        holder.action.setOnClickListener{
            userInfo.let { index->
                if (index?.micIndex != -1){
                    listener?.onApplyClickListener(it,userInfo,position)
                }
                holder.action.alpha = 0.2f
            }
        }
    }

    override fun getItemCount(): Int {
        if (dataList.size > 0){
            return dataList.size
        }
        return 0
    }

    fun refresh(data:MutableList<AUIUserInfo?>){
        val list: MutableList<AUIUserInfo?> = data
        Log.e("apex","apply refresh ${list.size}")
        dataList = list
        notifyDataSetChanged()
    }

    interface ApplyEventListener{
        fun onApplyClickListener(view:View,user:AUIUserInfo?,position:Int)
    }

    fun setInvitedEventListener(listener: ApplyEventListener){
        this.listener = listener
    }
}

class ApplyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var avatar: AUIImageView
    var name: MaterialTextView
    var action: MaterialTextView

    init {
        avatar = itemView.findViewById<AUIImageView>(R.id.ivApplyAvatar)
        name = itemView.findViewById<MaterialTextView>(R.id.mtApplyUsername)
        action = itemView.findViewById<MaterialTextView>(R.id.mtApplyAction)
    }
}




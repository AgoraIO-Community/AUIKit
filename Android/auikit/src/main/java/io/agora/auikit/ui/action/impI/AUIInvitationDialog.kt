package io.agora.auikit.ui.action.impI

import android.content.res.TypedArray
import android.graphics.Typeface
import android.os.Bundle
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.util.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import io.agora.auikit.R
import io.agora.auikit.databinding.AuiInvitationLayoutBinding
import io.agora.auikit.model.AUIUserInfo
import io.agora.auikit.model.AUIActionModel
import io.agora.auikit.ui.action.IAUIListViewBinderRefresh
import io.agora.auikit.ui.action.fragment.VoiceRoomInvitedListFragment
import io.agora.auikit.ui.action.listener.AUIInvitationDialogEventListener
import io.agora.auikit.ui.basic.AUISheetFragmentDialog
import io.agora.auikit.utils.ResourcesTools

class AUIInvitationDialog : AUISheetFragmentDialog<AuiInvitationLayoutBinding>(), IAUIListViewBinderRefresh{

    companion object {
        const val KEY_ROOM_INVITED_BEAN = "room_invited_bean"
        const val KEY_CURRENT_ITEM = "current_Item"
    }

    private val roomBean: AUIActionModel by lazy {
        arguments?.getSerializable(KEY_ROOM_INVITED_BEAN) as AUIActionModel
    }

    private val currentItem: Int by lazy {
        arguments?.getInt(KEY_CURRENT_ITEM, 0) ?: 0
    }

    private var listener: AUIInvitationDialogEventListener?=null
    private var adapter: RoomInvitedFragmentAdapter?=null
    private var appearanceId:Int = 0
    private var mTabSelectedColor:Int = 0
    private var mTabUnSelectedColor:Int = 0

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): AuiInvitationLayoutBinding {
        return AuiInvitationLayoutBinding.inflate(inflater,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 获取自定义样式的ID
        activity?.let {
            val themeTa: TypedArray = it.theme.obtainStyledAttributes(R.styleable.AUIAction)
            appearanceId = themeTa.getResourceId(R.styleable.AUIAction_aui_action_appearance, 0)
            themeTa.recycle()
        }

        initFragmentAdapter()
    }

    private fun initFragmentAdapter() {
        activity?.let { fragmentActivity->
            val typedArray = fragmentActivity.obtainStyledAttributes(appearanceId, R.styleable.AUIAction)
            mTabSelectedColor = typedArray.getResourceId(
                R.styleable.AUIAction_aui_tabLayout_selected_textColor,
                R.color.aui_color_040925
            )
            mTabUnSelectedColor = typedArray.getResourceId(
                R.styleable.AUIAction_aui_tabLayout_unselected_textColor,
                R.color.aui_color_6c7192
            )
            typedArray.recycle()

            adapter = RoomInvitedFragmentAdapter(fragmentActivity,roomBean,listener)
            binding?.apply {
                setOnApplyWindowInsets(root)
                vpInvitedLayout.adapter = adapter
                val tabMediator = TabLayoutMediator(tabInvitedLayout, vpInvitedLayout) { tab, position ->
                    val customView =
                        LayoutInflater.from(root.context).inflate(R.layout.aui_action_tab_item_layout, tab.view, false)
                    val tabText = customView.findViewById<TextView>(R.id.mtTabText)
                    tab.customView = customView
                    if (position == RoomInvitedFragmentAdapter.PAGE_INDEX0) {
                        tabText.text = getString(R.string.aui_room_invited_list)
                        onTabLayoutSelected(tab)
                    } else {
                        onTabLayoutUnselected(tab)
                    }

                }
                tabMediator.attach()

                tabInvitedLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab?) {
                        onTabLayoutSelected(tab)
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab?) {
                        onTabLayoutUnselected(tab)
                    }

                    override fun onTabReselected(tab: TabLayout.Tab?) {
                    }
                })
                vpInvitedLayout.setCurrentItem(currentItem, false)
            }
        }
    }

    private fun onTabLayoutSelected(tab: TabLayout.Tab?) {
        tab?.customView?.let {
            val tabText = it.findViewById<TextView>(R.id.mtTabText)
            tabText.setTextColor(ResourcesTools.getColor(resources, mTabSelectedColor))
            tabText.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
            val tabTip = it.findViewById<View>(R.id.vTabTip)
            tabTip.visibility = View.VISIBLE
        }
    }

    private fun onTabLayoutUnselected(tab: TabLayout.Tab?) {
        tab?.customView?.let {
            val tabText = it.findViewById<TextView>(R.id.mtTabText)
            tabText.setTextColor(ResourcesTools.getColor(resources, mTabUnSelectedColor))
            tabText.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
            val tabTip = it.findViewById<View>(R.id.vTabTip)
            tabTip.visibility = View.GONE
        }
    }

    override fun refreshInvitationData(userList:MutableList<AUIUserInfo?>) {
        adapter?.refreshData(userList)
    }

    override fun setInvitationDialogListener(listener:AUIInvitationDialogEventListener){
        this.listener = listener
    }

    class RoomInvitedFragmentAdapter constructor(
        fragmentActivity: FragmentActivity,
        roomBean: AUIActionModel,
        event:AUIInvitationDialogEventListener?
    ) : FragmentStateAdapter(fragmentActivity), VoiceRoomInvitedListFragment.InviteEventListener {

        companion object {
            const val PAGE_INDEX0 = 0
            const val PAGE_INDEX1 = 1
        }

        private val fragments: SparseArray<Fragment> = SparseArray()
        private var listener:AUIInvitationDialogEventListener?=null

        init {
            this.listener = event
            with(fragments) {
                put(PAGE_INDEX0, VoiceRoomInvitedListFragment.getInstance(fragmentActivity,roomBean))
            }
        }

        override fun createFragment(position: Int): Fragment {
            val fragment = fragments[position]
            if (PAGE_INDEX0 == position){
                (fragment as VoiceRoomInvitedListFragment).setInviteEventListener(this)
            }
            return fragments[position]
        }

        override fun getItemCount(): Int {
            return fragments.size()
        }

        override fun onInviteItemClick(view: View, invitedIndex: Int, user: AUIUserInfo?) {
            listener?.onInvitedItemClick(view,invitedIndex,user)
        }

        fun refreshData(userList:MutableList<AUIUserInfo?>) {
            fragments.forEach { key, value ->
                if (key == PAGE_INDEX0){
                    val fragment = value as VoiceRoomInvitedListFragment
                    fragment.refreshData(userList)
                }
            }
        }
    }

}
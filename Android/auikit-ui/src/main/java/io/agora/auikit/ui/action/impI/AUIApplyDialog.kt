package io.agora.auikit.ui.action.impI

import android.content.res.TypedArray
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import io.agora.auikit.ui.R
import io.agora.auikit.ui.action.AUIActionUserInfo
import io.agora.auikit.ui.action.AUIActionUserInfoList
import io.agora.auikit.ui.action.IAUIListViewBinderRefresh
import io.agora.auikit.ui.action.fragment.VoiceRoomApplyListFragment
import io.agora.auikit.ui.action.listener.AUIApplyDialogEventListener
import io.agora.auikit.ui.basic.AUISheetFragmentDialog
import io.agora.auikit.ui.databinding.AuiApplyLayoutBinding
import io.agora.auikit.utils.ResourcesTools

class AUIApplyDialog : AUISheetFragmentDialog<AuiApplyLayoutBinding>(), IAUIListViewBinderRefresh {

    private var mEvnetListener: AUIApplyDialogEventListener? = null
    private var mTabSelectedColor: Int = 0
    private var mTabUnSelectedColor: Int = 0
    private val mUserLiveData = MutableLiveData(AUIActionUserInfoList(emptyList(), 0))
    private val mPageList by lazy {
        listOf<Pair<String, () -> Fragment>>(
            getString(R.string.aui_room_apply_list) to {
                VoiceRoomApplyListFragment(mUserLiveData).apply {
                    setApplyEventListener(object : VoiceRoomApplyListFragment.ApplyEventListener{
                        override fun onApplyItemClick(
                            view: View,
                            applyIndex: Int?,
                            user: AUIActionUserInfo?,
                            position: Int
                        ) {
                            super.onApplyItemClick(view, applyIndex, user, position)
                            mEvnetListener?.onApplyItemClick(view, applyIndex, user, position)
                        }
                    })
                }
            }
        )
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): AuiApplyLayoutBinding {
        return AuiApplyLayoutBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initTheme()
        initFragmentAdapter()
    }

    private fun initTheme() {
        val context = activity ?: return
        val themeTa: TypedArray = context.theme.obtainStyledAttributes(R.styleable.AUIAction)
        val appearanceId = themeTa.getResourceId(R.styleable.AUIAction_aui_action_appearance, 0)
        themeTa.recycle()

        val typedArray = context.obtainStyledAttributes(appearanceId, R.styleable.AUIAction)
        mTabSelectedColor = typedArray.getResourceId(
            R.styleable.AUIAction_aui_tabLayout_selected_textColor,
            R.color.aui_color_040925
        )
        mTabUnSelectedColor = typedArray.getResourceId(
            R.styleable.AUIAction_aui_tabLayout_unselected_textColor,
            R.color.aui_color_6c7192
        )
        typedArray.recycle()
    }

    override fun setApplyDialogListener(listener: AUIApplyDialogEventListener) {
        this.mEvnetListener = listener
    }

    private fun initFragmentAdapter() {
        val context = activity ?: return
        val binding = binding ?: return
        val currentItem = 0

        setOnApplyWindowInsets(binding.root)
        binding.vpApplyLayout.adapter = RoomApplyFragmentAdapter(context)
        val tabMediator =
            TabLayoutMediator(binding.tabApplyLayout, binding.vpApplyLayout) { tab, position ->
                val customView =
                    LayoutInflater.from(binding.root.context)
                        .inflate(R.layout.aui_action_tab_item_layout, tab.view, false)
                val tabText = customView.findViewById<TextView>(R.id.mtTabText)
                tab.customView = customView
                tabText.text = mPageList[position].first
                if (position == currentItem) {
                    onTabLayoutSelected(tab)
                } else {
                    onTabLayoutUnselected(tab)
                }
            }
        tabMediator.attach()
        binding.tabApplyLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                onTabLayoutSelected(tab)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                onTabLayoutUnselected(tab)
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
        binding.vpApplyLayout.setCurrentItem(currentItem, false)

        mUserLiveData.value?.let {
            mUserLiveData.value = AUIActionUserInfoList(it.userList, it.invitedIndex)
        }
    }

    override fun refreshApplyData(userList: List<AUIActionUserInfo>) {
        mUserLiveData.value = AUIActionUserInfoList(userList, -1)
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

    inner class RoomApplyFragmentAdapter constructor(
        fragment: FragmentActivity,
    ) : FragmentStateAdapter(fragment) {

        override fun createFragment(position: Int): Fragment {
            return mPageList[position].second.invoke()
        }

        override fun getItemCount(): Int {
            return mPageList.size
        }
    }

}
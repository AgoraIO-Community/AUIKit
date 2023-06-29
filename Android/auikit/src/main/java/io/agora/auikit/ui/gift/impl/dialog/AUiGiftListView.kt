package io.agora.auikit.ui.gift.impl.dialog
import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import io.agora.auikit.R
import io.agora.auikit.databinding.AuiGiftListViewLayoutBinding
import io.agora.auikit.model.AUIGiftEntity
import io.agora.auikit.model.AUIGiftTabEntity
import io.agora.auikit.ui.basic.AUISheetFragmentDialog
import io.agora.auikit.ui.basic.AUIImageView
import io.agora.auikit.ui.gift.IAUIGiftBarrageView
import io.agora.auikit.utils.DeviceTools

class AUiGiftListView constructor(
    context: Context,
    data:List<AUIGiftTabEntity>
) : AUISheetFragmentDialog<AuiGiftListViewLayoutBinding>(), IAUIGiftBarrageView {

    private var mGiftList : List<AUIGiftTabEntity> = mutableListOf()
    private var tapList: MutableList<Int> = mutableListOf()
    private var mContext:Context
    private var listener:ActionListener?=null

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): AuiGiftListViewLayoutBinding {
        return AuiGiftListViewLayoutBinding.inflate(inflater, container, false)
    }

    init {
        this.mContext = context
        this.mGiftList = data

        for (auiGiftTabEntity in mGiftList) {
            auiGiftTabEntity.tabId.let { tapList.add(it) }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view,savedInstanceState)
        binding?.root?.let { setOnApplyWindowInsets(it) }
        initView()
        initListener()
    }

    fun initView() {
        binding?.viewPager?.adapter = object :
            FragmentStateAdapter(this.requireActivity().supportFragmentManager, this.lifecycle) {
            override fun getItemCount(): Int {
                return tapList.size
            }

            override fun createFragment(position: Int): Fragment {
                val fragment = AUIGiftListFragment(mContext, mGiftList, tapList[position])
                fragment.setOnItemSelectClickListener { view, bean -> listener?.onGiftSend(bean) }
                return fragment
            }
        }

        // set TabLayoutMediator
        val mediator = binding?.tabLayout?.let {
            binding?.viewPager?.let { it1 ->
                TabLayoutMediator(
                    it, it1
                ) { tab, position ->
                    tab.setCustomView(R.layout.aui_gift_tab_item_layout)
                    val title = tab.customView?.findViewById<TextView>(R.id.tab_item_title)
                    title?.text = mGiftList[position].displayName
                    binding?.tabLayout?.getTabAt(0)?.select() //默认选中某项放在加载viewpager之后
                }
            }
        }
        // setup with viewpager2
        mediator?.attach()

        val tabAt = binding?.tabLayout?.getTabAt(0)
        val tagIcon = tabAt?.customView?.findViewById<AUIImageView>(R.id.tab_bg)
        tagIcon?.visibility = View.VISIBLE
    }

    private fun initListener(){
        binding?.tabLayout?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.customView != null) {
                    val title = tab.customView?.findViewById<TextView>(R.id.tab_item_title)
                    val tagIcon = tab.customView?.findViewById<AUIImageView>(R.id.tab_bg)
                    val layoutParams = title?.layoutParams
                    layoutParams?.height = DeviceTools.dp2px(mContext, 26f).toInt()
                    title?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                    title?.gravity = Gravity.CENTER
                    tagIcon?.visibility = View.VISIBLE
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                if (tab.customView != null) {
                    val title = tab.customView!!.findViewById<TextView>(R.id.tab_item_title)
                    val tagIcon = tab.customView?.findViewById<AUIImageView>(R.id.tab_bg)
                    title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                    tagIcon?.visibility = View.GONE
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })
    }

    override fun setDialogActionListener(listener:ActionListener){
        this.listener = listener
    }

    interface ActionListener{
        fun onGiftSend(bean: AUIGiftEntity?){}
    }
}
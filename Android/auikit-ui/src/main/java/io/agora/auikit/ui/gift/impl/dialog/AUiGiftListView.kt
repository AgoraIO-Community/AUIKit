package io.agora.auikit.ui.gift.impl.dialog
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import io.agora.auikit.ui.R
import io.agora.auikit.ui.basic.AUIImageView
import io.agora.auikit.ui.basic.AUISheetFragmentDialog
import io.agora.auikit.ui.databinding.AuiGiftListViewLayoutBinding
import io.agora.auikit.ui.gift.AUIGiftInfo
import io.agora.auikit.ui.gift.AUIGiftTabInfo
import io.agora.auikit.ui.gift.IAUIGiftBarrageView
import io.agora.auikit.utils.ResourcesTools

class AUiGiftListView constructor(
    context: Context,
    data:List<AUIGiftTabInfo>
) : AUISheetFragmentDialog<AuiGiftListViewLayoutBinding>(), IAUIGiftBarrageView {

    private var mGiftList : List<AUIGiftTabInfo> = mutableListOf()
    private var tapList: MutableList<Int> = mutableListOf()
    private var mContext:Context
    private var listener:ActionListener?=null
    private var giftDialogBg:Int = 0
    private var mTabSelectedColor:Int = 0
    private var mTabUnSelectedColor:Int = 0

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

        // 获取自定义样式的ID
        val themeTa: TypedArray = mContext.theme.obtainStyledAttributes(R.styleable.AUIGiftBottomDialog)
        val appearanceId = themeTa.getResourceId(R.styleable.AUIGiftBottomDialog_aui_giftBottomDialog_appearance, 0)
        themeTa.recycle()

        initView(appearanceId)
        initListener()
    }

    fun initView(appearanceId:Int) {
        val typedArray = mContext.obtainStyledAttributes(appearanceId, R.styleable.AUIGiftBottomDialog)
        giftDialogBg = typedArray.getResourceId(
            R.styleable.AUIGiftBottomDialog_aui_giftBottomDialog_bg,
            R.drawable.aui_gift_r20_white_bg_light
        )
        mTabSelectedColor = typedArray.getResourceId(
            R.styleable.AUIGiftBottomDialog_aui_giftBottomDialog_tabLayout_title_color,
            R.color.voice_gift_black_171A1C
        )
        mTabUnSelectedColor = typedArray.getResourceId(
            R.styleable.AUIGiftBottomDialog_aui_giftBottomDialog_tabLayout_title_color,
            R.color.voice_gift_grey_ACB4B9
        )
        typedArray.recycle()

        binding?.viewPager?.adapter = object :
            FragmentStateAdapter(this.requireActivity().supportFragmentManager, this.lifecycle) {
            override fun getItemCount(): Int {
                return tapList.size
            }

            override fun createFragment(position: Int): Fragment {
                val fragment = AUIGiftListFragment(mGiftList, tapList[position])
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
                    title?.text = mGiftList[position].tabName
                    binding?.tabLayout?.getTabAt(0)?.select() //默认选中某项放在加载viewpager之后
                    if (position == 0){
                        onTabLayoutSelected(tab)
                    }else{
                        onTabLayoutUnselected(tab)
                    }
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
                onTabLayoutSelected(tab)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                onTabLayoutUnselected(tab)
            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })
    }

    override fun setDialogActionListener(listener:ActionListener){
        this.listener = listener
    }

    interface ActionListener{
        fun onGiftSend(bean: AUIGiftInfo?){}
    }

    private fun onTabLayoutSelected(tab: TabLayout.Tab?) {
        tab?.customView?.let {
            val tabText = it.findViewById<TextView>(R.id.tab_item_title)
            tabText.setTextColor(ResourcesTools.getColor(resources, mTabSelectedColor))
            tabText.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
            tabText?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            val tabTip = it.findViewById<AUIImageView>(R.id.tab_bg)
            tabTip.visibility = View.VISIBLE
        }
    }

    private fun onTabLayoutUnselected(tab: TabLayout.Tab?) {
        tab?.customView?.let {
            val tabText = it.findViewById<TextView>(R.id.tab_item_title)
            tabText.setTextColor(ResourcesTools.getColor(resources, mTabUnSelectedColor))
            tabText.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
            tabText?.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            val tabTip = it.findViewById<AUIImageView>(R.id.tab_bg)
            tabTip.visibility = View.GONE
        }
    }
}
package io.agora.auikit.ui.gift.impl.dialog

import android.content.res.TypedArray
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.agora.auikit.ui.R
import io.agora.auikit.ui.databinding.AuiGiftListFragmentLayoutBinding
import io.agora.auikit.ui.gift.AUIGiftInfo
import io.agora.auikit.ui.gift.AUIGiftTabInfo
import io.agora.auikit.ui.gift.listener.AUIConfirmClickListener
import io.agora.auikit.ui.gift.listener.AUIGiftItemClickListener
import io.agora.auikit.ui.gift.selected
import io.agora.auikit.utils.DeviceTools

class AUIGiftListFragment : Fragment,AUIGiftItemClickListener{
    constructor():this(null,null)
    constructor(
        gift:List<AUIGiftTabInfo>?,
        tagId:Int?
    ){
        if (gift != null) {
            this.gift = gift
        }
        this.currentTag = tagId

        if (gift != null) {
            for (auiGiftTabEntity in gift) {
                map[auiGiftTabEntity.tabId] = auiGiftTabEntity.gifts as List<AUIGiftInfo>
            }
        }
    }
    private var adapter: AUIGiftListAdapter? = null
    private var giftBean: AUIGiftInfo? = null
    private var listener: AUIConfirmClickListener? = null
    private var gift:List<AUIGiftTabInfo> = mutableListOf()
    private val mColumns = 4
    private var currentTag:Int? = 0

    private lateinit var aGiftListBinding:AuiGiftListFragmentLayoutBinding

    private var map:MutableMap<Int,List<AUIGiftInfo>> = mutableMapOf()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        aGiftListBinding = AuiGiftListFragmentLayoutBinding.inflate(inflater)
        return aGiftListBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 获取自定义样式的ID
        context?.let {
            val themeTa: TypedArray = it.theme.obtainStyledAttributes(R.styleable.AUIGiftBottomDialog)
            val appearanceId = themeTa.getResourceId(R.styleable.AUIGiftBottomDialog_aui_giftBottomDialog_appearance, 0)
            themeTa.recycle()
            initView(appearanceId)
        }
    }

    private fun initView(appearanceId:Int) {
        context?.let {
            val typedArray = it.obtainStyledAttributes(appearanceId, R.styleable.AUIGiftBottomDialog)
            aGiftListBinding.gridview.verticalSpacing = DeviceTools.dp2px(requireContext(), 20F)
            aGiftListBinding.gridview.numColumns = mColumns
            aGiftListBinding.gridview.verticalSpacing = 40
            adapter = AUIGiftListAdapter(it, 1,typedArray, map[currentTag] as List<AUIGiftInfo>)
            aGiftListBinding.gridview.adapter = adapter
            adapter?.setSelectedPosition(0)
            adapter?.setOnItemClickListener(this)
        }
    }

    fun setOnItemSelectClickListener(listener: AUIConfirmClickListener?) {
        this.listener = listener
    }

    override fun sendGift(view:View,position:Int,gift: AUIGiftInfo) {
        giftBean = adapter?.getItem(position)
        listener?.sendGift(view,gift)
    }



    override fun selectGift(view: View, position: Int, gift: AUIGiftInfo) {
        adapter?.getItem(position)?.let {
            it.selected = !it.selected
            if (it.selected) {
                adapter?.setSelectedPosition(position)
            } else {
                adapter?.setSelectedPosition(-1)
            }
        }
    }

    override fun onDestroy() {
        adapter?.clearRun()
        super.onDestroy()
    }
}
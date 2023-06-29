package io.agora.auikit.ui.gift.impl.dialog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.agora.auikit.databinding.AuiGiftListFragmentLayoutBinding
import io.agora.auikit.model.AUIGiftEntity
import io.agora.auikit.model.AUIGiftTabEntity
import io.agora.auikit.ui.gift.listener.AUIGiftItemClickListener
import io.agora.auikit.ui.gift.listener.AUIConfirmClickListener
import io.agora.auikit.utils.DeviceTools

class AUIGiftListFragment constructor(
    context: Context,
    gift:List<AUIGiftTabEntity>,
    tagId:Int
) : Fragment(),AUIGiftItemClickListener{
    private var adapter: AUIGiftListAdapter? = null
    private var giftBean: AUIGiftEntity? = null
    private var listener: AUIConfirmClickListener? = null
    private val mColumns = 4
    private var currentTag = 0
    private val aGiftListBinding = AuiGiftListFragmentLayoutBinding.inflate(LayoutInflater.from(context))

    private var map:MutableMap<Int,List<AUIGiftEntity>> = mutableMapOf()

    init {
        for (auiGiftTabEntity in gift) {
            map[auiGiftTabEntity.tabId] = auiGiftTabEntity.gifts as List<AUIGiftEntity>
        }
        this.currentTag = tagId

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return aGiftListBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        context.let {
            aGiftListBinding.gridview.verticalSpacing = DeviceTools.dp2px(requireContext(), 20F)
            aGiftListBinding.gridview.numColumns = mColumns
            aGiftListBinding.gridview.verticalSpacing = 40
            adapter = AUIGiftListAdapter(requireContext(), 1, map[currentTag] as List<AUIGiftEntity>)
            aGiftListBinding.gridview.adapter = adapter
            adapter?.setSelectedPosition(0)
            adapter?.setOnItemClickListener(this)
        }
    }

    fun setOnItemSelectClickListener(listener: AUIConfirmClickListener?) {
        this.listener = listener
    }

    override fun sendGift(view:View,position:Int,gift: AUIGiftEntity) {
        giftBean = adapter?.getItem(position)
        listener?.sendGift(view,gift)
    }

    override fun selectGift(view: View, position: Int, gift: AUIGiftEntity) {
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
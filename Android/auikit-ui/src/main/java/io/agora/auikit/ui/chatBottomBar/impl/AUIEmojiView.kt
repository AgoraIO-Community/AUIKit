package io.agora.auikit.ui.chatBottomBar.impl

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.AdapterView
import androidx.appcompat.widget.LinearLayoutCompat
import io.agora.auikit.model.AUIExpressionIcon
import io.agora.auikit.ui.chatBottomBar.listener.AUIExpressionClickListener
import io.agora.auikit.ui.databinding.AuiEmojiGridviewLayoutBinding
import io.agora.auikit.utils.DeviceTools

class AUIEmojiView : LinearLayoutCompat {
    private var aContext: Context? = null
    private val mColumns = 7
    private val mRoomViewBinding = AuiEmojiGridviewLayoutBinding.inflate(LayoutInflater.from(context))
    private var gridAdapter: AUIEmojiGridAdapter? = null
    private var listener: AUIExpressionClickListener? = null
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        this.aContext = context
        addView(mRoomViewBinding.root)
        initView()
        initListener()
    }

    fun initView(){
        mRoomViewBinding.gridview.let {
            it.verticalSpacing = DeviceTools.dp2px(context, 20F)
            it.numColumns = mColumns
            it.verticalSpacing = 40
            gridAdapter = AUIEmojiGridAdapter(context, 1, mutableListOf(*AUIDefaultEmojiData.getData()))
            it.adapter = gridAdapter
        }
    }

    private fun initListener(){
        mRoomViewBinding.ivEmojiDelete.setOnClickListener{
           listener?.onDeleteImageClicked()
        }
        mRoomViewBinding.gridview.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val emojiIcon: AUIExpressionIcon? = gridAdapter?.getItem(position)
                listener?.onExpressionClicked(emojiIcon)
            }
    }

    fun setExpressionListener(listener: AUIExpressionClickListener) {
       this.listener = listener
    }

}
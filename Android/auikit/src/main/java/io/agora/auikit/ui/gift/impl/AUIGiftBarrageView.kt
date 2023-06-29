package io.agora.auikit.ui.gift.impl

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import io.agora.auikit.databinding.AuiGiftBarrageLayoutBinding
import io.agora.auikit.model.AUIGiftEntity
import io.agora.auikit.ui.gift.IAUIGiftBarrageView
import io.agora.auikit.utils.DeviceTools

class AUIGiftBarrageView : LinearLayout,
    IAUIGiftBarrageView {
    private var mContext: Context
    private val aGiftViewBinding = AuiGiftBarrageLayoutBinding.inflate(LayoutInflater.from(context))
    private var mMainHandler: Handler? = null
    private val delay = 3000
    private var task: Runnable? = null
    private lateinit var adapter:AUIGiftRowAdapter

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        this.mContext = context
        addView(aGiftViewBinding.root)
        initView()
    }

    init {
        mMainHandler = Handler(Looper.getMainLooper())
    }

    fun initView(){
        // 获取缓存中 指定房间的礼物列表
        adapter = AUIGiftRowAdapter(mContext)
        val linearLayoutManager = LinearLayoutManager(mContext)
        aGiftViewBinding.recyclerView.layoutManager = linearLayoutManager
        aGiftViewBinding.recyclerView.adapter = adapter

        //设置item 间距
        val itemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        val drawable = GradientDrawable()
        drawable.setSize(0, DeviceTools.dp2px(context, 6f))
        itemDecoration.setDrawable(drawable)
        aGiftViewBinding.recyclerView.addItemDecoration(itemDecoration)

        //设置item动画
        val defaultItemAnimator = DefaultItemAnimator()
        defaultItemAnimator.addDuration = 500
        defaultItemAnimator.removeDuration = 500
        aGiftViewBinding.recyclerView.itemAnimator = defaultItemAnimator
    }

    override fun refresh(giftList:ArrayList<AUIGiftEntity>?) {
        giftList?.let { adapter.refresh(it) }
        if (adapter.itemCount > 0) {
            aGiftViewBinding.recyclerView.smoothScrollToPosition(adapter.itemCount - 1)
        }
        clearTiming()
    }

    /**
     * 定时清理礼物列表信息
     */
    private fun clearTiming() {
        if (childCount > 0) {
            startTask()
        }
    }

    // 开启定时任务
    private fun startTask() {
        stopTask()
        mMainHandler?.postDelayed(object : Runnable {
            override fun run() {
                // 在这里执行具体的任务
                if (adapter.dataList.size > 0) {
                    adapter.removeAll()
                }
                // 任务执行完后再次调用postDelayed开启下一次任务
                mMainHandler?.postDelayed(this, delay.toLong())
            }
        }.also { task = it }, delay.toLong())
    }

    // 停止定时任务
    private fun stopTask() {
        if (task != null) {
            mMainHandler?.removeCallbacks(task!!)
            task = null
        }
    }

    fun clear() {
        if (task != null) {
            removeCallbacks(task)
        }
        mMainHandler?.removeCallbacksAndMessages(null)
        mMainHandler = null
    }

}
package io.agora.auikit.ui.gift.impl.dialog

import android.content.Context
import android.content.res.TypedArray
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.material.textview.MaterialTextView
import io.agora.auikit.model.AUIGiftEntity
import io.agora.auikit.ui.R
import io.agora.auikit.ui.basic.AUIImageView
import io.agora.auikit.ui.gift.listener.AUIGiftItemClickListener

class AUIGiftListAdapter constructor(
    context: Context,
    resource: Int,
    typedArray:TypedArray,
    objects: List<AUIGiftEntity>
): ArrayAdapter<AUIGiftEntity>(context,resource, objects)  {

    private var selectedPosition = -1
    private var listener:AUIGiftItemClickListener?=null
    private var mContext:Context
    private var mMainHandler: Handler? = null
    private var Animation_time = 3
    private var showTask: Runnable? = null
    private var giftTextColor:Int = 0

    init {
        mContext = context
        mMainHandler = Handler(Looper.getMainLooper())

        giftTextColor = typedArray.getResourceId(
            R.styleable.AUIGiftBottomDialog_aui_giftBottomDialog_gift_textColor,
            R.color.voice_gift_black_171A1C
        )
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.aui_gift_list_item_layout, null)
        }
        val giftInfo = getItem(position)

        convertView?.let {
            val price = convertView.findViewById<MaterialTextView>(R.id.price)
            val action = convertView.findViewById<MaterialTextView>(R.id.action)
            val name = convertView.findViewById<MaterialTextView>(R.id.tv_gift_name)
            val img = convertView.findViewById<AUIImageView>(R.id.iv_gift)
            val itemLayout = convertView.findViewById<ConstraintLayout>(R.id.item_layout)

            price?.text = giftInfo?.giftPrice
            name?.text = giftInfo?.giftName

            Glide.with(context).load(giftInfo?.giftIcon).error(R.drawable.aui_gift_default_icon).into(img)

            selectedViewChange(giftInfo,action,name,itemLayout,selectedPosition == position)

            action.setOnClickListener { giftInfo?.let { it1 ->
                listener?.sendGift(action,position, it1)
            } }

            itemLayout?.setOnClickListener{giftInfo?.let { it1 ->
                listener?.selectGift(action,position, it1)
            } }

        }
        return convertView!!
    }

    private fun selectedViewChange(giftInfo:AUIGiftEntity?,action:MaterialTextView, name:MaterialTextView,layout:ConstraintLayout,isSelected: Boolean){
        if (isSelected){
            action.visibility = View.VISIBLE
            action.text = mContext.resources.getString(R.string.voice_gift_dialog_action)
            name.visibility = View.GONE
            giftInfo?.selected = true
            layout.background = context.let {
                ContextCompat.getDrawable(
                    it,
                    R.drawable.aui_gift_selected_shape_bg
                )
            }
        }else{
            action.visibility = View.GONE
            name.visibility = View.VISIBLE
            giftInfo?.selected = false
            layout.background = null
        }
    }


    fun setSelectedPosition(position: Int) {
        this.selectedPosition = position
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener:AUIGiftItemClickListener){
        this.listener = listener
    }

    /**
     * 定时更新礼物可发送状态
     */
    private fun changeStatusTiming(action:MaterialTextView,layout: ConstraintLayout) {
        startTask(action,layout)
    }

    // 开启倒计时任务
    private fun startTask(action:MaterialTextView,layout: ConstraintLayout) {
        mMainHandler?.postDelayed(object : Runnable {
            override fun run() {
                // 在这里执行具体的任务
                Animation_time--
                action.text = Animation_time.toString() + "s"
                layout.alpha = 0.2f
                action.isEnabled = false
                // 任务执行完后再次调用postDelayed开启下一次任务
                if (Animation_time == 0) {
                    stopActionTask()
                    action.isEnabled = true
                    action.text = mContext.getString(R.string.voice_gift_dialog_action)
                    layout.alpha = 1.0f
                } else {
                    mMainHandler?.postDelayed(this, 1000)
                }
            }
        }.also { showTask = it }, 1000)
    }


    // 停止计时任务
    private fun stopActionTask() {
        showTask?.let {
            mMainHandler?.removeCallbacks(it)
            showTask = null
            Animation_time = 3
        }
    }


    fun clearRun() {
        showTask?.let {
            mMainHandler?.removeCallbacks(it)
        }
        mMainHandler?.removeCallbacksAndMessages(null)
        mMainHandler = null
    }

}
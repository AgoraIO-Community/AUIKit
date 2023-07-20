package io.agora.auikit.ui.micseats.impl

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.agora.auikit.R
import io.agora.auikit.model.MicSeatItem
import io.agora.auikit.model.MicSeatOption
import io.agora.auikit.ui.micseats.IMicSeatItemView
import io.agora.auikit.ui.micseats.IMicSeatItemView.ChorusType
import io.agora.auikit.ui.micseats.IMicSeatsView
import io.agora.auikit.utils.DeviceTools
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class AUIMicSeatCircleLayout : FrameLayout, IMicSeatsView {

    /**
     * 多边形的边数 默认正六边形
     */
    private var micSeatCount = 6

    /**
     * 偏转角
     */
    private var mStartAngle = 0

    /**
     * 半径
     */
    private var mRadius = 0

    /**
     * 中心X坐标
     */
    private var centerX = 0f
    /**
     * 中心Y坐标
     */
    private var centerY = 0f

    private var micSeatViewList = arrayOfNulls<MicSeatCircleViewWrap>(8)

    private var isReady:Boolean = false

    /**
     * view 的宽度
     */
    private var mWidth = 0

    /**
     * view 的高度
     */
    private var mHeight = 0

    /**
     * ItemView 的宽度
     */
    private var mItemWidth = 90

    /**
     * ItemView 的高度
     */
    private var mItemHeight = 120


    private var mMicSeatMap = mutableMapOf<Int,MicSeatItem>()

    private var actionDelegate: IMicSeatsView.ActionDelegate? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        for (i in 0 until 8) {
            micSeatViewList[i] = MicSeatCircleViewWrap()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w
        mHeight = h

        centerX = (mWidth/2).toFloat()
        centerY = (mHeight/2).toFloat()

        invalidate()
        if (!isReady){
            initView(context)
            isReady = true
        }
    }

    private fun initView(context: Context){
        val view = inflate(context, R.layout.aui_micseat_frame_layout, this)
        val layout = view.findViewById<FrameLayout>(R.id.rootLayout)
        addView(layout)
    }

    /**
     * 设置布局参数
     */
    fun setOptions(option: MicSeatOption){
        micSeatCount = option.micSeatCount
        if (micSeatCount > 6){
            micSeatCount = 6
        }
        Log.e("apex","setOptions ${option.mItemWidth}  ${option.mItemHeight}")
        mStartAngle = option.startAngle
        mRadius = option.mRadius
        mItemWidth = option.mItemWidth
        mItemHeight = option.mItemHeight

        invalidate()
    }

    fun addView(layout:FrameLayout){
        getMicSeatCoordinate()
        micSeatViewList.let {
            for (i in 0 until micSeatCount) {
                val micSeatItemViewWrap = micSeatViewList[i]
                val itemView = AUIMicSeatItemView(context)
                itemView.tag = i
                val seatItem = mMicSeatMap[i]
                seatItem?.let {
                    itemView.x = it.micX - DeviceTools.dp2px(context,(mItemWidth/2).toFloat())
                    itemView.y = it.micY - DeviceTools.dp2px(context,(min(mItemWidth,mItemHeight)/2).toFloat())
                }
                itemView.let {
                    it.layoutParams = ViewGroup.LayoutParams(
                        DeviceTools.dp2px(context,mItemWidth.toFloat()),
                        DeviceTools.dp2px(context,mItemHeight.toFloat()))
                    micSeatItemViewWrap?.setView(it)
                    itemView.setOnClickListener{ it1 ->
                        showMicSeatDialog(it1)
                        Log.e("apex","setOnClickListener  $i")
                    }
                    if (i <= micSeatCount){
                        layout.addView(it)
                    }
                }
            }
        }
    }

    /**
     * 获取麦位view坐标
     */
    private fun getMicSeatCoordinate(){
        if (mRadius == 0) {
            when (micSeatCount) {
                3 -> {
                    mRadius = (min(mWidth, mHeight) / 4.5).toInt()
                }
                4 -> {
                    mRadius = ((min(mWidth, mHeight) / 4.5)* 1.2).toInt()
                }
                5 -> {
                    mRadius = (min(mWidth, mHeight) / 3.3).toInt()
                }
                6 -> {
                    mRadius = ((min(mWidth, mHeight) / 3)* 1.2).toInt()
                }
            }
        }
        val r: Float = (2 * mRadius * sin(Math.PI / micSeatCount)).toFloat() // 边长
        if (micSeatCount == 1){
            mMicSeatMap[0] = MicSeatItem(0,centerX,centerY)
            mItemWidth = 120
            mItemHeight = 180
        }else{
            for (i in 0 until micSeatCount) {
                val angle: Double =
                    2 * Math.PI * ((mStartAngle + i * 360 / micSeatCount) % 360) / 360 // 角度
                val endX = (centerX + r * cos(angle)).toFloat()
                val endY = (centerY + r * sin(angle)).toFloat()
                mMicSeatMap[i] = MicSeatItem(i,endX,endY)
            }
        }
    }

    private class MicSeatCircleViewWrap : IMicSeatItemView {
        private var titleText: String? = null
        private var titleIndex = 0
        private var audioMuteVisibility = GONE
        private var videoMuteVisibility = GONE
        private var roomOwnerVisibility = GONE
        private var chorusType = ChorusType.None
        private var userAvatarImageDrawable: Drawable? = null
        private var seatStatus = 0
        private var userAvatarImageUrl: String? = null
        private var view: IMicSeatItemView? = null

        fun getView(): IMicSeatItemView? {
            return view
        }

        fun setView(view: IMicSeatItemView) {
            this.view = view
            titleText?.let { setTitleText(it) }
            setTitleIndex(titleIndex)
            setRoomOwnerVisibility(roomOwnerVisibility)
            setAudioMuteVisibility(audioMuteVisibility)
            setVideoMuteVisibility(videoMuteVisibility)
            setUserAvatarImageDrawable(userAvatarImageDrawable)
            setMicSeatState(seatStatus)
            userAvatarImageUrl?.let { setUserAvatarImageUrl(it) }
        }

        override fun setTitleText(text: String) {
            titleText = text
            if (view != null) {
                view?.setTitleText(text)
            }
        }

        override fun setRoomOwnerVisibility(visible: Int) {
            roomOwnerVisibility = visible
            if (view != null) {
                view?.setRoomOwnerVisibility(visible)
            }
        }

        override fun setTitleIndex(index: Int) {
            titleIndex = index
            if (view != null) {
                view?.setTitleIndex(index)
            }
        }

        override fun setAudioMuteVisibility(visible: Int) {
            audioMuteVisibility = visible
            if (view != null) {
                view?.setAudioMuteVisibility(visible)
            }
        }

        override fun setVideoMuteVisibility(visible: Int) {
            videoMuteVisibility = visible
            if (view != null) {
                view?.setVideoMuteVisibility(visible)
            }
        }

        override fun setUserAvatarImageDrawable(drawable: Drawable?) {
            userAvatarImageDrawable = drawable
            if (view != null) {
                view?.setUserAvatarImageDrawable(drawable)
            }
        }

        override fun setMicSeatState(state: Int) {
            seatStatus = state
            if (view != null) {
                view?.setMicSeatState(state)
            }
        }

        override fun setUserAvatarImageUrl(url: String) {
            userAvatarImageUrl = url
            if (view != null) {
                view?.setUserAvatarImageUrl(url)
            }
        }

        override fun setChorusMicOwnerType(type: ChorusType) {
            chorusType = type
            if (view != null) {
                view?.setChorusMicOwnerType(type)
            }
        }
    }

    override fun setMicSeatCount(count: Int) {

    }

    override fun getMicSeatItemViewList(): Array<IMicSeatItemView> {
        return micSeatViewList as Array<IMicSeatItemView>
    }

    override fun setMicSeatActionDelegate(actionDelegate: IMicSeatsView.ActionDelegate?) {
        this.actionDelegate = actionDelegate
    }

    private fun showMicSeatDialog(view:View) {
        val index = view.tag as Int
        Log.e("apex","showMicSeatDialog: $index")
        val bottomSheetDialog =
            BottomSheetDialog(context, R.style.Theme_AppCompat_Dialog_Transparent)
        val contentView = AUIMicSeatDialogView(context)
        contentView.setEnterSeatClickListener { v: View? ->
            if (actionDelegate != null) {
                actionDelegate?.onClickEnterSeat(index)
            }
            bottomSheetDialog.dismiss()
        }
        contentView.setLeaveSeatClickListener { v: View? ->
            if (actionDelegate != null) {
                actionDelegate?.onClickLeaveSeat(index)
            }
            bottomSheetDialog.dismiss()
        }
        contentView.setKickSeatClickListener { v: View? ->
            if (actionDelegate != null) {
                actionDelegate?.onClickKickSeat(index)
            }
            bottomSheetDialog.dismiss()
        }
        contentView.setCloseSeatClickListener { v: View? ->
            if (actionDelegate != null) {
                actionDelegate?.onClickCloseSeat(index, !contentView.isSeatClosed)
            }
            bottomSheetDialog.dismiss()
        }
        contentView.setMuteAudioClickListener { v: View? ->
            if (actionDelegate != null) {
                actionDelegate?.onClickMuteAudio(index, !contentView.isMuteAudio)
            }
            bottomSheetDialog.dismiss()
        }
        contentView.setMuteVideoClickListener { v: View? ->
            if (actionDelegate != null) {
                actionDelegate?.onClickMuteVideo(index, !contentView.isMuteVideo)
            }
            bottomSheetDialog.dismiss()
        }
        contentView.setInvitedClickListener { v: View? ->
            if (actionDelegate != null) {
                actionDelegate?.onClickInvited(index)
            }
            bottomSheetDialog.dismiss()
        }
        if (actionDelegate != null) {
            if (actionDelegate?.onClickSeat(index, contentView) == false) {
                return
            }
        }
        bottomSheetDialog.setContentView(contentView)
        bottomSheetDialog.show()
    }

    override fun startRippleAnimation(index: Int) {
        val circleViewWrap = micSeatViewList[index]
        val auiMicSeatItemView = circleViewWrap?.getView() as AUIMicSeatItemView
        auiMicSeatItemView.startRippleAnimation()

    }

    override fun stopRippleAnimation(index: Int) {
        val circleViewWrap = micSeatViewList[index]
        val auiMicSeatItemView = circleViewWrap?.getView() as AUIMicSeatItemView
        auiMicSeatItemView.stopRippleAnimation()
    }
}
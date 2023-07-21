package io.agora.auikit.ui.micseats.impl

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.agora.auikit.model.MicSeatItem
import io.agora.auikit.ui.R
import io.agora.auikit.ui.micseats.IMicSeatItemView
import io.agora.auikit.ui.micseats.IMicSeatsView
import io.agora.auikit.utils.DeviceTools
import kotlin.math.min

class AUIMicSeatHostAudienceLayout : FrameLayout, IMicSeatsView{
    /**
     * 行数
     */
    private var numColumns = 4
    /**
     * 列数
     */
    private var numRows = 2

    /**
     * 横向间距
     */
    private var horizontalSpacing = 0

    /**
     * 纵向间距
     */
    private var verticalSpacing = 0
    /**
     * 座位数
     */
    private var micSeatCount = 9
    /**
     * 中心X坐标
     */
    private var centerX = 0f
    /**
     * 中心Y坐标
     */
    private var centerY = 0f
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
    private var childWidth = 62

    /**
     * ItemView 的高度
     */
    private var childHeight = 100

    private var isReady:Boolean = false

    private var mMicSeatMap = mutableMapOf<Int, MicSeatItem>()
    private var micSeatViewList = arrayOfNulls<MicSeatHostViewWrap>(9)
    private var actionDelegate: IMicSeatsView.ActionDelegate? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        for (i in 0 until 9) {
            micSeatViewList[i] = MicSeatHostViewWrap()
        }
    }

    fun initView(context: Context){
        val view = inflate(context, R.layout.aui_micseat_frame_layout, this)
        val layout = view.findViewById<FrameLayout>(R.id.rootLayout)
        addView(layout)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w
        mHeight = h

        centerX = (mWidth/2).toFloat()
        centerY = (mHeight/2).toFloat()

        Log.e("apex","onSizeChanged $mWidth $mHeight")

        post {
            invalidate()
            if (!isReady){
                initView(context)
                isReady = true
            }
        }
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
                    itemView.x = it.micX - DeviceTools.dp2px(context,(childWidth/2).toFloat())
                    itemView.y = it.micY - DeviceTools.dp2px(context,(min(childWidth,childHeight)/2).toFloat())
                }
                itemView.let {
                    it.layoutParams = ViewGroup.LayoutParams(
                        DeviceTools.dp2px(context,childWidth+20.toFloat()),
                        DeviceTools.dp2px(context,childHeight.toFloat()))

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
        horizontalSpacing = (mWidth - childWidth * numColumns) / (numColumns+1)
        verticalSpacing = (mHeight - childHeight * numRows) / (numRows + 1)
        var endX = 0f
        var endY = 0f
        for (i in 0 until micSeatCount) {
            if (i == 0){
                mMicSeatMap[0] = MicSeatItem(i,centerX ,childHeight+(childHeight/4).toFloat())
            }else{
                if (i <= 4){
                    endX = ((horizontalSpacing * i) + childWidth * (i-1)).toFloat()
                    endY = verticalSpacing * 1 + childHeight * 1 + (childHeight/3).toFloat()
                }else{
                    val b = (i - 4)
                    endX = ((horizontalSpacing * b) + childWidth * (b-1)).toFloat()
                    endY = (verticalSpacing * 2 + childHeight * 2).toFloat()
                }
                mMicSeatMap[i] = MicSeatItem(i,endX,endY)
            }
        }
    }

    private class MicSeatHostViewWrap : IMicSeatItemView {
        private var titleText: String? = null
        private var titleIndex = 0
        private var audioMuteVisibility = GONE
        private var videoMuteVisibility = GONE
        private var roomOwnerVisibility = GONE
        private var chorusType = IMicSeatItemView.ChorusType.None
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

        override fun setChorusMicOwnerType(type: IMicSeatItemView.ChorusType) {
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

    private fun showMicSeatDialog(view: View) {
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
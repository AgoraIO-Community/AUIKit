package io.agora.auikit.ui.roomInfo.impl

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.agora.auikit.R
import io.agora.auikit.databinding.AuiRoomInfoLayoutBinding
import io.agora.auikit.ui.roomInfo.IAUIRoomInfoView
import io.agora.auikit.ui.roomInfo.listener.AUIRoomInfoActionListener
import io.agora.auikit.utils.DeviceTools

class AUIRoomInfoView : ConstraintLayout,
    IAUIRoomInfoView {
    private val aRoomViewBinding = AuiRoomInfoLayoutBinding.inflate(LayoutInflater.from(context))
    private var aUpperLeftListener: AUIRoomInfoActionListener? = null
    private var aContext: Context? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    @SuppressLint("CustomViewStyleable")
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        this.aContext = context
        addView(aRoomViewBinding.root)
        val themeTa = context.obtainStyledAttributes(attrs, R.styleable.AUIUpperLeftInformationView, defStyleAttr, 0)
        val appearanceId = themeTa.getResourceId(R.styleable.AUIUpperLeftInformationView_aui_upperLeft_appearance, 0)
        themeTa.recycle()
        initListener()
        initView(appearanceId)
    }

    @SuppressLint("CustomViewStyleable")
    private fun initView(appearanceId:Int){
        val typedArray = context.obtainStyledAttributes(appearanceId, R.styleable.AUIUpperLeftInformationView)
        val gradientType = typedArray.getInt(
            R.styleable.AUIUpperLeftInformationView_aui_upperLeft_rootLayout_style,
            0
        )
        val gradientColor = typedArray.getInt(
            R.styleable.AUIUpperLeftInformationView_aui_upperLeft_Layout_background,
            resources.getColor(R.color.aui_upper_left_bg)
        )

        val gradientRadius = typedArray.getInt(
            R.styleable.AUIUpperLeftInformationView_aui_upperLeft_layout_radius,
            0
        )

        typedArray.recycle()
        val gradientDrawable = GradientDrawable()
        gradientDrawable.setColor(gradientColor)
        gradientDrawable.cornerRadius = DeviceTools.dp2px(context,gradientRadius.toFloat()).toFloat()
        when(gradientType){
            0 -> { gradientDrawable.shape = GradientDrawable.RECTANGLE }
            1 -> { gradientDrawable.shape = GradientDrawable.OVAL }
            2 -> { gradientDrawable.shape = GradientDrawable.LINE }
            3 -> { gradientDrawable.shape = GradientDrawable.RING }
        }
        aRoomViewBinding.rootLayout.background = gradientDrawable
    }

    private fun initListener(){
        aRoomViewBinding.backIcon.setOnClickListener{
            aUpperLeftListener?.onBackClickListener(it)
        }
        aRoomViewBinding.ivRoomCover.setOnClickListener {
            aUpperLeftListener?.onClickUpperLeftAvatar(it)
        }
        aRoomViewBinding.ivRoomCover.setOnLongClickListener { v ->
            v?.let { aUpperLeftListener?.onLongClickUpperLeftAvatar(it) } == true
        }
        aRoomViewBinding.ivRoomRightIcon.setOnClickListener{
            aUpperLeftListener?.onUpperLeftRightIconClickListener(it)
        }
    }

    override fun setVoiceTitle(title:String){
        aRoomViewBinding.tvRoomTitle.text = title
    }

    override fun setVoiceSubTitle(subtitle:String){
        aRoomViewBinding.tvRoomSubtitle.text = subtitle
    }

    override fun setMemberAvatar(url:String){
        aContext?.let {
            Glide.with(it)
                .load(url)
                .error(R.drawable.aui_room_info_avatar)
                .apply(RequestOptions.circleCropTransform())
                .into(aRoomViewBinding.ivRoomCover)
        }
    }

    override fun setMemberAvatar(uri:Uri){
        aContext?.let {
            Glide.with(it)
                .load(uri)
                .error(R.drawable.aui_room_info_avatar)
                .apply(RequestOptions.circleCropTransform())
                .into(aRoomViewBinding.ivRoomCover)
        }
    }

    override fun setRightIcon(url:String){
        aContext?.let {
            Glide.with(it)
                .load(url)
                .error(R.drawable.aui_room_info_avatar)
                .apply(RequestOptions.circleCropTransform())
                .into(aRoomViewBinding.ivRoomCover)
        }
    }

    override fun setRightIcon(uri:Uri){
        aContext?.let {
            Glide.with(it)
                .load(uri)
                .error(R.drawable.aui_room_info_avatar)
                .apply(RequestOptions.circleCropTransform())
                .into(aRoomViewBinding.ivRoomCover)
        }
    }

    override fun setRoomInfoActionListener(listener: AUIRoomInfoActionListener?) {
       this.aUpperLeftListener = listener
    }

}
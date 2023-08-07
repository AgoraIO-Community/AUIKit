package io.agora.auikit.ui.gift.impl

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Typeface
import android.text.SpannableString
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.textview.MaterialTextView
import io.agora.auikit.ui.R
import io.agora.auikit.ui.basic.AUIImageView
import io.agora.auikit.ui.gift.AUIGiftInfo

class AUIGiftRowAdapter constructor(
    private val context: Context,
    typedArray: TypedArray
): RecyclerView.Adapter<GiftViewHolder>() {
    var dataList:ArrayList<AUIGiftInfo> = ArrayList()
    private var mContext:Context?=null
    private var giftBarrageLayoutBg: Int

    init {
        this.mContext = context
        giftBarrageLayoutBg = typedArray.getResourceId(
            R.styleable.AUIGiftBarrageView_aui_giftBarrage_layout_bg,
            R.drawable.aui_gift_widget_bg_light
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GiftViewHolder {
        return GiftViewHolder(
            LayoutInflater.from(context).inflate(R.layout.aui_gift_barrage_row_item, parent, false)
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: GiftViewHolder, position: Int) {
        holder.baseLayout.setBackgroundResource(giftBarrageLayoutBg)
        val auiGiftInfo = dataList[position]
        Log.e("apex"," data $dataList")
        show(holder.avatar, holder.icon, holder.name, auiGiftInfo)

        if (mContext != null) {
            val fromAsset = Typeface.createFromAsset(
                mContext?.assets,
                "fonts/RobotoNembersVF.ttf"
            ) //根据路径得到Typeface
            holder.iconCount.typeface = fromAsset
        }
        holder.iconCount.text = "x" + auiGiftInfo.giftCount
    }

    private fun show(avatar: AUIImageView, icon: AUIImageView, name: MaterialTextView, auiGiftEntity: AUIGiftInfo?) {
        val builder = StringBuilder()
        if (null != auiGiftEntity) {
            val sendName = if(TextUtils.isEmpty(auiGiftEntity.sendUserName)){
                auiGiftEntity.sendUserId
            }else{
                auiGiftEntity.sendUserName
            }

            builder.append(sendName)
                .append(":").append("\n").append(context.getString(R.string.voice_gift_sent))
                .append(" ").append(auiGiftEntity.giftName)

            Glide.with(context)
                .load(auiGiftEntity.sendUserAvatar)
                .error(R.drawable.aui_gift_user_default_icon)
                .into(avatar)

            Glide.with(context)
                .load(auiGiftEntity.giftIcon)
                .into(icon)
        }
        val span = SpannableString(builder.toString())
        name.text = span
    }

    fun removeAll() {
        notifyItemRangeRemoved(0, dataList.size)
        dataList.clear()
    }

    fun refresh(giftList:List<AUIGiftInfo>) {
        // 刷新礼物列表 记录当前礼物列表数量 计算起始 position
        val positionStart: Int = dataList.size
        // 获取增量礼物列表插入原礼物列表
        giftList.let { dataList.addAll(it) }
        // 只更新新插入的礼物item
        if ( dataList.size > 0 && positionStart < dataList.size) {
            notifyItemRangeInserted(
                positionStart,
                dataList.size
            )
        }
    }

    override fun getItemCount(): Int {
        if (dataList.size > 0){
            return dataList.size
        }
        return 0
    }
}


class GiftViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var name: MaterialTextView
    var icon: AUIImageView
    var iconCount: MaterialTextView
    var avatar: AUIImageView
    var baseLayout:ConstraintLayout

    init {
        avatar = itemView.findViewById(R.id.avatar)
        name = itemView.findViewById(R.id.nick_name)
        icon = itemView.findViewById(R.id.icon)
        iconCount = itemView.findViewById(R.id.gift_count)
        baseLayout = itemView.findViewById(R.id.rootLayout)
    }
}
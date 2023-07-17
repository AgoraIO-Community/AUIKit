package io.agora.auikit.ui.chatBottomBar.impl

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.agora.auikit.model.AUIExpressionIcon
import io.agora.auikit.ui.R
import io.agora.auikit.ui.basic.AUIImageView

class AUIEmojiGridAdapter(
    context: Context,
    resource: Int,
    objects: MutableList<AUIExpressionIcon>
) : ArrayAdapter<AUIExpressionIcon>(context, resource, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.aui_emoji_item_layout, null)
        }
        val imageView = convertView!!.findViewById<AUIImageView>(R.id.iv_expression)
        val emojIcon: AUIExpressionIcon? = getItem(position)
        emojIcon.let {
            if (emojIcon?.icon != 0) {
                emojIcon?.icon?.let { imageView.setImageResource(it) }
            } else if (emojIcon?.iconPath != null) {
                Glide.with(context).load(emojIcon.iconPath)
                    .apply(RequestOptions.placeholderOf(R.drawable.voice_icon_default_expression))
                    .into(imageView)
            }
            return convertView
        }
    }

}
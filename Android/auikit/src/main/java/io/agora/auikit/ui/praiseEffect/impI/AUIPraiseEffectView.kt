package io.agora.auikit.ui.praiseEffect.impI
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import io.agora.auikit.R

class AUIPraiseEffectView : AppCompatImageView {
    private var mHeartResId: Int = R.drawable.aui_praise_icon_1
    private var mHeartBorderResId: Int = R.drawable.aui_praise_icon_2

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr) {

    }

    companion object {
        private var sPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        private var sCanvas = Canvas()
        private var sHeart: Bitmap? = null
        private var sHeartBorder: Bitmap? = null
    }

    fun setDrawable(resourceId: Int) {
        val heart = BitmapFactory.decodeResource(resources, resourceId)
        setImageDrawable(BitmapDrawable(resources, heart))
    }

    fun setColor(color: Int) {
        val heart: Bitmap? = createHeart(color)
        setImageDrawable(BitmapDrawable(resources, heart))
    }

    fun setColorAndDrawables(color: Int, heartResId: Int, heartBorderResId: Int) {
        if (heartResId != mHeartResId) {
            sHeart = null
        }
        if (heartBorderResId != mHeartBorderResId) {
            sHeartBorder = null
        }
        mHeartResId = heartResId
        mHeartBorderResId = heartBorderResId
        setColor(color)
    }

    private fun createHeart(color: Int): Bitmap? {
        if (sHeart == null) {
            sHeart = BitmapFactory.decodeResource(resources, mHeartResId)
        }
        if (sHeartBorder == null) {
            sHeartBorder = BitmapFactory.decodeResource(resources, mHeartBorderResId)
        }
        val heart = sHeart
        val heartBorder = sHeartBorder
        val bm = heartBorder?.let { createBitmapSafely(it.width, heartBorder.height) } ?: return null
        val canvas = sCanvas
        canvas.setBitmap(bm)
        val p = sPaint
        canvas.drawBitmap(heartBorder, 0f, 0f, p)
        p.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        heart?.let {
            val dx = (heartBorder.width - heart.width) / 2f
            val dy = (heartBorder.height - heart.height) / 2f
            canvas.drawBitmap(it, dx, dy, p)
        }
        p.colorFilter = null
        canvas.setBitmap(null)
        return bm
    }

    private fun createBitmapSafely(width: Int, height: Int): Bitmap? {
        try {
            return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        } catch (error: OutOfMemoryError) {
            error.printStackTrace()
        }
        return null
    }

}
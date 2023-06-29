package io.agora.auikit.ui.praiseEffect.impI
import android.content.res.TypedArray
import android.graphics.Path
import android.view.View
import android.view.ViewGroup
import io.agora.auikit.R
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.roundToInt

abstract class AUIAbstractPathAnimator(
    protected val mConfig: Config? = null)
{
    private var mRandom: Random? = null

    init {
        mRandom = Random()

    }

    open fun randomRotation(): Float {
        return mRandom!!.nextFloat() * 28.6f - 14.3f
    }

    open fun createPath(counter: AtomicInteger, view: View, fact: Int): Path? {
        var factor = fact
        val r = mRandom!!
        var x = r.nextInt(mConfig!!.xRand)
        var x2 = r.nextInt(mConfig.xRand)
        val y = view.height - mConfig.initY
        var y2 =
            counter.toInt() * 15 + mConfig.animLength * factor + r.nextInt(mConfig.animLengthRand)
        factor = y2 / mConfig.bezierFactor
        x += mConfig.xPointFactor
        x2 += mConfig.xPointFactor
        val y3 = y - y2
        y2 = y - y2 / 2
        val p = Path()
        p.moveTo(mConfig.initX.toFloat(), y.toFloat())
        p.cubicTo(
            mConfig.initX.toFloat(),
            (y - factor).toFloat(),
            x.toFloat(),
            (y2 + factor).toFloat(),
            x.toFloat(),
            y2.toFloat()
        )
        p.moveTo(x.toFloat(), y2.toFloat())
        p.cubicTo(
            x.toFloat(),
            (y2 - factor).toFloat(),
            x2.toFloat(),
            (y3 + factor).toFloat(),
            x2.toFloat(),
            y3.toFloat()
        )
        return p
    }

    abstract fun start(child: View?, parent: ViewGroup?)

    class Config {
        var initX = 0
        var initY = 0
        var xRand = 0
        var animLengthRand = 0
        var bezierFactor = 0
        var xPointFactor = 0
        var animLength = 0
        var heartWidth = 0
        var heartHeight = 0
        var animDuration = 0

        companion object {
            fun fromTypeArray(
                typedArray: TypedArray,
                x: Float,
                y: Float,
                pointx: Int,
                heartWidth: Int,
                heartHeight: Int
            ): Config {
                val config = Config()
                val res = typedArray.resources
                config.initX =
                    typedArray.getDimension(R.styleable.voice_LikeLayout_voice_initX, x).toInt()
                config.initY =
                    typedArray.getDimension(R.styleable.voice_LikeLayout_voice_initY, y).toInt()
                config.xRand = typedArray.getDimension(
                    R.styleable.voice_LikeLayout_voice_xRand,
                    res.getDimensionPixelOffset(R.dimen.voice_like_anim_bezier_x_rand).toFloat()
                ).toInt()
                config.animLength = typedArray.getDimension(
                    R.styleable.voice_LikeLayout_voice_animLength,
                    res.getDimensionPixelOffset(R.dimen.voice_like_anim_length).toFloat()
                ).toInt() //动画长度
                config.animLengthRand = typedArray.getDimension(
                    R.styleable.voice_LikeLayout_voice_animLengthRand,
                    res.getDimensionPixelOffset(R.dimen.voice_like_anim_length_rand).toFloat()
                ).toInt()
                config.bezierFactor = typedArray.getInteger(
                    R.styleable.voice_LikeLayout_voice_bezierFactor,
                    res.getInteger(R.integer.voice_like_anim_bezier_factor)
                )
                config.xPointFactor = pointx
                config.heartWidth = typedArray.getDimension(
                    R.styleable.voice_LikeLayout_voice_heart_width,
                    heartWidth.toFloat()
                ).roundToInt()
                config.heartHeight = typedArray.getDimension(
                    R.styleable.voice_LikeLayout_voice_heart_height,
                    heartHeight.toFloat()
                ).roundToInt()
//                config.heartWidth = heartWidth
//                config.heartHeight = heartHeight
                config.animDuration = typedArray.getInteger(
                    R.styleable.voice_LikeLayout_voice_anim_duration,
                    res.getInteger(R.integer.voice_anim_duration)
                ) //持续期
                return config
            }
        }
    }

}
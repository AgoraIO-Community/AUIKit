package io.agora.auikit.ui.micseats.impl

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import io.agora.auikit.ui.R
import java.util.concurrent.CopyOnWriteArrayList


class AUIRippleAnimationView:View {

    private var mInitialRadius = 80f // 初始波纹半径
    private var mMaxRadius = 110f // 最大波纹半径
    private var mDuration: Long = 3000 // 一个波纹从创建到消失的持续时间
    private var mSpeed = 1000 // 波纹的创建速度
    private var mMaxRadiusRate = 0.85f
    private var rippleStrokeWidth = 2
    private var rippleColor = 0
    private var mMaxRadiusSet = false
    private var mIsRunning = false
    private var mLastCreateTime: Long = 0

    private val mCircleList = CopyOnWriteArrayList<Circle>()

    private var mInterpolator: Interpolator = LinearInterpolator()
    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val mCreateCircle: Runnable = object : Runnable {
        override fun run() {
            if (mIsRunning) {
                newCircle()
                postDelayed(this, mSpeed.toLong())
            }
        }
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        val themeTa = context.obtainStyledAttributes(attrs, R.styleable.AUIRippleAnimationView, defStyleAttr, 0)
        val appearanceId = themeTa.getResourceId(R.styleable.AUIRippleAnimationView_aui_ripple_appearance, 0)
        themeTa.recycle()
        initView(appearanceId)
    }

    private fun initView(appearanceId:Int){
        val typedArray = context.obtainStyledAttributes(appearanceId, R.styleable.AUIRippleAnimationView)
        mInitialRadius = typedArray.getDimensionPixelSize(R.styleable.AUIRippleAnimationView_aui_ripple_initial_radius,80).toFloat()
        mMaxRadius = typedArray.getDimensionPixelSize(R.styleable.AUIRippleAnimationView_aui_ripple_max_radius, 110).toFloat()
        rippleStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.AUIRippleAnimationView_aui_ripple_stroke_width, 2)
        mSpeed =  typedArray.getInteger(R.styleable.AUIRippleAnimationView_aui_ripple_speed,500)
        mMaxRadiusRate = typedArray.getFloat(R.styleable.AUIRippleAnimationView_aui_ripple_max_radiusRate, 0.85f)
        rippleColor = typedArray.getInt(
            R.styleable.AUIRippleAnimationView_aui_ripple_color,
            resources.getColor(R.color.aui_ripple_stroke_color)
        )
        mPaint.color = rippleColor
        mPaint.strokeWidth = rippleStrokeWidth.toFloat()
        typedArray.recycle()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (!mMaxRadiusSet) {
            mMaxRadius = w.coerceAtMost(h) * mMaxRadiusRate / 2.0f
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val iterator: MutableIterator<Circle> =
            mCircleList.iterator()
        while (iterator.hasNext()) {
            val circle: Circle = iterator.next()
            val radius: Float = circle.currentRadius
            if (System.currentTimeMillis() - circle.mCreateTime < mDuration) {
                mPaint.alpha = circle.alpha
                canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), radius, mPaint)
            }
        }
        if (mCircleList.size > 0) {
            postInvalidateDelayed(1000)
        }
    }

    /**
     * 开始
     */
    fun startRippleAnimation() {
        if (!mIsRunning) {
            mIsRunning = true
            mCreateCircle.run()
        }
    }

    /**
     * 缓慢停止
     */
    fun stopRippleAnimation() {
        mIsRunning = false
    }

    /**
     * 立即停止
     */
    fun stopImmediatelyRippleAnimation() {
        mIsRunning = false
        mCircleList.clear()
        invalidate()
    }

    fun setStyle(style: Paint.Style?) {
        mPaint.style = style
    }

    fun setMaxRadiusRate(maxRadiusRate: Float) {
        mMaxRadiusRate = maxRadiusRate
    }

    fun setColor(color: Int) {
        mPaint.color = color
    }

    fun setInitialRadius(radius: Float) {
        mInitialRadius = radius
        invalidate()
    }

    fun setDuration(duration: Long) {
        mDuration = duration
    }

    fun setMaxRadius(maxRadius: Float) {
        mMaxRadius = maxRadius
        mMaxRadiusSet = true
        invalidate()
    }

    fun setStrokeWidth(strokeWidth: Float) {
        mPaint.strokeWidth = strokeWidth
    }

    fun setSpeed(speed: Int) {
        mSpeed = speed
    }


    private fun newCircle() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - mLastCreateTime < mSpeed) {
            return
        }
        val circle = Circle()
        mCircleList.add(circle)
        invalidate()
        mLastCreateTime = currentTime
    }


    inner class Circle {
        val mCreateTime: Long = System.currentTimeMillis()
        val alpha: Int
            get() {
                val percent: Float =
                    (currentRadius - mInitialRadius) / (mMaxRadius - mInitialRadius)
                return (255 - mInterpolator.getInterpolation(percent) * 255).toInt()
            }
        val currentRadius: Float
            get() {
                val percent: Float = (System.currentTimeMillis() - mCreateTime) * 1.0f / mDuration
                return mInitialRadius + mInterpolator.getInterpolation(percent) * (mMaxRadius - mInitialRadius)
            }

    }

    fun setInterpolator(interpolator: Interpolator) {
        mInterpolator = interpolator
        if (mInterpolator == null) {
            mInterpolator = LinearInterpolator()
        }
        invalidate()
    }
}
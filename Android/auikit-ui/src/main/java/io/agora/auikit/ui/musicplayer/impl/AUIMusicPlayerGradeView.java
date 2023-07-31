package io.agora.auikit.ui.musicplayer.impl;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import io.agora.auikit.ui.R;

public class AUIMusicPlayerGradeView extends View {
    private int backgroundColor;
    private int contentStartColor;
    private int contentMiddleColor;
    private int contentEndColor;
    private int labelTextColor;
    private int labelSeparatorColor;

    public AUIMusicPlayerGradeView(Context context) {
        this(context, null);
    }

    public AUIMusicPlayerGradeView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AUIMusicPlayerGradeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AUIMusicPlayerGradeView);
        backgroundColor = typedArray.getColor(R.styleable.AUIMusicPlayerGradeView_aui_musicPlayerGradeView_backgroundColor, Color.parseColor("#4130C7"));
        contentStartColor = typedArray.getColor(R.styleable.AUIMusicPlayerGradeView_aui_musicPlayerGradeView_contentStartColor, Color.parseColor("#FF99f5FF"));
        contentMiddleColor = typedArray.getColor(R.styleable.AUIMusicPlayerGradeView_aui_musicPlayerGradeView_contentMiddleColor, Color.parseColor("#FF1B6FFF"));
        contentEndColor = typedArray.getColor(R.styleable.AUIMusicPlayerGradeView_aui_musicPlayerGradeView_contentEndColor, Color.parseColor("#FFD598FF"));
        labelSeparatorColor = typedArray.getColor(R.styleable.AUIMusicPlayerGradeView_aui_musicPlayerGradeView_labelSeparatorColor, Color.parseColor("#171A1C"));
        labelTextColor = typedArray.getColor(R.styleable.AUIMusicPlayerGradeView_aui_musicPlayerGradeView_labelTextColor, Color.parseColor("#4130C7"));
        typedArray.recycle();
    }

    private final RectF mDefaultBackgroundRectF = new RectF();

    private final Paint mDefaultBackgroundPaint = new Paint();

    /**
     * Separate this view by 5 parts(->C, C->B, B->A, A->S, S->)
     * 0.6, 0.7, 0.8, 0.9 by PRD
     */
    private static final float xRadioOfGradeC = 0.6f;
    private static final float xRadioOfGradeB = 0.7f;
    private static final float xRadioOfGradeA = 0.8f;
    private static final float xRadioOfGradeS = 0.9f;

    private final Paint mGradeSeparatorIndicatorPaint = new Paint();
    private final Paint mGradeSeparatorLabelIndicatorPaint = new Paint();

    private float mWidth = 0;
    private float mHeight = 0;

    private final RectF mCumulativeScoreBarRectF = new RectF();
    private final Paint mCumulativeScoreBarPaint = new Paint();

    private LinearGradient mCumulativeLinearGradient;

    private int mCumulativeScore;
    private int mPerfectScore;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            mWidth = right - left;
            mHeight = bottom - top;

            mDefaultBackgroundRectF.top = 0;
            mDefaultBackgroundRectF.left = 0;
            mDefaultBackgroundRectF.right = mWidth;
            mDefaultBackgroundRectF.bottom = mHeight;
        }
    }

    private boolean isCanvasNotReady() {
        return mWidth <= 0 && mHeight <= 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isCanvasNotReady()) { // Fail fast
            return;
        }

        mGradeSeparatorIndicatorPaint.setShader(null);
        mGradeSeparatorIndicatorPaint.setAntiAlias(true);

        mGradeSeparatorLabelIndicatorPaint.setShader(null);
        mGradeSeparatorLabelIndicatorPaint.setAntiAlias(true);
        mGradeSeparatorLabelIndicatorPaint.setTextSize(32);
        mGradeSeparatorLabelIndicatorPaint.setStyle(Paint.Style.FILL);
        mGradeSeparatorLabelIndicatorPaint.setTextAlign(Paint.Align.CENTER);

        mDefaultBackgroundPaint.setShader(null);
        mDefaultBackgroundPaint.setColor(backgroundColor);
        mGradeSeparatorIndicatorPaint.setColor(labelSeparatorColor);
        mGradeSeparatorLabelIndicatorPaint.setColor(labelTextColor);

        Paint.FontMetrics fontMetrics = mGradeSeparatorLabelIndicatorPaint.getFontMetrics();
        float offsetForLabelX = mGradeSeparatorLabelIndicatorPaint.measureText("S");
        float baseLineForLabel = mHeight / 2 - fontMetrics.top / 2 - fontMetrics.bottom / 2;

        mDefaultBackgroundPaint.setAntiAlias(true);
        canvas.drawRoundRect(mDefaultBackgroundRectF, mHeight / 2, mHeight / 2, mDefaultBackgroundPaint);

        canvas.drawLine((float) (mWidth * xRadioOfGradeC), 0, (float) (mWidth * xRadioOfGradeC), mHeight, mGradeSeparatorIndicatorPaint);
        canvas.drawText("C", (float) ((mWidth * xRadioOfGradeC) + offsetForLabelX), baseLineForLabel, mGradeSeparatorLabelIndicatorPaint);
        canvas.drawLine((float) (mWidth * xRadioOfGradeB), 0, (float) (mWidth * xRadioOfGradeB), mHeight, mGradeSeparatorIndicatorPaint);
        canvas.drawText("B", (float) ((mWidth * xRadioOfGradeB) + offsetForLabelX), baseLineForLabel, mGradeSeparatorLabelIndicatorPaint);
        canvas.drawLine((float) (mWidth * xRadioOfGradeA), 0, (float) (mWidth * xRadioOfGradeA), mHeight, mGradeSeparatorIndicatorPaint);
        canvas.drawText("A", (float) ((mWidth * xRadioOfGradeA) + offsetForLabelX), baseLineForLabel, mGradeSeparatorLabelIndicatorPaint);
        canvas.drawLine((float) (mWidth * xRadioOfGradeS), 0, (float) (mWidth * xRadioOfGradeS), mHeight, mGradeSeparatorIndicatorPaint);
        canvas.drawText("S", (float) ((mWidth * xRadioOfGradeS) + offsetForLabelX), baseLineForLabel, mGradeSeparatorLabelIndicatorPaint);

        if (mCumulativeLinearGradient == null) {
            buildDefaultCumulativeScoreBarStyle(contentStartColor, contentMiddleColor);
        }
        mCumulativeScoreBarPaint.setShader(mCumulativeLinearGradient);
        mCumulativeScoreBarPaint.setAntiAlias(true);
        canvas.drawRoundRect(mCumulativeScoreBarRectF, mHeight / 2, mHeight / 2, mCumulativeScoreBarPaint);
    }

    public void setScore(int score, int cumulativeScore, int perfectScore) {
        mCumulativeScore = cumulativeScore;
        mPerfectScore = perfectScore;

        if (mCumulativeScore <= perfectScore * 0.1) {
            buildDefaultCumulativeScoreBarStyle(contentStartColor, contentStartColor);
        } else {
            float currentWidthOfScoreBar = mWidth * cumulativeScore / perfectScore;

            if (mCumulativeScore > perfectScore * 0.1 && mCumulativeScore < perfectScore * 0.8) {
                mCumulativeLinearGradient = new LinearGradient(0, 0, currentWidthOfScoreBar, mHeight, contentStartColor, contentMiddleColor, Shader.TileMode.CLAMP);
            } else {
                mCumulativeLinearGradient = new LinearGradient(0, 0, currentWidthOfScoreBar, mHeight, new int[]{contentStartColor, contentMiddleColor, contentEndColor}, null, Shader.TileMode.CLAMP);
            }

            mCumulativeScoreBarRectF.top = 0;
            mCumulativeScoreBarRectF.bottom = mHeight;
            mCumulativeScoreBarRectF.left = 0;
            mCumulativeScoreBarRectF.right = currentWidthOfScoreBar;
        }

        invalidate();
    }

    protected int getCumulativeDrawable() {
        int res = 0;

//        if (mCumulativeScore >= mPerfectScore * xRadioOfGradeS) {
//            res = R.drawable.ktv_ic_grade_s;
//        } else if (mCumulativeScore >= mPerfectScore * xRadioOfGradeA) {
//            res = R.drawable.ktv_ic_grade_a;
//        } else if (mCumulativeScore >= mPerfectScore * xRadioOfGradeB) {
//            res = R.drawable.ktv_ic_grade_b;
//        } else if (mCumulativeScore >= mPerfectScore * xRadioOfGradeC) {
//            res = R.drawable.ktv_ic_grade_c;
//        }

        return res;
    }

    private void buildDefaultCumulativeScoreBarStyle(int fromColor, int toColor) {
        if (mHeight <= 0) {
            return;
        }

        mCumulativeLinearGradient = new LinearGradient(0, 0, mHeight, mHeight, fromColor, toColor, Shader.TileMode.CLAMP);

        mCumulativeScoreBarRectF.top = 0;
        mCumulativeScoreBarRectF.bottom = mHeight;
        mCumulativeScoreBarRectF.left = 0;
        mCumulativeScoreBarRectF.right = mHeight;
    }
}


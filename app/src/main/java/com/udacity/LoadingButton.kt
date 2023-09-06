package com.udacity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.animation.ValueAnimator.INFINITE
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0

    private val loadingRect = RectF(0f, 0f, 0f, 0f)
    private var circleRect = RectF(0f, 0f, 0f, 0f)
    private var sweepAngle = 0f
    private var textResource = resources.getString(R.string.button_download)

    private var valueAnimatorFirstHalf = ValueAnimator()
    private var valueAnimatorSecondHalf = ValueAnimator()
    private var valueAnimatorCircleFirstHalf = ValueAnimator()
    private var valueAnimatorCircleSecondHalf = ValueAnimator()
    private val setFirstHalf = AnimatorSet()
    private val setSecondHalf = AnimatorSet()

    private var textColor = 0
    private var backgroundColor = 0
    private var loadingBarColor = 0
    private var loadingCircleColor = 0

    var buttonState: ButtonState by Delegates.observable(ButtonState.Completed) { p, old, new ->
        when (buttonState) {
            ButtonState.Clicked -> {
                textResource = resources.getString(R.string.button_downloading)
                isClickable = false
                startLoadingAnimation()
            }
            ButtonState.Loading -> {
                isClickable = false
                finishLoadingAnimation()
            }
            ButtonState.Completed -> {
                textResource = resources.getString(R.string.button_download)
                isClickable = true
                resetLoadingAnimation()
            }
        }
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
    }

    init {
        isClickable = true

        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            textColor = getColor(R.styleable.LoadingButton_textColor, 0)
            backgroundColor = getColor(R.styleable.LoadingButton_backgroundColor, 0)
            loadingBarColor = getColor(R.styleable.LoadingButton_loadingBarColor, 0)
            loadingCircleColor = getColor(R.styleable.LoadingButton_loadingCircleColor, 0)
        }
    }

    override fun performClick(): Boolean {
        super.performClick()
        buttonState = ButtonState.Clicked
        return true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        paint.color = backgroundColor
        canvas?.drawRect(width.toFloat(), height.toFloat(), 0f, 0f, paint)

        paint.color = loadingBarColor
        loadingRect.bottom = height.toFloat()
        canvas?.drawRect(loadingRect, paint)

        valueAnimatorFirstHalf = ValueAnimator.ofFloat(0f, width.toFloat() * 0.8f).apply {
            duration = 30000
            addUpdateListener { updatedAnimation ->
                loadingRect.right = updatedAnimation.animatedValue as Float
                postInvalidate()
            }
        }
        valueAnimatorFirstHalf.repeatCount = INFINITE
        valueAnimatorFirstHalf.interpolator = DecelerateInterpolator(1f)

        valueAnimatorSecondHalf = ValueAnimator.ofFloat(loadingRect.right, width.toFloat()).apply {
            duration = 3000
            addUpdateListener { updatedAnimation ->
                loadingRect.right = updatedAnimation.animatedValue as Float
                postInvalidate()
            }
        }
        valueAnimatorSecondHalf.interpolator = AccelerateInterpolator(2f)

        paint.color = textColor
        val textXPos = (width/2).toFloat()
        val textYPos = (height/2).toFloat() - (paint.ascent() + paint.descent()) / 2
        canvas?.drawText(textResource, textXPos, textYPos, paint)

        paint.color = loadingCircleColor
        circleRect.apply {
            left = width * 0.75f - 30f
            top = (height/2).toFloat() - 30f
            right = width * 0.75f + 30f
            bottom = (height/2).toFloat() + 30f
        }

        canvas?.drawArc(circleRect, 0f, sweepAngle, true, paint)
        valueAnimatorCircleFirstHalf = ValueAnimator.ofFloat(0f, 270f).apply {
            duration = 30000
            addUpdateListener { updatedAnimation ->
                sweepAngle = updatedAnimation.animatedValue as Float
                postInvalidate()
            }
        }
        valueAnimatorCircleFirstHalf.repeatCount = INFINITE
        valueAnimatorCircleFirstHalf.interpolator = DecelerateInterpolator(1f)

        valueAnimatorCircleSecondHalf = ValueAnimator.ofFloat(sweepAngle, 360f).apply {
            duration = 3000
            addUpdateListener { updatedAnimation ->
                sweepAngle = updatedAnimation.animatedValue as Float
                postInvalidate()
            }
        }
        valueAnimatorCircleSecondHalf.interpolator = AccelerateInterpolator(2f)
    }

    private fun startLoadingAnimation() {
        setFirstHalf.playTogether(valueAnimatorFirstHalf, valueAnimatorCircleFirstHalf)
        setFirstHalf.start()
    }

    private fun finishLoadingAnimation() {
        setFirstHalf.end()

        setSecondHalf.apply {
            playTogether(valueAnimatorSecondHalf, valueAnimatorCircleSecondHalf)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    buttonState = ButtonState.Completed
                }
            })
            start()
        }
    }

    private fun resetLoadingAnimation() {
        loadingRect.right = 0f
        sweepAngle = 0f
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }
}
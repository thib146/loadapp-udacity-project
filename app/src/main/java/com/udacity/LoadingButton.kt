package com.udacity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
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

    private var valueAnimator = ValueAnimator()
    private var valueAnimatorCircle = ValueAnimator()

    private var buttonState: ButtonState by Delegates.observable(ButtonState.Completed) { p, old, new ->
        when (buttonState) {
            ButtonState.Clicked -> {
                textResource = resources.getString(R.string.button_downloading)
                isClickable = false
                startLoadingAnimation()
            }
            ButtonState.Loading -> { }
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
    }

    override fun performClick(): Boolean {
        super.performClick()
        buttonState = ButtonState.Clicked
        return true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        paint.color = resources.getColor(R.color.colorPrimary, null)
        canvas?.drawRect(width.toFloat(), height.toFloat(), 0f, 0f, paint)

        paint.color = resources.getColor(R.color.colorPrimaryDark, null)
        loadingRect.bottom = height.toFloat()
        canvas?.drawRect(loadingRect, paint)

        valueAnimator = ValueAnimator.ofFloat(0f, width.toFloat()).apply {
            duration = 3000
            addUpdateListener { updatedAnimation ->
                loadingRect.right = updatedAnimation.animatedValue as Float
                postInvalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    buttonState = ButtonState.Completed
                }
            })
        }

        paint.color = Color.WHITE
        val textXPos = (width/2).toFloat()
        val textYPos = (height/2).toFloat() - (paint.ascent() + paint.descent()) / 2
        canvas?.drawText(textResource, textXPos, textYPos, paint)

        paint.color = resources.getColor(R.color.colorAccent, null)
        circleRect.apply {
            left = width * 0.75f - 30f
            top = (height/2).toFloat() - 30f
            right = width * 0.75f + 30f
            bottom = (height/2).toFloat() + 30f
        }

        canvas?.drawArc(circleRect, 0f, sweepAngle, true, paint)
        valueAnimatorCircle = ValueAnimator.ofFloat(0f, 360f).apply {
            duration = 3000
            addUpdateListener { updatedAnimation ->
                sweepAngle = updatedAnimation.animatedValue as Float
                postInvalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    buttonState = ButtonState.Completed
                }
            })
        }
    }

    private fun startLoadingAnimation() {
        valueAnimator.start()
        valueAnimatorCircle.start()
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
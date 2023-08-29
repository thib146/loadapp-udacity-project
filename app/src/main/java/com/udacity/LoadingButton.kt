package com.udacity

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

    private val rectA = RectF(0f, 0f, 0f, 0f)

    private var valueAnimator = ValueAnimator()

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->

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

        startLoadingAnimation()

        return true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        paint.color = resources.getColor(R.color.colorPrimary, null)
        canvas?.drawRect(width.toFloat(), height.toFloat(), 0f, 0f, paint)

        paint.color = resources.getColor(R.color.colorPrimaryDark, null)
        rectA.bottom = height.toFloat()
        canvas?.drawRect(rectA, paint)

        valueAnimator = ValueAnimator.ofFloat(0f, width.toFloat()).apply {
            duration = 3000
            addUpdateListener { updatedAnimation ->
                rectA.right = updatedAnimation.animatedValue as Float
                postInvalidate()
            }
        }

        paint.color = Color.WHITE
        val textResource = resources.getString(R.string.button_download)
        val textXPos = (width/2).toFloat()
        val textYPos = (height/2).toFloat() - (paint.ascent()+paint.descent())/2
        canvas?.drawText(textResource, textXPos, textYPos, paint)
    }

    private fun startLoadingAnimation() {
        valueAnimator.start()
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
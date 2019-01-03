package com.github.loadingview

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Thanks to @JeasonWong
 * https://github.com/JeasonWong/BezierLoadingView
 * */

class LoadingView(context: Context, atr: AttributeSet?) : View(context, atr) {

    private val DEFAULT_EXTERNAL_RADIUS = dp2px(20f)
    private val DEFAULT_INTERNAL_RADIUS = dp2px(4f)
    private val DEFAULT_RADIAN = 45

    private var mWidth: Int = 0
    private var mHeight: Int = 0
    private var disposable: Disposable? = null
    private var mPaint: Paint? = null
    private val mPath = Path()
    private var mDuration = 2

    internal var colors: IntArray? = null
    internal var angle1 = 0
    internal var cyclic = 0
    private var biggerCircleRadius: Float = 0.toFloat()
    private var smallerCircleRadius: Float = 0.toFloat()
    private var points: MutableList<PointF>? = null
    private var animators: MutableList<ValueAnimator>? = null
    private var xX: Float = 0.toFloat()
    private var yY: Float = 0.toFloat()
    private val radian = DEFAULT_RADIAN
    private val MAX_DURATION = 120
    private val MIN_DURATION = 1
    private var internalRadius: Float = 0.toFloat()
    private val MAX_INTERNAL_R = dp2px(18f)
    private val MIN_INTERNAL_R = dp2px(2f)
    private var externalRadius: Float = 0.toFloat()
    private val MAX_EXTERNAL_R = dp2px(150f)
    private val MIN_EXTERNAL_R = dp2px(25f)

    init {
        init(atr)
    }

    constructor(c: Context) : this(c, null) {

    }

    constructor(c: Context, atr: AttributeSet?, defStyleAttr: Int) : this(c, atr) {
        init(atr)
    }

    private fun init(attrs: AttributeSet?) {
        animators = ArrayList()
        points = ArrayList()

        mPaint = Paint()
        mPaint!!.isAntiAlias = true
        mPaint!!.style = Paint.Style.FILL

        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.LoadingView)
        mDuration = typeArray.getInt(R.styleable.LoadingView_lv_duration, mDuration)
        internalRadius = typeArray.getDimension(R.styleable.LoadingView_lv_internal_radius, DEFAULT_INTERNAL_RADIUS.toFloat())
        externalRadius = typeArray.getDimension(R.styleable.LoadingView_lv_external_radius, DEFAULT_EXTERNAL_RADIUS.toFloat())
        val defaultColor = 999999
        val startColor = typeArray.getColor(R.styleable.LoadingView_lv_start_color, defaultColor)
        val endColor = typeArray.getColor(R.styleable.LoadingView_lv_end_color, defaultColor)
        val colorList = ArrayList<Int>()
        if (startColor != defaultColor) {
            colorList.add(startColor)
        }
        if (endColor != defaultColor) {
            colorList.add(endColor)
        }
        //needs >= 2 number of colors
        if (colorList.size == 1) {
            colorList.add(colorList[0])
        }

        if (colorList.size == 0) {
            colors = intArrayOf(ContextCompat.getColor(context, R.color.color_start), ContextCompat.getColor(context, R.color.color_end))
        } else {
            colors = IntArray(colorList.size)
            for (i in colorList.indices) {
                colors!![i] = colorList[i]
            }
        }
        typeArray.recycle()

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w
        mHeight = h
        setShader()
        resetPoint()
    }

    private fun setShader() {
        val mLinearGradient = LinearGradient(mWidth / 2 - externalRadius, mHeight / 2 - externalRadius, mWidth / 2 - externalRadius, mHeight / 2 + externalRadius, colors, null, Shader.TileMode.CLAMP)
        mPaint!!.shader = mLinearGradient
    }

    fun start() {
        if (disposable == null || disposable!!.isDisposed) {
            disposable = Observable.interval(mDuration.toLong(), TimeUnit.MILLISECONDS).subscribe({ aLong -> update() })
        }
        this.visibility = View.VISIBLE
    }

    fun stop() {
        if (disposable != null) {
            disposable!!.dispose()
        }
        this.visibility = View.GONE
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stop()
    }

    private fun update() {

        setOffset(angle1 % radian / radian.toFloat())

        angle1++
        if (angle1 == 360) {
            angle1 = 0
            cyclic++
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawCircle(canvas)
        drawBezier(canvas)
    }

    private fun drawCircle(canvas: Canvas) {
        for (i in points!!.indices) {
            val index = angle1 / radian
            if (isEvenCyclic()) {
                if (i == index) {
                    if (angle1 % radian == 0) {
                        canvas.drawCircle(getCircleX(angle1), getCircleY(angle1), getMaxInternalRadius(), mPaint!!)
                    } else if (angle1 % radian > 0) {
                        canvas.drawCircle(getCircleX(angle1), getCircleY(angle1), if (smallerCircleRadius < internalRadius) internalRadius else smallerCircleRadius, mPaint!!)
                    }
                } else if (i == index + 1) {
                    if (angle1 % radian == 0) {
                        canvas.drawCircle(points!![i].x, points!![i].y, internalRadius, mPaint!!)
                    } else {
                        canvas.drawCircle(points!![i].x, points!![i].y, if (biggerCircleRadius < internalRadius) internalRadius else biggerCircleRadius, mPaint!!)
                    }
                } else if (i > index + 1) {
                    canvas.drawCircle(points!![i].x, points!![i].y, internalRadius, mPaint!!)
                }
            } else {
                if (i < index) {
                    canvas.drawCircle(points!![i + 1].x, points!![i + 1].y, internalRadius, mPaint!!)
                } else if (i == index) {
                    if (angle1 % radian == 0) {
                        canvas.drawCircle(getCircleX(angle1), getCircleY(angle1), getMaxInternalRadius(), mPaint!!)
                    } else {
                        canvas.drawCircle(getCircleX(angle1), getCircleY(angle1), if (smallerCircleRadius < internalRadius) internalRadius else smallerCircleRadius, mPaint!!)
                    }
                } else if (i == index + 1) {
                    if (angle1 % radian == 0) {
                        canvas.drawCircle(getCircleX(angle1), getCircleY(angle1), getMinInternalRadius(), mPaint!!)
                    } else if (angle1 % radian > 0) {
                        canvas.drawCircle(getCircleX(angle1), getCircleY(angle1), if (biggerCircleRadius < internalRadius) internalRadius else biggerCircleRadius, mPaint!!)
                    }
                }
            }
        }
    }

    private fun drawBezier(canvas: Canvas) {

        mPath.reset()

        val circleIndex = angle1 / radian

        val rightX = getCircleX(angle1)
        val rightY = getCircleY(angle1)

        val leftX: Float
        val leftY: Float
        if (isEvenCyclic()) {
            val index: Int = circleIndex + 1
            leftX = points!![if (index >= points!!.size) points!!.size - 1 else index].x
            leftY = points!![if (index >= points!!.size) points!!.size - 1 else index].y
        } else {
            leftX = points!![if (circleIndex < 0) 0 else circleIndex].x
            leftY = points!![if (circleIndex < 0) 0 else circleIndex].y
        }

        val theta = getTheta(PointF(leftX, leftY), PointF(rightX, rightY))
        val sinTheta = Math.sin(theta).toFloat()
        val cosTheta = Math.cos(theta).toFloat()

        val pointF1 = PointF(leftX - internalRadius * sinTheta, leftY + internalRadius * cosTheta)
        val pointF2 = PointF(rightX - internalRadius * sinTheta, rightY + internalRadius * cosTheta)
        val pointF3 = PointF(rightX + internalRadius * sinTheta, rightY - internalRadius * cosTheta)
        val pointF4 = PointF(leftX + internalRadius * sinTheta, leftY - internalRadius * cosTheta)

        if (isEvenCyclic()) {
            if (angle1 % radian < radian / 2) {

                mPath.moveTo(pointF3.x, pointF3.y)
                mPath.quadTo(rightX + (leftX - rightX) / (radian / 2) * if (angle1 % radian > radian / 2) radian / 2 else angle1 % radian, rightY + (leftY - rightY) / (radian / 2) * if (angle1 % radian > radian / 2) radian / 2 else angle1 % radian, pointF2.x, pointF2.y)
                mPath.lineTo(pointF3.x, pointF3.y)

                mPath.moveTo(pointF4.x, pointF4.y)
                mPath.quadTo(leftX + (rightX - leftX) / (radian / 2) * if (angle1 % radian > radian / 2) radian / 2 else angle1 % radian, leftY + (rightY - leftY) / (radian / 2) * if (angle1 % radian > radian / 2) radian / 2 else angle1 % radian, pointF1.x, pointF1.y)
                mPath.lineTo(pointF4.x, pointF4.y)

                mPath.close()
                canvas.drawPath(mPath, mPaint!!)
                return
            }
        } else {
            if (circleIndex > 0 && angle1 % radian > radian / 2) {

                mPath.moveTo(pointF3.x, pointF3.y)
                mPath.quadTo(rightX + (leftX - rightX) / (radian / 2) * if (radian - angle1 % radian > radian / 2) radian / 2 else radian - angle1 % radian, rightY + (leftY - rightY) / (radian / 2) * if (radian - angle1 % radian > radian / 2) radian / 2 else radian - angle1 % radian, pointF2.x, pointF2.y)
                mPath.lineTo(pointF3.x, pointF3.y)

                mPath.moveTo(pointF4.x, pointF4.y)
                mPath.quadTo(leftX + (rightX - leftX) / (radian / 2) * if (radian - angle1 % radian > radian / 2) radian / 2 else radian - angle1 % radian, leftY + (rightY - leftY) / (radian / 2) * if (radian - angle1 % radian > radian / 2) radian / 2 else radian - angle1 % radian, pointF1.x, pointF1.y)
                mPath.lineTo(pointF4.x, pointF4.y)

                mPath.close()
                canvas.drawPath(mPath, mPaint!!)
                return
            }
        }

        if (circleIndex == 0 && !isEvenCyclic()) return

        mPath.moveTo(pointF1.x, pointF1.y)
        mPath.quadTo((leftX + rightX) / 2, (leftY + rightY) / 2, pointF2.x, pointF2.y)
        mPath.lineTo(pointF3.x, pointF3.y)
        mPath.quadTo((leftX + rightX) / 2, (leftY + rightY) / 2, pointF4.x, pointF4.y)
        mPath.lineTo(pointF1.x, pointF1.y)

        mPath.close()

        canvas.drawPath(mPath, mPaint!!)
    }

    private fun createAnimator() {

        if (points!!.isEmpty()) {
            return
        }
        animators!!.clear()

        val circleGetSmallerAnimator = ValueAnimator.ofFloat(getMaxInternalRadius(), getMinInternalRadius())
        circleGetSmallerAnimator.duration = 5000L
        circleGetSmallerAnimator.addUpdateListener { animation -> smallerCircleRadius = animation.animatedValue as Float }
        animators!!.add(circleGetSmallerAnimator)

        val circleGetBiggerAnimator = ValueAnimator.ofFloat(getMinInternalRadius(), getMaxInternalRadius())
        circleGetBiggerAnimator.duration = 5000L
        circleGetBiggerAnimator.addUpdateListener { animation -> biggerCircleRadius = animation.animatedValue as Float }
        animators!!.add(circleGetBiggerAnimator)

    }

    private fun seekAnimator(offset: Float) {
        for (animator in animators!!) {
            animator.currentPlayTime = (5000.0f * offset).toLong()
        }
    }

    fun setOffset(offSet: Float) {
        createAnimator()
        seekAnimator(offSet)
        postInvalidate()
    }

    private fun resetPoint() {

        xX = (mWidth / 2).toFloat()
        yY = (mHeight / 2).toFloat()

        createPoints()

        if (!points!!.isEmpty()) {
            biggerCircleRadius = getMaxInternalRadius()
            smallerCircleRadius = getMinInternalRadius()
            postInvalidate()
        }
    }

    private fun createPoints() {
        points!!.clear()
        for (i in 0..360) {
            if (i % radian == 0) {
                val x1 = getCircleX(i)
                val y1 = getCircleY(i)
                points!!.add(PointF(x1, y1))
            }
        }
    }

    private fun isEvenCyclic(): Boolean {
        return cyclic % 2 == 0
    }

    private fun getCircleY(angle: Int): Float {
        return yY + externalRadius * Math.sin(angle * 3.14 / 180).toFloat()
    }

    private fun getCircleX(angle: Int): Float {
        return xX + externalRadius * Math.cos(angle * 3.14 / 180).toFloat()
    }

    private fun getTheta(pointCenterLeft: PointF, pointCenterRight: PointF): Double {
        return Math.atan(((pointCenterRight.y - pointCenterLeft.y) / (pointCenterRight.x - pointCenterLeft.x)).toDouble())
    }

    fun setExternalRadius(progress: Int) {
        val R = (progress / 100f * MAX_EXTERNAL_R).toInt()
        externalRadius = (if (R < MIN_EXTERNAL_R) MIN_EXTERNAL_R else R).toFloat()
        setShader()
        createPoints()
    }

    fun setInternalRadius(progress: Int) {
        val r = (progress / 100f * MAX_INTERNAL_R).toInt()
        internalRadius = (if (r < MIN_INTERNAL_R) MIN_INTERNAL_R else r).toFloat()
    }

    fun setDuration(progress: Int) {
        stop()
        val duration = ((1 - progress / 100f) * MAX_DURATION).toInt()
        mDuration = if (duration < MIN_DURATION) MIN_DURATION else duration
        start()
    }

    private fun getMaxInternalRadius(): Float {
        return internalRadius / 10f * 14f
    }

    private fun getMinInternalRadius(): Float {
        return internalRadius / 10f
    }

    private fun dp2px(dp: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }
}
package com.tw.speedometerdemo

import android.animation.Animator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import java.util.Random
import kotlin.math.min

class SpeedoMeterView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var backgroundbitmap: Bitmap? = null
    private var backimagebitmap: Bitmap? = null
    private var centerx = 0f
    private var centery = 0f
    private var radius = 0f
    private var path: Path? = null
    private var isSpeedIncrease = false
    private var speed = 0f
    private var currentSpeed = 0f
    private var speedAnimator: ValueAnimator? = null
    private var trembleAnimator: ValueAnimator? = null
    private var realSpeedAnimator: ValueAnimator? = null
    private var animatorListener: Animator.AnimatorListener? = null
    private val trembleDegree = 4f
    private var canceled = false
    private var drawable: Drawable?
    private var isborder: Boolean
    private var linecolor: Int
    private var needlecolor: Int
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun isborder(): Boolean {
        return isborder
    }

    init {


        val ta = getContext().obtainStyledAttributes(attrs, R.styleable.MyCustomView)
        needlecolor = ta.getColor(R.styleable.MyCustomView_needlecolor, Color.WHITE)
        linecolor = ta.getColor(R.styleable.MyCustomView_linecolor, Color.WHITE)
        drawable = ta.getDrawable(R.styleable.MyCustomView_backimage)
        isborder = ta.getBoolean(R.styleable.MyCustomView_removeborder, true)

        init()
    }

    fun transform(source: Bitmap): Bitmap {
        var source = source
        source = Bitmap.createScaledBitmap(source, width, height, false)
        val size = min(source.width.toDouble(), source.height.toDouble())
            .toInt()
        val x = (source.width - size) / 2
        val y = (source.height - size) / 2

        val squaredBitmap = Bitmap.createBitmap(source, x, y, size, size)
        if (squaredBitmap != source) {
            source.recycle()
        }

        val bitmap = Bitmap.createBitmap(size, size, source.config)

        val canvas = Canvas(bitmap)
        val paint = Paint()
        val shader = BitmapShader(
            squaredBitmap,
            Shader.TileMode.CLAMP, Shader.TileMode.CLAMP
        )
        paint.setShader(shader)
        paint.isAntiAlias = true


        canvas.drawCircle(centerx, centery, radius - ((radius / 9.85f) * 2.0f), paint)

        squaredBitmap.recycle()
        return bitmap
    }

    private fun init() {
        speedAnimator = ValueAnimator.ofFloat(0f, 1f)
        trembleAnimator = ValueAnimator.ofFloat(0f, 1f)
        realSpeedAnimator = ValueAnimator.ofFloat(0f, 1f)
        animatorListener = object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
            }

            override fun onAnimationEnd(animation: Animator) {
                if (!canceled) tremble()
            }

            override fun onAnimationCancel(animation: Animator) {
            }

            override fun onAnimationRepeat(animation: Animator) {
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private fun cancelTremble() {
        if (Build.VERSION.SDK_INT < 11) return
        canceled = true
        trembleAnimator!!.cancel()
        canceled = false
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    protected fun tremble() {
        cancelTremble()
        val random = Random()
        var mad = trembleDegree * random.nextFloat() * (if ((random.nextBoolean())) -1 else 1)
        mad = if ((speed + mad > 140)) 140 - speed
        else if ((speed + mad < 0)) 0 - speed else mad
        trembleAnimator = ValueAnimator.ofFloat(currentSpeed, speed + mad)
        trembleAnimator!!.interpolator = DecelerateInterpolator()
        trembleAnimator!!.setDuration(1000)
        trembleAnimator!!.addUpdateListener {
            isSpeedIncrease = trembleAnimator!!.getAnimatedValue() as Float > currentSpeed
            currentSpeed = trembleAnimator!!.getAnimatedValue() as Float
            postInvalidate()
        }
        trembleAnimator!!.addListener(animatorListener)
        trembleAnimator!!.start()
    }

    fun setbackImageResource(@DrawableRes resId: Int) {
        drawable = ContextCompat.getDrawable(context, resId)
        createback()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (width > 0 && height > 0) {
            createback()
        }
    }

    fun createback() {
        centerx = width / 2.0f
        centery = height / 2.0f

        radius = min((width / 2.0f).toDouble(), (height / 2.0f).toDouble()).toFloat()
        if (drawable != null) {
            backimagebitmap = transform(drawableToBitmap(drawable!!))
        }
        createSpeedometerDisk()
        path = Path()
        val bottomY = width / 1.6f
        path!!.moveTo(width / 2.0f, radius / 4.0f)
        path!!.lineTo(centerx - (radius / 78.8f), radius / 4.0f)
        path!!.lineTo(centerx - (radius / 39.4f), bottomY)
        path!!.lineTo(centerx + (radius / 39.4f), bottomY)
        path!!.lineTo(centerx + (radius / 78.8f), radius / 4.0f)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint.color = Color.BLACK
        if (backimagebitmap != null) canvas.drawBitmap(backimagebitmap!!, 0f, 0f, paint)
        if (backgroundbitmap != null) {
            paint.color = Color.BLACK
            canvas.drawBitmap(backgroundbitmap!!, 0f, 0f, paint)
        }


        val angle = 215.0f + (252 * currentSpeed) / 140.0f
        canvas.save()
        canvas.rotate(angle, centerx, centery)
        paint.style = Paint.Style.FILL
        paint.color = needlecolor
        paint.style = Paint.Style.FILL
        canvas.drawPath(path!!, paint)
        canvas.drawCircle(centerx, centery, radius / 19.7f, paint)
        paint.color = getComplimentColor(needlecolor)
        canvas.drawCircle(centerx, centery, radius / 197.0f, paint)
        canvas.restore()
    }

    fun setisborder(isborder: Boolean) {
        this.isborder = isborder
        createSpeedometerDisk()
        invalidate()
    }


    private fun createSpeedometerDisk() {
        Log.e("radius", radius.toString() + "")
        val strokewidth = radius / 9.85f
        backgroundbitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444)
        val canvas = Canvas(backgroundbitmap!!)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokewidth
        val shader: Shader = LinearGradient(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            Color.rgb(181, 181, 181),
            Color.rgb(0, 0, 0),
            Shader.TileMode.CLAMP
        )
        paint.setShader(shader)
        if (isborder) canvas.drawCircle(centerx, centery, radius - (strokewidth / 2.0f), paint)
        val shader1: Shader = LinearGradient(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            Color.rgb(0, 0, 0),
            Color.rgb(181, 181, 181),
            Shader.TileMode.CLAMP
        )
        paint.setShader(shader1)
        if (isborder) canvas.drawCircle(
            centerx,
            centerx,
            radius - (strokewidth + (strokewidth / 2.0f)),
            paint
        )
        paint.setShader(null)
        paint.color = Color.rgb(35, 35, 35)
        paint.style = Paint.Style.FILL
        if (backimagebitmap == null) canvas.drawCircle(
            centerx,
            centerx,
            radius - (strokewidth * 2.0f),
            paint
        )
        paint.style = Paint.Style.STROKE


        //        paint.setColor(Color.RED);
//        paint.setStrokeWidth(6);
//        canvas.drawArc(new RectF(80,80,getWidth()-80,getHeight()-80),125,252,false,paint);
//        paint.setColor(Color.GREEN);
//        canvas.drawArc(new RectF(80,80,getWidth()-80,getHeight()-80),125,180,false,paint);
        paint.strokeWidth = radius / 26.26f
        paint.style = Paint.Style.STROKE
        paint.color = Color.RED
        canvas.drawArc(
            RectF(
                (strokewidth * 2.0f) + paint.strokeWidth / 2.0f,
                (strokewidth * 2.0f) + paint.strokeWidth / 2.0f,
                width - (strokewidth * 2.0f) - paint.strokeWidth / 2.0f,
                height - (strokewidth * 2.0f) - paint.strokeWidth / 2.0f
            ), 125f, 252f, false, paint
        )
        paint.color = Color.GREEN
        canvas.drawArc(
            RectF(
                (strokewidth * 2.0f) + paint.strokeWidth / 2.0f,
                (strokewidth * 2.0f) + paint.strokeWidth / 2.0f,
                width - (strokewidth * 2.0f) - paint.strokeWidth / 2.0f,
                height - (strokewidth * 2.0f) - paint.strokeWidth / 2.0f
            ), 125f, 180f, false, paint
        )

        val linepaint = Paint(Paint.ANTI_ALIAS_FLAG)
        linepaint.color = linecolor
        var point = -10
        var lastodd = 0
        var i = 90 + 125
        while (i <= 90 + 125 + 252) {
            canvas.save()
            canvas.rotate(i.toFloat(), centerx, centery)
            canvas.translate(0f, strokewidth * 2.0f)

            if (lastodd == 0) {
                linepaint.strokeWidth = radius / 65.66f
                canvas.drawLine(width / 2.0f, 0f, width / 2.0f, radius / 12.90f, linepaint)
            } else {
                linepaint.strokeWidth = radius / 98.5f
                canvas.drawLine(width / 2.0f, 0f, width / 2.0f, radius / 24.26f, linepaint)
            }
            if (lastodd == 0) {
                canvas.translate(0f, radius / 13.13f)
                point += 10
                val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
                textPaint.textSize = radius / 13.13f
                textPaint.color = linecolor
                val staticLayout = StaticLayout(
                    point.toString() + "", textPaint, width,
                    Layout.Alignment.ALIGN_CENTER, 0.0f, 0.0f, false
                )
                staticLayout.draw(canvas)
            }
            lastodd = if (lastodd == 0) 1 else 0
            canvas.restore()
            i = (i + 9f).toInt()
        }
    }

    fun setNeedlecolor(@ColorInt needlecolor: Int) {
        this.needlecolor = needlecolor
        invalidate()
    }

    fun setLinecolor(@ColorInt linecolor: Int) {
        this.linecolor = linecolor
        createSpeedometerDisk()
        invalidate()
    }


    fun setSpeed(speed: Int, wantmaintainspeed: Boolean) {
        var speed = speed
        speed = if ((speed > 140)) 140 else if ((speed < 0)) 0 else speed
        if (speed.toFloat() == this.speed) return
        this.speed = speed.toFloat()



        isSpeedIncrease = speed > currentSpeed

        cancelSpeedAnimator()
        speedAnimator = ValueAnimator.ofFloat(currentSpeed, speed.toFloat())
        speedAnimator!!.interpolator = DecelerateInterpolator()
        speedAnimator!!.setDuration(2000)
        speedAnimator!!.addUpdateListener {
            currentSpeed = speedAnimator!!.getAnimatedValue() as Float
            postInvalidate()
        }
        speedAnimator!!.addListener(animatorListener)
        speedAnimator!!.start()
    }

    protected fun cancelSpeedAnimator() {
        cancelSpeedMove()
        cancelTremble()
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private fun cancelSpeedMove() {
        if (Build.VERSION.SDK_INT < 11) return
        canceled = true
        speedAnimator!!.cancel()
        realSpeedAnimator!!.cancel()
        canceled = false
    }

    companion object {
        fun drawableToBitmap(drawable: Drawable): Bitmap {
            var bitmap: Bitmap? = null

            if (drawable is BitmapDrawable) {
                val bitmapDrawable = drawable
                if (bitmapDrawable.bitmap != null) {
                    return bitmapDrawable.bitmap
                }
            }

            bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
                Bitmap.createBitmap(
                    1,
                    1,
                    Bitmap.Config.ARGB_8888
                ) // Single color bitmap will be created of 1x1 pixel
            } else {
                Bitmap.createBitmap(
                    drawable.intrinsicWidth,
                    drawable.intrinsicHeight,
                    Bitmap.Config.ARGB_8888
                )
            }

            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        }

        fun getComplimentColor(color: Int): Int {
            // get existing colors
            val alpha = Color.alpha(color)
            var red = Color.red(color)
            var blue = Color.blue(color)
            var green = Color.green(color)

            // find compliments
            red = (red.inv()) and 0xff
            blue = (blue.inv()) and 0xff
            green = (green.inv()) and 0xff

            return Color.argb(alpha, red, green, blue)
        }
    }
}

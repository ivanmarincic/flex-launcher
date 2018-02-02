package io.github.twoloops.flexlauncher.homescreen.views

import android.content.Context
import android.graphics.*
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.AttributeSet
import android.view.View

class AdaptiveIconView : View {

    private var iconSize: Int = 100
    private var left = 0f
    private var top = 0f
    private var cornerDiameter = 80f
    private val foregroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var background: Bitmap? = null
    private var foreground: Bitmap? = null
    private var shapePath: Path? = null
    var shapeId: Int = 0
    var icon: Drawable? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private fun init() {
        shapePath = Path()
        initShape(shapeId)
        background = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888)
        backgroundPaint.shader = BitmapShader(background, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        foreground = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888)
        foregroundPaint.shader = BitmapShader(foreground, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        initIcon(icon!!)
    }

    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(w, h, oldW, oldH)
        iconSize = Math.min(w, h)
        init()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.run {
            val saveCount = save()
            translate(left, top)
            drawPath(shapePath, backgroundPaint)
            drawPath(shapePath, foregroundPaint)
            restoreToCount(saveCount)
        }
    }

    private fun initIcon(icon: Drawable) {
        background!!.eraseColor(Color.TRANSPARENT)
        foreground!!.eraseColor(Color.TRANSPARENT)
        val c = Canvas()
        if (icon::class == AdaptiveIconDrawable::class) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                rasterize((icon as AdaptiveIconDrawable).background, background!!, c)
                rasterize(icon.foreground, foreground!!, c)
            }
        } else {
            rasterize(ColorDrawable(Color.WHITE), background!!, c)
            rasterize(icon, foreground!!, c)
        }
    }

    private fun initShape(shapeId: Int) {
        this.shapeId = shapeId
        when (this.shapeId) {
            SHAPE_CIRCLE -> {
                val size = iconSize / 2f
                shapePath!!.addCircle(size, size, size, Path.Direction.CW)
            }
            SHAPE_RECTANGLE -> {
                shapePath!!.addRect(0f, 0f, iconSize.toFloat(), iconSize.toFloat(), Path.Direction.CW)
            }
            SHAPE_ROUNDED_RECTANGLE -> {
                val sideLength = iconSize - (2 * cornerDiameter)
                shapePath!!.arcTo(RectF(0f, 0f, cornerDiameter, cornerDiameter),
                        180f,
                        90f)
                shapePath!!.rLineTo(sideLength, 0f)
                shapePath!!.arcTo(RectF(sideLength + cornerDiameter, 0f, sideLength + 2 * cornerDiameter, cornerDiameter),
                        270f,
                        90f)
                shapePath!!.rLineTo(0f, sideLength)
                shapePath!!.arcTo(RectF(sideLength + cornerDiameter, sideLength + cornerDiameter, sideLength + 2 * cornerDiameter, sideLength + 2 * cornerDiameter),
                        0f,
                        90f)
                shapePath!!.rLineTo(sideLength, 0f)
                shapePath!!.arcTo(RectF(0f, sideLength + cornerDiameter, cornerDiameter, sideLength + 2 * cornerDiameter),
                        90f,
                        90f)
                shapePath!!.close()
            }
        }
    }

    private fun rasterize(drawable: Drawable, bitmap: Bitmap, canvas: Canvas) {
        drawable.setBounds(0, 0, iconSize, iconSize)
        canvas.setBitmap(bitmap)
        drawable.draw(canvas)
    }

    companion object {
        const val SHAPE_CIRCLE = 0
        const val SHAPE_RECTANGLE = 1
        const val SHAPE_ROUNDED_RECTANGLE = 2
    }
}
package io.github.twoloops.flexlauncher.homescreen.views

import android.content.Context
import android.graphics.*
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.AttributeSet
import android.view.View

@RequiresApi(Build.VERSION_CODES.O)
class AdaptiveIconView : View {

    private val iconSize: Int = 50
    private var left = 0f
    private var top = 0f
    private var cornerRadius = 0f
    private val foregroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var background: Bitmap? = null
    private var foreground: Bitmap? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        background = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888)
        backgroundPaint.shader = BitmapShader(background, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        foreground = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888)
        foregroundPaint.shader = BitmapShader(foreground, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        left = (w - iconSize) / 2f
        top = (h - iconSize) / 2f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.run {
            val saveCount = save()
            translate(left, top)

            drawRoundRect(0f, 0f, iconSize.toFloat(), iconSize.toFloat(),
                    cornerRadius, cornerRadius, backgroundPaint)
            drawRoundRect(0f, 0f, iconSize.toFloat(), iconSize.toFloat(),
                    cornerRadius, cornerRadius, foregroundPaint)
            restoreToCount(saveCount)
        }
    }

    fun setIcon(icon: AdaptiveIconDrawable) {
        background!!.eraseColor(Color.TRANSPARENT)
        foreground!!.eraseColor(Color.TRANSPARENT)
        val c = Canvas()
        rasterize(icon.background, background!!, c)
        rasterize(icon.foreground, foreground!!, c)
    }

    private fun rasterize(drawable: Drawable, bitmap: Bitmap, canvas: Canvas) {
        drawable.setBounds(0, 0, 50, 50)
        canvas.setBitmap(bitmap)
        drawable.draw(canvas)
    }
}
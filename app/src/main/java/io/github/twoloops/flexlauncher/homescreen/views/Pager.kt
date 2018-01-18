package io.github.twoloops.flexlauncher.homescreen.views

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.support.v4.content.ContextCompat
import android.support.v4.view.GestureDetectorCompat
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.eightbitlab.supportrenderscriptblur.SupportRenderScriptBlur
import eightbitlab.com.blurview.BlurView
import io.github.twoloops.flexlauncher.R


class Pager : ViewPager {

    var gestureDetector: GestureDetectorCompat? = null

    private var color: Int = 0
    private var _dark: Boolean = false
    var dark: Boolean
        get() = _dark
        set(value) {
            _dark = value
            color = if (_dark) {
                ContextCompat.getColor(context, R.color.backgroundColorDarkPrimary)
            } else {
                ContextCompat.getColor(context, R.color.backgroundColorPrimary)
            }
        }

    init {
        gestureDetector = GestureDetectorCompat(context, XScrollDetector())
        setFadingEdgeLength(0)
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onPageScrolled(position: Int, offset: Float, offsetPixels: Int) {
        super.onPageScrolled(position, offset, offsetPixels)
        if (position == 0) {
            if (offset <= 0.5 && !_dark) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                }
            }
            animateBackground(1 - offset)
        } else {
            (parent as BlurView).setBlurEnabled(false)
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return super.onInterceptTouchEvent(ev) && gestureDetector!!.onTouchEvent(ev)
    }

    override fun getAdapter(): io.github.twoloops.flexlauncher.homescreen.adapters.PagerAdapter? {
        return super.getAdapter() as io.github.twoloops.flexlauncher.homescreen.adapters.PagerAdapter?
    }

    fun resume() {
        val anim = ValueAnimator.ofFloat(Float.MIN_VALUE, 1.0f)
        anim.addUpdateListener { animation -> animateBackground(animation?.animatedValue as Float) }
        anim.duration = 1000
        anim.start()
    }

    private fun animateBackground(progress: Float) {
        val color = String.format("#%02X", (255 * 0.5 * progress).toInt()) + String.format("%06X", 0xFFFFFF and color)
        setBackgroundColor(Color.parseColor(color))
        val radius = 10f * progress
        val decorView = (context as Activity).window.decorView
        (parent as BlurView).setupWith(decorView.findViewById(android.R.id.content) as ViewGroup)
                .windowBackground(decorView.background)
                .blurAlgorithm(SupportRenderScriptBlur(context))
                .blurRadius(radius)
        if (radius == Float.MIN_VALUE) {
            (parent as BlurView).setBlurEnabled(false)
        } else {
            (parent as BlurView).setBlurEnabled(true)
        }
    }

    internal inner class XScrollDetector : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
            return Math.abs(distanceY) <= Math.abs(distanceX)
        }
    }
}
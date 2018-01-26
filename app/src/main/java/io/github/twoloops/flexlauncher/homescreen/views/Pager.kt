package io.github.twoloops.flexlauncher.homescreen.views

import android.animation.Animator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.icu.util.UniversalTimeScale.toLong
import android.os.Build
import android.support.v4.content.ContextCompat
import android.support.v4.view.GestureDetectorCompat
import android.util.AttributeSet
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import com.eightbitlab.supportrenderscriptblur.SupportRenderScriptBlur
import eightbitlab.com.blurview.BlurView
import io.github.twoloops.flexlauncher.R
import io.github.twoloops.flexlauncher.homescreen.contracts.PagerAdapter


class Pager : ViewGroup {

    private var _adapter: PagerAdapter? = null
    var adapter: PagerAdapter?
        get() = _adapter
        set(value) {
            _adapter = value
            fillViewsFromAdapter()
        }
    private var _currentPage: Int = 0
    var currentPage: Int
        get() = _currentPage
        set(value) {
            _currentPage = value
            scrollToPage()
        }
    private var maxScrollX = 0
    private var cancelAnimation = false
    private var color: Int = 0
    private var _dark: Boolean = false
    private var lastEventX = 0f
    private var lastEventY = 0f
    private var startEventX = 0f
    private var lastScrollX = 0
    private var isDragged = false
    private var pageWidth = 0
    private var pageHeight = 0
    private var scrollAnimator: ValueAnimator? = null
    private val velocityTracker: VelocityTracker by lazy(LazyThreadSafetyMode.NONE) {
        VelocityTracker.obtain()
    }
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
        setFadingEdgeLength(0)
    }

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        pageWidth = MeasureSpec.getSize(widthMeasureSpec)
        pageHeight = MeasureSpec.getSize(heightMeasureSpec)
        maxScrollX = (_adapter!!.getCount() - 1) * pageWidth
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (_adapter != null) {
            for (i in 0 until _adapter!!.getCount()) {
                val view: View = getChildAt(i)
                val left: Int = i * width
                val top = 0
                val right: Int = left + width
                val bottom: Int = top + height
                view.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY))
                view.layout(left, top, right, bottom)
            }
        }
    }

    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                lastEventX = event.x
                lastEventY = event.y
                startEventX = lastEventX
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> isDragged = false
            MotionEvent.ACTION_MOVE -> {
                val x = event.x
                val y = event.y
                val xDelta = Math.abs(x - lastEventX)
                val yDelta = Math.abs(y - lastEventY)

                val xDeltaTotal = x - startEventX
                if (xDelta > yDelta && Math.abs(xDeltaTotal) > ViewConfiguration.get(context).scaledTouchSlop) {
                    isDragged = true
                    startEventX = x
                    return true
                }
            }
        }
        return false
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_UP -> {
                velocityTracker.computeCurrentVelocity(1, ViewConfiguration.get(context).scaledMaximumFlingVelocity.toFloat())
                val scrollXDelta = Math.abs(scrollX - lastScrollX)
                if (scrollXDelta >= pageWidth / 3 || Math.abs(velocityTracker.xVelocity) >= ViewConfiguration.get(context).scaledMaximumFlingVelocity) {
                    if (scrollX >= lastScrollX) {
                        currentPage++
                    } else {
                        currentPage--
                    }
                } else {
                    scrollToPage()
                }
            }
            MotionEvent.ACTION_DOWN -> {
                lastEventX = event.x
                lastEventY = event.y
                lastScrollX = scrollX
                velocityTracker.clear()
            }
            MotionEvent.ACTION_CANCEL -> {

            }
            MotionEvent.ACTION_MOVE -> {
                velocityTracker.addMovement(event)
                if (scrollAnimator != null && scrollAnimator?.isRunning!!) {
                    scrollAnimator?.cancel()
                }
                var xScrollDiff: Int = (lastScrollX + lastEventX - event.x).toInt()
                if (xScrollDiff < 0) {
                    xScrollDiff = 0
                    lastScrollX = 0
                    lastEventX = event.x
                }
                if (xScrollDiff > maxScrollX) {
                    xScrollDiff = maxScrollX
                }
                scrollTo(xScrollDiff, y.toInt())
            }
        }
        return true
    }

    private fun scrollToPage() {
        val newScrollX = _currentPage * pageWidth
        val scrollXAmount = Math.abs(newScrollX - scrollX)
        val velocityX = 2f + Math.abs(velocityTracker.xVelocity)
        scrollAnimator = ValueAnimator.ofInt(scrollX, newScrollX)
        scrollAnimator?.addUpdateListener {
            scrollTo(it.animatedValue as Int, y.toInt())
        }
        scrollAnimator?.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {
            }

            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                lastScrollX = scrollX
            }

            override fun onAnimationCancel(animation: Animator?) {
                lastScrollX = scrollX
            }

        })
        scrollAnimator?.interpolator = DecelerateInterpolator()
        val duration = Math.abs((2 * scrollXAmount) / velocityX).toLong()
        scrollAnimator?.duration = if (duration < 100) {
            100
        } else {
            duration
        }
        scrollAnimator?.start()
    }

    fun onPageScrolled(position: Int, offset: Float, offsetPixels: Int) {
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
            cancelAnimation = true
            (parent as BlurView).setBlurEnabled(false)
        }
    }

    private fun fillViewsFromAdapter() {
        removeAllViews()
        var view: View
        if (_adapter == null) return
        for (i in 0 until _adapter!!.getCount()) {
            view = _adapter!!.getView(i)
            addView(view, i)
        }
    }

    fun resume() {
        val anim = ValueAnimator.ofFloat(Float.MIN_VALUE, 1.0f)
        anim.addUpdateListener { animation ->
            animateBackground(animation?.animatedValue as Float)
            if (cancelAnimation) {
                cancelAnimation = false
                animation.cancel()
            }
        }
        anim.duration = 400
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
}
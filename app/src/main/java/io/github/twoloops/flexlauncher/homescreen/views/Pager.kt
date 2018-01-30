package io.github.twoloops.flexlauncher.homescreen.views

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.*
import android.view.animation.Interpolator
import android.widget.Scroller
import com.eightbitlab.supportrenderscriptblur.SupportRenderScriptBlur
import eightbitlab.com.blurview.BlurView
import io.github.twoloops.flexlauncher.R
import io.github.twoloops.flexlauncher.homescreen.contracts.PagerAdapter
import kotlin.math.roundToInt


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

    private var settingsPanelWidth = 0
    private var pageHeight = 0
    private var pageWidth = 0
    private var leftBound = 0
    private var rightBound = 0

    private var lastEventX = 0f
    private var lastEventY = 0f
    private var startEventX = 0f
    private var startEventY = 0f
    private var lastScrollX = 0

    private var isDragged = false
    private var isSettingsPage = false

    private var _scroller: Scroller? = null
    private val scroller: Scroller by lazy(LazyThreadSafetyMode.NONE) {
        val scrollerInterpolator = Interpolator { t ->
            var t = t
            t -= 1.0f
            t * t * t * t * t + 1.0f
        }
        if (_scroller == null) {
            _scroller = Scroller(context, scrollerInterpolator)
        }
        _scroller!!
    }
    private var _velocityTracker: VelocityTracker? = null
    private val velocityTracker: VelocityTracker by lazy(LazyThreadSafetyMode.NONE) {
        if (_velocityTracker == null) {
            _velocityTracker = VelocityTracker.obtain()
        }
        _velocityTracker!!
    }
    private var _viewConfiguration: ViewConfiguration? = null
    private val viewConfiguration: ViewConfiguration by lazy(LazyThreadSafetyMode.NONE) {
        if (_viewConfiguration == null) {
            _viewConfiguration = ViewConfiguration.get(context)
        }
        _viewConfiguration!!
    }

    private val MAX_SETTLE_DURATION = 600

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
        settingsPanelWidth = pageWidth / 3
        rightBound = (_adapter!!.getCount() - 1) * pageWidth + settingsPanelWidth
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
            val settingsPanel: View = getChildAt(childCount - 1)
            val left: Int = _adapter!!.getCount() * width
            val top = 0
            val right: Int = left + settingsPanelWidth
            val bottom: Int = top + height
            settingsPanel.measure(MeasureSpec.makeMeasureSpec(settingsPanelWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY))
            settingsPanel.layout(left, top, right, bottom)
        }
    }

    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
        val action = event!!.action and MotionEvent.ACTION_MASK
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            endScroll()
            return false
        }
        if (action == MotionEvent.ACTION_DOWN && isDragged) {
            return true
        }
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                lastEventX = event.x
                startEventX = lastEventX
                lastEventY = event.y
                startEventY = lastEventY
                lastScrollX = scrollX
                scroller.computeScrollOffset()
                if (!scroller.isFinished) {
                    scroller.abortAnimation()
                    scrollToPage()
                }
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                isDragged = false
            }
            MotionEvent.ACTION_MOVE -> {
                val x = event.x
                val y = event.y
                val dx = x - lastEventX
                val xDelta = Math.abs(dx)
                val yDelta = Math.abs(y - startEventY)

                if (xDelta > yDelta && xDelta > viewConfiguration.scaledTouchSlop) {
                    isDragged = true
                    requestParentDisallowInterceptTouchEvent(true)
                    lastEventX = if (dx > 0) {
                        startEventX + viewConfiguration.scaledTouchSlop
                    } else {
                        startEventX - viewConfiguration.scaledTouchSlop
                    }
                    return true
                }

                if (isDragged) {
                    scrollPager((lastScrollX + startEventX - x).roundToInt())
                }
            }
        }
        return false
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        velocityTracker.addMovement(event)
        when (event?.action) {
            MotionEvent.ACTION_UP -> {
                velocityTracker.computeCurrentVelocity(1000, viewConfiguration.scaledMaximumFlingVelocity.toFloat())
                val pagerScrollDiff = Math.abs(lastScrollX - scrollX)
                if (pagerScrollDiff > pageWidth / 3f || (isSettingsPage && pagerScrollDiff > pageWidth / 9f)) {
                    if (lastScrollX - scrollX < 0) {
                        if (currentPage < _adapter!!.getCount()) {
                            currentPage++
                        } else {
                            scrollToPage()
                        }
                    } else {
                        if (currentPage > 0) {
                            currentPage--
                        } else {
                            scrollToPage()
                        }
                    }
                } else {
                    scrollToPage()
                }
                isDragged = false
            }
            MotionEvent.ACTION_CANCEL -> {
                isDragged = false
            }
            MotionEvent.ACTION_MOVE -> {
                val x = event.x
                val y = event.y
                if (!isDragged) {
                    scroller.abortAnimation()
                    val xDiff: Float = Math.abs(lastEventX - x)
                    val yDiff: Float = Math.abs(lastEventY - y)
                    if (xDiff > viewConfiguration.scaledPagingTouchSlop && xDiff > yDiff) {
                        isDragged = true
                        requestParentDisallowInterceptTouchEvent(true)
                        lastEventX = if (x - startEventX > 0) {
                            startEventX + viewConfiguration.scaledPagingTouchSlop
                        } else {
                            startEventX - viewConfiguration.scaledPagingTouchSlop
                        }
                        lastEventY = y
                    }
                }
                if (isDragged) {
                    isSettingsPage = (x < startEventX && currentPage == 1) || currentPage == 2
                    var scrollXDiff = startEventX - x
                    if (isSettingsPage) {
                        scrollXDiff *= 1f / 3
                    }
                    scrollPager((lastScrollX + scrollXDiff).roundToInt())

                }
            }
        }
        return true
    }

    private fun scrollPager(xToScroll: Int) {
        if (!scroller.isFinished) {
            scroller.abortAnimation()
        }
        if (xToScroll in (leftBound + 1)..(rightBound - 1)) {
            scrollTo(xToScroll, scrollY)
        }
    }

    private fun endScroll() {
        isDragged = false
        if (_velocityTracker != null) {
            velocityTracker.recycle()
            _velocityTracker = null
        }
    }

    private fun requestParentDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        val parent = parent
        parent?.requestDisallowInterceptTouchEvent(disallowIntercept)
    }

    private fun scrollToPage() {
        lastScrollX = scrollX
        val currentPageWidth = if (isSettingsPage) {
            settingsPanelWidth
        } else
            pageWidth
        var dx = (pageWidth * currentPage) - lastScrollX
        if (isSettingsPage && currentPage == 2) {
            dx -= pageWidth - currentPageWidth
        }
        val halfWidth = currentPageWidth / 2
        val distanceRatio = Math.min(1f, 1.0f * Math.abs(dx) / currentPageWidth)
        val distance = halfWidth + halfWidth * distanceInfluenceForSnapDuration(distanceRatio)
        var duration: Int
        val velocity = Math.abs(velocityTracker.xVelocity)
        duration = if (velocity > 0) {
            4 * Math.round(1000 * Math.abs(distance / velocity))
        } else {
            val pageDelta = Math.abs(dx).toFloat() / currentPageWidth
            ((pageDelta + 1) * 100).toInt()
        }
        duration = Math.min(duration, MAX_SETTLE_DURATION)
        scroller.forceFinished(true)
        scroller.startScroll(lastScrollX, 0, dx, 0, duration)
        ViewCompat.postInvalidateOnAnimation(this)
    }

    override fun computeScroll() {
        if (!scroller.isFinished && scroller.computeScrollOffset()) {
            scrollTo(scroller.currX, 0)
            lastScrollX = scrollX
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    private fun onPageScrolled(position: Int, offset: Float) {
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

    private fun distanceInfluenceForSnapDuration(f: Float): Float {
        var f = f
        f -= 0.5f // center the values about 0.
        f *= 0.3f * Math.PI.toFloat() / 2.0f
        return Math.sin(f.toDouble()).toFloat()
    }

    private fun fillViewsFromAdapter() {
        removeAllViews()
        var view: View
        if (_adapter == null) return
        for (i in 0 until _adapter!!.getCount()) {
            view = _adapter!!.getView(i)
            addView(view, i)
        }
        addView(_adapter!!.getSettingsPanel())
    }

    fun resume() {
        val anim = ValueAnimator.ofFloat(Float.MIN_VALUE, 1.0f)
        anim.addUpdateListener { animation ->
            animateBackground(animation?.animatedValue as Float)
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
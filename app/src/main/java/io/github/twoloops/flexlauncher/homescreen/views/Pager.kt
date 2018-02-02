package io.github.twoloops.flexlauncher.homescreen.views

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
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
            scrollToPage(_currentPage)
        }
    private var nextPage: Int = 0
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

    private var optionsPanelWidth = 0
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
    private var isOptionsPanel = false

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
        optionsPanelWidth = pageWidth / 3
        rightBound = (_adapter!!.getScrollableCount() - 1) * pageWidth + optionsPanelWidth
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (_adapter != null) {
            for (i in 0 until _adapter!!.getCount()) {
                when (i) {
                    in 0..1 -> {
                        val view: View = getChildAt(i)
                        val left: Int = i * width
                        val top = 0
                        val right: Int = left + width
                        val bottom: Int = top + height
                        view.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY))
                        view.layout(left, top, right, bottom)
                    }
                    2 -> {
                        val optionsPanel: View = getChildAt(i)
                        val left: Int = i * width
                        val top = 0
                        val right: Int = left + optionsPanelWidth
                        val bottom: Int = top + height
                        optionsPanel.measure(MeasureSpec.makeMeasureSpec(optionsPanelWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY))
                        optionsPanel.layout(left, top, right, bottom)
                    }
                    else -> {
                        val view: View = getChildAt(i)
                        val left: Int = (i - 1) * width + optionsPanelWidth
                        val top = 0
                        val right: Int = left + width
                        val bottom: Int = top + height
                        view.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY))
                        view.layout(left, top, right, bottom)
                    }
                }
            }
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
                    scrollPager(lastEventX)
                }
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                isDragged = false
            }
            MotionEvent.ACTION_MOVE -> {
                val x = event.getX(0)
                val y = event.getY(0)
                val dx = x - lastEventX
                val xDelta = Math.abs(dx)
                val yDelta = Math.abs(y - startEventY)

                if (xDelta > yDelta && xDelta > viewConfiguration.scaledTouchSlop) {
                    isDragged = true
                    lastEventX = if (dx > 0) {
                        startEventX + viewConfiguration.scaledTouchSlop
                    } else {
                        startEventX - viewConfiguration.scaledTouchSlop
                    }
                    return true
                }
            }
        }
        return false
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        velocityTracker.addMovement(event)
        when (event?.action) {
            MotionEvent.ACTION_UP -> {
                isDragged = false
                velocityTracker.computeCurrentVelocity(1000, viewConfiguration.scaledMaximumFlingVelocity.toFloat())
                val pagerScrollDiff = Math.abs(lastScrollX - scrollX)
                if (pagerScrollDiff > pageWidth / 4f || (isOptionsPanel && pagerScrollDiff > pageWidth / 9f)) {
                    if (lastScrollX - scrollX < 0) {
                        if (currentPage < (_adapter!!.getCount() - 1)) {
                            scrollToPage(currentPage + 1)
                        } else {
                            scrollToPage(currentPage)
                        }
                    } else {
                        if (currentPage > 0) {
                            scrollToPage(currentPage - 1)
                        } else {
                            scrollToPage(currentPage)
                        }
                    }
                } else {
                    scrollToPage(currentPage)
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                isDragged = false
            }
            MotionEvent.ACTION_MOVE -> {
                val x = event.getX(0)
                val y = event.getY(0)
                if (!isDragged) {
                    val xDiff: Float = Math.abs(lastEventX - x)
                    val yDiff: Float = Math.abs(lastEventY - y)
                    if (xDiff > viewConfiguration.scaledPagingTouchSlop && xDiff > yDiff) {
                        isDragged = true
                        lastEventX = if (x - startEventX > 0) {
                            startEventX + viewConfiguration.scaledPagingTouchSlop
                        } else {
                            startEventX - viewConfiguration.scaledPagingTouchSlop
                        }
                        lastEventY = y
                    }
                }
                if (isDragged) {
                    scrollPager(x)

                }
            }
        }
        return true
    }

    fun openOptionsView(viewId: Int) {
        if (isOptionsPanel) {
            _adapter!!.setViewForOptionsPanel(viewId)
            scrollToPage(3)
        }
    }

    private fun scrollPager(xToScroll: Float) {
        var scrollXDiff = startEventX - xToScroll
        if (isOptionsPanel) {
            scrollXDiff *= 1f / 3
        }
        var newScrollX = (lastScrollX + scrollXDiff).roundToInt()
        if (newScrollX in (leftBound)..(rightBound)) {
            if (!scroller.isFinished) {
                scroller.forceFinished(true)
            }
            if (!isOptionsPanel && newScrollX > pageWidth) {
                newScrollX -= (scrollXDiff - (scrollXDiff * (1f / 3))).roundToInt()
            }
            isOptionsPanel = newScrollX > pageWidth
            if (newScrollX in (leftBound)..(rightBound)) {
                onScroll(newScrollX)
                scrollTo(newScrollX, scrollY)
            }
        }
    }

    private fun scrollToPage(page: Int) {
        nextPage = page
        lastScrollX = scrollX
        val currentPageWidth = if (isOptionsPanel) {
            optionsPanelWidth
        } else
            pageWidth
        var dx = (pageWidth * page) - lastScrollX
        if (isOptionsPanel && page >= 2) {
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
        if (lastScrollX + dx < leftBound) {
            dx = lastScrollX * (-1)
        }
        scroller.forceFinished(true)
        scroller.startScroll(lastScrollX, 0, dx, 0, duration)
        ViewCompat.postInvalidateOnAnimation(this)
    }

    private fun endScroll() {
        isDragged = false
        if (_velocityTracker != null) {
            velocityTracker.recycle()
            _velocityTracker = null
        }
    }

    override fun computeScroll() {
        if (!scroller.isFinished && scroller.computeScrollOffset()) {
            if (scroller.currX != scrollX) {
                onScroll(scroller.currX)
                scrollTo(scroller.currX, 0)
                lastScrollX = scrollX
            }
            ViewCompat.postInvalidateOnAnimation(this)
        } else if (!isDragged) {
            _currentPage = nextPage
        }
    }

    private fun onPageScrolled(currentPosition: Int, nextPosition: Int, offset: Float) {
        if (currentPosition == 0 || nextPosition == 0) {
            if (offset >= 0.5 && !_dark) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
            }
            animateBackground(1 - offset)
        } else {
            (parent as BlurView).setBlurEnabled(false)
        }
    }

    private fun onScroll(newScroll: Int) {
        val adjustedNextPage = if (isDragged) {
            if (lastScrollX > scrollX) {
                nextPage - 1
            } else {
                nextPage + 1
            }
        } else {
            nextPage
        }
        val pageOffset: Float = if (isOptionsPanel) {
            ((newScroll.toFloat() - pageWidth) / optionsPanelWidth)
        } else {
            (newScroll.toFloat() / pageWidth)
        }
        onPageScrolled(currentPage, adjustedNextPage, pageOffset)
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
        for (i in 0 until (_adapter!!.getCount() - 2)) {
            view = _adapter!!.getView(i)
            addView(view, i)
        }
        addView(_adapter!!.getOptionsView())
        addView(_adapter!!.initOptionsViewHolder(this))
    }

    fun pausePager() {
    }

    fun resumePager() {
        val anim = ValueAnimator.ofFloat(Float.MIN_VALUE, 1.0f)
        anim.addUpdateListener { animation ->
            val animationFloat = if (currentPage == 0) {
                animation?.animatedValue as Float
            } else {
                1f - animation?.animatedValue as Float
            }
            animateBackground(animationFloat)
        }
        anim.duration = 600
        anim.start()
    }

    private fun animateBackground(progress: Float) {
        val color = String.format("#%02X", (255 * 0.5 * progress).toInt()) + String.format("%06X", 0xFFFFFF and color)
        setBackgroundColor(Color.parseColor(color))
        val radius = Math.max(10f * progress, Float.MIN_VALUE)
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
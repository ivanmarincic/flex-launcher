package io.github.twoloops.flexlauncher.homescreen.views

import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.database.DataSetObserver
import android.os.Handler
import android.support.v4.view.GestureDetectorCompat
import android.util.AttributeSet
import android.util.SparseArray
import android.view.*
import io.github.twoloops.flexlauncher.helpers.Utils
import io.github.twoloops.flexlauncher.database.entities.HomeScreenItem
import io.github.twoloops.flexlauncher.homescreen.contracts.GridAdapter
import io.github.twoloops.flexlauncher.database.entities.App
import io.github.twoloops.flexlauncher.database.entities.Widget


open class Grid : ViewGroup {

    private var _rowCount: Int = 1
    var rowCount: Int
        get() = _rowCount
        set(value) {
            _rowCount = value
        }

    private var _columnCount: Int = 1
    var columnCount: Int
        get() = _columnCount
        set(value) {
            _columnCount = value
        }

    private var _droppable: Boolean = true
    var droppable: Boolean
        get() = _droppable
        set(value) {
            _droppable = value
        }

    private var _adapter: GridAdapter? = null
    var adapter: GridAdapter?
        get() = _adapter
        set(value) {
            _adapter?.unregisterDataSetObserver(observer)
            _adapter = value
            _adapter?.registerDataSetObserver(observer)
            fillViewsFromAdapter()
        }

    private var _itemListener = ItemGestureDetector()

    private val gestureDetector: GestureDetectorCompat by lazy(LazyThreadSafetyMode.NONE) {
        GestureDetectorCompat(context, _itemListener)
    }
    private val touchGestureDetector: TouchGestureDetector by lazy(LazyThreadSafetyMode.NONE) {
        TouchGestureDetector()
    }
    private var dragEnded = true
    private var flags = 0
    private var statusBarHeight = 0
    private var navigationBarHeight = 0
    private var readyForDrag = false
    private var disableNextUp = false
    private var contextMenuOpen = false
    private var rowHeight: Int = 0
    private var columnWidth: Int = 0
    private var typedViewsCache = SparseArray<List<View>>()
    private val observer = object : DataSetObserver() {

        override fun onChanged() {
            refreshViewsFromAdapter()
        }

        override fun onInvalidated() {
            removeAllViews()
        }
    }

    companion object {
        const val FLAG_FEATURE_STATUS_BAR = 1
        const val FLAG_FEATURE_NAVIGATION_BAR = 2
    }

    init {
        touchGestureDetector.onLongTouchListener(Runnable {
            _adapter?.touchGestureListener?.onLongTouch()
        })
        touchGestureDetector.onDownListener(Runnable {
            _adapter?.touchGestureListener?.onDown()
        })
    }

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minWidth = MeasureSpec.getSize(widthMeasureSpec)
        val minHeight = MeasureSpec.getSize(heightMeasureSpec)

        rowHeight = minHeight / rowCount
        columnWidth = minWidth / columnCount

        var height = minHeight
        if (_adapter != null) {
            height = (_adapter!!.count.toFloat() / columnCount * rowHeight).toInt() + statusBarHeight + navigationBarHeight
            if (minHeight > height) {
                height = minHeight
            }
        }
        setMeasuredDimension(MeasureSpec.makeMeasureSpec(minWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY))
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (!contextMenuOpen) {
            if (_adapter != null) {
                for (i in 0 until _adapter!!.count) {
                    val item: HomeScreenItem<*> = _adapter!!.getItem(i) as HomeScreenItem<*>
                    val view: View = getChildAt(i)
                    val left: Int = item.column * columnWidth
                    val top: Int = item.row * rowHeight + statusBarHeight
                    val right: Int = left + (columnWidth * item.rowSpan)
                    val bottom: Int = top + (rowHeight * item.columnSpan)
                    view.measure(MeasureSpec.makeMeasureSpec(columnWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(rowHeight, MeasureSpec.EXACTLY))
                    view.layout(left, top, right, bottom)
                }
            }
        }
    }

    fun setLayoutFeatures(flags: Int) {
        this.flags = flags
        if (this.flags and FLAG_FEATURE_STATUS_BAR != 0) {
            statusBarHeight = Utils.getStatusBarHieght(context.resources)
        }
        if (this.flags and FLAG_FEATURE_NAVIGATION_BAR != 0) {
            navigationBarHeight = Utils.getNavigationBarHieght(context.resources)
        }
    }

    fun openContextMenu(item: HomeScreenItem<App>, view: View) {
        contextMenuOpen = true
        addView(view)
        val menuView = getChildAt(childCount - 1)
        val left: Int = item.column * columnWidth
        val top: Int = item.row * rowHeight + statusBarHeight
        menuView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST))
        menuView.layout(left, top, left + menuView.measuredWidth, top + menuView.measuredHeight)
    }

    fun closeContextMenu() {
        if (contextMenuOpen) {
            removeView(getChildAt(childCount - 1))
            contextMenuOpen = false
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_UP) {
            _adapter?.cancelTouchEvents(event)
        }
        return touchGestureDetector.onTouchEvent(event)
    }

    override fun onDragEvent(event: DragEvent?): Boolean {
        if ((event?.action == DragEvent.ACTION_DRAG_ENDED || event?.action == DragEvent.ACTION_DROP) && !dragEnded) {
            _itemListener.onDragEnd(event)
            dragEnded = true
        }

        if (event?.action == DragEvent.ACTION_DRAG_STARTED) {
            dragEnded = false
        }
        if (event != null && (event.localState as View).parent == this) {
            if (event.action == DragEvent.ACTION_DRAG_STARTED) {
                _itemListener.onDragStart(event)
            }
        }
        return (event?.action == DragEvent.ACTION_DRAG_STARTED || event?.action == DragEvent.ACTION_DROP) && _droppable
    }

    private fun fillViewsFromAdapter() {
        typedViewsCache.clear()
        removeAllViews()
        var view: View
        if (_adapter == null) return
        for (i in 0 until _adapter!!.count) {
            view = _adapter!!.getView(i, null, this)
            addTouchListener(view, i)
            addToTypesMap(_adapter!!.getItemViewType(i), view, typedViewsCache)
            addView(view, i)
        }
    }

    private fun refreshViewsFromAdapter() {
        val typedViewsCacheCopy = typedViewsCache
        typedViewsCache = SparseArray<List<View>>()
        removeAllViews()
        var convertView: View?
        var type: Int
        for (i in 0 until _adapter!!.count) {
            type = _adapter!!.getItemViewType(i)
            convertView = shiftCachedViewOfType(type, typedViewsCacheCopy)
            convertView = _adapter!!.getView(i, convertView, this)
            addTouchListener(convertView, i)
            addToTypesMap(type, convertView, typedViewsCache)
            addView(convertView, i)
        }
    }

    private fun addToTypesMap(type: Int, view: View?, typedViewsCache: SparseArray<List<View>>) {
        var singleTypeViews = typedViewsCache.get(type)
        if (singleTypeViews == null) {
            singleTypeViews = ArrayList<View>()
            typedViewsCache.put(type, singleTypeViews)
        }
        (singleTypeViews as ArrayList<View?>).add(view)
    }

    private fun shiftCachedViewOfType(type: Int, typedViewsCache: SparseArray<List<View>>): View? {
        val singleTypeViews = typedViewsCache.get(type)
        if (singleTypeViews != null) {
            if (singleTypeViews.isNotEmpty()) {
                return (singleTypeViews as ArrayList<View>).removeAt(0)
            }
        }
        return null
    }

    private fun addTouchListener(convertView: View, position: Int) {
        convertView.setOnTouchListener({ view, event ->
            _itemListener.gridItem = _adapter!!.getItem(position) as HomeScreenItem<*>?
            _itemListener.itemView = view
            gestureDetector.onTouchEvent(event)
        })
    }

    private fun getDistance(ev1: DragEvent, ev2: MotionEvent): Float {
        return Math.sqrt(Math.pow((ev1.x - ev2.x).toDouble(), 2.0) + Math.pow((ev1.y - ev2.y).toDouble(), 2.0)).toFloat()
    }

    fun evaluatePosition(x: Float, y: Float, gridItem: HomeScreenItem<*>): HomeScreenItem<*> {
        gridItem.column = (x / columnWidth).toInt()
        gridItem.row = (y / rowHeight).toInt()
        return gridItem
    }

    fun evaluateWidgetSize(widgetProviderInfo: AppWidgetProviderInfo): HomeScreenItem<*> {
        var width = Math.ceil(widgetProviderInfo.minWidth.toDouble() / columnWidth).toInt()
        if (width > columnCount) {
            width = columnCount
        }
        var height = Math.ceil(widgetProviderInfo.minHeight.toDouble() / rowHeight).toInt()
        if (height > rowCount) {
            height = rowCount
        }
        return HomeScreenItem(0, 0, height, 0, width, Widget(0))
    }

    inner class TouchGestureDetector {

        var handler: Handler = Handler()
        var longTouchCallback: Runnable? = null
        var downTouchCallback: Runnable? = null

        fun onTouchEvent(event: MotionEvent?): Boolean {

            when (event?.action) {
                MotionEvent.ACTION_UP -> {
                    handler.removeCallbacks(longTouchCallback)
                    return true
                }
                MotionEvent.ACTION_CANCEL -> {
                    handler.removeCallbacks(longTouchCallback)
                    return true
                }
                MotionEvent.ACTION_DOWN -> {
                    downTouchCallback?.run()
                    handler.postDelayed(longTouchCallback, ViewConfiguration.getLongPressTimeout().toLong())
                    return true
                }
            }
            return false
        }

        fun onDownListener(callback: Runnable) {
            downTouchCallback = callback
        }

        fun onLongTouchListener(callback: Runnable) {
            longTouchCallback = callback
        }
    }

    inner class ItemGestureDetector : GestureDetector.SimpleOnGestureListener() {

        var gridItem: HomeScreenItem<*>? = null
        var itemView: View? = null

        override fun onShowPress(e: MotionEvent?) {
        }

        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            return if (disableNextUp) {
                disableNextUp = false
                false
            } else {
                _adapter?.onPress(gridItem!!, itemView!!)
                true
            }
        }

        override fun onDown(e: MotionEvent?): Boolean {
            readyForDrag = false
            if (contextMenuOpen) {
                closeContextMenu()
                disableNextUp = true
            }
            return true
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            return true
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
            return true
        }

        override fun onLongPress(e: MotionEvent?) {
            _adapter!!.startDrag(gridItem!!, itemView!!)
            _adapter!!.onLongPress(gridItem!!, itemView!!)
        }

        fun onDragStart(e: DragEvent) {
            _adapter!!.onDragStart(e, gridItem!!, itemView!!)
        }

        fun onDragEnd(e: DragEvent) {
            _adapter!!.onDragEnd(e, gridItem, itemView)

        }
    }

}
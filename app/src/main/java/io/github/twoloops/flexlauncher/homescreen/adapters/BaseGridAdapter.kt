package io.github.twoloops.flexlauncher.homescreen.adapters

import android.annotation.TargetApi
import android.app.Activity
import android.appwidget.AppWidgetProviderInfo
import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.database.DataSetObserver
import android.graphics.drawable.AdaptiveIconDrawable
import android.os.Build
import android.os.Process
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import io.github.twoloops.flexlauncher.R
import io.github.twoloops.flexlauncher.database.entities.HomeScreenItem
import io.github.twoloops.flexlauncher.homescreen.contracts.GridAdapter
import io.github.twoloops.flexlauncher.database.entities.App
import io.github.twoloops.flexlauncher.homescreen.services.HapticFeedbackService
import io.github.twoloops.flexlauncher.homescreen.views.AdaptiveIconView
import io.github.twoloops.flexlauncher.homescreen.views.Grid


open class BaseGridAdapter(private var activity: Activity) : GridAdapter {

    var items: ArrayList<HomeScreenItem<*>>? = null
    open var observer: DataSetObserver? = null
    override var dragAndDropListener: GridAdapter.DragAndDropListener? = null
    override var touchGestureListener: GridAdapter.TouchGestureListener? = null
    open val hapticFeedbackService: HapticFeedbackService by lazy(LazyThreadSafetyMode.NONE) {
        HapticFeedbackService(activity)
    }

    override fun isEmpty(): Boolean {
        return count == 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return convertView ?: when (getItemViewType(position)) {
            0 -> {
                val rootView: View = convertView
                        ?: LayoutInflater.from(parent!!.context).inflate(R.layout.homescreen_apps_view_item, parent, false)
                val item = getItem(position) as HomeScreenItem<App>
                val textView: TextView = rootView.findViewById(R.id.homescreen_view_apps_app_label)
                val imageHolder: FrameLayout = rootView.findViewById(R.id.homescreen_view_apps_app_icon_holder)
                textView.text = item.content.label
                val drawable = item.content.getIconDrawable(activity)
                val layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                @TargetApi(Build.VERSION_CODES.O)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && drawable::class == AdaptiveIconDrawable::class) {
                    val adaptiveIconView = AdaptiveIconView(activity)
                    adaptiveIconView.shapeId = AdaptiveIconView.SHAPE_CIRCLE
                    adaptiveIconView.icon = drawable
                    adaptiveIconView.layoutParams = layoutParams
                    imageHolder.addView(adaptiveIconView)
                } else {
                    val iconView = ImageView(activity)
                    iconView.setImageDrawable(drawable)
                    iconView.layoutParams = layoutParams
                    imageHolder.addView(iconView)
                }
                return rootView
            }
            1 -> {
                LayoutInflater.from(parent!!.context).inflate(R.layout.homescreen_apps_view_item, parent, false)
            }
            else -> {
                View(activity)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)!!.content::class) {
            App::class -> 0
            AppWidgetProviderInfo::class -> 1
            else -> -1
        }
    }

    override fun addItem(gridItem: HomeScreenItem<*>): Boolean {
        return if (hasItemInCell(gridItem)) {
            false
        } else {
            items!!.add(gridItem)
            notifyDataChanged()
            true
        }
    }

    override fun removeItem(gridItem: HomeScreenItem<*>): Boolean {
        if (items!!.remove(gridItem)) {
            notifyDataChanged()
            return true
        }
        return false
    }

    override fun hasItemInCell(gridItem: HomeScreenItem<*>): Boolean {
        return items!!.filter { it.row == gridItem.row && it.column == gridItem.column }.count() > 0
    }

    override fun hasItem(gridItem: HomeScreenItem<*>): Boolean {
        return items!!.indexOf(gridItem) != -1
    }

    override fun getItem(position: Int): HomeScreenItem<*>? {
        return items?.get(position)
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getCount(): Int {
        return items?.count() ?: 0
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun registerDataSetObserver(observer: DataSetObserver?) {
        this.observer = observer
    }

    override fun unregisterDataSetObserver(observer: DataSetObserver?) {
        if (this.observer == observer) {
            this.observer = null
        }
    }

    override fun notifyDataChanged() {
        if (observer != null) {
            observer!!.onChanged()
        }
    }

    override fun notifyDataInvalid() {
        if (observer != null) {
            observer!!.onInvalidated()
        }
    }

    override fun onPress(gridItem: HomeScreenItem<*>, itemView: View) {
        activity.startActivity((gridItem.content as App).launchIntent)
        activity.overridePendingTransition(0, 0)
        touchGestureListener?.onDown()
        hapticFeedbackService.sendTouchFeedback(activity)
    }

    @TargetApi(Build.VERSION_CODES.N_MR1)
    override fun onLongPress(gridItem: HomeScreenItem<*>, itemView: View) {
        val menuView: ViewGroup = LayoutInflater.from(activity).inflate(R.layout.homescreen_apps_view_menu, itemView.parent as Grid, false) as ViewGroup
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val launcherApps = activity.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
            for (shortcut: ShortcutInfo in (gridItem.content as App).shortcuts) {
                val shortcutView = LayoutInflater.from(activity).inflate(R.layout.homescreen_apps_view_menu_shortcut, menuView, false)
                val textView: TextView = shortcutView.findViewById(R.id.homescreen_view_apps_menu_shortcut_label)
                val imageView: ImageView = shortcutView.findViewById(R.id.homescreen_view_apps_menu_shortcut_icon)
                textView.text = shortcut.shortLabel
                imageView.setImageDrawable(launcherApps.getShortcutIconDrawable(shortcut,
                        activity.resources.displayMetrics.densityDpi))
                shortcutView.setOnClickListener({
                    launcherApps.startShortcut(shortcut.`package`, shortcut.id, null, null, Process.myUserHandle())
                })
                menuView.addView(shortcutView)
            }
        }
//        (itemView.parent as Grid).openContextMenu((gridItem as GridItem<App>), menuView)
        hapticFeedbackService.sendLongTouchFeedback(activity)
    }

    override fun cancelTouchEvents(e: MotionEvent?) {
    }

    override fun startDrag(gridItem: HomeScreenItem<*>, itemView: View) {
        val clipItem: ClipData.Item = ClipData.Item((gridItem.content as App).label)
        val clipData = ClipData((gridItem.content as App).label, arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN), clipItem)
        val shadowBuilder: View.DragShadowBuilder = View.DragShadowBuilder(itemView)
        itemView.startDrag(clipData, shadowBuilder, itemView, 0)
    }

    override fun onDragStart(event: DragEvent, gridItem: HomeScreenItem<*>, itemView: View) {
        val view = event.localState as View
        view.visibility = View.INVISIBLE
        dragAndDropListener?.onDragStart(event, gridItem, itemView)
    }

    override fun onDragEnd(event: DragEvent, gridItem: HomeScreenItem<*>?, itemView: View?) {
        val view = event.localState as View
        view.visibility = View.VISIBLE
        dragAndDropListener?.onDragEnd(event, gridItem, itemView)
    }
}
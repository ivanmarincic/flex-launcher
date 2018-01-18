package io.github.twoloops.flexlauncher.homescreen.presenters

import android.Manifest
import android.app.Activity
import android.app.WallpaperManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.support.design.widget.CoordinatorLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import io.github.twoloops.flexlauncher.R
import io.github.twoloops.flexlauncher.Utils
import io.github.twoloops.flexlauncher.getPreference
import io.github.twoloops.flexlauncher.homescreen.adapters.BaseGridAdapter
import io.github.twoloops.flexlauncher.homescreen.contracts.GridAdapter
import io.github.twoloops.flexlauncher.homescreen.contracts.HomeScreen
import io.github.twoloops.flexlauncher.homescreen.models.App
import io.github.twoloops.flexlauncher.homescreen.models.GridItem
import io.github.twoloops.flexlauncher.homescreen.services.AppLoaderService
import io.github.twoloops.flexlauncher.homescreen.services.WidgetLoaderService
import io.github.twoloops.flexlauncher.homescreen.views.Grid
import io.github.twoloops.flexlauncher.homescreen.views.HomeScreenView
import io.github.twoloops.flexlauncher.homescreen.views.Pager


class HomeScreenPresenter : HomeScreen.Presenter {

    private lateinit var view: HomeScreenView
    private var draggedItem: GridItem<*>? = null

    override fun start(view: HomeScreen.View) {
        this.view = view as HomeScreenView
    }


    override fun initializePager(pager: Pager, grid: View, dashboard: View, wallpaperManager: WallpaperManager) {
        pager.adapter = io.github.twoloops.flexlauncher.homescreen.adapters.PagerAdapter(grid, dashboard)
        if (Build.VERSION.SDK_INT >= 27) {
            pager.dark = Utils.isWallpaperDark(wallpaperManager)
        }
    }

    override fun initializeGrid(parent: ViewGroup, items: ArrayList<GridItem<App>>): View {
        val gridView = LayoutInflater.from(view.applicationContext).inflate(R.layout.homescreen_view_apps, parent, false)
        val grid = gridView.findViewById<Grid>(R.id.homescreen_view_apps_grid)
        val gridAdapter = BaseGridAdapter(view as Activity)
        gridAdapter.items = items as ArrayList<GridItem<*>>
        grid.setLayoutFeatures(Grid.FLAG_FEATURE_NAVIGATION_BAR or Grid.FLAG_FEATURE_STATUS_BAR)
        grid.droppable = false
        grid.adapter = gridAdapter
        grid.columnCount = view.getPreference<Int>("ColumnCount", 5)
        grid.rowCount = view.getPreference<Int>("RowCount", 6)
        view.appGrid = grid
        return gridView
    }


    override fun initializeDashboard(parent: ViewGroup, items: ArrayList<GridItem<*>>): View {
        val dashboardView = LayoutInflater.from(view.applicationContext).inflate(R.layout.homescreen_view_dashboard, parent, false)
        val grid = dashboardView.findViewById<Grid>(R.id.homescreen_view_dashboard_grid)
        val gridAdapter = BaseGridAdapter(view as Activity)
        gridAdapter.items = items
        grid.setLayoutFeatures(Grid.FLAG_FEATURE_NAVIGATION_BAR or Grid.FLAG_FEATURE_STATUS_BAR)
        grid.adapter = gridAdapter
        grid.columnCount = view.getPreference("ColumnCount", 5)
        grid.rowCount = view.getPreference("RowCount", 6)
        view.dashboardGrid = grid
        return dashboardView
    }

    override fun getAppsForGrid(): ArrayList<GridItem<App>> {
        val apps: ArrayList<App> = AppLoaderService(view.applicationContext).getAllApps()
        val gridItems: ArrayList<GridItem<App>> = ArrayList()
        val columnCount = view.getPreference<Int>("ColumnCount", 5)
        var currentRow = 0
        var currentColumn = 0
        for (app: App in apps) {
            gridItems.add(GridItem(currentRow, 1, currentColumn, 1, app))
            currentColumn++
            if (currentColumn == columnCount) {
                currentRow++
                currentColumn = 0
            }
        }
        return gridItems
    }

    override fun getItemsForDashboard(): ArrayList<GridItem<*>> {
        val items = ArrayList<GridItem<*>>()
        items.add(getAppsForGrid()[19])
        return items
    }

    override fun getWidgets(): ArrayList<GridItem<*>> {
        val widgets: ArrayList<AppWidgetProviderInfo> = WidgetLoaderService(view).getAllWidgets() as ArrayList<AppWidgetProviderInfo>
        val items: ArrayList<GridItem<*>> = ArrayList()
        widgets.mapTo(items) { view.dashboardGrid?.evaluateWidgetSize(it)!! }
        return items
    }

    override fun initializeBackground(backgroundView: ImageView, wallpaperManager: WallpaperManager) {
        if (ContextCompat.checkSelfPermission(view, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            val backgroundDrawable: Drawable = wallpaperManager.drawable
            backgroundView.setImageDrawable(backgroundDrawable)
        } else {
            backgroundView.setImageDrawable(ColorDrawable(Color.BLACK))
            ActivityCompat.requestPermissions(view, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), Utils.REQUEST_READ_STORAGE_PERMISSION)

        }
    }

    override fun initializeDragging() {
        view.appGrid!!.adapter!!.setOnDragAndDropListener(object : GridAdapter.DragAndDropListener {
            override fun onDragStart(event: DragEvent, gridItem: GridItem<*>, itemView: View) {
                view.dashboardActions.alpha = 1f
                if (view.pager.childCount == 2) {
                    view.pager.currentItem = 1
                    draggedItem = gridItem
                }
            }

            override fun onDragEnd(event: DragEvent, gridItem: GridItem<*>, itemView: View) {
                view.dashboardActions.alpha = 0f
            }

        })
        view.dashboardGrid!!.adapter!!.setOnDragAndDropListener(object : GridAdapter.DragAndDropListener {
            override fun onDragStart(event: DragEvent, gridItem: GridItem<*>, itemView: View) {
                view.dashboardActions.alpha = 1f
                if (view.pager.childCount == 2) {
                    view.pager.currentItem = 1
                    draggedItem = gridItem
                }
            }

            override fun onDragEnd(event: DragEvent, gridItem: GridItem<*>, itemView: View) {
                val itemToAdd = view.dashboardGrid!!.evaluatePosition(event.x, event.y, draggedItem!!.clone() as GridItem<*>)
                if (!view.dashboardGrid!!.adapter!!.hasItemInCell(itemToAdd)) {
                    view.dashboardGrid!!.adapter!!.removeItem(draggedItem!!)
                    view.dashboardGrid!!.adapter!!.addItem(itemToAdd)
                }
                view.dashboardActions.alpha = 0f
            }

        })
    }

    override fun initializeDraggingActions() {
        val layoutParams: CoordinatorLayout.LayoutParams = view.dashboardActions.layoutParams as CoordinatorLayout.LayoutParams
        layoutParams.setMargins(0, Utils.getStatusBarHieght(view.resources), 0, 0)
        val actionInfo = view.dashboardActions.findViewById<TextView>(R.id.homescreen_view_dashboard_actions_info)
        val actionUninstall = view.dashboardActions.findViewById<TextView>(R.id.homescreen_view_dashboard_actions_uninstall)
        val actionRemove = view.dashboardActions.findViewById<TextView>(R.id.homescreen_view_dashboard_actions_remove)
        actionInfo.setOnDragListener { v, event ->
            when (event?.action) {
                DragEvent.ACTION_DRAG_ENDED -> {
                    val view = event.localState as View
                    view.visibility = View.VISIBLE
                }
                DragEvent.ACTION_DROP -> {
                    val sourceView = event.localState as View
                    sourceView.visibility = View.VISIBLE
                    try {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = Uri.parse("package:" + (draggedItem!!.content as App).activityInfo?.packageName)
                        view.startActivity(intent)

                    } catch (e: ActivityNotFoundException) {
                        val intent = Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)
                        view.startActivity(intent)
                    }
                    view.dashboardActions.alpha = 0f
                }
            }
            true
        }
        actionUninstall.setOnDragListener { v, event ->
            when (event?.action) {
                DragEvent.ACTION_DRAG_ENDED -> {
                    val view = event.localState as View
                    view.visibility = View.VISIBLE
                }
                DragEvent.ACTION_DROP -> {
                    val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE)
                    intent.data = Uri.parse("package:" + (draggedItem!!.content as App).activityInfo?.packageName)
                    view.startActivity(intent)
                    view.appGrid!!.adapter!!.removeItem(draggedItem!!)
                    view.dashboardGrid!!.adapter!!.removeItem(draggedItem!!)
                    view.dashboardActions.alpha = 0f
                }
            }
            true
        }
        actionRemove.setOnDragListener { v, event ->
            when (event?.action) {
                DragEvent.ACTION_DRAG_ENDED -> {
                    val view = event.localState as View
                    view.visibility = View.VISIBLE
                }
                DragEvent.ACTION_DROP -> {
                    view.dashboardGrid!!.adapter!!.removeItem(draggedItem!!)
                    draggedItem = null
                    view.dashboardActions.alpha = 0f
                }
            }
            true
        }
    }

    override fun initializeWidgetsPanel() {
        val widgets: ArrayList<AppWidgetProviderInfo> = WidgetLoaderService(view).getAllWidgets() as ArrayList<AppWidgetProviderInfo>
        val items: ArrayList<GridItem<*>> = ArrayList()
        widgets.mapTo(items) { view.dashboardGrid?.evaluateWidgetSize(it)!! }
        val imageView = ImageView(view)
        imageView.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        try {
            val manager = view.packageManager
            val resources = manager.getResourcesForApplication(widgets[0].provider.packageName)
            imageView.setImageDrawable(resources.getDrawable(widgets[0].previewImage))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        view.widgetsPanel.addView(imageView)
    }
}
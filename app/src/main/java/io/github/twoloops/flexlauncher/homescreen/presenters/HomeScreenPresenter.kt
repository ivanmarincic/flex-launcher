package io.github.twoloops.flexlauncher.homescreen.presenters

import android.Manifest
import android.app.Activity
import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import io.github.twoloops.flexlauncher.R
import io.github.twoloops.flexlauncher.helpers.Utils
import io.github.twoloops.flexlauncher.database.controllers.HomeScreenAppsController
import io.github.twoloops.flexlauncher.database.controllers.HomeScreenContentController
import io.github.twoloops.flexlauncher.database.controllers.HomeScreenDashboardController
import io.github.twoloops.flexlauncher.database.entities.HomeScreenItem
import io.github.twoloops.flexlauncher.getPreference
import io.github.twoloops.flexlauncher.homescreen.adapters.BaseGridAdapter
import io.github.twoloops.flexlauncher.homescreen.adapters.BasePagerAdapter
import io.github.twoloops.flexlauncher.homescreen.contracts.GridAdapter
import io.github.twoloops.flexlauncher.homescreen.contracts.HomeScreen
import io.github.twoloops.flexlauncher.database.entities.App
import io.github.twoloops.flexlauncher.homescreen.services.AppLoaderService
import io.github.twoloops.flexlauncher.homescreen.services.WidgetLoaderService
import io.github.twoloops.flexlauncher.homescreen.views.Grid
import io.github.twoloops.flexlauncher.homescreen.views.HomeScreenView
import io.github.twoloops.flexlauncher.homescreen.views.Pager


class HomeScreenPresenter : HomeScreen.Presenter {

    private lateinit var view: HomeScreenView
    var draggedItem: HomeScreenItem<*>? = null

    override fun start(view: HomeScreen.View) {
        this.view = view as HomeScreenView

    }

    override fun initializePager(pager: Pager, grid: View, dashboard: View, wallpaperManager: WallpaperManager) {
        pager.adapter = BasePagerAdapter(grid, dashboard)
        if (Build.VERSION.SDK_INT >= 27) {
            pager.dark = Utils.isWallpaperDark(wallpaperManager)
        }
    }

    override fun initializeGrid(parent: ViewGroup, items: ArrayList<HomeScreenItem<App>>): View {
        val gridView = LayoutInflater.from(view.applicationContext).inflate(R.layout.homescreen_view_apps, parent, false)
        val grid = gridView.findViewById<Grid>(R.id.homescreen_view_apps_grid)
        val gridAdapter = BaseGridAdapter(view as Activity)
        gridAdapter.items = items as ArrayList<HomeScreenItem<*>>
        grid.setLayoutFeatures(Grid.FLAG_FEATURE_NAVIGATION_BAR or Grid.FLAG_FEATURE_STATUS_BAR)
        grid.droppable = false
        grid.adapter = gridAdapter
        grid.columnCount = view.getPreference<Int>("ColumnCount", 5)
        grid.rowCount = view.getPreference<Int>("RowCount", 6)
        view.appGrid = grid
        return gridView
    }


    override fun initializeDashboard(parent: ViewGroup, items: ArrayList<HomeScreenItem<*>>): View {
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

    override fun getItemsForApps(): ArrayList<HomeScreenItem<App>> {
        val gridItems: ArrayList<HomeScreenItem<App>> = ArrayList()
        var apps: ArrayList<App> = HomeScreenAppsController.getInstance(view).getAll()
        var hasBeenSaved = true
        if (apps.count() == 0) {
            hasBeenSaved = false
            apps = AppLoaderService.getInstance(view).getAllApps()
        }
        val columnCount = view.getPreference<Int>("ColumnCount", 5)
        var currentRow = 0
        var currentColumn = 0
        for (app: App in apps) {
            if (!hasBeenSaved) {
                HomeScreenAppsController.getInstance(view).save(app)
            }
            val item: HomeScreenItem<App> = HomeScreenItem(0, currentColumn, 1, currentRow, 1, app)
            gridItems.add(item)
            currentColumn++
            if (currentColumn == columnCount) {
                currentRow++
                currentColumn = 0
            }
        }
        return gridItems
    }

    override fun getItemsForDashboard(): ArrayList<HomeScreenItem<*>> {
        return HomeScreenDashboardController.getInstance(view).getAll()
    }

    override fun getWidgets(): ArrayList<HomeScreenItem<*>> {
        val widgets: ArrayList<AppWidgetProviderInfo> = WidgetLoaderService(view).getAllWidgets() as ArrayList<AppWidgetProviderInfo>
        val items: ArrayList<HomeScreenItem<*>> = ArrayList()
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
        view.appGrid!!.adapter!!.dragAndDropListener = object : GridAdapter.DragAndDropListener {
            override fun onDragStart(event: DragEvent, gridItem: HomeScreenItem<*>, itemView: View) {
                view.dashboardActions.alpha = 1f
                if (view.pager.childCount == 2) {
                    view.pager.currentPage = 1
                    draggedItem = gridItem
                }
            }

            override fun onDragEnd(event: DragEvent, gridItem: HomeScreenItem<*>?, itemView: View?) {
                view.dashboardActions.alpha = 0f
            }

        }
        view.dashboardGrid!!.adapter!!.dragAndDropListener = object : GridAdapter.DragAndDropListener {
            override fun onDragStart(event: DragEvent, gridItem: HomeScreenItem<*>, itemView: View) {
                view.dashboardActions.alpha = 1f
                if (view.pager.childCount == 2) {
                    view.pager.currentPage = 1
                    draggedItem = gridItem
                }
            }

            override fun onDragEnd(event: DragEvent, gridItem: HomeScreenItem<*>?, itemView: View?) {
                if (draggedItem != null) {
                    val itemToAdd = view.dashboardGrid!!.evaluatePosition(event.x, event.y, draggedItem!!.clone() as HomeScreenItem<*>)
                    if (!view.dashboardGrid!!.adapter!!.hasItemInCell(itemToAdd)) {
                        if (view.dashboardGrid!!.adapter!!.removeItem(draggedItem!!)) {
                            view.dashboardGrid!!.adapter!!.addItem(itemToAdd)
                            HomeScreenDashboardController.getInstance(view).update(itemToAdd)
                        } else {
                            HomeScreenDashboardController.getInstance(view).save(itemToAdd)
                        }
                    }
                    view.dashboardActions.alpha = 0f
                }
            }
        }
    }

    override fun initializeDraggingActions() {
        val layoutParams: FrameLayout.LayoutParams = view.dashboardActions.layoutParams as FrameLayout.LayoutParams
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
                        intent.data = Uri.parse("package:" + (draggedItem!!.content as App).packageName)
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
                    intent.data = Uri.parse("package:" + (draggedItem!!.content as App).packageName)
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
        val items: ArrayList<HomeScreenItem<*>> = ArrayList()
        widgets.mapTo(items) { view.dashboardGrid?.evaluateWidgetSize(it)!! }
        view.dashboardGrid!!.adapter!!.touchGestureListener = object : GridAdapter.TouchGestureListener {
            override fun onLongTouch() {
                view.widgetsPanel.visibility = View.VISIBLE
            }

            override fun onDown() {
                view.widgetsPanel.visibility = View.INVISIBLE
            }
        }
    }

    override fun pickWidget(appWidgetProviderInfo: AppWidgetProviderInfo, appWidgetId: Int) {
        if (appWidgetProviderInfo.configure != null) {
            val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE)
            intent.component = appWidgetProviderInfo.configure
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            view.startActivityForResult(intent, Utils.REQUEST_ADD_WIDGET)
        } else {
            addWidget(appWidgetProviderInfo, appWidgetId)
        }
    }

    override fun addWidget(appWidgetProviderInfo: AppWidgetProviderInfo, appWidgetId: Int) {
        if (view.appWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, appWidgetProviderInfo.provider)) {
            val hostView = view.appWidgetHost.createView(view, appWidgetId, appWidgetProviderInfo)
            hostView?.setAppWidget(appWidgetId, appWidgetProviderInfo)
            view.widgetsPanel.addView(hostView)
        } else {
            val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, appWidgetProviderInfo.provider)
            view.startActivityForResult(intent, Utils.REQUEST_BIND_WIDGET)
        }
    }
}
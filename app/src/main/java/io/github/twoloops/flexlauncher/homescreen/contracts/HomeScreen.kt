package io.github.twoloops.flexlauncher.homescreen.contracts

import android.app.WallpaperManager
import android.appwidget.AppWidgetProviderInfo
import android.view.ViewGroup
import android.widget.ImageView
import io.github.twoloops.flexlauncher.database.entities.HomeScreenItem
import io.github.twoloops.flexlauncher.database.entities.App
import io.github.twoloops.flexlauncher.homescreen.views.Pager

class HomeScreen {


    interface View

    interface Presenter {
        fun start(view: View)
        fun initializePager(pager: Pager, grid: android.view.View, dashboard: android.view.View, optionsPanel: android.view.View, wallpaperManager: WallpaperManager)
        fun initializeGrid(parent: Pager, items: ArrayList<HomeScreenItem<App>>): android.view.View
        fun initializeDashboard(parent: Pager, items: ArrayList<HomeScreenItem<*>>): android.view.View
        fun initializeOptionsPanel(parent: Pager): android.view.View
        fun initializeBackground(backgroundView: ImageView, wallpaperManager: WallpaperManager)
        fun initializeDragging()
        fun initializeDraggingActions()
        fun initializeWidgetsPanel()
        fun pickWidget(appWidgetProviderInfo: AppWidgetProviderInfo, appWidgetId: Int)
        fun addWidget(appWidgetProviderInfo: AppWidgetProviderInfo, appWidgetId: Int)
        fun getItemsForApps(): ArrayList<HomeScreenItem<App>>
        fun getItemsForDashboard(): ArrayList<HomeScreenItem<*>>
        fun getWidgets(): ArrayList<HomeScreenItem<*>>
    }
}
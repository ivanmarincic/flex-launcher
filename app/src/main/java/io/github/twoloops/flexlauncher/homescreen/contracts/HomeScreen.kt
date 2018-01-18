package io.github.twoloops.flexlauncher.homescreen.contracts

import android.app.WallpaperManager
import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import io.github.twoloops.flexlauncher.homescreen.models.App
import io.github.twoloops.flexlauncher.homescreen.models.GridItem
import io.github.twoloops.flexlauncher.homescreen.views.Pager

class HomeScreen {


    interface View

    interface Presenter {
        fun start(view: View)
        fun initializePager(pager: Pager, grid: android.view.View, dashboard: android.view.View, wallpaperManager: WallpaperManager)
        fun initializeGrid(parent: ViewGroup, items: ArrayList<GridItem<App>>): android.view.View
        fun initializeDashboard(parent: ViewGroup, items: ArrayList<GridItem<*>>): android.view.View
        fun initializeBackground(backgroundView: ImageView, wallpaperManager: WallpaperManager)
        fun initializeDragging()
        fun initializeDraggingActions()
        fun initializeWidgetsPanel()
        fun getAppsForGrid(): ArrayList<GridItem<App>>
        fun getItemsForDashboard(): ArrayList<GridItem<*>>
        fun getWidgets(): ArrayList<GridItem<*>>
    }
}
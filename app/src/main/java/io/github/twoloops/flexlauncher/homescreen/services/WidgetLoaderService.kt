package io.github.twoloops.flexlauncher.homescreen.services

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.appwidget.AppWidgetProviderInfo
import android.content.Context


class WidgetLoaderService(context: Context) {

    private val context = context

    fun getAllWidgets(): List<AppWidgetProviderInfo> {
        var appWidgetManager: AppWidgetManager = context.getSystemService(Context.APPWIDGET_SERVICE) as AppWidgetManager
        return appWidgetManager.installedProviders
    }
}
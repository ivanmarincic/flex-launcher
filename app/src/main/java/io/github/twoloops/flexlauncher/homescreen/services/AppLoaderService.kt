package io.github.twoloops.flexlauncher.homescreen.services

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import io.github.twoloops.flexlauncher.helpers.SingletonHolder
import io.github.twoloops.flexlauncher.database.entities.App
import kotlin.collections.ArrayList

class AppLoaderService private constructor(context: Context) {

    private val packageManager = context.packageManager

    fun getAllApps(): ArrayList<App> {
        val apps = ArrayList<App>()

        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)

        val activities = packageManager.queryIntentActivities(intent, PackageManager.GET_META_DATA)
        for (resolveInfo: ResolveInfo in activities) {
            try {
                val app = App()
                app.label = resolveInfo.loadLabel(packageManager).toString()
                app.icon = resolveInfo.iconResource
                app.packageName = resolveInfo.activityInfo.packageName
                app.activityName = resolveInfo.activityInfo.name
                app.intentFlags = resolveInfo.activityInfo.flags

                apps.add(app)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        return apps
    }

    companion object : SingletonHolder<AppLoaderService, Context>(::AppLoaderService)
}
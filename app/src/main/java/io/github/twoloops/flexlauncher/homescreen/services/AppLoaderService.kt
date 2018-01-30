package io.github.twoloops.flexlauncher.homescreen.services

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.BitmapDrawable
import io.github.twoloops.flexlauncher.helpers.SingletonHolder
import io.github.twoloops.flexlauncher.database.entities.App
import kotlin.collections.ArrayList

class AppLoaderService private constructor(context: Context) {

    private val packageManager = context.packageManager

    fun getAllApps(): ArrayList<App> {
        return getApps("")
    }

    fun getAppByPackageName(packageName: String): App {
        val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
        return getApps(packageName)[0]
    }

    private fun getApps(packageName: String): ArrayList<App> {
        val apps = ArrayList<App>()
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val activities = packageManager.queryIntentActivities(intent, 0)
        for (resolveInfo: ResolveInfo in activities) {
            try {
                if (!packageName.isBlank() && packageName == resolveInfo.activityInfo.packageName) {
                    apps.add(createApp(resolveInfo))
                    break
                } else {
                    apps.add(createApp(resolveInfo))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return apps
    }

    private fun createApp(resolveInfo: ResolveInfo): App {
        val app = App()
        app.label = resolveInfo.loadLabel(packageManager).toString()
        app.icon = resolveInfo.iconResource
        app.packageName = resolveInfo.activityInfo.packageName
        app.activityName = resolveInfo.activityInfo.name
        app.intentFlags = resolveInfo.activityInfo.flags
        return app
    }

    companion object : SingletonHolder<AppLoaderService, Context>(::AppLoaderService)
}
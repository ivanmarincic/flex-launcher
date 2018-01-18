package io.github.twoloops.flexlauncher.homescreen.services

import android.annotation.TargetApi
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.pm.ShortcutInfo
import android.os.Build
import android.util.Log
import android.os.Process
import io.github.twoloops.flexlauncher.homescreen.models.App
import java.util.*
import kotlin.collections.ArrayList

class AppLoaderService(var context: Context) {

    private val packageManager = context.packageManager

    @TargetApi(Build.VERSION_CODES.N_MR1)
    fun getAllApps(): ArrayList<App> {
        val apps = ArrayList<App>()

        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)

        val activities = packageManager.queryIntentActivities(intent, PackageManager.GET_META_DATA)
        for (resolveInfo: ResolveInfo in activities) {
            try {
                val app = App()
                app.name = resolveInfo.loadLabel(packageManager).toString()
                app.icon = resolveInfo.loadIcon(packageManager)
                app.activityInfo = resolveInfo.activityInfo
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
                    val shortcutQuery = LauncherApps.ShortcutQuery()
                    shortcutQuery.setQueryFlags(LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC
                            or LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST
                            or LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED)
                    shortcutQuery.setPackage(app.activityInfo!!.packageName)
                    app.shortcuts = try {
                        launcherApps.getShortcuts(shortcutQuery, Process.myUserHandle())
                    } catch (e: SecurityException) {
                        ArrayList()
                    }
                }
                apps.add(app)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        return apps
    }
}
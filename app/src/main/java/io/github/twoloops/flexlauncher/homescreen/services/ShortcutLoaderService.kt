package io.github.twoloops.flexlauncher.homescreen.services

import android.annotation.TargetApi
import android.content.Context
import android.content.pm.LauncherApps
import android.os.Build
import android.os.Process
import io.github.twoloops.flexlauncher.helpers.SingletonHolder
import io.github.twoloops.flexlauncher.homescreen.models.Shortcut

@TargetApi(Build.VERSION_CODES.N_MR1)
class ShortcutLoaderService private constructor(context: Context) {


    private val launcherApps: LauncherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps

    fun getShorcutsByPackageName(packageName: String): ArrayList<Shortcut> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val shortcutQuery = LauncherApps.ShortcutQuery()
            shortcutQuery.setQueryFlags(LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC
                    or LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST)
            shortcutQuery.setPackage(packageName)
            return try {
                launcherApps.getShortcuts(shortcutQuery, Process.myUserHandle()) as ArrayList<Shortcut>
            } catch (e: SecurityException) {
                ArrayList()
            }
        } else {
            ArrayList()
        }
    }

    companion object : SingletonHolder<ShortcutLoaderService, Context>(::ShortcutLoaderService)
}
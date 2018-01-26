package io.github.twoloops.flexlauncher.database.entities

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.graphics.drawable.Drawable
import org.jetbrains.anko.db.RowParser
import org.jetbrains.anko.db.rowParser

class App(override var id: Long, var label: String, var icon: Int, var packageName: String, var activityName: String, var intentFlags: Int, var shortcuts: List<ShortcutInfo>) : BaseEntity {

    val launchIntent: Intent?
        get() {
            val componentName = ComponentName(packageName, activityName)
            val appLaunchIntent = Intent(Intent.ACTION_MAIN)
            appLaunchIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            appLaunchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or
                    intentFlags
            appLaunchIntent.component = componentName
            return appLaunchIntent
        }

    constructor() : this(0, "", 0, "", "", 0, ArrayList())

    fun getIconDrawable(context: Context): Drawable {
        return context.packageManager.getApplicationIcon(packageName)
    }

    companion object {
        fun getParser(): RowParser<App> {
            return rowParser { id: Long, label: String, icon: Int, packageName: String, activityName: String, intentFlags: Int ->
                App(id, label, icon, packageName, activityName, intentFlags, ArrayList())
            }
        }
    }
}
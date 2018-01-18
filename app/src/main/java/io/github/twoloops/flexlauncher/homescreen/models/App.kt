package io.github.twoloops.flexlauncher.homescreen.models

import android.content.ComponentName
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ShortcutInfo
import android.graphics.drawable.Drawable

class App {
    private var _name: String? = null
    private var _icon: Drawable? = null
    private var _activityInfo: ActivityInfo? = null

    var name: String?
        get() = _name
        set(value) {
            _name = value
        }

    var icon: Drawable?
        get() = _icon
        set(value) {
            _icon = value
        }

    var activityInfo: ActivityInfo?
        get() = _activityInfo
        set(value) {
            _activityInfo = value
        }

    val launchIntent: Intent?
        get() {
            val componentName = ComponentName(activityInfo!!.packageName, activityInfo!!.name)
            val appLaunchIntent = Intent(Intent.ACTION_MAIN)
            appLaunchIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            appLaunchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or
                    activityInfo!!.flags
            appLaunchIntent.component = componentName
            return appLaunchIntent
        }

    private var _shortcuts: List<ShortcutInfo>? = ArrayList()
    var shortcuts: List<ShortcutInfo>?
        get() = _shortcuts
        set(value) {
            _shortcuts = value
        }
}
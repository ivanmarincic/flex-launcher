package io.github.twoloops.flexlauncher.homescreen.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.github.twoloops.flexlauncher.database.controllers.HomeScreenAppsController
import io.github.twoloops.flexlauncher.database.controllers.HomeScreenContentController
import io.github.twoloops.flexlauncher.database.controllers.HomeScreenDashboardController


class PackageRemovedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val packageName = intent?.data!!.schemeSpecificPart
        if (packageName == null || packageName.isEmpty()) {
            return
        }
        if (context != null) {
            val app = HomeScreenAppsController.getInstance(context).delete(packageName)
            if (app != null) {
                val content = HomeScreenContentController.getInstance(context).getByContentId(app.id)
                if (HomeScreenDashboardController.getInstance(context).deleteByContent(content) > 0) {
                    HomeScreenContentController.getInstance(context).delete(content)
                }
            }
        }
    }
}
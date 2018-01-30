package io.github.twoloops.flexlauncher.homescreen.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.github.twoloops.flexlauncher.database.controllers.HomeScreenAppsController
import io.github.twoloops.flexlauncher.homescreen.services.AppLoaderService


class PackageAddedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val packageName = intent?.data!!.schemeSpecificPart
        if (packageName == null || packageName.isBlank()) {
            return
        }
        if (context != null) {
            val app = AppLoaderService.getInstance(context).getAppByPackageName(packageName)
            HomeScreenAppsController.getInstance(context).save(app)
        }
    }
}
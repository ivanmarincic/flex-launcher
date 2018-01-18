package io.github.twoloops.flexlauncher

import android.app.WallpaperManager
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.support.annotation.RequiresApi
import android.support.v4.graphics.ColorUtils
import android.util.TypedValue

class Utils {
    companion object {
        var REQUEST_READ_STORAGE_PERMISSION: Int = 3212

        fun convertDip2Pixels(context: Context, dip: Float): Int {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, context.resources.displayMetrics).toInt()
        }

        fun hasNavBar(resources: Resources): Boolean {
            val id = resources.getIdentifier("config_showNavigationBar", "bool", "android")
            return id > 0 && resources.getBoolean(id)
        }

        fun getStatusBarHieght(resources: Resources): Int{
            var result = 0
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                result = resources.getDimensionPixelSize(resourceId)
            }
            return result
        }

        fun getNavigationBarHieght(resources: Resources): Int{
            var result = 0
            val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
            if (resourceId > 0 && hasNavBar(resources)) {
                result = resources.getDimensionPixelSize(resourceId)
            }
            return result
        }

        @RequiresApi(27)
        fun isWallpaperDark(wallpaperManager: WallpaperManager): Boolean {
            val colors = wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
            return getColorLightness(colors.primaryColor.toArgb()) < 0.5f && getColorLightness(colors.secondaryColor.toArgb()) < 0.5f && getColorLightness(colors.tertiaryColor.toArgb()) < 0.5f
        }

        fun getColorLightness(color: Int): Float {
            val red = Color.red(color)
            val green = Color.green(color)
            val blue = Color.blue(color)

            val hsl = FloatArray(3)
            ColorUtils.RGBToHSL(red, green, blue, hsl)
            return hsl[2]
        }
    }
}
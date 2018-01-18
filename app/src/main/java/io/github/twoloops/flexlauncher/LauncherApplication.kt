package io.github.twoloops.flexlauncher

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.IdRes
import android.view.View
import io.github.twoloops.flexlauncher.homescreen.views.HomeScreenView
import net.danlew.android.joda.JodaTimeAndroid

class LauncherApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        JodaTimeAndroid.init(this)
    }
}

fun <T : View> Activity.bindView(@IdRes res: Int): Lazy<T> {
    return lazy(LazyThreadSafetyMode.NONE) { findViewById<T>(res) }
}

inline fun <reified T> Activity.getPreference(name: String, defaultValue: Any): T {
    val preferences: SharedPreferences = getSharedPreferences("io.github.twoloops.flexlauncher", Context.MODE_PRIVATE)
    return when (T::class) {
        Boolean::class -> preferences.getBoolean(name, defaultValue as Boolean) as T
        Int::class -> preferences.getInt(name, defaultValue as Int) as T
        String::class -> preferences.getString(name, defaultValue as String) as T
        Float::class -> preferences.getFloat(name, defaultValue as Float) as T
        Long::class -> preferences.getLong(name, defaultValue as Long) as T
        Set::class -> preferences.getStringSet(name, defaultValue as Set<String>) as T
        else -> throw Exception("Unhandled return type")
    }
}

inline fun <reified T> Activity.setPreference(name: String, value: Any) {
    val preferences: SharedPreferences = getSharedPreferences("io.github.twoloops.flexlauncher", Context.MODE_PRIVATE)
    when (T::class) {
        Boolean::class -> preferences.edit().putBoolean(name, value as Boolean).apply()
        Int::class -> preferences.edit().putInt(name, value as Int).apply()
        String::class -> preferences.edit().putString(name, value as String).apply()
        Float::class -> preferences.edit().putFloat(name, value as Float).apply()
        Long::class -> preferences.edit().putLong(name, value as Long).apply()
        Set::class -> preferences.edit().putStringSet(name, value as Set<String>).apply()
        else -> throw Exception("Unhandled return type")
    }
}

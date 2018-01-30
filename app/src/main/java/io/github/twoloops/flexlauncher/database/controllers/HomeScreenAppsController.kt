package io.github.twoloops.flexlauncher.database.controllers

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import io.github.twoloops.flexlauncher.database.entities.App
import io.github.twoloops.flexlauncher.database.helpers.DatabaseOpenHelper
import io.github.twoloops.flexlauncher.helpers.SingletonHolder
import org.jetbrains.anko.db.*


class HomeScreenAppsController private constructor(context: Context) {

    private val helper: DatabaseOpenHelper = DatabaseOpenHelper.getInstance(context)

    fun getAll(): ArrayList<App> {
        return helper.readableDatabase
                .select(tableName)
                .orderBy("label")
                .parseList(App.getParser()) as ArrayList<App>
    }

    fun getById(id: Long): App? {
        return helper.readableDatabase
                .select(tableName)
                .whereArgs("(id = {_id})",
                        "_id" to id)
                .parseOpt(App.getParser())
    }

    fun save(app: App): App {
        return App(helper.writableDatabase
                .insert(tableName,
                        "label" to app.label,
                        "packageName" to app.packageName,
                        "activityName" to app.activityName,
                        "icon" to app.icon,
                        "intentFlags" to app.intentFlags
                ), app.label, app.icon, app.packageName, app.activityName, app.intentFlags, app.shortcuts)
    }

    fun delete(app: App): Int {
        return helper.writableDatabase
                .delete(tableName,
                        ("id = ${app.id}").toString())
    }

    fun delete(packageName: String): App? {
        val app = helper.readableDatabase
                .select(tableName)
                .whereArgs("(packageName = {_packageName})",
                        "_packageName" to packageName)
                .parseSingle(App.getParser())
        return if (delete(app) > 0) {
            app
        } else {
            null
        }
    }

    fun update(app: App) {
        helper.writableDatabase
                .update(tableName,
                        "id" to app.id,
                        "label" to app.label,
                        "icon" to app.icon,
                        "packageName" to app.packageName,
                        "activityName" to app.activityName,
                        "intentFlags" to app.intentFlags)
                .whereArgs("(packageName = {_packageName})",
                        "_packageName" to app.packageName)
                .exec()
    }

    companion object : SingletonHolder<HomeScreenAppsController, Context>(::HomeScreenAppsController) {
        const val tableName: String = "HomeScreenApps"
        fun createTable(db: SQLiteDatabase) {
            db.createTable(tableName, true,
                    "id" to INTEGER + PRIMARY_KEY + UNIQUE,
                    "label" to TEXT,
                    "icon" to INTEGER,
                    "packageName" to TEXT,
                    "activityName" to TEXT,
                    "intentFlags" to INTEGER)
        }
    }
}
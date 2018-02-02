package io.github.twoloops.flexlauncher.database.helpers

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Environment
import com.facebook.stetho.inspector.protocol.module.Database
import io.github.twoloops.flexlauncher.database.controllers.*
import io.github.twoloops.flexlauncher.helpers.SingletonHolder
import org.jetbrains.anko.db.ManagedSQLiteOpenHelper
import org.jetbrains.anko.db.dropTable
import java.io.File


class DatabaseOpenHelper private constructor(context: Context) : ManagedSQLiteOpenHelper(context, context.packageName + ".database", null, 23) {

    override fun onCreate(db: SQLiteDatabase) {
        HomeScreenDashboardController.createTable(db)
        HomeScreenContentController.createTable(db)
        HomeScreenAppsController.createTable(db)
        HomeScreenShortcutsController.createTable(db)
        HomeScreenWidgetsController.createTable(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.dropTable(HomeScreenDashboardController.tableName, true)
        HomeScreenDashboardController.createTable(db)
        db.dropTable(HomeScreenContentController.tableName, true)
        HomeScreenContentController.createTable(db)
        db.dropTable(HomeScreenAppsController.tableName, true)
        HomeScreenAppsController.createTable(db)
        db.dropTable(HomeScreenShortcutsController.tableName, true)
        HomeScreenShortcutsController.createTable(db)
        db.dropTable(HomeScreenWidgetsController.tableName, true)
        HomeScreenWidgetsController.createTable(db)
    }

    companion object : SingletonHolder<DatabaseOpenHelper, Context>(::DatabaseOpenHelper)
}

val Context.database: DatabaseOpenHelper
    get() = DatabaseOpenHelper.getInstance(applicationContext)
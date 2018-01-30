package io.github.twoloops.flexlauncher.database.controllers

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import io.github.twoloops.flexlauncher.database.entities.HomeScreenItem
import io.github.twoloops.flexlauncher.database.helpers.DatabaseOpenHelper
import io.github.twoloops.flexlauncher.helpers.SingletonHolder
import org.jetbrains.anko.db.*


class HomeScreenShortcutsController private constructor(context: Context) {

    private val helper: DatabaseOpenHelper = DatabaseOpenHelper.getInstance(context)

    fun getAll(): ArrayList<HomeScreenItem<*>> {
        return helper.writableDatabase
                .select(tableName)
                .parseList(classParser<HomeScreenItem<*>>()) as ArrayList<HomeScreenItem<*>>
    }

    fun save(item: HomeScreenItem<*>): HomeScreenItem<*> {
        return HomeScreenItem(helper.writableDatabase
                .insert(tableName,
                        "column" to item.column,
                        "columnSpan" to item.columnSpan,
                        "row" to item.row,
                        "rowSpan" to item.rowSpan
                ), item.column, item.columnSpan, item.row, item.rowSpan, item.content)
    }

    fun delete(item: HomeScreenItem<*>): Int {
        return helper.writableDatabase
                .delete(tableName,
                        ("id" to item.id).toString())
    }

    fun update(item: HomeScreenItem<*>) {
        helper.writableDatabase
                .update(tableName,
                        "id" to item.id,
                        "column" to item.column,
                        "columnSpan" to item.columnSpan,
                        "row" to item.row,
                        "rowSpan" to item.rowSpan).exec()
    }

    companion object : SingletonHolder<HomeScreenShortcutsController, Context>(::HomeScreenShortcutsController) {
        const val tableName: String = "HomeScreenShortcuts"
        fun createTable(db: SQLiteDatabase) {
            db.createTable(tableName, true,
                    "id" to INTEGER + PRIMARY_KEY + UNIQUE,
                    "type" to INTEGER,
                    "contentId" to INTEGER)
        }
    }
}
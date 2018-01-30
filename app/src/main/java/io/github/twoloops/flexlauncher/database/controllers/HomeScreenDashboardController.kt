package io.github.twoloops.flexlauncher.database.controllers

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import io.github.twoloops.flexlauncher.database.entities.Content
import io.github.twoloops.flexlauncher.database.entities.HomeScreenItem
import io.github.twoloops.flexlauncher.database.helpers.DatabaseOpenHelper
import io.github.twoloops.flexlauncher.helpers.SingletonHolder
import org.jetbrains.anko.db.*


class HomeScreenDashboardController private constructor(context: Context) {

    private val helper: DatabaseOpenHelper = DatabaseOpenHelper.getInstance(context)
    private val context = context

    fun getAll(): ArrayList<HomeScreenItem<*>> {
        return helper.readableDatabase
                .select(tableName)
                .parseList(HomeScreenItem.getParser(context)) as ArrayList<HomeScreenItem<*>>
    }

    fun save(item: HomeScreenItem<*>): HomeScreenItem<*> {
        val contentId = HomeScreenContentController.getInstance(context)
                .save(item.content.id, item.getType(item.content))
        return HomeScreenItem(helper.writableDatabase
                .insert(tableName,
                        "column" to item.column,
                        "columnSpan" to item.columnSpan,
                        "row" to item.row,
                        "rowSpan" to item.rowSpan,
                        "content" to contentId
                ), item.column, item.columnSpan, item.row, item.rowSpan, item.content)
    }

    fun delete(item: HomeScreenItem<*>): Int {
        return delete(item.id)
    }

    fun delete(id: Long): Int {
        return helper.writableDatabase
                .delete(tableName,
                        "id = $id")
    }

    fun deleteByContent(content: Content): Int {
        return helper.writableDatabase
                .delete(tableName,
                        "content = ${content.id}")
    }

    fun update(item: HomeScreenItem<*>) {
        helper.writableDatabase
                .update(tableName,
                        "column" to item.column,
                        "columnSpan" to item.columnSpan,
                        "row" to item.row,
                        "rowSpan" to item.rowSpan)
                .whereArgs("(id = {_id})",
                        "_id" to item.id)
                .exec()
    }

    companion object : SingletonHolder<HomeScreenDashboardController, Context>(::HomeScreenDashboardController) {
        const val tableName: String = "HomeScreenDashboard"
        fun createTable(db: SQLiteDatabase) {
            db.createTable(tableName, true,
                    "id" to INTEGER + PRIMARY_KEY + UNIQUE,
                    "column" to INTEGER,
                    "columnSpan" to INTEGER,
                    "row" to INTEGER,
                    "rowSpan" to INTEGER,
                    "content" to INTEGER)
        }
    }
}
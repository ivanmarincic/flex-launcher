package io.github.twoloops.flexlauncher.database.controllers

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import io.github.twoloops.flexlauncher.database.entities.Content
import io.github.twoloops.flexlauncher.database.helpers.DatabaseOpenHelper
import io.github.twoloops.flexlauncher.helpers.SingletonHolder
import org.jetbrains.anko.db.*


class HomeScreenContentController private constructor(context: Context) {

    private val helper: DatabaseOpenHelper = DatabaseOpenHelper.getInstance(context)

    fun save(contentId: Long, type: Int): Long {
        return helper.writableDatabase.insert(tableName,
                "contentId" to contentId,
                "type" to type
        )
    }

    fun getAll(): List<Content> {
        return helper.readableDatabase
                .select(tableName)
                .parseList(Content.getParser())
    }

    fun getById(id: Long): Content {
        return helper.readableDatabase
                .select(tableName)
                .whereArgs("(id = {_id})",
                        "_id" to id)
                .parseSingle(Content.getParser())
    }

    companion object : SingletonHolder<HomeScreenContentController, Context>(::HomeScreenContentController) {
        const val tableName: String = "HomeScreenContent"
        fun createTable(db: SQLiteDatabase) {
            db.createTable(tableName, true,
                    "id" to INTEGER + PRIMARY_KEY + UNIQUE,
                    "contentId" to INTEGER,
                    "type" to INTEGER)
        }
    }
}
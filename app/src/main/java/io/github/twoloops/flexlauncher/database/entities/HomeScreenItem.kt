package io.github.twoloops.flexlauncher.database.entities

import android.content.Context
import io.github.twoloops.flexlauncher.database.controllers.HomeScreenAppsController
import io.github.twoloops.flexlauncher.database.controllers.HomeScreenContentController
import io.github.twoloops.flexlauncher.database.controllers.HomeScreenDashboardController
import org.jetbrains.anko.db.RowParser
import org.jetbrains.anko.db.rowParser


class HomeScreenItem<ContentType : BaseEntity>(var id: Long, var column: Int, var columnSpan: Int, var row: Int, var rowSpan: Int, var content: ContentType) : Cloneable {

    fun getType(content: Any): Int {
        return when (content::class) {
            App::class -> 1
            else -> 0
        }
    }

    public override fun clone(): Any {
        return super.clone()
    }

    companion object {
        fun getParser(context: Context): RowParser<HomeScreenItem<*>> {
            return rowParser { id: Long, column: Int, columnSpan: Int, row: Int, rowSpan: Int, content: Long ->
                val defaultEntity = object : BaseEntity {
                    override var id: Long = 0
                }
                val contentConnection: Content = HomeScreenContentController.getInstance(context).getById(content)
                var contentObject: BaseEntity? = when (contentConnection.type) {
                    1 -> {
                        HomeScreenAppsController.getInstance(context).getById(contentConnection.contentId)
                    }
                    else -> defaultEntity
                }
                if (contentObject == null) {
                    HomeScreenDashboardController.getInstance(context).delete(id)
                    contentObject = defaultEntity
                }
                HomeScreenItem(id, column, columnSpan, row, rowSpan, contentObject)

            }
        }
    }
}
package io.github.twoloops.flexlauncher.database.entities

import org.jetbrains.anko.db.RowParser
import org.jetbrains.anko.db.rowParser


class Content(override var id: Long, var contentId: Long, var type: Int) : BaseEntity {

    companion object {
        fun getParser(): RowParser<Content> {
            return rowParser { id: Long, contentId: Long, type: Int ->
                Content(id, contentId, type)
            }
        }
    }
}
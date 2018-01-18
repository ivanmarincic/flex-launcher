package io.github.twoloops.flexlauncher.homescreen.models


class GridItem<ContentType>(row: Int, rowSpan: Int, column: Int, columnSpan: Int, content: ContentType?): Cloneable {
    private var _row: Int = row
    var row: Int
        get() = _row
        set(value) {
            _row = value
        }
    private var _rowSpan: Int = rowSpan
    var rowSpan: Int
        get() = _rowSpan
        set(value) {
            _rowSpan = value
        }
    private var _column: Int = column
    var column: Int
        get() = _column
        set(value) {
            _column = value
        }
    private var _columnSpan: Int = columnSpan
    var columnSpan: Int
        get() = _columnSpan
        set(value) {
            _columnSpan = value
        }

    private var _content: ContentType? = content
    var content: ContentType?
        get() = _content
        set(value) {
            _content = value
        }

    override public fun clone(): Any {
        return super.clone()
    }
}
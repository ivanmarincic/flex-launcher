package io.github.twoloops.flexlauncher.homescreen.contracts

import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import android.widget.Adapter
import io.github.twoloops.flexlauncher.database.entities.HomeScreenItem


interface GridAdapter : Adapter {
    fun notifyDataChanged()
    fun notifyDataInvalid()
    fun hasItemInCell(gridItem: HomeScreenItem<*>): Boolean
    fun hasItem(gridItem: HomeScreenItem<*>): Boolean
    fun addItem(gridItem: HomeScreenItem<*>): Boolean
    fun removeItem(gridItem: HomeScreenItem<*>): Boolean
    fun onLongPress(gridItem: HomeScreenItem<*>, itemView: View)
    fun onPress(gridItem: HomeScreenItem<*>, itemView: View)
    fun cancelTouchEvents(e: MotionEvent?)
    fun startDrag(gridItem: HomeScreenItem<*>, itemView: View)
    fun onDragStart(event: DragEvent, gridItem: HomeScreenItem<*>, itemView: View)
    fun onDragEnd(event: DragEvent, gridItem: HomeScreenItem<*>?, itemView: View?)
    var dragAndDropListener: DragAndDropListener?
    var touchGestureListener: TouchGestureListener?
    interface DragAndDropListener {
        fun onDragStart(event: DragEvent, gridItem: HomeScreenItem<*>, itemView: View)
        fun onDragEnd(event: DragEvent, gridItem: HomeScreenItem<*>?, itemView: View?)
    }
    interface TouchGestureListener {
        fun onLongTouch()
        fun onDown()
    }
}
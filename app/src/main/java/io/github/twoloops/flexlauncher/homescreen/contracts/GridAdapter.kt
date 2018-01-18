package io.github.twoloops.flexlauncher.homescreen.contracts

import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import android.widget.Adapter
import io.github.twoloops.flexlauncher.homescreen.models.GridItem


interface GridAdapter : Adapter {
    fun notifyDataChanged()
    fun notifyDataInvalid()
    fun hasItemInCell(gridItem: GridItem<*>): Boolean
    fun hasItem(gridItem: GridItem<*>): Boolean
    fun addItem(gridItem: GridItem<*>): Boolean
    fun removeItem(gridItem: GridItem<*>): Boolean
    fun onLongPress(gridItem: GridItem<*>, itemView: View)
    fun onPress(gridItem: GridItem<*>, itemView: View)
    fun cancelTouchEvents(e: MotionEvent?)
    fun startDrag(gridItem: GridItem<*>, itemView: View)
    fun onDragStart(event: DragEvent, gridItem: GridItem<*>, itemView: View)
    fun onDragEnd(event: DragEvent, gridItem: GridItem<*>, itemView: View)
    fun setOnDragAndDropListener(listener: DragAndDropListener)
    interface DragAndDropListener {
        fun onDragStart(event: DragEvent, gridItem: GridItem<*>, itemView: View)
        fun onDragEnd(event: DragEvent, gridItem: GridItem<*>, itemView: View)
    }
}
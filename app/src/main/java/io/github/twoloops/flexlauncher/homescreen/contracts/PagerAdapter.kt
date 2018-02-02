package io.github.twoloops.flexlauncher.homescreen.contracts

import android.view.View
import android.view.ViewGroup


interface PagerAdapter {
    fun getView(position: Int): View
    fun getCount(): Int
    fun getScrollableCount(): Int
    fun getOptionsView(): View
    fun initOptionsViewHolder(parent: ViewGroup): ViewGroup
    fun setViewForOptionsPanel(viewId: Int)
}
package io.github.twoloops.flexlauncher.homescreen.contracts

import android.view.View


interface PagerAdapter {
    fun getView(position: Int): View
    fun getCount(): Int
}
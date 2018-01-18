package io.github.twoloops.flexlauncher.homescreen.adapters

import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup


class PagerAdapter(public var grid: View, public var dashboard: View) : PagerAdapter() {

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        return if (position == 0) {
            container.addView(grid, position)
            grid
        } else {
            container.addView(dashboard, position)
            dashboard
        }
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun getCount(): Int {
        return 2
    }
}
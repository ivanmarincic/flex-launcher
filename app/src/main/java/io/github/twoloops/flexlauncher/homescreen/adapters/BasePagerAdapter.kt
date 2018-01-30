package io.github.twoloops.flexlauncher.homescreen.adapters

import android.view.View
import io.github.twoloops.flexlauncher.homescreen.contracts.PagerAdapter

class BasePagerAdapter(private var grid: View, private var dashboard: View, private var settingsPanel: View) : PagerAdapter {

    override fun getView(position: Int): View {
        return if (position == 0) {
            grid
        } else {
            dashboard
        }
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getSettingsPanel(): View {
        return settingsPanel
    }
}
package io.github.twoloops.flexlauncher.homescreen.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.github.twoloops.flexlauncher.R
import io.github.twoloops.flexlauncher.helpers.Utils
import io.github.twoloops.flexlauncher.homescreen.contracts.PagerAdapter
import io.github.twoloops.flexlauncher.settings.views.AppsSettingsFragment

class BasePagerAdapter(private val activity: Activity) : PagerAdapter {

    lateinit var grid: View
    lateinit var dashboard: View
    lateinit var optionsPanel: View
    private var optionsViewHolder: ViewGroup? = null

    override fun getView(position: Int): View {
        return if (position == 0) {
            grid
        } else {
            dashboard
        }
    }

    override fun getCount(): Int {
        return 4
    }

    override fun getScrollableCount(): Int {
        return 2
    }

    override fun getOptionsView(): View {
        return optionsPanel
    }

    override fun initOptionsViewHolder(parent: ViewGroup): ViewGroup {
        if (optionsViewHolder == null) {
            optionsViewHolder = LayoutInflater.from(activity).inflate(R.layout.homescreen_options_panel_view_child_holder, parent, false) as ViewGroup
            optionsViewHolder!!.setPadding(0, Utils.getStatusBarHieght(activity.resources), 0, Utils.getNavigationBarHieght(activity.resources))
        }
        return optionsViewHolder!!
    }

    override fun setViewForOptionsPanel(viewId: Int) {
        when (viewId) {
            VIEW_SETTINGS -> {
                activity.fragmentManager.beginTransaction()
                        .replace(R.id.homescreen_options_panel_view_child_holder_content, AppsSettingsFragment())
                        .commit()
            }
            VIEW_WALLPAPER_PICKER -> {
                LayoutInflater.from(activity).inflate(R.layout.homescreen_wallpaper_picker_view_list, optionsViewHolder, false)
            }
            VIEW_WIDGETS -> {
                LayoutInflater.from(activity).inflate(R.layout.homescreen_settings_view_list, optionsViewHolder, false)
            }
            else -> {
                View(activity)
            }
        }
    }

    companion object {
        const val VIEW_SETTINGS = 0
        const val VIEW_WALLPAPER_PICKER = 1
        const val VIEW_WIDGETS = 2
    }
}
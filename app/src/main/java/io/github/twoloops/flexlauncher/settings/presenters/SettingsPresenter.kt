package io.github.twoloops.flexlauncher.settings.presenters

import android.support.v7.widget.Toolbar
import android.util.TypedValue
import android.view.ViewGroup
import io.github.twoloops.flexlauncher.R
import io.github.twoloops.flexlauncher.settings.contracts.Settings
import io.github.twoloops.flexlauncher.settings.views.SettingsView
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.util.TypedValue.applyDimension
import android.R.attr.data
import android.support.v7.widget.TintTypedArray.obtainStyledAttributes


class SettingsPresenter : Settings.Presenter {

    lateinit var view: SettingsView

    override fun start(view: Settings.View) {
        this.view = view as SettingsView
    }

    override fun initializeSettings() {
    }

    override fun initializeActionBar() {
        view.layoutInflater.inflate(R.layout.settings_view_toolbar, view.findViewById(android.R.id.content) as ViewGroup)
        val toolbar = view.findViewById(R.id.settings_view_toolbar) as Toolbar
        toolbar.title = view.resources.getString(R.string.settings_view_title)
        view.setSupportActionBar(toolbar)
        val horizontalMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, view.resources.displayMetrics)
        val verticalMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, view.resources.displayMetrics)
        val tv = TypedValue()
        val topMargin = if (view.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            TypedValue.complexToDimensionPixelSize(tv.data, view.resources.displayMetrics)
        }else{
            0
        }
        view.listView.setPadding(horizontalMargin.toInt(), topMargin, horizontalMargin.toInt(), verticalMargin.toInt())
        view.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        view.supportActionBar!!.setDisplayShowHomeEnabled(true)
    }
}
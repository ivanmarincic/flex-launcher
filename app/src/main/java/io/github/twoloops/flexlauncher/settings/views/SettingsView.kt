package io.github.twoloops.flexlauncher.settings.views

import android.app.Fragment
import android.os.Bundle
import android.os.PersistableBundle
import android.preference.PreferenceActivity
import android.support.v7.app.AppCompatActivity
import io.github.twoloops.flexlauncher.R
import io.github.twoloops.flexlauncher.settings.contracts.Settings
import io.github.twoloops.flexlauncher.settings.presenters.SettingsPresenter


class SettingsView : AppCompatPreferenceActivity(), Settings.View {

    private lateinit var presenter: Settings.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = SettingsPresenter()
        presenter.start(this)
        presenter.initializeActionBar()
    }

    override fun onBuildHeaders(target: MutableList<Header>?) {
        super.onBuildHeaders(target)
        loadHeadersFromResource(R.xml.homescreen_settings_view_preference_headers, target)
    }

    override fun onHeaderClick(header: Header?, position: Int) {
        super.onHeaderClick(header, position)
        if (header != null) {
            supportActionBar!!.title = header.title ?: resources.getString(header.titleRes)
        } else {
            supportActionBar!!.title = resources.getString(R.string.settings_view_title)

        }
    }

    override fun isValidFragment(fragmentName: String?): Boolean {
        return when (fragmentName) {
            "io.github.twoloops.flexlauncher.settings.views.AppsSettingsFragment" -> true
            else -> false
        }
    }
}
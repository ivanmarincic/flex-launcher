package io.github.twoloops.flexlauncher.settings.views

import android.os.Bundle
import io.github.twoloops.flexlauncher.R


class AppsSettingsFragment : AppCompatPreferenceFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.homescreen_settings_view_preferences_apps)
    }

}
package io.github.twoloops.flexlauncher.settings.contracts


class Settings {
    interface View {
    }

    interface Presenter {
        fun start(view: View)
        fun initializeSettings()
        fun initializeActionBar()
    }
}
package io.github.twoloops.flexlauncher.settings.views

import android.os.Bundle
import android.preference.PreferenceFragment
import android.util.TypedValue
import android.view.View


abstract class AppCompatPreferenceFragment : PreferenceFragment() {

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val horizontalMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, activity.resources.displayMetrics)
        val verticalMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, activity.resources.displayMetrics)
        val topMargin = (activity as AppCompatPreferenceActivity).supportActionBar!!.height
        view!!.setPadding(horizontalMargin.toInt(), topMargin, horizontalMargin.toInt(), verticalMargin.toInt())
    }

}
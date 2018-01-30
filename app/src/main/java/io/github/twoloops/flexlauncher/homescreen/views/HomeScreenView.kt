package io.github.twoloops.flexlauncher.homescreen.views

import android.app.WallpaperManager
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import android.widget.Toast.makeText
import com.facebook.stetho.Stetho
import io.github.twoloops.flexlauncher.R
import io.github.twoloops.flexlauncher.bindView
import io.github.twoloops.flexlauncher.helpers.Utils
import io.github.twoloops.flexlauncher.homescreen.contracts.HomeScreen
import io.github.twoloops.flexlauncher.homescreen.presenters.HomeScreenPresenter


class HomeScreenView : AppCompatActivity(), HomeScreen.View {

    private lateinit var presenter: HomeScreenPresenter

    private val backgroundView by bindView<ImageView>(R.id.homescreen_view_background)
    val dashboardActions by bindView<LinearLayout>(R.id.homescreen_view_dashboard_actions)
    val pager by bindView<Pager>(R.id.homescreen_view_pager)
    val widgetsPanel by bindView<FrameLayout>(R.id.homescreen_view_widgets_list)
    var appGrid: Grid? = null
    var dashboardGrid: Grid? = null
    val appWidgetManager: AppWidgetManager by lazy(LazyThreadSafetyMode.NONE) {
        AppWidgetManager.getInstance(this)
    }
    val appWidgetHost: AppWidgetHost by lazy(LazyThreadSafetyMode.NONE) {
        AppWidgetHost(this, R.id.homescreen_view_widgets_list)
    }
    private var pagerInitialized = false

    private val wallpaperManager: WallpaperManager by lazy {
        WallpaperManager.getInstance(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.requestFeature(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            }
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homescreen_view)
        Stetho.initializeWithDefaults(this);
        presenter = HomeScreenPresenter()
        presenter.start(this)
        presenter.initializeBackground(backgroundView, wallpaperManager)
        presenter.initializePager(
                pager,
                presenter.initializeGrid(pager, presenter.getItemsForApps()),
                presenter.initializeDashboard(pager, presenter.getItemsForDashboard()),
                presenter.initializeSettingsPanel(pager),
                wallpaperManager)
        pagerInitialized = true
        presenter.initializeDragging()
        presenter.initializeDraggingActions()
        presenter.initializeWidgetsPanel()
    }

    override fun onStart() {
        super.onStart()
        appWidgetHost.startListening()
    }

    override fun onStop() {
        super.onStop()
        appWidgetHost.stopListening()
    }

    override fun onResume() {
        super.onResume()
        overridePendingTransition(0, 0)
        if (pager.currentPage == 0) {
            if (pagerInitialized) {
                pager.resume()
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val isDark = if (Build.VERSION.SDK_INT >= 27) {
                    Utils.isWallpaperDark(wallpaperManager)
                } else {
                    false
                }
                if (!isDark) {
                    pager.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                pager.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            }
        }
    }

    override fun onBackPressed() {
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            Utils.REQUEST_READ_STORAGE_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    presenter.initializeBackground(backgroundView, wallpaperManager)
                } else {
                    makeText(this, "Cant set wallpaper without storage permission", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                Utils.REQUEST_ADD_WIDGET, Utils.REQUEST_BIND_WIDGET -> {
                    if (data != null) {
                        val appWidgetId = data.extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                        if (appWidgetId != -1) {
                            presenter.addWidget(appWidgetManager!!.getAppWidgetInfo(appWidgetId), appWidgetId)
                        }
                    }
                }
            }
        }
    }
}

package io.github.timtim17.android.dockboard

import android.animation.ValueAnimator
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.widget.toast
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private val mHideHandler = Handler()
    private val mHideRunnable = Runnable { hideNav() }
    private val mHidePart2Runnable = Runnable {
        fullscreen_content.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LOW_PROFILE or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }
    private val mShowPart2Runnable = Runnable {
        supportActionBar?.show()
    }
    private var mNavVisible: Boolean = false

    private var mWidgetsVisible: Boolean = false
    private var sharedPreferences: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        mNavVisible = true

        // Set up the user interaction to manually showNav or hideNav the system UI.
        fullscreen_content.setOnClickListener { toggleNavVisibility() }

        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        mWidgetsVisible = sharedPreferences?.getBoolean(SHARED_PREF_WIDGET_VIS, false) ?: false
        updateWidgetVisibility()

        val colors = resources.getIntArray(R.array.rainbowColors)
        val bgAnimator = ValueAnimator.ofArgb(*colors)
        bgAnimator.duration = 30000
        bgAnimator.addUpdateListener { animator -> fullscreen_content.setBackgroundColor(animator.animatedValue as Int) }
        bgAnimator.repeatCount = ValueAnimator.INFINITE
        bgAnimator.start()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hideNav() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHideNav(100)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_settings -> toast("show settings", Toast.LENGTH_LONG)
            R.id.action_toggle_widgets -> {
                mWidgetsVisible = !mWidgetsVisible
                updateWidgetVisibility()
                sharedPreferences?.edit {
                    putBoolean(SHARED_PREF_WIDGET_VIS, mWidgetsVisible)
                }
            }
            else -> return false
        }
        return true
    }

    private fun toggleNavVisibility() {
        if (mNavVisible) {
            hideNav()
        } else {
            showNav()
        }
    }

    private fun hideNav() {
        // Hide UI first
        supportActionBar?.hide()
        mNavVisible = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable)
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun showNav() {
        // Show the system bar
        fullscreen_content.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        mNavVisible = true

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable)
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY.toLong())

        delayedHideNav(AUTO_HIDE_DELAY_MILLIS)
    }

    /**
     * Schedules a call to hideNav() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHideNav(delayMillis: Int) {
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.postDelayed(mHideRunnable, delayMillis.toLong())
    }

    private fun updateWidgetVisibility() {
        val widgetVisibility = if (mWidgetsVisible) View.VISIBLE else View.GONE
        divider.visibility = widgetVisibility
        widget_parent.visibility = widgetVisibility
    }

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private val UI_ANIMATION_DELAY = 300

        private val SHARED_PREF_NAME = "DOCKBOARD_PREFS"
        private val SHARED_PREF_WIDGET_VIS = "WIDGET_VIS"
    }
}

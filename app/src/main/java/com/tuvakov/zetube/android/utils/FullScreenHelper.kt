package com.tuvakov.zetube.android.utils

import android.app.Activity
import android.view.View

class FullScreenHelper(private val context: Activity) {
    fun enterFullScreen() {
        val decorView = context.window.decorView
        hideSystemUi(decorView)
    }

    fun exitFullScreen() {
        val decorView = context.window.decorView
        showSystemUi(decorView)
    }

    private fun hideSystemUi(mDecorView: View) {
        mDecorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    private fun showSystemUi(mDecorView: View) {
        mDecorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    }
}
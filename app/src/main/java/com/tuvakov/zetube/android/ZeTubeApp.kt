package com.tuvakov.zetube.android

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class ZeTubeApp : Application() {

    override fun onCreate() {
        super.onCreate()
        setupTimber()
    }

    private fun setupTimber() {
        val tree = object : Timber.DebugTree() {
            override fun log(
                priority: Int,
                tag: String?,
                message: String,
                t: Throwable?
            ) {
                super.log(priority, "${this@ZeTubeApp::class.simpleName}: $tag", message, t)
            }
        }
        Timber.plant(tree)
    }
}
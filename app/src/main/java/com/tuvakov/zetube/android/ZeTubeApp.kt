package com.tuvakov.zetube.android

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen
import com.tuvakov.zetube.android.dagger.AppComponent
import com.tuvakov.zetube.android.dagger.AppModule
import com.tuvakov.zetube.android.dagger.DaggerAppComponent

class ZeTubeApp : Application() {
    lateinit var appComponent: AppComponent
        private set

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        appComponent = DaggerAppComponent.factory().create(AppModule(this))
    }
}
package com.tuvakov.zetube.android.dagger

import dagger.Module
import dagger.Provides
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Singleton

@Module
internal object ExecutorModule {
    @JvmStatic
    @Provides
    @Singleton
    fun providesSingleThreadExecutor(): ExecutorService {
        return Executors.newSingleThreadExecutor()
    }
}
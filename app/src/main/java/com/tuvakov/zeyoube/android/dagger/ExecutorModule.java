package com.tuvakov.zeyoube.android.dagger;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
abstract class ExecutorModule {
    @Provides @Singleton
    static Executor providesSingleThreadExecutor() {
        return Executors.newSingleThreadExecutor();
    }
}
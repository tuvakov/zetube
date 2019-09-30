package com.tuvakov.zeyoube.android.utils;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AppExecutors {

    private final Executor diskIO;
    private final Executor networkIO;

    private static AppExecutors ourInstance;

    private AppExecutors(Executor diskIO, Executor networkIO) {
        this.diskIO = diskIO;
        this.networkIO = networkIO;
    }

    public static synchronized AppExecutors getInstance() {

        if (ourInstance == null) {
            ourInstance = new AppExecutors(
                    Executors.newSingleThreadExecutor(),
                    Executors.newFixedThreadPool(3)
            );
        }

        return ourInstance;
    }

    public Executor getDiskIO() {
        return diskIO;
    }

    public Executor getNetworkIO() {
        return networkIO;
    }
}

package com.tuvakov.zeyoube.android.dagger;

import com.tuvakov.zeyoube.android.MainActivity;
import com.tuvakov.zeyoube.android.VideoFeedScratchSyncService;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {AppModule.class, RoomModule.class, ExecutorModule.class})
public interface AppComponent {

    void injectMainActivityFields(MainActivity mainActivity);

    void injectVideoFeedScratchSyncService(VideoFeedScratchSyncService service);

    @Component.Factory
    interface Factory {
        AppComponent create(AppModule appModule);
    }
}

package com.tuvakov.zetube.android.dagger;

import com.tuvakov.zetube.android.VideoFeedSyncService;
import com.tuvakov.zetube.android.ui.feed.VideoFeedActivity;
import com.tuvakov.zetube.android.ui.player.PlayerActivity;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {AppModule.class, RoomModule.class, ExecutorModule.class})
public interface AppComponent {

    void injectVideoFeedActivityFields(VideoFeedActivity videoFeedActivity);

    void injectPlayerActivityFields(PlayerActivity playerActivity);

    void injectVideoFeedSyncService(VideoFeedSyncService service);

    @Component.Factory
    interface Factory {
        AppComponent create(AppModule appModule);
    }
}

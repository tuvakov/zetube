package com.tuvakov.zetube.android.dagger

import com.tuvakov.zetube.android.ui.channels.ChannelsActivity
import com.tuvakov.zetube.android.ui.feed.VideoFeedActivity
import com.tuvakov.zetube.android.ui.player.PlayerActivity
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, RoomModule::class])
interface AppComponent {
    fun injectVideoFeedActivityFields(videoFeedActivity: VideoFeedActivity)
    fun injectPlayerActivityFields(playerActivity: PlayerActivity)
    fun injectChannelsActivity(channelsActivity: ChannelsActivity)

    @Component.Factory
    interface Factory {
        fun create(appModule: AppModule): AppComponent
    }
}
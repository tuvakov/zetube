package com.tuvakov.zetube.android

import android.app.IntentService
import android.content.Intent
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.tuvakov.zetube.android.data.SyncStatus
import com.tuvakov.zetube.android.repository.SubscriptionRepo
import com.tuvakov.zetube.android.repository.VideoRepo
import com.tuvakov.zetube.android.utils.DateTimeUtils
import com.tuvakov.zetube.android.utils.PrefUtils
import com.tuvakov.zetube.android.utils.SyncUtils
import com.tuvakov.zetube.android.utils.YouTubeApiUtils
import com.tuvakov.zetube.android.utils.YouTubeApiUtils.PlaylistItemComparator
import javax.inject.Inject

class VideoFeedSyncService : IntentService(TAG) {

    @Inject
    lateinit var mYouTubeUtils: YouTubeApiUtils
    @Inject
    lateinit var mPrefUtils: PrefUtils
    @Inject
    lateinit var mSyncUtils: SyncUtils
    @Inject
    lateinit var mVideoRepo: VideoRepo
    @Inject
    lateinit var mSubscriptionRepo: SubscriptionRepo
    @Inject
    lateinit var mDateTimeUtils: DateTimeUtils

    override fun onCreate() {
        super.onCreate()
        (application as ZeTubeApp).appComponent.injectVideoFeedSyncService(this)
    }

    override fun onHandleIntent(intent: Intent?) {
        if (!mDateTimeUtils.hasDayPassed(mPrefUtils.lastSyncTime)) {
            Log.d(TAG, "onHandleIntent: Immature sync")
            STATUS.postValue(SyncStatus(STATUS_IMMATURE_SYNC))
            return
        }

        val youTubeService = mYouTubeUtils.youTubeService
        if (youTubeService == null) {
            Log.d(TAG, "onHandleIntent: Couldn't get service. Probably account name is null")
            STATUS.postValue(SyncStatus(STATUS_SYNC_FAILURE))
            return
        }

        try {
            STATUS.postValue(SyncStatus(STATUS_SYNC_STARTED))
            val startingDay = mDateTimeUtils.getUtcEpochNDaysAgo(LAST_DAY_NO)
            val subscriptions = mSyncUtils.getSubscriptions(youTubeService)
            val videos = mSyncUtils.getVideos(
                    youTubeService,
                    subscriptions,
                    startingDay,
                    PlaylistItemComparator()
            )
            mSubscriptionRepo.deleteAllForService()
            // TODO: Hidden and saved videos should be excluded later.
            mVideoRepo.deleteAllForService()
            mSubscriptionRepo.bulkInsertForService(subscriptions)
            mVideoRepo.bulkInsertForService(videos)
            mPrefUtils.saveLastSyncTime(mDateTimeUtils.utcEpoch)
        } catch (e: GooglePlayServicesAvailabilityIOException) {
            Log.d(TAG, "onHandleIntent: GooglePlayServicesAvailabilityIOException")
            STATUS.postValue(SyncStatus(STATUS_SYNC_GOOGLE_PLAY_FAILURE, e))
            return
        } catch (e: UserRecoverableAuthIOException) {
            Log.d(TAG, "onHandleIntent: UserRecoverableAuthIOException")
            STATUS.postValue(SyncStatus(STATUS_SYNC_AUTH_FAILURE, e))
            return
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(TAG, "onHandleIntent: Exception " + e.message)
            STATUS.postValue(SyncStatus(STATUS_SYNC_FAILURE))
            return
        }
        STATUS.postValue(SyncStatus(STATUS_SYNC_SUCCESS))
    }

    companion object {
        private const val TAG = "ScratchSyncService"
        const val STATUS_SYNC_IDLE = 0
        const val STATUS_SYNC_STARTED = 10
        const val STATUS_SYNC_SUCCESS = 11
        const val STATUS_SYNC_FAILURE = 12
        const val STATUS_SYNC_GOOGLE_PLAY_FAILURE = 13
        const val STATUS_SYNC_AUTH_FAILURE = 14
        const val STATUS_IMMATURE_SYNC = 20
        val STATUS = MutableLiveData<SyncStatus>()
        private const val LAST_DAY_NO = 7
    }
}
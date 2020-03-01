package com.tuvakov.zeyoube.android;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.youtube.YouTube;
import com.tuvakov.zeyoube.android.data.Subscription;
import com.tuvakov.zeyoube.android.data.SyncStatus;
import com.tuvakov.zeyoube.android.data.Video;
import com.tuvakov.zeyoube.android.repository.SubscriptionRepo;
import com.tuvakov.zeyoube.android.repository.VideoRepo;
import com.tuvakov.zeyoube.android.utils.DateTimeUtils;
import com.tuvakov.zeyoube.android.utils.PrefUtils;
import com.tuvakov.zeyoube.android.utils.SyncUtils;
import com.tuvakov.zeyoube.android.utils.YouTubeApiUtils;

import java.util.List;

import javax.inject.Inject;

public class VideoFeedSyncService extends IntentService {

    private static final String TAG = "ScratchSyncService";

    public static final int STATUS_SYNC_IDLE = 0;
    public static final int STATUS_SYNC_STARTED = 10;
    public static final int STATUS_SYNC_SUCCESS = 11;
    public static final int STATUS_SYNC_FAILURE = 12;
    public static final int STATUS_SYNC_GOOGLE_PLAY_FAILURE = 13;
    public static final int STATUS_SYNC_AUTH_FAILURE = 14;
    public static final int STATUS_IMMATURE_SYNC = 20;

    public static final MutableLiveData<SyncStatus> STATUS = new MutableLiveData<>();
    private static final int LAST_DAY_NO = 7;

    @Inject
    YouTubeApiUtils mYouTubeUtils;
    @Inject
    PrefUtils mPrefUtils;
    @Inject
    SyncUtils mSyncUtils;
    @Inject
    VideoRepo mVideoRepo;
    @Inject
    SubscriptionRepo mSubscriptionRepo;
    @Inject
    DateTimeUtils mDateTimeUtils;

    public VideoFeedSyncService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ((ZeYouBe) getApplication()).getAppComponent().injectVideoFeedSyncService(this);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        if (!mDateTimeUtils.hasDayPassed(mPrefUtils.getLastSyncTime())) {
            Log.d(TAG, "onHandleIntent: Immature sync");
            STATUS.postValue(new SyncStatus(STATUS_IMMATURE_SYNC));
            return;
        }

        YouTube youTubeService = mYouTubeUtils.getYouTubeService();

        if (youTubeService == null) {
            Log.d(TAG, "onHandleIntent: Couldn't get service. Probably account name is null");
            STATUS.postValue(new SyncStatus(STATUS_SYNC_FAILURE));
            return;
        }

        try {
            STATUS.postValue(new SyncStatus(STATUS_SYNC_STARTED));
            long startingDay = mDateTimeUtils.getUtcEpochNDaysAgo(LAST_DAY_NO);

            List<Subscription> subscriptions = mSyncUtils.getSubscriptions(youTubeService);
            List<Video> videos = mSyncUtils.getVideos(
                    youTubeService,
                    subscriptions,
                    startingDay,
                    new YouTubeApiUtils.PlaylistItemComparator()
            );

            mSubscriptionRepo.deleteAllForService();
            // TODO: Hidden and saved videos should be excluded later.
            mVideoRepo.deleteAllForService();

            mSubscriptionRepo.bulkInsertForService(subscriptions);
            mVideoRepo.bulkInsertForService(videos);

            mPrefUtils.saveLastSyncTime(mDateTimeUtils.getUtcEpoch());

        } catch (GooglePlayServicesAvailabilityIOException e) {
            Log.d(TAG, "onHandleIntent: GooglePlayServicesAvailabilityIOException");
            STATUS.postValue(new SyncStatus(STATUS_SYNC_GOOGLE_PLAY_FAILURE, e));
            return;
        } catch (UserRecoverableAuthIOException e) {
            Log.d(TAG, "onHandleIntent: UserRecoverableAuthIOException");
            STATUS.postValue(new SyncStatus(STATUS_SYNC_AUTH_FAILURE, e));
            return;
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "onHandleIntent: Exception " + e.getMessage());
            STATUS.postValue(new SyncStatus(STATUS_SYNC_FAILURE));
            return;
        }

        STATUS.postValue(new SyncStatus(STATUS_SYNC_SUCCESS));
    }
}

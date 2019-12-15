package com.tuvakov.zeyoube.android;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.google.api.services.youtube.model.SubscriptionListResponse;
import com.google.api.services.youtube.model.SubscriptionSnippet;
import com.tuvakov.zeyoube.android.data.Subscription;
import com.tuvakov.zeyoube.android.data.Video;
import com.tuvakov.zeyoube.android.repository.SubscriptionRepo;
import com.tuvakov.zeyoube.android.repository.VideoRepo;
import com.tuvakov.zeyoube.android.utils.PrefUtils;
import com.tuvakov.zeyoube.android.utils.YouTubeApiUtils;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneOffset;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;


public class VideoFeedScratchSyncService extends IntentService {

    private static final String TAG = "ScratchSyncService";

    public static final int STATUS_SYNC_STARTED = 10;
    public static final int STATUS_SYNC_SUCCESS = 11;
    public static final int STATUS_SYNC_FAILURE = 12;
    public static final int STATUS_NOT_SCRATCH_SYNC = 20;

    public static final MutableLiveData<Integer> STATUS = new MutableLiveData<>();

    private static final long MAX_LIMIT_SUBS = 50;
    private static final long MAX_LIMIT_VIDEOS = 15;
    private static final long LAST_DAY_NO = 7;

    private YouTube mYouTubeService;

    @Inject
    PrefUtils mPrefUtils;
    @Inject
    YouTubeApiUtils mYouTubeApiUtils;
    @Inject
    VideoRepo mVideoRepo;
    @Inject
    SubscriptionRepo mSubscriptionRepo;

    public VideoFeedScratchSyncService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ((ZeYouBe) getApplication()).getAppComponent().injectVideoFeedScratchSyncService(this);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        if (!mPrefUtils.isFromScratchSync()) {
            Log.d(TAG, "onHandleIntent: It is not a scratch sync");
            STATUS.postValue(STATUS_NOT_SCRATCH_SYNC);
            return;
        }

        mYouTubeService = mYouTubeApiUtils.getYouTubeService();

        if (mYouTubeService == null) {
            Log.d(TAG, "onHandleIntent: Couldn't get service. Probably account name is null");
            STATUS.postValue(STATUS_SYNC_FAILURE);
            return;
        }

        try {

            STATUS.postValue(STATUS_SYNC_STARTED);

            List<Subscription> subscriptions = getSubscriptions();
            List<Video> videos = getVideos(subscriptions);

            Log.d(TAG, "onHandleIntent: Subscriptions size = " + subscriptions.size());
            Log.d(TAG, "onHandleIntent: videos size = " + videos.size());

            mSubscriptionRepo.bulkInsertForService(subscriptions);
            mVideoRepo.bulkInsertForService(videos);

            mPrefUtils.setIsFromScratchSync(false);

        } catch (GooglePlayServicesAvailabilityIOException e) {
            Log.d(TAG, "onHandleIntent: GooglePlayServicesAvailabilityIOException");
            STATUS.postValue(STATUS_SYNC_FAILURE);
            return;
        } catch (UserRecoverableAuthIOException e) {
            Log.d(TAG, "onHandleIntent: UserRecoverableAuthIOException");
            STATUS.postValue(STATUS_SYNC_FAILURE);
            return;
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "onHandleIntent: Exception " + e.getMessage());
            STATUS.postValue(STATUS_SYNC_FAILURE);
            return;
        }

        STATUS.postValue(STATUS_SYNC_SUCCESS);
    }

    private List<Subscription> getSubscriptions() throws IOException {

        List<Subscription> subscriptions = new ArrayList<>();

        YouTube.Subscriptions.List list = mYouTubeService.subscriptions()
                .list("snippet")
                .setMine(true)
                .setMaxResults(MAX_LIMIT_SUBS)
                .setOrder("alphabetical");

        SubscriptionListResponse subResponse = list.execute();
        int totalResults = subResponse.getPageInfo().getTotalResults();

        mapSubscriptions(subResponse, subscriptions);

        /*
         * If all of the fetched subscriptions were added then there might be more in the next
         * page.
         */
        while (subscriptions.size() < totalResults) {
            list.setPageToken(subResponse.getNextPageToken());
            subResponse = list.execute();
            mapSubscriptions(subResponse, subscriptions);
        }

        return subscriptions;
    }

    private List<Video> getVideos(List<Subscription> subscriptions) throws IOException {

        if (subscriptions == null) {
            return new ArrayList<>();
        }

        LocalDateTime now = LocalDateTime.now();
        long startingDate = now.minusDays(LAST_DAY_NO).toInstant(ZoneOffset.UTC).toEpochMilli();

        int i = 0;

        List<Video> videos = new ArrayList<>();
        for (Subscription subscription : subscriptions) {

            String uploadPlayListId =
                    subscription.getChannelId().replaceFirst("[C]", "U");

            YouTube.PlaylistItems.List list = mYouTubeService.playlistItems()
                    .list("snippet")
                    .setMaxResults(MAX_LIMIT_VIDEOS)
                    .setPlaylistId(uploadPlayListId);

            PlaylistItemListResponse response = list.execute();

            List<PlaylistItem> items = response.getItems();
            // Originally the list is not sorted by time
            Collections.sort(items, new YouTubeApiUtils.PlaylistItemComparator());

            int added = mapVideos(items, subscription, videos, startingDate);
            Log.d(TAG, "getVideoSyncTask: " + ++i + ". " + subscription.getTitle() +
                    ": " + added);

            /* If all of the fetched videos were added then there might be more in the next page */
            while (added == MAX_LIMIT_VIDEOS) {
                list.setPageToken(response.getNextPageToken());
                response = list.execute();
                items = response.getItems();
                Collections.sort(items, new YouTubeApiUtils.PlaylistItemComparator());
                added = mapVideos(items, subscription, videos, startingDate);
                Log.d(TAG, "getVideoSyncTask: " + ++i + ". " + subscription.getTitle() +
                        ": " + added);
            }
        }
        return videos;
    }

    private void mapSubscriptions(SubscriptionListResponse response,
                                  List<Subscription> subscriptions) {
        for (com.google.api.services.youtube.model.Subscription sub : response.getItems()) {
            SubscriptionSnippet snippet = sub.getSnippet();
            subscriptions.add(new Subscription(
                    snippet.getResourceId().getChannelId(),
                    snippet.getTitle(),
                    snippet.getDescription(),
                    snippet.getThumbnails().getMedium().getUrl(),
                    sub.getEtag())
            );
        }
    }

    private int mapVideos(List<PlaylistItem> playlistItems, Subscription subscription,
                          List<Video> videos, long startingDate) {
        int counter = 0;

        for (PlaylistItem item : playlistItems) {
            PlaylistItemSnippet snippet = item.getSnippet();
            if (snippet.getPublishedAt().getValue() < startingDate) {
                break;
            }
            videos.add(new Video(
                    snippet.getTitle(),
                    snippet.getThumbnails().getHigh().getUrl(),
                    snippet.getDescription(),
                    subscription.getTitle(),
                    subscription.getThumbnail(),
                    snippet.getResourceId().getVideoId(),
                    false,
                    snippet.getPublishedAt().getValue())
            );
            counter++;
        }
        return counter;
    }

    /* For debugging purposes */
    private void writeFileOnInternalStorage(Context mcoContext, String sFileName, String sBody){
        File file = new File(mcoContext.getFilesDir(),"videos-request-dump");
        if(!file.exists()){
            file.mkdir();
        }

        try{
            File gpxfile = new File(file, sFileName);
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody);
            writer.flush();
            writer.close();

        }catch (Exception e){
            e.printStackTrace();

        }
    }
}

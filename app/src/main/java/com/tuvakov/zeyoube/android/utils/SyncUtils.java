package com.tuvakov.zeyoube.android.utils;

import android.util.Log;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.google.api.services.youtube.model.SubscriptionListResponse;
import com.google.api.services.youtube.model.SubscriptionSnippet;
import com.tuvakov.zeyoube.android.data.Subscription;
import com.tuvakov.zeyoube.android.data.Video;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SyncUtils {

    private static final String TAG = "SyncUtils";

    private static final long MAX_LIMIT_SUBS = 50;
    private static final long MAX_LIMIT_VIDEOS = 15;


    @Inject
    public SyncUtils() { }

    public List<Subscription> getSubscriptions(YouTube youTubeService) throws IOException {

        List<Subscription> subscriptions = new ArrayList<>();

        YouTube.Subscriptions.List list = youTubeService.subscriptions()
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

    public List<Video> getVideos(YouTube youTubeService,
                                 List<Subscription> subscriptions,
                                 long startingDay,
                                 YouTubeApiUtils.PlaylistItemComparator comparator) throws IOException {

        if (subscriptions == null) { return new ArrayList<>(); }

        int i = 0;

        List<Video> videos = new ArrayList<>();
        for (Subscription subscription : subscriptions) {

            String uploadPlayListId =
                    subscription.getChannelId().replaceFirst("[C]", "U");

            YouTube.PlaylistItems.List list = youTubeService.playlistItems()
                    .list("snippet")
                    .setMaxResults(MAX_LIMIT_VIDEOS)
                    .setPlaylistId(uploadPlayListId);

            PlaylistItemListResponse response = list.execute();

            List<PlaylistItem> items = response.getItems();

            // Originally the list is not sorted by timestamp java.util.ArrayList;
            Collections.sort(items, comparator);


            int added = mapVideos(items, subscription, videos, startingDay);
            Log.d(TAG, "getVideoSyncTask: " + ++i + ". " + subscription.getTitle() +
                    ": " + added);

            /* If all of the fetched videos were added then there might be more in the next page */
            while (added == MAX_LIMIT_VIDEOS) {
                list.setPageToken(response.getNextPageToken());
                response = list.execute();
                items = response.getItems();
                Collections.sort(items, comparator);
                added = mapVideos(items, subscription, videos, startingDay);
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
            if (snippet.getPublishedAt().getValue() < startingDate) break;
            videos.add(new Video(
                    snippet.getTitle(),
                    snippet.getThumbnails().getHigh().getUrl(),
                    snippet.getDescription(),
                    subscription.getChannelId(),
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
}
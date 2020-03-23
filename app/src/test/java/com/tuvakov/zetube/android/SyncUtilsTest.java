package com.tuvakov.zetube.android;

import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.tuvakov.zetube.android.data.Subscription;
import com.tuvakov.zetube.android.utils.DateTimeUtils;
import com.tuvakov.zetube.android.utils.SyncUtils;
import com.tuvakov.zetube.android.utils.YouTubeApiUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class SyncUtilsTest {

    private DateTimeUtils dateTimeUtils = new DateTimeUtils();

    private SyncUtils mSyncUtils;
    private YouTubeApiUtils.PlaylistItemComparator comparator =
            new YouTubeApiUtils.PlaylistItemComparator();

    @Before
    public void before() {
        mSyncUtils = new SyncUtils();
    }

    @Test
    public void sortPlaylistItems_unsortedList() {
        long oneDay = dateTimeUtils.getUtcEpochNDaysAgo(1);
        long twoDays = dateTimeUtils.getUtcEpochNDaysAgo(2);
        long threeDays = dateTimeUtils.getUtcEpochNDaysAgo(3);
        long fourDays = dateTimeUtils.getUtcEpochNDaysAgo(4);

        List<PlaylistItem> items = Arrays.asList(
                getPlaylistItem(twoDays),
                getPlaylistItem(threeDays),
                getPlaylistItem(fourDays),
                getPlaylistItem(oneDay)
        );

        assertFalse(isSorted(items));
        Collections.sort(items, comparator);
        assertTrue(isSorted(items));

        assertEquals(oneDay, items.get(0).getSnippet().getPublishedAt().getValue());
        assertEquals(twoDays, items.get(1).getSnippet().getPublishedAt().getValue());
        assertEquals(threeDays, items.get(2).getSnippet().getPublishedAt().getValue());
        assertEquals(fourDays, items.get(3).getSnippet().getPublishedAt().getValue());
    }

    @Test
    public void sortPlaylistItems_sortedList() {
        long oneDay = dateTimeUtils.getUtcEpochNDaysAgo(1);
        long twoDays = dateTimeUtils.getUtcEpochNDaysAgo(2);
        long threeDays = dateTimeUtils.getUtcEpochNDaysAgo(3);
        long fourDays = dateTimeUtils.getUtcEpochNDaysAgo(4);

        List<PlaylistItem> items = Arrays.asList(
                getPlaylistItem(oneDay),
                getPlaylistItem(twoDays),
                getPlaylistItem(threeDays),
                getPlaylistItem(fourDays)
        );

        assertTrue(isSorted(items));
        Collections.sort(items, comparator);
        assertTrue(isSorted(items));

        assertEquals(oneDay, items.get(0).getSnippet().getPublishedAt().getValue());
        assertEquals(twoDays, items.get(1).getSnippet().getPublishedAt().getValue());
        assertEquals(threeDays, items.get(2).getSnippet().getPublishedAt().getValue());
        assertEquals(fourDays, items.get(3).getSnippet().getPublishedAt().getValue());
    }

    @Test
    public void sortPlaylistItems_randomList() {

        List<PlaylistItem> items = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < 15; i++) {
            long time = dateTimeUtils.getUtcEpochNDaysAgo(random.nextInt(30));
            time -= random.nextLong();
            System.out.println("random.nextLong() = " + random.nextLong());
            items.add(getPlaylistItem(time));
        }

        assertFalse(isSorted(items));
        Collections.sort(items, comparator);
        assertTrue(isSorted(items));
    }

    private boolean isSorted(List<PlaylistItem> items) {
        for (int i = 0; i < items.size() - 1; i++) {
            long first = items.get(i).getSnippet().getPublishedAt().getValue();
            long second = items.get(i + 1).getSnippet().getPublishedAt().getValue();
            if (first < second) return false;
        }
        return true;
    }

    private PlaylistItem getPlaylistItem(long time) {
        PlaylistItemSnippet snippet = new PlaylistItemSnippet();
        DateTime dateTime = new DateTime(time);
        snippet.setPublishedAt(dateTime);
        PlaylistItem item = new PlaylistItem();
        item.setSnippet(snippet);
        return item;
    }

    private List<Subscription> getSubscriptions() {

        Subscription adorama = new Subscription(
                "UC6T6XySnZS2Gghs7xQk8yZg",
                "AdoramaTv",
                "A channel about photography",
                "https://yt3.ggpht.com/-1Y8x3MKEQJA/AAAAAAAAAAI/AAAAAAAAAAA/QIjFzrRKiHs"
        );

        Subscription threeBrownOneBlue = new Subscription(
                "UCYO_jab_esuFRV4b17AJtAw",
                "3Brown1Blue",
                "A channel about numbers",
                "https://yt3.ggpht.com/-1Y8x3MKEQJA/AAAAAAAAAAI/AAAAAAAAAAA/QIjFzrRKiHs"
        );

        Subscription tasty = new Subscription(
                "UCJFp8uSYCjXOMnkUyb3CQ3Q",
                "Tasty",
                "A channel about cooking",
                "https://yt3.ggpht.com/-1Y8x3MKEQJA/AAAAAAAAAAI/AAAAAAAAAAA/QIjFzrRKiHs"
        );
        return Arrays.asList(adorama, threeBrownOneBlue, tasty);
    }
}

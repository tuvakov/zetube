package com.tuvakov.zeyoube.android.utils;

import android.content.Context;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.PlaylistItem;
import com.tuvakov.zeyoube.android.R;

import java.util.Arrays;
import java.util.Comparator;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class YouTubeApiUtils {

    private static final String TAG = "YouTubeApiUtils";
    private static final String[] SCOPES = { YouTubeScopes.YOUTUBE_READONLY };

    private final Context mContext;
    private final PrefUtils mPrefUtils;

    @Inject
    YouTubeApiUtils(Context appContext, PrefUtils prefUtils) {
        mContext = appContext;
        mPrefUtils = prefUtils;
    }

    public YouTube getYouTubeService() {

        String accountName = mPrefUtils.getAccountName();
        if (accountName == null) {
            Log.d(TAG, "Account name is null");
            return null;
        }

        GoogleAccountCredential credential = getGoogleCredential();
        credential.setSelectedAccountName(accountName);

        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        return new YouTube.Builder(transport, jsonFactory, credential)
                .setApplicationName(mContext.getString(R.string.app_name))
                .build();
    }

    public GoogleAccountCredential getGoogleCredential() {
        GoogleAccountCredential credential =
                GoogleAccountCredential.usingOAuth2(mContext, Arrays.asList(SCOPES));
        credential.setBackOff(new ExponentialBackOff());
        return credential;
    }

    public static class PlaylistItemComparator implements Comparator<PlaylistItem> {
        @Override
        public int compare(PlaylistItem o1, PlaylistItem o2) {
            long first = o1.getSnippet().getPublishedAt().getValue();
            long second = o2.getSnippet().getPublishedAt().getValue();
            // Reverse order
            return (int) (second - first);
        }
    }
}

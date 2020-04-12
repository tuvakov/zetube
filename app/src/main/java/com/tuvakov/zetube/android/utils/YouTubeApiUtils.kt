package com.tuvakov.zetube.android.utils

import android.content.Context
import android.util.Log
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.YouTubeScopes
import com.google.api.services.youtube.model.PlaylistItem
import com.tuvakov.zetube.android.R
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YouTubeApiUtils @Inject internal constructor(
        private val mContext: Context,
        private val mPrefUtils: PrefUtils
) {
    val youTubeService: YouTube?
        get() {
            val accountName = mPrefUtils.accountName
            if (accountName == null) {
                Log.d(TAG, "Account name is null")
                return null
            }
            val credential = googleCredential
            credential.selectedAccountName = accountName
            val transport = AndroidHttp.newCompatibleTransport()
            val jsonFactory: JsonFactory = JacksonFactory.getDefaultInstance()
            return YouTube.Builder(transport, jsonFactory, credential)
                    .setApplicationName(mContext.getString(R.string.app_name))
                    .build()
        }

    val googleCredential: GoogleAccountCredential
        get() {
            val credential = GoogleAccountCredential.usingOAuth2(mContext, SCOPES)
            credential.backOff = ExponentialBackOff()
            return credential
        }

    companion object {
        private const val TAG = "YouTubeApiUtils"
        private val SCOPES = listOf(YouTubeScopes.YOUTUBE_READONLY)
    }
}


class PlaylistItemComparator : Comparator<PlaylistItem> {
    override fun compare(o1: PlaylistItem, o2: PlaylistItem): Int {
        val first = o1.snippet.publishedAt.value
        val second = o2.snippet.publishedAt.value
        // Reverse order
        return second.compareTo(first)
    }
}
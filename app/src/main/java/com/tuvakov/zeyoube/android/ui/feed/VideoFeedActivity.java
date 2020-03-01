package com.tuvakov.zeyoube.android.ui.feed;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.tuvakov.zeyoube.android.R;
import com.tuvakov.zeyoube.android.VideoFeedSyncService;
import com.tuvakov.zeyoube.android.ZeYouBe;
import com.tuvakov.zeyoube.android.data.SyncStatus;
import com.tuvakov.zeyoube.android.ui.player.PlayerActivity;
import com.tuvakov.zeyoube.android.utils.DateTimeUtils;
import com.tuvakov.zeyoube.android.utils.PrefUtils;
import com.tuvakov.zeyoube.android.utils.YouTubeApiUtils;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class VideoFeedActivity extends AppCompatActivity
        implements EasyPermissions.PermissionCallbacks {

    private static final int REQUEST_ACCOUNT_PICKER = 1000;
    private static final int REQUEST_AUTHORIZATION = 1001;
    private static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String TAG = "VideoFeedActivity";

    @Inject
    MainViewModelFactory mMainViewModelFactory;
    @Inject
    YouTubeApiUtils mYouTubeApiUtils;
    @Inject
    PrefUtils mPrefUtils;
    @Inject
    DateTimeUtils mDateTimeUtils;

    private GoogleAccountCredential mCredential;
    private MainViewModel mMainViewModel;

    /* Views */
    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private TextView mTextViewFeedback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_feed);

        ((ZeYouBe) getApplication()).getAppComponent().injectVideoFeedActivityFields(this);

        mProgressBar = findViewById(R.id.progress_circular);
        mTextViewFeedback = findViewById(R.id.tv_feedback);

        /* Setup the RecyclerView */
        mRecyclerView = findViewById(R.id.rv_video_feed);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);

        VideoFeedAdapter videoFeedAdapter = new VideoFeedAdapter();
        videoFeedAdapter.setItemClickListener(video -> {
            Intent intent = new Intent(this, PlayerActivity.class);
            intent.putExtra(PlayerActivity.KEY_EXTRA_VIDEO_ID, video.getId());
            startActivity(intent);
        });

        mRecyclerView.setAdapter(videoFeedAdapter);

        /* Setup the ViewModel and start observing */
        mMainViewModel = new ViewModelProvider(this, mMainViewModelFactory)
                .get(MainViewModel.class);
        mMainViewModel.getVideoFeed().observe(this, videos -> {

            if (!hasContactsPermission()) {
                showMessage(R.string.msg_permission_get_accounts, View.GONE);
                return;
            }

            if (mMainViewModel.isSyncing()) {
                return;
            }

            if (videos.size() == 0) {
                showMessage(R.string.msg_info_empty_result_set, View.GONE);
                return;
            }

            videoFeedAdapter.submitList(videos);

            showRecyclerView();
            // Scroll to top after sync
            mRecyclerView.scrollToPosition(0);
        });

        /* Observe and react sync status */
        VideoFeedSyncService.STATUS.observe(this, this::handleSyncStatus);

        mCredential = mYouTubeApiUtils.getGoogleCredential();
        startSyncChain();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_delete_all_videos:
                clearUpData();
                return true;
            case R.id.menu_item_update_video_feed:
                if (mMainViewModel.isSyncing()) {
                    Toast.makeText(this, R.string.msg_info_already_syncing,
                            Toast.LENGTH_SHORT).show();
                    return true;
                } else if (!hasDayPassed()) {
                    Toast.makeText(this, getString(R.string.msg_info_sync_not_allowed),
                            Toast.LENGTH_LONG).show();
                    return true;
                }
                startSyncChain();
                return true;
            default:
                return true;
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     *
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode  code indicating the result of the incoming
     *                    activity result.
     * @param data        Intent (containing result data) returned by incoming
     *                    activity result.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    Toast.makeText(this, R.string.msg_warning_install_google_play_services,
                            Toast.LENGTH_SHORT).show();
                } else {
                    startSyncChain();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        mPrefUtils.saveAccountName(accountName);
                        startSyncChain();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    startSyncChain();
                } else {
                    showMessage(R.string.msg_permission_get_accounts, View.GONE);
                }
                break;
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     *
     * @param requestCode  The request code passed in
     *                     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions,
                grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Log.d(TAG, "onPermissionsGranted: ");
//        showMessage(R.string.msg_info_restart_or_man_sync, View.GONE);
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Log.d(TAG, "onPermissionsDenied: ");
        showMessage(R.string.msg_permission_get_accounts, View.GONE);
    }

    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private void startSyncChain() {

        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mPrefUtils.getAccountName() == null) {
            chooseAccount();
        } else if (!isDeviceOnline()) {
            showMessage(R.string.msg_warning_no_network, View.GONE);
        } else if (hasDayPassed()) {
            mMainViewModel.setIsSyncing(true);
            Intent intent = new Intent(this, VideoFeedSyncService.class);
            startService(intent);
        }
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (hasContactsPermission()) {
            String accountName = mPrefUtils.getAccountName();
            if (accountName != null) {
                startSyncChain();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER
                );
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.msg_permission_get_accounts),
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS
            );
        }
    }

    private boolean isDeviceOnline() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = Objects.requireNonNull(connectivityManager).getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     *
     * @return true if Google Play Services is available and up to
     * date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     *
     * @param connectionStatusCode code describing the presence (or lack of)
     *                             Google Play Services on this device.
     */
    private void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                VideoFeedActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES
        );
        dialog.show();
    }

    private void handleSyncStatus(SyncStatus status) {
        int resultCode = status.getResultCode();
        if (resultCode == VideoFeedSyncService.STATUS_SYNC_STARTED) {
            showMessage(R.string.msg_info_sync_start, View.VISIBLE);
            return;
        }

        if (!hasContactsPermission()) {
            setStatusIdle();
            showMessage(R.string.msg_permission_get_accounts, View.GONE);
            clearUpData();
        }

        switch (resultCode) {
            case VideoFeedSyncService.STATUS_SYNC_SUCCESS:
                setStatusIdle();
                showRecyclerView();
                break;
            case VideoFeedSyncService.STATUS_SYNC_GOOGLE_PLAY_FAILURE:
                int code = ((GooglePlayServicesAvailabilityIOException) status.getException())
                        .getConnectionStatusCode();
                showGooglePlayServicesAvailabilityErrorDialog(code);
                setStatusIdle();
                break;
            case VideoFeedSyncService.STATUS_SYNC_AUTH_FAILURE:
                Intent intent = ((UserRecoverableAuthIOException) status.getException()).getIntent();
                startActivityForResult(intent, REQUEST_AUTHORIZATION);
                setStatusIdle();
                break;
            case VideoFeedSyncService.STATUS_SYNC_FAILURE:
                showMessage(R.string.msg_error_sync_failure, View.GONE);
                setStatusIdle();
                break;
        }

        mMainViewModel.setIsSyncing(false);
    }

    private void setStatusIdle() {
        VideoFeedSyncService.STATUS.setValue(
                new SyncStatus(VideoFeedSyncService.STATUS_SYNC_IDLE)
        );
    }

    private void showRecyclerView() {
        mProgressBar.setVisibility(View.GONE);
        mTextViewFeedback.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showMessage(int messageStringId, int progressBarVisibility) {
        mRecyclerView.setVisibility(View.GONE);
        mProgressBar.setVisibility(progressBarVisibility);
        mTextViewFeedback.setVisibility(View.VISIBLE);
        mTextViewFeedback.setText(messageStringId);
    }

    private boolean hasContactsPermission() {
        return EasyPermissions.hasPermissions(this, Manifest.permission.GET_ACCOUNTS);
    }

    private boolean hasDayPassed() {
        return mDateTimeUtils.hasDayPassed(mPrefUtils.getLastSyncTime());
    }

    private void clearUpData() {
        mPrefUtils.deleteAccountName();
        mPrefUtils.saveLastSyncTime(0);
        mMainViewModel.deleteAllVideos();
        mMainViewModel.deleteAllSubscriptions();
    }
}
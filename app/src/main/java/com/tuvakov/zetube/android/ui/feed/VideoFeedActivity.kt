package com.tuvakov.zetube.android.ui.feed

import android.Manifest
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.navigation.NavigationView
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.tuvakov.zetube.android.R
import com.tuvakov.zetube.android.ZeTubeApp
import com.tuvakov.zetube.android.data.SyncStatus
import com.tuvakov.zetube.android.data.Video
import com.tuvakov.zetube.android.ui.channels.ChannelsActivity
import com.tuvakov.zetube.android.utils.*
import kotlinx.android.synthetic.main.activity_video_feed.*
import kotlinx.android.synthetic.main.layout_toolbar.*
import kotlinx.android.synthetic.main.layout_video_feed.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks
import java.util.*
import javax.inject.Inject

class VideoFeedActivity : AppCompatActivity(), PermissionCallbacks, NavigationView.OnNavigationItemSelectedListener {
    @Inject
    lateinit var mMainViewModelFactory: MainViewModelFactory

    @Inject
    lateinit var mYouTubeApiUtils: YouTubeApiUtils

    @Inject
    lateinit var mPrefUtils: PrefUtils

    @Inject
    lateinit var mDateTimeUtils: DateTimeUtils

    private lateinit var mCredential: GoogleAccountCredential
    private lateinit var mMainViewModel: MainViewModel
    private lateinit var mTextViewAccountName: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_feed)

        // Inject fields
        (application as ZeTubeApp).appComponent.injectVideoFeedActivityFields(this)

        setSupportActionBar(toolbar)

        /* Setup navigation and drawer */
        nav_view.setNavigationItemSelectedListener(this)
        mTextViewAccountName = nav_view.getHeaderView(0).findViewById(R.id.tv_nav_account)
        setupDrawer()

        /* Setup the RecyclerView */
        rv_video_feed.setHasFixedSize(true)
        val videoFeedAdapter = VideoFeedAdapter()
        rv_video_feed.adapter = videoFeedAdapter

        /* Setup the ViewModel and start observing */
        mMainViewModel = ViewModelProvider(this, mMainViewModelFactory)
                .get(MainViewModel::class.java)

        mMainViewModel.videoFeed.observe(this, Observer { videos: List<Video> ->

            if (!hasContactsPermission()) {
                showMessage(R.string.msg_permission_get_accounts, View.GONE)
                return@Observer
            }

            if (mMainViewModel.isSyncing) {
                return@Observer
            }

            if (videos.isEmpty()) {
                showMessage(R.string.msg_info_empty_result_set, View.GONE)
                return@Observer
            }

            /* Update data */
            videoFeedAdapter.submitList(videos)
            showRecyclerView()
            rv_video_feed.scrollToPosition(0)
        })

        /* Observe and react sync status */
        mMainViewModel.status.observe(this, Observer { status: SyncStatus ->
            handleSyncStatus(status)
        })

        mCredential = mYouTubeApiUtils.googleCredential

        startSyncChain()
    }

    override fun onBackPressed() {
        if (layout_drawer.isDrawerOpen(GravityCompat.START)) {
            layout_drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_channels -> gotoChannels()
            R.id.nav_saved_videos -> Log.d(TAG, "onNavigationItemSelected: Saved Videos")
            else -> {
            }
        }
        layout_drawer.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_delete_all_videos -> {
                showDeleteDialog()
                true
            }
            R.id.menu_item_update_video_feed -> {
                if (mMainViewModel.isSyncing) {
                    Toast.makeText(this, R.string.msg_info_already_syncing,
                            Toast.LENGTH_SHORT).show()
                    return true
                } else if (!hasDayPassed()) {
                    Toast.makeText(this, getString(R.string.msg_info_sync_not_allowed),
                            Toast.LENGTH_LONG).show()
                    return true
                }
                startSyncChain()
                true
            }
            else -> true
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     *
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode  code indicating the result of the incoming
     * activity result.
     * @param data        Intent (containing result data) returned by incoming
     * activity result.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_GOOGLE_PLAY_SERVICES -> {
                if (resultCode != Activity.RESULT_OK) {
                    Toast.makeText(this, R.string.msg_warning_install_google_play_services,
                            Toast.LENGTH_SHORT).show()
                } else {
                    startSyncChain()
                }
            }
            REQUEST_ACCOUNT_PICKER -> {
                if (resultCode == Activity.RESULT_OK && data != null && data.extras != null) {
                    val accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                    if (accountName != null) {
                        mPrefUtils.saveAccountName(accountName)
                        startSyncChain()
                    }
                }
            }
            REQUEST_AUTHORIZATION -> {
                if (resultCode == Activity.RESULT_OK) {
                    startSyncChain()
                } else {
                    showMessage(R.string.msg_permission_get_accounts, View.GONE)
                }
            }
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     *
     * @param requestCode  The request code passed in
     * requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     * which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions,
                grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        Log.d(TAG, "onPermissionsGranted: ")
//        showMessage(R.string.msg_info_restart_or_man_sync, View.GONE);
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        Log.d(TAG, "onPermissionsDenied: ")
        showMessage(R.string.msg_permission_get_accounts, View.GONE)
    }

    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private fun startSyncChain() {
        if (!isGooglePlayServicesAvailable) {
            acquireGooglePlayServices()
        } else if (mPrefUtils.accountName == null) {
            chooseAccount()
        } else if (!isDeviceOnline) {
            showMessage(R.string.msg_warning_no_network, View.GONE)
        } else if (hasDayPassed()) {
           mMainViewModel.sync()
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
    private fun chooseAccount() {
        if (hasContactsPermission()) {
            val accountName = mPrefUtils.accountName
            if (accountName != null) {
                startSyncChain()
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER
                )
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.msg_permission_get_accounts),
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS
            )
        }
    }

    private val isDeviceOnline: Boolean
        get() {
            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = Objects.requireNonNull(connectivityManager).activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }

    /**
     * Check that Google Play services APK is installed and up to date.
     *
     * @return true if Google Play Services is available and up to
     * date on this device; false otherwise.
     */
    private val isGooglePlayServicesAvailable: Boolean
        get() {
            val apiAvailability = GoogleApiAvailability.getInstance()
            val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)
            return connectionStatusCode == ConnectionResult.SUCCESS
        }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private fun acquireGooglePlayServices() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode)
        }
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     *
     * @param connectionStatusCode code describing the presence (or lack of)
     * Google Play Services on this device.
     */
    private fun showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode: Int) {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val dialog = apiAvailability.getErrorDialog(
                this@VideoFeedActivity,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES
        )
        dialog.show()
    }

    private fun handleSyncStatus(status: SyncStatus) {
        val resultCode = status.resultCode
        if (resultCode == SyncUtils.STATUS_SYNC_STARTED) {
            showMessage(R.string.msg_info_sync_start, View.VISIBLE)
            return
        }
        if (!hasContactsPermission()) {
            setStatusIdle()
            showMessage(R.string.msg_permission_get_accounts, View.GONE)
            clearUpData()
        }
        when (resultCode) {
            SyncUtils.STATUS_SYNC_SUCCESS -> {
                setStatusIdle()
                showRecyclerView()
            }
            SyncUtils.STATUS_SYNC_GOOGLE_PLAY_FAILURE -> {
                val code = (status.exception as GooglePlayServicesAvailabilityIOException)
                        .connectionStatusCode
                showGooglePlayServicesAvailabilityErrorDialog(code)
                setStatusIdle()
            }
            SyncUtils.STATUS_SYNC_AUTH_FAILURE -> {
                val intent = (status.exception as UserRecoverableAuthIOException?)!!.intent
                startActivityForResult(intent, REQUEST_AUTHORIZATION)
                setStatusIdle()
            }
            SyncUtils.STATUS_SYNC_FAILURE -> {
                showMessage(R.string.msg_error_sync_failure, View.GONE)
                setStatusIdle()
            }
        }
    }

    private fun setupDrawer() {
        val toggle = ActionBarDrawerToggle(this, layout_drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        layout_drawer.addDrawerListener(toggle)
        toggle.syncState()

        /* Set up account name in navigation header */
        val accountName = mPrefUtils.accountName
        mTextViewAccountName.text = accountName
    }

    private fun gotoChannels() {
        if (mPrefUtils.accountName != null && !mMainViewModel.isSyncing) {
            val intent = Intent(this, ChannelsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setStatusIdle() {
        mMainViewModel.setStatusIdle()
    }

    private fun showRecyclerView() {
        progress_circular.hide()
        tv_feedback.hide()
        rv_video_feed.show()
    }

    private fun showMessage(messageStringId: Int, progressBarVisibility: Int = View.GONE) {
        rv_video_feed.hide()
        tv_feedback.show()
        tv_feedback.setText(messageStringId)
        progress_circular.visibility = progressBarVisibility
    }

    private fun hasContactsPermission(): Boolean {
        return EasyPermissions.hasPermissions(this, Manifest.permission.GET_ACCOUNTS)
    }

    private fun hasDayPassed(): Boolean {
        return mDateTimeUtils.hasDayPassed(mPrefUtils.lastSyncTime)
    }

    private fun clearUpData() {
        mPrefUtils.deleteAccountName()
        mPrefUtils.saveLastSyncTime(0)
        mMainViewModel.deleteAllVideos()
        mMainViewModel.deleteAllSubscriptions()
        /* Set account name text view empty */
        mTextViewAccountName.text = ""
    }

    private fun showDeleteDialog() {
        /* Don't show the dialog if there is no data */
        if (mPrefUtils.accountName == null && mPrefUtils.lastSyncTime == 0L) {
            Toast.makeText(this, R.string.msg_no_data_to_delete, Toast.LENGTH_SHORT).show()
            return
        }
        val builder = AlertDialog.Builder(this, R.style.ZeTubeAlertDialog)
        builder.setMessage(R.string.msg_delete_dialog)
        builder.setCancelable(true)
        builder.setPositiveButton(R.string.btn_txt_confirm) { _: DialogInterface?, _: Int -> clearUpData() }
        builder.setNegativeButton(R.string.btn_txt_cancel) { dialog: DialogInterface, _: Int -> dialog.cancel() }
        builder.create().show()
    }

    companion object {
        private const val REQUEST_ACCOUNT_PICKER = 1000
        private const val REQUEST_AUTHORIZATION = 1001
        private const val REQUEST_GOOGLE_PLAY_SERVICES = 1002
        private const val REQUEST_PERMISSION_GET_ACCOUNTS = 1003
        private const val TAG = "VideoFeedActivity"
    }
}
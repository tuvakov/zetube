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
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.navigation.NavigationView
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.tuvakov.zetube.android.R
import com.tuvakov.zetube.android.ZeTubeApp
import com.tuvakov.zetube.android.data.Video
import com.tuvakov.zetube.android.databinding.ActivityVideoFeedBinding
import com.tuvakov.zetube.android.ui.channeldetail.*
import com.tuvakov.zetube.android.ui.channels.ChannelsActivity
import com.tuvakov.zetube.android.utils.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks
import java.util.*
import javax.inject.Inject

class VideoFeedActivity : AppCompatActivity(),
        PermissionCallbacks,
        NavigationView.OnNavigationItemSelectedListener {

    @Inject
    lateinit var mViewModelFactory: ViewModelFactory

    @Inject
    lateinit var mYouTubeApiUtils: YouTubeApiUtils

    @Inject
    lateinit var mPrefUtils: PrefUtils

    @Inject
    lateinit var mDateTimeUtils: DateTimeUtils

    private lateinit var mCredential: GoogleAccountCredential
    private lateinit var mMainViewModel: MainViewModel
    private lateinit var binding: ActivityVideoFeedBinding
    private lateinit var mTextViewAccountName: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoFeedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inject fields
        (application as ZeTubeApp).appComponent.injectVideoFeedActivityFields(this)

        setSupportActionBar(binding.lToolbar.toolbar)

        /* Setup navigation and drawer */
        binding.navView.setNavigationItemSelectedListener(this)
        mTextViewAccountName = binding.navView.getHeaderView(0).findViewById(R.id.tv_nav_account)
        setupDrawer()

        /* Setup the RecyclerView */
        val videoFeedAdapter = VideoFeedAdapter().also {
            it.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onChanged() {
                    binding.lVideoFeed.rvVideoFeed.scrollToPosition(0)
                }

                override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                    binding.lVideoFeed.rvVideoFeed.scrollToPosition(0)
                }

                override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                    binding.lVideoFeed.rvVideoFeed.scrollToPosition(0)
                }

                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    binding.lVideoFeed.rvVideoFeed.scrollToPosition(0)
                }

                override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                    binding.lVideoFeed.rvVideoFeed.scrollToPosition(0)
                }

                override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
                    binding.lVideoFeed.rvVideoFeed.scrollToPosition(0)
                }
            })
        }
        binding.lVideoFeed.rvVideoFeed.adapter = videoFeedAdapter

        /* Setup the ViewModel and start observing */
        mMainViewModel = ViewModelProvider(this, mViewModelFactory)
                .get(MainViewModel::class.java)

        mMainViewModel.videoFeed.observe(this, Observer { videos: List<Video> ->
            if (videos.isEmpty() && mMainViewModel.isSuccess) {
                mMainViewModel.setEmptyListStatus()
            }
            videoFeedAdapter.submitList(videos)
        })

        /* Observe and react sync status */
        mMainViewModel.status.observe(this, Observer { status: LiveDataState ->
            handleSyncStatus(status)
        })

        mCredential = mYouTubeApiUtils.googleCredential

        startSyncChain()
    }

    override fun onBackPressed() {
        if (binding.layoutDrawer.isDrawerOpen(GravityCompat.START)) {
            binding.layoutDrawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_feed -> mMainViewModel.loadAllVideos()
            R.id.nav_channels -> gotoChannels()
            R.id.nav_saved_videos -> mMainViewModel.loadSavedVideos()
        }
        binding.layoutDrawer.closeDrawer(GravityCompat.START)
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
                } else if (!isSyncAllowed()) {
                    Toast.makeText(
                            this,
                            getString(R.string.msg_info_sync_not_allowed, SyncUtils.SYNC_INTERVAL_HOURS),
                            Toast.LENGTH_LONG).show()
                    return true
                }
                startSyncChain()
                true
            }
            else -> true
        }
    }

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

    private fun startSyncChain() {
        if (!hasContactsPermission()) {
            showMessage(R.string.msg_permission_get_accounts, View.GONE)
            clearUpData()
        }
        if (!isGooglePlayServicesAvailable) {
            acquireGooglePlayServices()
        } else if (mPrefUtils.accountName == null) {
            chooseAccount()
        } else if (!isDeviceOnline) {
            showMessage(R.string.msg_warning_no_network, View.GONE)
        } else if (isSyncAllowed()) {
            mMainViewModel.sync()
        }
    }

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

    private val isGooglePlayServicesAvailable: Boolean
        get() {
            val apiAvailability = GoogleApiAvailability.getInstance()
            val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)
            return connectionStatusCode == ConnectionResult.SUCCESS
        }

    private fun acquireGooglePlayServices() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode)
        }
    }

    private fun showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode: Int) {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val dialog = apiAvailability.getErrorDialog(
                this@VideoFeedActivity,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES
        )
        dialog.show()
    }

    private fun handleSyncStatus(status: LiveDataState) {
        Log.d(TAG, status.toString())
        when (status) {
            InProgress -> showMessage(R.string.msg_info_sync_start, View.VISIBLE)
            Success -> showRecyclerView()
            EmptyList -> showMessage(R.string.msg_info_empty_result_set, View.GONE)
            is Error -> {
                Log.e(TAG, status.exception?.message, status.exception!!)
                when (status.exception) {
                    is ImmatureSyncException -> {
                        TODO()
                    }
                    is GooglePlayServicesAvailabilityIOException -> {
                        val code = status.exception.connectionStatusCode
                        showGooglePlayServicesAvailabilityErrorDialog(code)
                    }
                    is UserRecoverableAuthIOException -> {
                        val intent = status.exception.intent
                        startActivityForResult(intent, REQUEST_AUTHORIZATION)
                    }
                    else -> {
                        showMessage(R.string.msg_error_sync_failure, View.GONE)
                    }
                }
            }
        }
    }

    private fun setupDrawer() {
        val toggle = ActionBarDrawerToggle(this, binding.layoutDrawer,
                binding.lToolbar.toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        )
        binding.layoutDrawer.addDrawerListener(toggle)
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

    private fun showRecyclerView() {
        with(binding.lVideoFeed) {
            progressCircular.hide()
            tvFeedback.hide()
            rvVideoFeed.show()
        }
    }

    private fun showMessage(messageStringId: Int, progressBarVisibility: Int = View.GONE) {
        with(binding.lVideoFeed) {
            rvVideoFeed.hide()
            tvFeedback.show()
            tvFeedback.setText(messageStringId)
            progressCircular.visibility = progressBarVisibility

        }
    }

    private fun hasContactsPermission(): Boolean {
        return EasyPermissions.hasPermissions(this, Manifest.permission.GET_ACCOUNTS)
    }

    private fun isSyncAllowed(): Boolean {
        return mDateTimeUtils.isSyncAllowed(mPrefUtils.lastSyncTime)
    }

    private fun clearUpData() {
        mPrefUtils.emptyPreferences()
        mMainViewModel.emptyDatabase()
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
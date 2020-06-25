package com.example.licenta.view.notification

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.Navigation
import com.example.licenta.R
import com.example.licenta.Utils.CHANNEL_ID
import com.example.licenta.view.activity.MainActivity
import com.google.android.material.snackbar.Snackbar

class NotificationHandler(private val mContext: Context, private val mMainActivity: Activity) : Handler() {
    private var notificationShow = true
    private var notificationManager: NotificationManagerCompat? = null
    private var builder: NotificationCompat.Builder? = null
    private var isInBackground = false
    override fun sendMessageAtTime(
        msg: Message,
        uptimeMillis: Long
    ): Boolean {
        return super.sendMessageAtTime(msg, uptimeMillis)
    }

    override fun handleMessage(msg: Message) {
        super.handleMessage(msg)
        if (msg.what == 0) {
            val locationManager =
                mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (mContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && mContext.checkSelfPermission(
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                10000,
                0f,
                object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        val mDistance = FloatArray(1)
//                        val mTreasureViewModel: TreasureViewModel =
//                            ViewModelProviders.of((mContext as FragmentActivity)).get(
//                                TreasureViewModel::class.java
//                            )
//                        for (treasure in mTreasureViewModel.getTreasureRepository()
//                            .getValue()) {
//                            Location.distanceBetween(
//                                location.latitude,
//                                location.longitude,
//                                treasure.getLocationLat(),
//                                treasure.getLocationLon(),
//                                mDistance
//                            )
//                            if (mDistance[0] < 0) {
//                                mDistance[0] = mDistance[0] * -1
//                            }
//                            if (mDistance[0] <= 100 && !treasure.isClaimed()) {
//                                showNotification()
//                                if (isInBackground) {
//                                    notificationManager!!.notify(NOTIFICATION_ID, builder!!.build())
//                                }
//                                break
//                            }
//                        }
                    }

                    override fun onStatusChanged(
                        s: String,
                        i: Int,
                        bundle: Bundle
                    ) {
                    }

                    override fun onProviderEnabled(s: String) {}
                    override fun onProviderDisabled(s: String) {}
                })
        }
    }

    override fun getMessageName(message: Message): String {
        return super.getMessageName(message)
    }

    private fun showNotification() {
        val navController =
            Navigation.findNavController(mMainActivity, R.id.my_nav_host_fragment)
        if (!notificationShow) {
            val snackbar: Snackbar = Snackbar.make(
                mMainActivity.findViewById(R.id.main),
                R.string.notificationTreasureNear,
                Snackbar.LENGTH_INDEFINITE
            )
            snackbar.setAction(
                R.string.notificationShowOnMap,
                View.OnClickListener {
                    notificationShow = true
                    navController.navigate(R.id.mapFragment)
                }).show()
        }
    }

    private fun notificationBuilder() {
        val collapsedView = RemoteViews(
            mMainActivity.packageName,
            R.layout.fragment_notification_collaps
        )
        val expandedView = RemoteViews(
            mMainActivity.packageName,
            R.layout.fragment_notification_expand
        )
        builder = NotificationCompat.Builder(mContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
            .setCustomContentView(collapsedView)
            .setCustomBigContentView(expandedView)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        notificationManager = NotificationManagerCompat.from(mContext)
        val clickIntent = Intent(mContext, MainActivity::class.java)
        clickIntent.flags = (Intent.FLAG_ACTIVITY_CLEAR_TOP
                or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val intent = PendingIntent.getActivity(
            mContext, 0,
            clickIntent, 0
        )
        expandedView.setOnClickPendingIntent(R.id.notificationShowMapButton, intent)
    }

    fun isInBackground(background: Boolean) {
        isInBackground = background
    }

    fun setNotificationShow(bool: Boolean) {
        notificationShow = bool
    }

    init {
        notificationBuilder()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel =
                NotificationChannel(CHANNEL_ID, CHANNEL_ID, importance)
            //Boolean value to set if lights are enabled for Notifications from this Channel
            notificationChannel.enableLights(true)
            //Boolean value to set if vibration are enabled for Notifications from this Channel
            notificationChannel.enableVibration(true)
            //Sets the color of Notification Light
            notificationChannel.lightColor = Color.GREEN
            //Set the vibration pattern for notifications. Pattern is in milliseconds with the format {delay,play,sleep,play,sleep...}
            notificationChannel.vibrationPattern = longArrayOf(
                500,
                500,
                500,
                500,
                500
            )
            //Sets whether notifications from these Channel should be visible on Lockscreen or not
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            notificationManager!!.createNotificationChannel(notificationChannel)
        }
    }
}
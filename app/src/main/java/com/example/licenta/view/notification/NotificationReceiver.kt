package com.example.licenta.view.notification

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.Navigation
import com.example.licenta.R
import com.example.licenta.Utils.NOTIFICATION_ID

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val navController = Navigation.findNavController(
            mMainActivity!!,
            R.id.my_nav_host_fragment
        )
        navController.navigate(R.id.mapFragment)
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(NOTIFICATION_ID)
    }

    companion object {
        private var mMainActivity: Activity? = null
        fun setMainActivity(mainActivity: Activity?) {
            mMainActivity = mainActivity
        }
    }
}
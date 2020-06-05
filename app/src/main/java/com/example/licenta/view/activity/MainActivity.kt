package com.example.licenta.view.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.example.licenta.R
import com.example.licenta.Utils.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
import com.example.licenta.view.fragment.AccountFragment
import com.example.licenta.view.fragment.CreateNewTripFragment
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {
    private val locationPermission: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Create the NavController
        val navController =
            Navigation.findNavController(this, R.id.my_nav_host_fragment)
        //Create bottom navigation component
        val bottomNav =
            findViewById<BottomNavigationView>(R.id.bottom_navigation)

        //Link NavController with bottom Navigation
        NavigationUI.setupWithNavController(bottomNav, navController)
        //HideBottom Navigation on fragments that don't need it
//        var notificationHandler = NotificationHandler(this, this)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.tripDetailsFragment || destination.id == R.id.pastTripsFragment || destination.id == R.id.futureTripsFragment || destination.id == R.id.createNewTripFragment || destination.id == R.id.accountFragment) {//destination.id == R.id.treasureListFragment || destination.id == R.id.userProfileFragment || destination.id == R.id.hallOfFameFragment || destination.id == R.id.mapsFragment) {
                bottomNav.visibility = View.VISIBLE
//                notificationHandler.setNotificationShow(false)
            } else {
                bottomNav.visibility = View.GONE
//                notificationHandler.setNotificationShow(true)
            }
////            if (destination.id == R.id.mapsFragment) {
////                notificationHandler.setNotificationShow(true)
////            }
//        }
//        NotificationReceiver.setMainActivity(this)
        }
    }

    private fun getLocationPermission(): Boolean {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        return if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            true
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
            false
        }
    }
}

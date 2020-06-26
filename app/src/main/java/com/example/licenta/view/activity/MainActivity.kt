package com.example.licenta.view.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.example.licenta.NavEvent
import com.example.licenta.R
import com.example.licenta.view.fragment.MapFragment.Companion.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
import com.example.licenta.view.notification.NotificationHandler
import com.example.licenta.view.notification.NotificationReceiver
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.Disposable
import io.reactivex.processors.PublishProcessor
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var navEvents: PublishProcessor<NavEvent>

    private val locationPermission: Boolean = false
    private lateinit var notificationHandler: NotificationHandler

    private lateinit var disposable: Disposable

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
        notificationHandler = NotificationHandler(this, this)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.tripDetailsFragment ||
                destination.id == R.id.pastTripsFragment ||
                destination.id == R.id.futureTripsFragment ||
                destination.id == R.id.createNewTripFragment ||
                destination.id == R.id.accountFragment //||
            //destination.id == R.id.mapFragment
            ) {
                bottomNav.visibility = View.VISIBLE
                notificationHandler.setNotificationShow(false)
            } else {
                bottomNav.visibility = View.GONE
                notificationHandler.setNotificationShow(true)
            }
            if (destination.id == R.id.mapFragment) {
                notificationHandler.setNotificationShow(true)
            }
            NotificationReceiver.setMainActivity(this)
            getLocation()
        }

        disposable = navEvents.subscribe {
            when (it.destination) {
                NavEvent.Destination.LOGIN -> {
                    when (navController.currentDestination!!.id) {
                        R.id.registrationFragment -> navController.navigate(R.id.action_registrationFragment_to_loginFragment)
                        R.id.accountFragment -> navController.navigate(R.id.action_accountFragment_to_loginFragment)
                        R.id.changePasswordFragment -> navController.navigate(R.id.action_changePasswordFragment_to_loginFragment)
                    }
                }
                NavEvent.Destination.REGISTRATION ->
                    when (navController.currentDestination!!.id) {
                        R.id.loginFragment -> navController.navigate(R.id.action_loginFragment_to_registrationFragment)
                        R.id.termsFragment -> navController.navigate(R.id.action_termsFragment_to_registrationFragment)
                    }
                NavEvent.Destination.TERMS ->
                    when (navController.currentDestination!!.id) {
                        R.id.accountFragment -> navController.navigate(R.id.action_accountFragment_to_termsFragment)
                        R.id.registrationFragment -> navController.navigate(R.id.action_registrationFragment_to_termsFragment)
                    }
                NavEvent.Destination.MYACCOUNT ->
                    navController.popBackStack(R.id.accountFragment, false)
                NavEvent.Destination.CHANGEPASSWORD ->
                    when (navController.currentDestination!!.id) {
                        R.id.accountFragment -> navController.navigate(R.id.action_accountFragment_to_changePasswordFragment)
                    }
                NavEvent.Destination.FUTURE ->
                    when (navController.currentDestination!!.id) {
                        R.id.loginFragment -> navController.navigate(R.id.action_loginFragment_to_futureTripsFragment)
                        R.id.createNewTripFragment -> navController.navigate(R.id.action_createNewTripFragment_to_futureTripsFragment)
                        R.id.mapFragment -> navController.navigate(R.id.action_mapFragment_to_futureTripsFragment)
                    }
                NavEvent.Destination.PAST ->
                    when (navController.currentDestination!!.id) {
                        R.id.mapFragment -> navController.navigate(R.id.action_mapFragment_to_pastTripsFragment)
                    }
                NavEvent.Destination.CREATE ->
                    when (navController.currentDestination!!.id) {
                        R.id.futureTripsFragment -> navController.navigate(R.id.action_futureTripsFragment_to_createNewTripFragment)
                    }
                NavEvent.Destination.DETAILS ->
                    when (navController.currentDestination!!.id) {
                        R.id.futureTripsFragment -> navController.navigate(R.id.action_futureTripsFragment_to_tripDetailsFragment)
                        R.id.pastTripsFragment -> navController.navigate(R.id.action_pastTripsFragment_to_tripDetailsFragment)
//                        R.id.mapFragment -> navController.navigate(R.id.action_mapFragment_to_tripDetailsFragment)
                    }
                NavEvent.Destination.MAP ->
                    when (navController.currentDestination!!.id) {
//                        R.id.loginFragment -> navController.navigate(R.id.action_loginFragment_to_mapFragment)
                        R.id.futureTripsFragment -> navController.navigate(R.id.action_futureTripsFragment_to_mapFragment)
                        R.id.tripDetailsFragment -> navController.navigate(R.id.action_tripDetailsFragment_to_mapFragment)
                    }
            }
        }
    }

    private fun getLocation() {
        getLocationPermission()
        if (getLocationPermission()) {
            notificationHandler.sendEmptyMessage(0)
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

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }
}

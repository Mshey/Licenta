package com.example.licenta.view.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.example.licenta.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

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
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            if (false) {//destination.id == R.id.treasureListFragment || destination.id == R.id.userProfileFragment || destination.id == R.id.hallOfFameFragment || destination.id == R.id.mapsFragment) {
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
}

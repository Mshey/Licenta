package com.example.licenta.model

import com.firebase.geofire.GeoLocation

data class User(val username: String = "") {
    var userLocation: GeoLocation = GeoLocation(0.0, 0.0)
}
package com.example.licenta.model

import com.firebase.geofire.GeoLocation

data class Trip(
    val title: String = "",
    val address: String = "",
    val date: Long = 0,
    val description: String = "",
    val participants: List<User> = emptyList()
) {
    var organizer: User = User("")
    var location: GeoLocation = GeoLocation(0.0, 0.0)
    var id: String = ""
    var active: Boolean = false
    var done: Boolean = false
}
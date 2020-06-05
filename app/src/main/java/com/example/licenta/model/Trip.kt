package com.example.licenta.model

import java.io.Serializable
import java.time.LocalDate
import java.util.*

data class Trip(
    val title: String = "",
    val address: String = "",
    val date: Long = 0,
    val description: String = "",
    val participants: List<String> = emptyList()
) : Serializable {
    var organizer: String = ""
    var location: MyAddress = MyAddress()
}
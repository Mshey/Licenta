package com.example.licenta

import android.content.res.Resources
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

object Utils {
    val Any.TAG: String
        get() {
            return javaClass.simpleName
        }

    fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    const val ACCOUNTS: String = "accounts"
    const val USERS: String = "users"
    const val EMAIL: String = "email"
    const val USERNAME: String = "username"
    const val ACTIVE:String = "active"

    const val TRIPS: String = "trips"
    const val ORGANIZED: String = "organized"
    const val PARTICIPANT: String = "participant"
    const val ADDRESS: String = "address"
    const val TITLE: String = "title"
    const val ORGANIZER: String = "organizer"
    const val LOCATION: String = "location"
    const val LAT: String = "lat"
    const val LNG: String = "lng"
    const val DATE: String = "date"
    const val DESCRIPTION: String = "description"
    const val PARTICIPANTS: String = "participants"

    const val EMPTY_FIELD:String = "Field can't be empty!"

    const val TRIP = "trip"

    var username = ""
    var helloUser = ""

    fun Int.toDp(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()
    fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

    fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        return format.format(date)
    }

    fun convertDateToLong(date: String): Long {
        val df = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        return df.parse(date)!!.time
    }

    const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9002

}
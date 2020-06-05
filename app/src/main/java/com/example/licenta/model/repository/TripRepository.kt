package com.example.licenta.model.repository

import android.content.Context
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.licenta.Utils.ADDRESS
import com.example.licenta.Utils.DATE
import com.example.licenta.Utils.DESCRIPTION
import com.example.licenta.Utils.LAT
import com.example.licenta.Utils.LNG
import com.example.licenta.Utils.LOCATION
import com.example.licenta.Utils.ORGANIZER
import com.example.licenta.Utils.PARTICIPANT
import com.example.licenta.Utils.PARTICIPANTS
import com.example.licenta.Utils.TAG
import com.example.licenta.Utils.TITLE
import com.example.licenta.Utils.TRIPS
import com.example.licenta.Utils.USERS
import com.example.licenta.Utils.username
import com.example.licenta.model.GenericCallback
import com.example.licenta.model.Trip
import com.example.licenta.model.TripState
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class TripRepository {
    private val firebaseReference = FirebaseDatabase.getInstance().reference

    fun getFutureOrganizedTripsFirebase(genericCallback: GenericCallback<List<Trip>>) {
        val tripList: ArrayList<Trip> = ArrayList()
        FirebaseDatabase.getInstance().reference.child(TRIPS).orderByChild(ORGANIZER)
            .equalTo(username)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    val trips: MutableList<Trip> = mutableListOf()
                    p0.children.mapNotNullTo(trips) {
                        it.getValue(Trip::class.java)
                    }
                    tripList.addAll(trips.filter {
                        TimeUnit.MILLISECONDS.toDays(it.date - System.currentTimeMillis()) >= 0
                    })
                    genericCallback.onResponseReady(tripList)
                }

                override fun onCancelled(p0: DatabaseError) {
                    Log.d(TAG, "onCancelled: ${p0.message}")
                }
            })
    }

    fun getFutureParticipantTripsFirebase(genericCallback: GenericCallback<List<Trip>>) {
        val tripList: MutableList<Trip> = mutableListOf()
        Log.d(TAG, "getFutureParticipantTripsFirebase, 1st")
        firebaseReference.child(USERS).child(username).child(PARTICIPANT)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    Log.d(TAG, "getFutureParticipantTripsFirebase, 2nd")
                    p0.children.forEach {
                        firebaseReference.child(TRIPS).child(it.key!!)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(p1: DataSnapshot) {
                                    Log.d(TAG, "getFutureParticipantTripsFirebase, 3rd")
                                    val trip = p1.getValue(Trip::class.java)
                                    if (TimeUnit.MILLISECONDS.toDays(trip?.date!! - System.currentTimeMillis()) >= 0) {
                                        tripList.add(trip)
                                        genericCallback.onResponseReady(tripList)
                                    }
                                }

                                override fun onCancelled(p1: DatabaseError) {
                                    Log.d(
                                        TAG,
                                        "p1, onCancelled, getFutureParticipantTripsFirebase: " + p1.message
                                    )
                                }
                            })
                    }
                }

                override fun onCancelled(p0: DatabaseError) {
                    Log.d(TAG, "p0, onCancelled, getFutureParticipantTripsFirebase: " + p0.message)
                }
            })
    }

    fun getPastOrganizedTripsFirebase(genericCallback: GenericCallback<List<Trip>>) {
        val tripList: ArrayList<Trip> = ArrayList()
        FirebaseDatabase.getInstance().reference.child(TRIPS).orderByChild(ORGANIZER)
            .equalTo(username)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    val trips: MutableList<Trip> = mutableListOf()
                    p0.children.mapNotNullTo(trips) {
                        it.getValue(Trip::class.java)
                    }
                    tripList.addAll(trips.filter {
                        TimeUnit.MILLISECONDS.toDays(it.date - System.currentTimeMillis()) < 0
                    })
                    genericCallback.onResponseReady(tripList)
                }

                override fun onCancelled(p0: DatabaseError) {
                    Log.d(TAG, "onCancelled: ${p0.message}")
                }
            })
    }

    fun getPastParticipantTripsFirebase(genericCallback: GenericCallback<List<Trip>>) {
        val tripList: MutableList<Trip> = mutableListOf()
        Log.d(TAG, "getPastParticipantTripsFirebase, 1st")
        firebaseReference.child(USERS).child(username).child(PARTICIPANT)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    Log.d(TAG, "getPastParticipantTripsFirebase, 2nd")
                    p0.children.forEach {
                        firebaseReference.child(TRIPS).child(it.key!!)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(p1: DataSnapshot) {
                                    Log.d(TAG, "getPastParticipantTripsFirebase, 3rd")
                                    val trip = p1.getValue(Trip::class.java)
                                    if (TimeUnit.MILLISECONDS.toDays(trip?.date!! - System.currentTimeMillis()) < 0) {
                                        tripList.add(trip)
                                        genericCallback.onResponseReady(tripList)
                                    }
                                }

                                override fun onCancelled(p1: DatabaseError) {
                                    Log.d(
                                        TAG,
                                        "p1, onCancelled, getPastParticipantTripsFirebase: " + p1.message
                                    )
                                }
                            })
                    }
                }

                override fun onCancelled(p0: DatabaseError) {
                    Log.d(TAG, "p0, onCancelled, getPastParticipantTripsFirebase: " + p0.message)
                }
            })
    }

    fun checkIfUserExistsFirebase(user: String): MutableLiveData<Boolean> {
        val userExistsMutableLiveData: MutableLiveData<Boolean> =
            MutableLiveData<Boolean>()
        firebaseReference.child(USERS).child(user)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    userExistsMutableLiveData.value = p0.value != null
                }

                override fun onCancelled(p0: DatabaseError) {
                    userExistsMutableLiveData.value = false
                }
            })
        return userExistsMutableLiveData
    }

    fun addNewTripToFirebase(
        trip: Trip,
        context: Context
    ): MutableLiveData<List<TripState>> {
        val addTripToFirebaseMutableLiveData: MutableLiveData<List<TripState>> =
            MutableLiveData<List<TripState>>()
        val tripStateList: MutableList<TripState> = mutableListOf()
        val geoCoder = Geocoder(context, Locale.getDefault())
        val addressList =
            geoCoder.getFromLocationName(trip.address, 1)

        if (addressList.isNotEmpty()) {
            val address = addressList[0]
            trip.location.latitude = address.latitude
            trip.location.longitude = address.longitude
            if ((trip.participants.contains(username))) {
                tripStateList.add(
                    TripState.PART_HAS_ORGANIZER
                )
            } else {
                trip.organizer = username

                val userRef = firebaseReference.child(USERS)
                val tripRef =
                    firebaseReference.child(TRIPS)
                        .push()
                tripRef.child(ORGANIZER).setValue(username)
                tripRef.child(TITLE).setValue(trip.title)
                tripRef.child(LOCATION).child(LAT)
                    .setValue(trip.location.latitude)
                tripRef.child(LOCATION).child(LNG)
                    .setValue(trip.location.longitude)
                tripRef.child(ADDRESS).setValue(trip.address)
                tripRef.child(DATE).setValue(trip.date)
                tripRef.child(DESCRIPTION)
                    .setValue(trip.description)
                tripRef.child(PARTICIPANTS)
                    .setValue(trip.participants)
                val id = tripRef.key
                userRef.child(username).child(ORGANIZER)
                    .child(id!!)
                    .setValue(true)
                trip.participants.forEach {
                    userRef.child(it)
                        .child(PARTICIPANT).child(id)
                        .setValue(true)
                }
                tripStateList.add(TripState.SUCCESS)
            }
        } else {
            tripStateList.add(TripState.WRONG_ADDRESS)
        }
        addTripToFirebaseMutableLiveData.value = tripStateList

        return addTripToFirebaseMutableLiveData
    }

    companion object {
        private var INSTANCE: TripRepository? = null
        fun getInstance() = INSTANCE
            ?: TripRepository().also {
                INSTANCE = it
            }
    }
}
package com.example.licenta.model.repository

import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.licenta.Utils.ACTIVE
import com.example.licenta.Utils.ADDRESS
import com.example.licenta.Utils.DATE
import com.example.licenta.Utils.DESCRIPTION
import com.example.licenta.Utils.DONE
import com.example.licenta.Utils.LOCATION
import com.example.licenta.Utils.ORGANIZER
import com.example.licenta.Utils.PARTICIPANT
import com.example.licenta.Utils.PARTICIPANTS
import com.example.licenta.Utils.TAG
import com.example.licenta.Utils.TITLE
import com.example.licenta.Utils.TRIPS
import com.example.licenta.Utils.USERNAME
import com.example.licenta.Utils.USERS
import com.example.licenta.Utils.username
import com.example.licenta.model.GenericCallback
import com.example.licenta.model.Trip
import com.example.licenta.model.TripState
import com.example.licenta.model.User
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.LocationCallback
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.*
import java.util.*
import java.util.concurrent.TimeUnit

class TripRepository {
    private val firebaseReference = FirebaseDatabase.getInstance().reference
    private lateinit var childEventListener: ChildEventListener

    fun getFutureTripsFirebase(genericCallback: GenericCallback<List<Trip>>) {
        val tripsList: MutableList<Trip> = mutableListOf()
        childEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                val trip = dataSnapshot.getValue(Trip::class.java)
                trip?.id = dataSnapshot.key!!
                var isParticipant = false
                trip?.participants?.forEach { it2 ->
                    if (it2.username == username) isParticipant = true
                }
                if (TimeUnit.MILLISECONDS.toDays(trip?.date!! - System.currentTimeMillis()) >= 0 && !trip.done && (trip.organizer.username == username || isParticipant)) {
                    tripsList.add(trip)
                    genericCallback.onResponseReady(tripsList.toList())
                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d(TAG, "onChildChanged:" + dataSnapshot.key)

                val trip = dataSnapshot.getValue(Trip::class.java)!!
                trip.id = dataSnapshot.key!!
                var isParticipant = false
                trip.participants.forEach { it2 ->
                    if (it2.username == username) isParticipant = true
                }
                var b = false
                var myIndex = -1
                tripsList.forEachIndexed { index, it ->
                    if (it.id == trip.id) {
                        b = true
                        myIndex = index
                        return@forEachIndexed
                    }
                }
                if (TimeUnit.MILLISECONDS.toDays(trip.date - System.currentTimeMillis()) >= 0 && !trip.done && ((trip.organizer.username == username && !trip.active) || isParticipant)) {
                    if (!b) {
                        tripsList.add(trip)
                        genericCallback.onResponseReady(tripsList.toList())
                    }
                    else {
                        tripsList[myIndex] = trip
                        genericCallback.onResponseReady(tripsList.toList())
                    }
                }
                if (myIndex != -1) {
                    tripsList.removeAt(myIndex)
                    genericCallback.onResponseReady(tripsList.toList())
                }
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.key)

                val id = dataSnapshot.key!!
                tripsList.forEachIndexed { index, it ->
                    if (it.id == id) {
                        tripsList.removeAt(index)
                        genericCallback.onResponseReady(tripsList.toList())
                    }
                }
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.key)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "postMessages:onCancelled", databaseError.toException())
            }
        }
        firebaseReference.child(TRIPS).addChildEventListener(childEventListener)
    }

    fun getPastTripsFirebase(genericCallback: GenericCallback<List<Trip>>) {
        val tripsList: MutableList<Trip> = mutableListOf()
        childEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                val trip = dataSnapshot.getValue(Trip::class.java)
                trip?.id = dataSnapshot.key!!
                var isParticipant = false
                trip?.participants?.forEach { it2 ->
                    if (it2.username == username) isParticipant = true
                }
                if (((TimeUnit.MILLISECONDS.toDays(trip?.date!! - System.currentTimeMillis()) < 0 || trip.done) && (trip.organizer.username == username || isParticipant))) {
                    tripsList.add(trip)
                }
                genericCallback.onResponseReady(tripsList.toList())
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d(TAG, "onChildChanged:" + dataSnapshot.key)

                val trip = dataSnapshot.getValue(Trip::class.java)!!
                trip.id = dataSnapshot.key!!
                var isParticipant = false
                trip.participants.forEach { it2 ->
                    if (it2.username == username) isParticipant = true
                }
                if ((TimeUnit.MILLISECONDS.toDays(trip.date - System.currentTimeMillis()) < 0 || trip.done) && (trip.organizer.username == username || isParticipant)) {
                    tripsList.add(trip)
                    genericCallback.onResponseReady(tripsList.toList())
                } else {
                    var myIndex = -1
                    tripsList.forEachIndexed { index, it ->
                        if (it.id == trip.id) {
                            if ((TimeUnit.MILLISECONDS.toDays(trip.date - System.currentTimeMillis()) < 0 || trip.done) && (trip.organizer.username == username || isParticipant)) {
                                tripsList[index] = trip
                                genericCallback.onResponseReady(tripsList.toList())
                            } else myIndex = index
                            return@forEachIndexed
                        }
                    }
                    if (myIndex != -1) {
                        tripsList.removeAt(myIndex)
                        genericCallback.onResponseReady(tripsList.toList())
                    }
                }
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.key)

                val id = dataSnapshot.key!!
                tripsList.forEachIndexed { index, it ->
                    if (it.id == id) {
                        tripsList.removeAt(index)
                        genericCallback.onResponseReady(tripsList.toList())
                    }
                }
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.key)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "postMessages:onCancelled", databaseError.toException())
            }
        }
        firebaseReference.child(TRIPS).addChildEventListener(childEventListener)
    }

    fun getActiveTripFirebase(genericCallback: GenericCallback<Trip?>) {
        firebaseReference.child(TRIPS).addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    val trip = it.getValue(Trip::class.java)
                    trip?.id = it.key!!
                    if (trip?.active!! && trip.organizer.username == username) {
                        genericCallback.onResponseReady(trip)
                    }
                }
//                genericCallback.onResponseReady(null)
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG, "onCancelled: ${p0.message}")
//                genericCallback.onResponseReady(null)
            }

        })
    }

    fun getTripFirebase(genericCallback: GenericCallback<Trip>, tripId: String) {
        val tripRef = firebaseReference.child(TRIPS).child(tripId)
        tripRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val trip = p0.getValue(Trip::class.java)
                trip?.id = tripId
                val geoFire = GeoFire(tripRef)
                geoFire.getLocation(tripId, object : LocationCallback {
                    override fun onLocationResult(
                        key: String?,
                        location: GeoLocation?
                    ) {
                        if (location != null) {
                            trip?.location = location
                            genericCallback.onResponseReady(trip!!)
                        } else {
                            Log.e(
                                TAG,
                                "There is no location for key %s in GeoFire $key"
                            )
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.e(
                            TAG,
                            "There was an error getting the GeoFire location: $databaseError"
                        )
                    }
                })
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG, "p0, onCancelled, getTripFirebase: " + p0.message)
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
            trip.location = GeoLocation(address.latitude, address.longitude)
            if ((trip.participants.contains(User(username)))) {
                tripStateList.add(
                    TripState.PART_HAS_ORGANIZER
                )
            } else {
                trip.organizer = User(username)

                val userRef = firebaseReference.child(USERS)
                val tripRef =
                    firebaseReference.child(TRIPS)
                        .push()
                tripRef.child(ORGANIZER).child(
                    USERNAME
                ).setValue(username)
                tripRef.child(TITLE).setValue(trip.title)
                tripRef.child(ADDRESS).setValue(trip.address)
                tripRef.child(DATE).setValue(trip.date)
                tripRef.child(DESCRIPTION)
                    .setValue(trip.description)
                tripRef.child(DONE).setValue(false)
                val tripParticipantsRef = tripRef.child(PARTICIPANTS)
                trip.participants.forEachIndexed { index, it ->
                    tripParticipantsRef.child(index.toString()).child(
                        USERNAME
                    ).setValue(it.username)
                }
                val id = tripRef.key
                val geoFire = GeoFire(tripRef)
                geoFire.setLocation(
                    id,
                    GeoLocation(trip.location.latitude, trip.location.longitude)
                )
                userRef.child(username).child(ORGANIZER)
                    .child(id!!)
                    .setValue(true)
                trip.participants.forEach {
                    userRef.child(it.username)
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

    fun startTrip(tripId: String) {
        firebaseReference.child(TRIPS).child(tripId).child(ACTIVE).setValue(true)
    }

    fun stopTrip(tripId: String) {
        firebaseReference.child(TRIPS).child(tripId).child(ACTIVE).setValue(false)
        firebaseReference.child(TRIPS).child(tripId).child(DONE).setValue(true)
    }

    fun updateLocationFirebase(
        tripId: String,
        location: Location,
        organizer: Boolean,
        tripParticipantIndex: Int
    ) {
        if (organizer) {
            val organizerRef = firebaseReference.child(TRIPS).child(tripId).child(ORGANIZER)
            val geoFire = GeoFire(organizerRef)
            geoFire.setLocation(
                LOCATION,
                GeoLocation(location.latitude, location.longitude)
            )
        } else {
            val participantRef = firebaseReference.child(TRIPS).child(tripId).child(PARTICIPANTS)
                .child(tripParticipantIndex.toString())
            val geoFire = GeoFire(participantRef)
            geoFire.setLocation(
                LOCATION,
                GeoLocation(location.latitude, location.longitude)
            )
        }
    }

    fun getTripParticipantFirebase(tripId: String, genericCallback: GenericCallback<List<User>>) {
        val participantsList: MutableList<User> = mutableListOf()
        val tripRef = firebaseReference.child(TRIPS).child(tripId).child(PARTICIPANTS)
//        tripRef.addChildEventListener(object :ChildEventListener{
//            override fun onCancelled(p0: DatabaseError) {
//                //NOP
//            }
//
//            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
//                //NOP
//            }
//
//            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
//                val innerUser = p0.getValue(User::class.java)!!
//                val index = p0.key!!
//                val geoFire = GeoFire(tripRef.child(index))
//                geoFire.getLocation(LOCATION, object : LocationCallback {
//                    override fun onLocationResult(
//                        key: String?,
//                        location: GeoLocation?
//                    ) {
//                        if (location != null) {
//                            innerUser.userLocation = location
//                        } else {
//                            Log.e(
//                                TAG,
//                                "There is no location for key %s in GeoFire $key"
//                            )
//                        }
//                    }
//
//                    override fun onCancelled(databaseError: DatabaseError) {
//                        Log.e(
//                            TAG,
//                            "There was an error getting the GeoFire location: $databaseError"
//                        )
//                    }
//                })
//                participantsList.add(innerUser)
//            }
//
//            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
//                //NOP
//            }
//
//            override fun onChildRemoved(p0: DataSnapshot) {
//                //NOP
//            }
//
//        })
        tripRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p1: DataSnapshot) {
                p1.children.forEachIndexed { index, it ->
                    val innerUser = it.getValue(User::class.java)!!
                    val geoFire = GeoFire(tripRef.child(index.toString()))
                    geoFire.getLocation(LOCATION, object : LocationCallback {
                        override fun onLocationResult(
                            key: String?,
                            location: GeoLocation?
                        ) {
                            if (location != null) {
                                innerUser.userLocation = location
                            } else {
                                Log.e(
                                    TAG,
                                    "There is no location for key %s in GeoFire $key"
                                )
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            Log.e(
                                TAG,
                                "There was an error getting the GeoFire location: $databaseError"
                            )
                        }
                    })
                    participantsList.add(innerUser)
                }
                genericCallback.onResponseReady(participantsList)
            }

            override fun onCancelled(p1: DatabaseError) {
                Log.d(
                    TAG,
                    "p1, onCancelled, getTripParticipantFirebase: " + p1.message
                )
            }
        })
    }

    fun getOrganizerLocationFirebase(tripId: String, genericCallback: GenericCallback<LatLng>) {
        val tripRef = firebaseReference.child(TRIPS).child(tripId).child(ORGANIZER)
        tripRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p1: DataSnapshot) {
                val innerUser = p1.getValue(User::class.java)!!
                val geoFire = GeoFire(tripRef)
                geoFire.getLocation(LOCATION, object : LocationCallback {
                    override fun onLocationResult(
                        key: String?,
                        location: GeoLocation?
                    ) {
                        if (location != null) {
                            genericCallback.onResponseReady(
                                LatLng(
                                    location.latitude,
                                    location.longitude
                                )
                            )
                        } else {
                            Log.e(
                                TAG,
                                "There is no location for key %s in GeoFire $key"
                            )
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.e(
                            TAG,
                            "There was an error getting the GeoFire location: $databaseError"
                        )
                    }
                })
            }

            override fun onCancelled(p1: DatabaseError) {
                Log.d(
                    TAG,
                    "p1, onCancelled, getTripParticipantFirebase: " + p1.message
                )
            }
        })
    }

    fun isTripActiveFirebase(tripId: String, genericCallback: GenericCallback<Boolean>) {
        firebaseReference.child(TRIPS).child(tripId).child(ACTIVE)
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(p0: DataSnapshot) {
                    genericCallback.onResponseReady(p0.value as Boolean)
                }

                override fun onCancelled(p0: DatabaseError) {
                    Log.d(TAG, "onCancelled: ${p0.message}")
                }

            })
    }

    fun removeListenersFirebase() {
        firebaseReference.child(TRIPS).removeEventListener(childEventListener)
    }

    companion object {
        private var INSTANCE: TripRepository? = null
        fun getInstance() = INSTANCE
            ?: TripRepository().also {
                INSTANCE = it
            }
    }
}
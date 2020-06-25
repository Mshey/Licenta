package com.example.licenta.viewmodel

import android.content.Context
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.licenta.model.GenericCallback
import com.example.licenta.model.Trip
import com.example.licenta.model.TripState
import com.example.licenta.model.User
import com.example.licenta.model.repository.TripRepository

class TripViewModel : ViewModel() {
    private val tripRepository = TripRepository.getInstance()

    var futureTripsMutableLiveData: MutableLiveData<List<Trip>> = MutableLiveData()
    var pastTripsMutableLiveData: MutableLiveData<List<Trip>> = MutableLiveData()
    var tripMutableLiveData: MutableLiveData<Trip> = MutableLiveData()

    var newTripLiveData: LiveData<List<TripState>>? = null
    var userLiveData: LiveData<Boolean>? = null

    var tripParticipants: MutableLiveData<List<User>> = MutableLiveData()

    var tripStoppedLiveData: MutableLiveData<Boolean> = MutableLiveData()

    fun getFutureTrips() {
        tripRepository
            .getFutureTripsFirebase(object : GenericCallback<List<Trip>> {
                override fun onResponseReady(generic: List<Trip>) {
                    futureTripsMutableLiveData.value = generic
                }
            })
    }

    fun getPastOrganizedTrips() {
        tripRepository
            .getPastTripsFirebase(object : GenericCallback<List<Trip>> {
                override fun onResponseReady(generic: List<Trip>) {
                    pastTripsMutableLiveData.value = generic
                }
            })
    }

    fun getTrip(tripId: String) {
        tripRepository.getTripFirebase(object : GenericCallback<Trip> {
            override fun onResponseReady(generic: Trip) {
                tripMutableLiveData.value = generic
            }
        }, tripId)
    }

    fun startTrip(tripId: String) {
        tripRepository.startTrip(tripId)
    }

    fun stopTrip(tripId: String) {
        tripRepository.stopTrip(tripId, object : GenericCallback<Boolean> {
            override fun onResponseReady(generic: Boolean) {
                tripStoppedLiveData.value = generic
            }
        })
    }

    fun updateTrip(
        tripId: String,
        location: Location,
        organizer: Boolean,
        tripParticipantIndex: Int
    ) {
        tripRepository.updateLocation(tripId, location, organizer, tripParticipantIndex)
    }

    fun checkIfUserExists(user: String) {
        userLiveData = tripRepository.checkIfUserExistsFirebase(user)
    }

    fun addNewTrip(trip: Trip, context: Context) {
        newTripLiveData = tripRepository.addNewTripToFirebase(trip, context)
    }

    fun getParticipants(tripId: String) {
        tripRepository.getTripParticipantFirebase(tripId, object : GenericCallback<List<User>> {
            override fun onResponseReady(generic: List<User>) {
                tripParticipants.value = generic
            }
        })
    }

    fun removeListeners() {
        tripRepository.removeListenersFirebase()
    }
}
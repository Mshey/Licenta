package com.example.licenta.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.licenta.model.GenericCallback
import com.example.licenta.model.Trip
import com.example.licenta.model.TripState
import com.example.licenta.model.repository.TripRepository

class TripViewModel : ViewModel() {
    var futureOrganizedTripsMutableLiveData: MutableLiveData<List<Trip>> = MutableLiveData()
    var futureParticipantTripsMutableLiveData: MutableLiveData<List<Trip>> = MutableLiveData()
    var pastOrganizedTripsMutableLiveData: MutableLiveData<List<Trip>> = MutableLiveData()
    var pastParticipantTripsMutableLiveData: MutableLiveData<List<Trip>> = MutableLiveData()
    var newTripLiveData: LiveData<List<TripState>>? = null
    var userLiveData: LiveData<Boolean>? = null

    fun getFutureOrganizedTrips() {
        TripRepository.getInstance()
            .getFutureOrganizedTripsFirebase(object : GenericCallback<List<Trip>> {
                override fun onResponseReady(generic: List<Trip>) {
                    futureOrganizedTripsMutableLiveData.value = generic
                }
            })
    }

    fun getFutureParticipantTrips() {
        TripRepository.getInstance()
            .getFutureParticipantTripsFirebase(object : GenericCallback<List<Trip>> {
                override fun onResponseReady(generic: List<Trip>) {
                    futureParticipantTripsMutableLiveData.value = generic
                }
            })
    }

    fun getPastOrganizedTrips() {
        TripRepository.getInstance()
            .getPastOrganizedTripsFirebase(object : GenericCallback<List<Trip>> {
                override fun onResponseReady(generic: List<Trip>) {
                    pastOrganizedTripsMutableLiveData.value = generic
                }
            })
    }

    fun getPastParticipantTrips() {
        TripRepository.getInstance()
            .getPastParticipantTripsFirebase(object : GenericCallback<List<Trip>> {
                override fun onResponseReady(generic: List<Trip>) {
                    pastParticipantTripsMutableLiveData.value = generic
                }
            })
    }

    fun checkIfUserExists(user: String) {
        userLiveData = TripRepository.getInstance().checkIfUserExistsFirebase(user)
    }

    fun addNewTrip(trip: Trip, context: Context) {
        newTripLiveData = TripRepository.getInstance().addNewTripToFirebase(trip, context)
    }
}
package com.example.licenta.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.licenta.R
import com.example.licenta.Utils.TAG
import com.example.licenta.Utils.TRIP
import com.example.licenta.Utils.convertLongToTime
import com.example.licenta.Utils.username
import com.example.licenta.model.Trip
import kotlinx.android.synthetic.main.fragment_trip_details.*
import java.lang.StringBuilder
import java.util.concurrent.TimeUnit

class TripDetailsFragment : Fragment() {
    private lateinit var mView: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        mView = inflater.inflate(R.layout.fragment_trip_details, container, false)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val trip: Trip = arguments?.getSerializable(TRIP) as Trip

        trip_details_title.text = trip.title
        trip_details_organizer.text = trip.organizer

        trip_details_address_text_view.text = trip.address
        trip_details_description_text_view.text = trip.description
        trip_details_date_text_view.text = convertLongToTime(trip.date)
        val participantsString = StringBuilder()
        var divider = ""
        trip.participants.forEach {
            Log.d(TAG, "part: $it")
            participantsString.append(divider).append(it)
            divider = "\n"
        }
        Log.d(TAG, participantsString.toString())
        trip_details_participants_text_view.text = participantsString.toString()

        if (TimeUnit.MILLISECONDS.toDays(trip.date - System.currentTimeMillis()) == 0.toLong() && trip.organizer == username) {
            start_trip_button.visibility = VISIBLE
        }
    }
}

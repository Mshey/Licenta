package com.example.licenta.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.licenta.NavEvent
import com.example.licenta.R
import com.example.licenta.Utils.convertLongToTime
import com.example.licenta.Utils.username
import com.example.licenta.model.User
import com.example.licenta.viewmodel.TripViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.processors.PublishProcessor
import kotlinx.android.synthetic.main.fragment_trip_details.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class TripDetailsFragment : Fragment() {
    @Inject
    lateinit var navEvents: PublishProcessor<NavEvent>
    private lateinit var mView: View
    private lateinit var tripViewModel: TripViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        mView = inflater.inflate(R.layout.fragment_trip_details, container, false)
        tripViewModel = ViewModelProvider(requireActivity()).get(
            "details",
            TripViewModel::class.java
        )
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var tripId: String
        setFragmentResultListener(getString(R.string.trip)) { _, bundle ->
            tripId = bundle.getString(getString(R.string.trip))!!
            tripViewModel.getTrip(tripId)
        }

        tripViewModel.tripMutableLiveData.observe(viewLifecycleOwner, Observer { trip ->
            trip_details_title.text = trip.title
            trip_details_organizer.text = trip.organizer.username

            trip_details_address_text_view.text = trip.address
            trip_details_description_text_view.text = trip.description
            trip_details_date_text_view.text = convertLongToTime(trip.date)
            val participantsString = StringBuilder()
            var divider = ""
            trip.participants.forEach {
                participantsString.append(divider).append(it.username)
                divider = "\n"
            }
            trip_details_participants_text_view.text = participantsString.toString()

            if (TimeUnit.MILLISECONDS.toDays(trip.date - System.currentTimeMillis()) == 0.toLong() && trip.organizer.username == username && !trip.done) {
                start_trip_button.visibility = VISIBLE
                if (trip.active) start_trip_button.text = "Reenter trip"
                start_trip_button.setOnClickListener {
                    tripViewModel.startTrip(trip.id)
                    setFragmentResult(
                        "details", bundleOf(
                            getString(R.string.trip_id) to trip.id,
                            getString(R.string.trip_org) to true,
                            "trip lat" to trip.location.latitude,
                            "trip lng" to trip.location.longitude
                        )
                    )
                    navEvents.onNext(NavEvent(NavEvent.Destination.MAP))
                }
            }

            val myUser = User(username)
            if (trip.active && trip.participants.contains(myUser)) {
                start_trip_button.visibility = VISIBLE
                start_trip_button.text = getString(R.string.join_trip)
                start_trip_button.setOnClickListener {
                    setFragmentResult(
                        "details", bundleOf(
                            getString(R.string.trip_id) to trip.id,
                            getString(R.string.trip_org) to false,
                            "participant index" to trip.participants.indexOf(myUser)
                        )
                    )
                    navEvents.onNext(NavEvent(NavEvent.Destination.MAP))
                }
            }
        })
    }
}

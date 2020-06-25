package com.example.licenta.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.example.licenta.R
import com.example.licenta.Utils.convertLongToTime
import com.example.licenta.Utils.username
import com.example.licenta.model.User
import com.example.licenta.viewmodel.TripViewModel
import kotlinx.android.synthetic.main.fragment_trip_details.*
import java.util.concurrent.TimeUnit

class TripDetailsFragment : Fragment() {
    private lateinit var mView: View
    private lateinit var tripViewModel: TripViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        mView = inflater.inflate(R.layout.fragment_trip_details, container, false)
        tripViewModel = ViewModelProvider(this).get(
            "details",
            TripViewModel::class.java
        )
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tripId: String = arguments?.getString(getString(R.string.trip))!!
        tripViewModel.getTrip(tripId)
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
                start_trip_button.setOnClickListener {
                    tripViewModel.startTrip(trip.id)
                    val bundle = Bundle()
                    bundle.putString(getString(R.string.trip_id), trip.id)
                    bundle.putBoolean(getString(R.string.trip_org), true)
                    bundle.putDouble("trip lat", trip.location.latitude)
                    bundle.putDouble("trip lng", trip.location.longitude)
                    val navController =
                        Navigation.findNavController(requireActivity(), R.id.my_nav_host_fragment)
                    navController.navigate(R.id.action_tripDetailsFragment_to_mapFragment, bundle)
                }
            }

            val myUser = User(username)
            if (trip.active && trip.participants.contains(myUser)) {
                start_trip_button.visibility = VISIBLE
                start_trip_button.text = getString(R.string.join_trip)
                val bundle = Bundle()
                bundle.putString(getString(R.string.trip_id), trip.id)
                bundle.putBoolean(getString(R.string.trip_org), false)
                bundle.putInt("participant index", trip.participants.indexOf(myUser))
                start_trip_button.setOnClickListener {
                    val navController =
                        Navigation.findNavController(requireActivity(), R.id.my_nav_host_fragment)
                    navController.navigate(R.id.action_tripDetailsFragment_to_mapFragment, bundle)
                }
            }
        })
    }
}

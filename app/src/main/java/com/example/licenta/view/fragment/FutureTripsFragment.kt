package com.example.licenta.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.RecyclerView
import com.example.licenta.OnDataClickListener
import com.example.licenta.R
import com.example.licenta.Utils.TAG
import com.example.licenta.Utils.TRIP
import com.example.licenta.model.Trip
import com.example.licenta.view.adapter.TripItemAdapter
import com.example.licenta.viewmodel.TripViewModel
import kotlinx.android.synthetic.main.fragment_future_trips.*


class FutureTripsFragment : Fragment() {
    private lateinit var mView: View
    private lateinit var tripViewModel: TripViewModel
    private lateinit var adapter: TripItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        mView = inflater.inflate(R.layout.fragment_future_trips, container, false)
        tripViewModel = ViewModelProvider(this).get(
            "future",
            TripViewModel::class.java
        )
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = TripItemAdapter(object : OnDataClickListener<Trip> {
            override fun onClick(ob: Trip) {
                val bundle = Bundle()
                bundle.putSerializable(TRIP, ob)
                Navigation.findNavController(activity!!, R.id.my_nav_host_fragment)
                    .navigate(R.id.action_futureTripsFragment_to_tripDetailsFragment, bundle)
            }
        })

        val futureTripsRecyclerView =
            mView.findViewById<RecyclerView>(R.id.future_trips_recyclerview)

        futureTripsRecyclerView.adapter = adapter
        futureTripsRecyclerView.layoutManager = LinearLayoutManager(context, VERTICAL, false)
        val dividerItemDecoration = DividerItemDecoration(
            futureTripsRecyclerView.context,
            VERTICAL
        )
        futureTripsRecyclerView.addItemDecoration(dividerItemDecoration)
        futureTripsRecyclerView.setHasFixedSize(true)

        tripViewModel.getFutureOrganizedTrips()
        tripViewModel.futureOrganizedTripsMutableLiveData.observe(viewLifecycleOwner, Observer {
            adapter.addTripsToList(it)
            Log.d(TAG, "emese: futureOrganizedTripsMutableLiveData")
        })

        tripViewModel.getFutureParticipantTrips()
        tripViewModel.futureParticipantTripsMutableLiveData.observe(viewLifecycleOwner, Observer {
            adapter.addTripsToList(it)
            Log.d(TAG, "emese: futureParticipantTripsMutableLiveData")
        })


        add_trip.setOnClickListener {
            val navController =
                Navigation.findNavController(requireActivity(), R.id.my_nav_host_fragment)
            navController.navigate(R.id.action_futureTripsFragment_to_createNewTripFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        adapter.clearTripList()
    }
}
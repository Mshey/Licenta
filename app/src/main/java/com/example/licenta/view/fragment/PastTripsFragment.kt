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
import androidx.recyclerview.widget.RecyclerView
import com.example.licenta.OnDataClickListener
import com.example.licenta.R
import com.example.licenta.Utils.TAG
import com.example.licenta.Utils.TRIP
import com.example.licenta.model.Trip
import com.example.licenta.view.adapter.TripItemAdapter
import com.example.licenta.viewmodel.TripViewModel

class PastTripsFragment : Fragment() {
    private lateinit var mView: View
    private lateinit var adapter: TripItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        mView = inflater.inflate(R.layout.fragment_past_trips, container, false)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tripViewModel = ViewModelProvider(this).get(
            "past",
            TripViewModel::class.java
        )

        adapter = TripItemAdapter(object : OnDataClickListener<Trip> {
            override fun onClick(ob: Trip) {
                val bundle = Bundle()
                bundle.putSerializable(TRIP, ob)
                Navigation.findNavController(activity!!, R.id.my_nav_host_fragment)
                    .navigate(R.id.action_pastTripsFragment_to_tripDetailsFragment, bundle)
            }
        })
        val pastTripsRecyclerView =
            mView.findViewById<RecyclerView>(R.id.past_trips_recyclerview)
        pastTripsRecyclerView.adapter = adapter
        pastTripsRecyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        val dividerItemDecoration = DividerItemDecoration(
            pastTripsRecyclerView.context,
            LinearLayoutManager.VERTICAL
        )
        pastTripsRecyclerView.addItemDecoration(dividerItemDecoration)
        pastTripsRecyclerView.setHasFixedSize(true)

        tripViewModel.getPastOrganizedTrips()
        tripViewModel.pastOrganizedTripsMutableLiveData.observe(viewLifecycleOwner, Observer {
            adapter.addTripsToList(it)
            Log.d(TAG, "emese: pastOrganizedTripsMutableLiveData")
        })

        tripViewModel.getPastParticipantTrips()
        tripViewModel.pastParticipantTripsMutableLiveData.observe(viewLifecycleOwner, Observer {
            adapter.addTripsToList(it)
            Log.d(TAG, "emese: pastParticipantTripsMutableLiveData")
        })
    }

    override fun onResume() {
        super.onResume()
        adapter.clearTripList()
    }
}
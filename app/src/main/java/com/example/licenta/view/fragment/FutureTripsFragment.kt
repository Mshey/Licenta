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
        tripViewModel = ViewModelProvider(requireActivity()).get(
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
                bundle.putString(getString(R.string.trip), ob.id)
                Navigation.findNavController(activity!!, R.id.my_nav_host_fragment)
                    .navigate(R.id.action_futureTripsFragment_to_tripDetailsFragment, bundle)
            }
        })

        val futureOrganizedTripsRecyclerView =
            mView.findViewById<RecyclerView>(R.id.future_trips_recyclerview)

        futureOrganizedTripsRecyclerView.adapter = adapter
        futureOrganizedTripsRecyclerView.layoutManager =
            LinearLayoutManager(context, VERTICAL, false)
        val dividerItemDecoration = DividerItemDecoration(
            futureOrganizedTripsRecyclerView.context,
            VERTICAL
        )
        futureOrganizedTripsRecyclerView.addItemDecoration(dividerItemDecoration)
        futureOrganizedTripsRecyclerView.setHasFixedSize(true)


        tripViewModel.getFutureTrips()
        tripViewModel.futureTripsMutableLiveData.observe(viewLifecycleOwner, Observer {
            it.forEach { it2 ->
                if (it2.active && !it2.done) {
                    val bundle = Bundle()
                    bundle.putString(getString(R.string.trip_id), it2.id)
                    bundle.putBoolean(getString(R.string.trip_org), true)
                    val navController =
                        Navigation.findNavController(requireActivity(), R.id.my_nav_host_fragment)
                    navController.navigate(R.id.action_futureTripsFragment_to_mapFragment, bundle)
                }
            }
            adapter.addTripsToList(it)
        })

        add_trip.setOnClickListener {
            val navController =
                Navigation.findNavController(requireActivity(), R.id.my_nav_host_fragment)
            navController.navigate(R.id.action_futureTripsFragment_to_createNewTripFragment)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tripViewModel.removeListeners()
    }
}
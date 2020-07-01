package com.example.licenta.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.RecyclerView
import com.example.licenta.model.NavEvent
import com.example.licenta.view.OnDataClickListener
import com.example.licenta.R
import com.example.licenta.model.Trip
import com.example.licenta.view.adapter.TripItemAdapter
import com.example.licenta.viewmodel.TripViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.processors.PublishProcessor
import kotlinx.android.synthetic.main.fragment_future_trips.*
import javax.inject.Inject

@AndroidEntryPoint
class FutureTripsFragment : Fragment() {
    @Inject
    lateinit var navEvents: PublishProcessor<NavEvent>
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
//        tripViewModel.getActiveTrip()
//        tripViewModel.tripMutableLiveData.observe(viewLifecycleOwner, Observer {
//            setFragmentResult("toMaps", bundleOf(getString(R.string.trip_id) to it.id, getString(R.string.trip_org) to true))
//            navEvents.onNext(NavEvent(NavEvent.Destination.MAP))
//        })
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = TripItemAdapter(object :
            OnDataClickListener<Trip> {
            override fun onClick(ob: Trip) {
                setFragmentResult(getString(R.string.trip), bundleOf(getString(R.string.trip) to ob.id))
                navEvents.onNext(
                    NavEvent(
                        NavEvent.Destination.DETAILS
                    )
                )
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
            adapter.addTripsToList(it)
        })

        add_trip.setOnClickListener {
            navEvents.onNext(
                NavEvent(
                    NavEvent.Destination.CREATE
                )
            )
        }
    }
}
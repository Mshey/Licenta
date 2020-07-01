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
import androidx.recyclerview.widget.RecyclerView
import com.example.licenta.model.NavEvent
import com.example.licenta.view.OnDataClickListener
import com.example.licenta.R
import com.example.licenta.model.Trip
import com.example.licenta.view.adapter.TripItemAdapter
import com.example.licenta.viewmodel.TripViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.processors.PublishProcessor
import javax.inject.Inject

@AndroidEntryPoint
class PastTripsFragment : Fragment() {
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
        mView = inflater.inflate(R.layout.fragment_past_trips, container, false)
        tripViewModel = ViewModelProvider(requireActivity()).get(
            "past",
            TripViewModel::class.java
        )
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = TripItemAdapter(object :
            OnDataClickListener<Trip> {
            override fun onClick(ob: Trip) {
//                val bundle = Bundle()
//                bundle.putString(getString(R.string.trip), ob.id)
                setFragmentResult(getString(R.string.trip), bundleOf(getString(R.string.trip) to ob.id))
                navEvents.onNext(
                    NavEvent(
                        NavEvent.Destination.DETAILS
                    )
                )
//                Navigation.findNavController(activity!!, R.id.my_nav_host_fragment)
//                    .navigate(R.id.action_pastTripsFragment_to_tripDetailsFragment, bundle)
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
        tripViewModel.pastTripsMutableLiveData.observe(viewLifecycleOwner, Observer {
            adapter.addTripsToList(it)
        })
    }

}
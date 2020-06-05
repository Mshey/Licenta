package com.example.licenta.view.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.licenta.OnDataClickListener
import com.example.licenta.R
import com.example.licenta.Utils.TAG
import com.example.licenta.Utils.username
import com.example.licenta.model.Trip
import kotlinx.android.synthetic.main.item_trip.view.*

class TripItemAdapter(
    private val dataClickListener: OnDataClickListener<Trip>
) :
    RecyclerView.Adapter<TripItemAdapter.ItemTripViewHolder>() {
    private var tripList: ArrayList<Trip> = ArrayList()

    class ItemTripViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.item_trip_title
        val descriptionTextView: TextView = itemView.item_trip_description
        val organizerTextView: TextView = itemView.item_trip_organizer
        val tripDetailsConstraintLayout: ConstraintLayout = itemView.trip_layout
        val mView = itemView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemTripViewHolder {
        Log.d(TAG, "onCreateView")
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.item_trip,
            parent, false
        )

        return ItemTripViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return tripList.size
    }

    override fun onBindViewHolder(holder: ItemTripViewHolder, position: Int) {
        val trip = tripList[position]
        holder.titleTextView.text = trip.title
        holder.descriptionTextView.text = trip.description
        holder.organizerTextView.text = trip.organizer
        if (trip.organizer == username) holder.tripDetailsConstraintLayout.setBackgroundResource(R.drawable.item_trip_with_border)
        holder.mView.setOnClickListener {
            dataClickListener.onClick(trip)
        }
    }

    fun addToTripList(trip: Trip) {
        tripList.add(trip)
        notifyDataSetChanged()
    }

    fun addTripsToList(trips: List<Trip>) {
        tripList.addAll(trips)
        notifyDataSetChanged()
    }

    fun clearTripList() {
        val size = itemCount
        tripList.clear()
        notifyItemRangeRemoved(0, size)
    }
}
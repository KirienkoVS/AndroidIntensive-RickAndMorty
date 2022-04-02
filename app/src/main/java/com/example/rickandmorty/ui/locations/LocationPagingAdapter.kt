package com.example.rickandmorty.ui.locations

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.navigation.findNavController
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.rickandmorty.R
import com.example.rickandmorty.model.LocationData

class LocationPagingAdapter: PagingDataAdapter<LocationData, RecyclerView.ViewHolder>(LOCATION_COMPARATOR) {

    class LocationViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private val name: TextView = view.findViewById(R.id.location_name)
        private val type: TextView = view.findViewById(R.id.location_type)
        private val dimension: TextView = view.findViewById(R.id.location_dimension)

        fun bind(location: LocationData) {
            name.text = location.name
            type.text = location.type
            dimension.text = location.dimension
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return LocationViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.location_item, parent, false))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        getItem(position)?.let { location ->
            (holder as LocationViewHolder).bind(location)

            holder.itemView.setOnClickListener { view ->
                val action = LocationsFragmentDirections.actionLocationsPageToLocationDetailsFragment(locationID = location.id)
                view.findNavController().navigate(action)
            }
        }
    }

    companion object {
        private val LOCATION_COMPARATOR = object : DiffUtil.ItemCallback<LocationData>() {
            override fun areItemsTheSame(oldItem: LocationData, newItem: LocationData): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: LocationData, newItem: LocationData): Boolean {
                return oldItem == newItem
            }
        }
    }

}
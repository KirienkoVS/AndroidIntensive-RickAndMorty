package com.example.rickandmorty.ui.episodes

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
import com.example.rickandmorty.model.EpisodeData

class EpisodePagingAdapter: PagingDataAdapter<EpisodeData, RecyclerView.ViewHolder>(EPISODE_COMPARATOR) {

    class EpisodeViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private val name: TextView = view.findViewById(R.id.episode_name)
        private val episodeNumber: TextView = view.findViewById(R.id.episode_episode)
        private val airDate: TextView = view.findViewById(R.id.episode_date)

        fun bind(episode: EpisodeData) {
            name.text = episode.name
            episodeNumber.text = episode.episodeNumber
            airDate.text = episode.airDate
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return EpisodeViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.episode_item, parent, false))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        getItem(position)?.let { episodeData ->
            (holder as EpisodeViewHolder).bind(episodeData)
        }

        holder.itemView.setOnClickListener { view ->
            val episodeID = getItem(position)?.id
            if (episodeID != null) {
                val action = EpisodesFragmentDirections.actionEpisodesPageToEpisodeDetailsFragment(episodeID = episodeID)
                view.findNavController().navigate(action)
            }
        }
    }

    companion object {
        private val EPISODE_COMPARATOR = object : DiffUtil.ItemCallback<EpisodeData>() {
            override fun areItemsTheSame(oldItem: EpisodeData, newItem: EpisodeData): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: EpisodeData, newItem: EpisodeData): Boolean {
                return oldItem == newItem
            }
        }
    }

}
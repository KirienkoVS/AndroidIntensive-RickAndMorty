package com.example.rickandmorty.ui.characters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.rickandmorty.R

class CharacterRecyclerViewAdapter(private var episodeList: List<String>): Adapter<ViewHolder>() {

    class CharacterEpisodeViewHolder(view: View): ViewHolder(view) {
        val episode: TextView = view.findViewById(R.id.character_episode_item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return CharacterEpisodeViewHolder(LayoutInflater.from(parent.context).inflate(
            R.layout.character_episode_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val characterEpisode = episodeList[position]
        (holder as CharacterEpisodeViewHolder).episode.text = characterEpisode
    }

    override fun getItemCount() = episodeList.size
}
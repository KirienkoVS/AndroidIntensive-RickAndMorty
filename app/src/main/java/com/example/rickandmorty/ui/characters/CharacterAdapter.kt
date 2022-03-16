package com.example.rickandmorty.ui.characters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rickandmorty.R
import com.example.rickandmorty.model.CharacterData

class CharacterAdapter: PagingDataAdapter<CharacterData, RecyclerView.ViewHolder>(CHARACTER_COMPARATOR) {

    class CharacterViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private val name: TextView = view.findViewById(R.id.character_name)
        private val species: TextView = view.findViewById(R.id.character_species)
        private val status: TextView = view.findViewById(R.id.character_status)
        private val gender: TextView = view.findViewById(R.id.character_gender)
        private val image: ImageView = view.findViewById(R.id.character_imageview)

        fun bind(character: CharacterData) {
            name.text = character.name
            species.text = character.species
            status.text = character.status
            gender.text = character.gender
            Glide.with(image).load(character.image).into(image)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return CharacterViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.character_item, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        getItem(position)?.let { characterData ->
            (holder as CharacterViewHolder).bind(characterData)
        }

//        holder.itemView.setOnClickListener {
//            getItem(position)?.let { it1 -> onClick(position, it1) }
//        }
    }

    companion object {
        private val CHARACTER_COMPARATOR = object : DiffUtil.ItemCallback<CharacterData>() {
            override fun areItemsTheSame(oldItem: CharacterData, newItem: CharacterData): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: CharacterData, newItem: CharacterData): Boolean {
                return oldItem == newItem
            }
        }
    }
}
package com.example.rickandmorty.ui.characters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.rickandmorty.R
import com.example.rickandmorty.databinding.LoadStateItemBinding
import com.example.rickandmorty.ui.characters.CharacterLoadStateAdapter.CharacterLoadStateViewHolder

class CharacterLoadStateAdapter(private val retry: () -> Unit) : LoadStateAdapter<CharacterLoadStateViewHolder>() {

    class CharacterLoadStateViewHolder(
        private val binding: LoadStateItemBinding,
        retry: () -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.retryButton.setOnClickListener { retry.invoke() }
        }

        fun bind(loadState: LoadState) {
            if (loadState is LoadState.Error) {
                binding.errorMsg.text = loadState.error.localizedMessage
            }
            binding.progressBar.isVisible = loadState is LoadState.Loading
            binding.retryButton.isVisible = loadState is LoadState.Error
            binding.errorMsg.isVisible = loadState is LoadState.Error
        }

        companion object {
            fun create(parent: ViewGroup, retry: () -> Unit): CharacterLoadStateViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.load_state_item, parent, false)
                val binding = LoadStateItemBinding.bind(view)
                return CharacterLoadStateViewHolder(binding, retry)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): CharacterLoadStateViewHolder {
        return CharacterLoadStateViewHolder.create(parent, retry)
    }

    override fun onBindViewHolder(holder: CharacterLoadStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }
}
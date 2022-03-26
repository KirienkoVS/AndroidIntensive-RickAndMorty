package com.example.rickandmorty.ui.episodes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.rickandmorty.R
import com.example.rickandmorty.databinding.EpisodesFragmentBinding

class EpisodesFragment : Fragment(R.layout.episodes_fragment)  {

    private var _binding: EpisodesFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        _binding = EpisodesFragmentBinding.inflate(inflater, container, false)

        val image = binding.image
        Glide.with(requireContext()).load("https://rickandmortyapi.com/api/character/avatar/3.jpeg").into(image)


        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
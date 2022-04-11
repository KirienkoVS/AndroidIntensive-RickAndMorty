package com.example.rickandmorty.ui.characters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rickandmorty.databinding.FragmentCharacterDetailsBinding
import com.example.rickandmorty.isOnline
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CharacterDetailsFragment : Fragment() {

    private var _binding: FragmentCharacterDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CharacterViewModel by viewModels()

    private lateinit var imageView: ImageView
    private lateinit var name: TextView
    private lateinit var species: TextView
    private lateinit var status: TextView
    private lateinit var gender: TextView
    private lateinit var type: TextView
    private lateinit var created: TextView
    private lateinit var originName: TextView
    private lateinit var locationName: TextView

    private var isOnline = true
    private var characterID = 0
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewAdapter: CharacterDetailsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCharacterDetailsBinding.inflate(inflater, container, false)

        isOnline = isOnline(requireContext())
        characterID = arguments?.getInt(CHARACTER_ID) ?: error("Should provide character ID")

        bindViews()
        setViews()
        initRecyclerView()
        showProgressBar()

        return binding.root
    }

    private fun bindViews() {
        with(binding) {
            imageView = characterImageView
            name = characterDetailsName
            species = characterDetailsSpecies
            status = characterDetailsStatus
            gender = characterDetailsGender
            type = characterDetailsType
            created = characterCreated
            originName = characterOriginName
            locationName = characterLocationName
            progressBar = episodeProgressBar
            recyclerView = characterRecyclerview
            recyclerViewAdapter = CharacterDetailsAdapter()
        }
    }

    private fun setViews() {
        viewModel.requestCharacterDetails(characterID)?.let {
            it.observe(viewLifecycleOwner) { character->
                if (character != null) {
                    name.text = character.name
                    species.text = character.species
                    status.text = character.status
                    gender.text = character.gender
                    type.text = character.type.ifBlank { "unknown" }
                    created.text = character.created.subSequence(0, 10)
                    originName.apply {
                        text = character.originName.ifBlank { "unknown" }
                        setOnClickListener { navigateToLocation(originName.text.toString()) }
                    }
                    locationName.apply {
                        text = character.locationName.ifBlank { "unknown" }
                        setOnClickListener { navigateToLocation(locationName.text.toString()) }
                    }
                    Glide.with(requireContext()).load(character.image).into(imageView)
                    preSaveCharacterLocations(listOf(
                        character.originName.ifBlank { "unknown" }, character.locationName.ifBlank { "unknown" }
                    ), isOnline)
                }
            }
        }
    }

    private fun initRecyclerView() {
        viewModel.requestCharacterDetails(characterID)?.let { characterLiveData ->
            characterLiveData.observe(viewLifecycleOwner) { characterData ->
                var episodeUrlList = emptyList<String>()
                if (characterData != null) {
                    episodeUrlList = characterData.episode
                }

                viewModel.requestCharacterEpisodes(episodeUrlList, isOnline)?.let { episodeLiveData ->
                    episodeLiveData.observe(viewLifecycleOwner) { episodeList ->
                        if (episodeList.isNotEmpty()) {
                            recyclerViewAdapter.episodeList = episodeList
                            recyclerView.adapter = recyclerViewAdapter
                            viewModel.setProgressBarVisibility(false)
                        } else {
                            binding.emptyEpisodes.visibility = View.VISIBLE
                            viewModel.setProgressBarVisibility(false)
                        }
                    }
                }
            }
        }
    }

    private fun showProgressBar() {
        viewModel.isProgressBarVisible.observe(viewLifecycleOwner) { isVisible ->
            when(isVisible) {
                true -> progressBar.visibility = View.VISIBLE
                false -> progressBar.visibility = View.GONE
            }
        }
    }

    private fun navigateToLocation(destination: String) {
        if (destination == "unknown") {
            Toast.makeText(activity, "Unknown location!", Toast.LENGTH_SHORT).show()
        } else {
            val action =
                CharacterDetailsFragmentDirections.actionCharacterDetailsFragmentToLocationDetailsFragment(locationName = destination)
            findNavController().navigate(action)
        }
    }

    private fun preSaveCharacterLocations(locations: List<String>, isOnline: Boolean) {
        viewModel.preSaveCharacterLocations(locations, isOnline)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        const val CHARACTER_ID = "characterID"
    }

}
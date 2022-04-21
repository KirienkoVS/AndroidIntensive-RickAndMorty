package com.example.rickandmorty.ui.characters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.rickandmorty.databinding.FragmentCharacterDetailsBinding
import com.example.rickandmorty.isOnline
import com.example.rickandmorty.model.CharacterData
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CharacterDetailsFragment : Fragment() {

    private var _binding: FragmentCharacterDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CharacterViewModel by viewModels()

    private var isOnline = true
    private var characterID = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCharacterDetailsBinding.inflate(inflater, container, false)

        isOnline = isOnline(requireContext())
        characterID = arguments?.getInt(CHARACTER_ID) ?: error("Should provide character ID")
        viewModel.requestCharacterDetails(characterID)

        showProgressBar()
        displayCharacterDetails()

        return binding.root
    }

    private fun displayCharacterDetails() {
        viewModel.characterDetails.observe(viewLifecycleOwner) { character ->
            if (character != null) {
                setUpViews(character)
                setUpRecyclerView(character)
            }
        }
    }

    private fun setUpViews(character: CharacterData) {
        with(binding) {
            characterDetailsName.text = character.name
            characterDetailsSpecies.text = character.species
            characterDetailsStatus.text = character.status
            characterDetailsGender.text = character.gender
            characterDetailsType.text = character.type.ifBlank { "unknown" }
            characterCreated.text = character.created.subSequence(0, 10)
            characterOriginName.apply {
                text = character.originName.ifBlank { "unknown" }
                setOnClickListener { navigateToLocation(this.text.toString()) }
            }
            characterLocationName.apply {
                text = character.locationName.ifBlank { "unknown" }
                setOnClickListener { navigateToLocation(this.text.toString()) }
            }
            Glide.with(requireContext()).load(character.image).into(imageView)
        }
    }

    private fun setUpRecyclerView(character: CharacterData) {
        val episodeUrlList = character.episode
        viewModel.requestCharacterEpisodes(episodeUrlList, isOnline)?.let { episodeLiveData ->
            episodeLiveData.observe(viewLifecycleOwner) { episodeList ->
                if (episodeList.isNotEmpty()) {
                    val recyclerViewAdapter = CharacterDetailsAdapter()
                    recyclerViewAdapter.episodeList = episodeList
                    binding.recyclerView.adapter = recyclerViewAdapter
                    viewModel.setProgressBarVisibility(false)
                } else {
                    binding.emptyEpisodes.visibility = View.VISIBLE
                    viewModel.setProgressBarVisibility(false)
                }
            }
        }
    }

    private fun showProgressBar() {
        viewModel.isProgressBarVisible.observe(viewLifecycleOwner) { isVisible ->
            when (isVisible) {
                true -> binding.progressBar.visibility = View.VISIBLE
                false -> binding.progressBar.visibility = View.GONE
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        const val CHARACTER_ID = "characterID"
    }

}
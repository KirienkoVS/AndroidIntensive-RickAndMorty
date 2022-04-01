package com.example.rickandmorty.ui.characters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rickandmorty.Injection
import com.example.rickandmorty.databinding.FragmentCharacterDetailsBinding

class CharacterDetailsFragment : Fragment() {

    private var _binding: FragmentCharacterDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CharacterViewModel

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
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewAdapter: CharacterDetailsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCharacterDetailsBinding.inflate(inflater, container, false)

        isOnline = Injection.isOnline(requireContext())
        characterID = arguments?.getInt(CHARACTER_ID) ?: error("Should provide character ID")

        initializeViewModel()
        bindViews()
        setViews()
        setUpRecyclerView()

        originName.setOnClickListener { navigateToLocation(originName.text.toString()) }
        locationName.setOnClickListener { navigateToLocation(locationName.text.toString()) }

        return binding.root
    }

    private fun initializeViewModel() {
        viewModel = ViewModelProvider(
            this,
            Injection.provideCharacterViewModelFactory(requireContext()))[CharacterViewModel::class.java]
    }

    private fun bindViews() {
        with(binding) {
            imageView = characterImageView
            name = characterName
            species = characterSpecies
            status = characterStatus
            gender = characterGender
            type = characterType
            created = characterCreated
            originName = characterOriginName
            locationName = characterLocationName
            recyclerView = characterRecyclerview
            recyclerViewAdapter = CharacterDetailsAdapter()
        }
    }

    private fun setViews() {
        viewModel.requestCharacterDetails(characterID)?.let {
            it.observe(viewLifecycleOwner) { character->
                name.text = character.name
                species.text = character.species
                status.text = character.status
                gender.text = character.gender
                type.text = character.type.ifBlank { "unknown" }
                created.text = character.created.subSequence(0, 10)
                originName.text = character.originName.ifBlank { "unknown" }
                locationName.text = character.locationName.ifBlank { "unknown" }
                Glide.with(requireContext()).load(character.image).into(imageView)

                requestCharacterLocation(character.locationName, character.originName, isOnline) // location empty string check?
            }
        }
    }

    private fun setUpRecyclerView() {
        viewModel.requestCharacterDetails(characterID)?.let { characterLiveData ->
            characterLiveData.observe(viewLifecycleOwner) { characterData ->
                val episodeUrlList = characterData.episode

                viewModel.requestCharacterEpisodes(episodeUrlList, isOnline)?.let { episodeLiveData ->
                    episodeLiveData.observe(viewLifecycleOwner) { episodeDataList ->
                        recyclerViewAdapter.episodeList = episodeDataList
                        recyclerView.adapter = recyclerViewAdapter
                    }
                }
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

    private fun requestCharacterLocation(location: String, origin: String, isOnline: Boolean) {
        viewModel.requestCharacterLocation(location, origin, isOnline)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        const val CHARACTER_ID = "characterID"
    }

}
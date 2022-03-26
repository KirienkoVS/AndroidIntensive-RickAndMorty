package com.example.rickandmorty.ui.characters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.rickandmorty.Injection
import com.example.rickandmorty.databinding.FragmentCharacterDetailsBinding

class CharacterDetailsFragment : Fragment() {

    private var _binding: FragmentCharacterDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CharacterViewModel

    private lateinit var imageView: ImageView
    private lateinit var imageUrl: TextView
    private lateinit var name: TextView
    private lateinit var species: TextView
    private lateinit var status: TextView
    private lateinit var gender: TextView
    private lateinit var type: TextView
    private lateinit var url: TextView
    private lateinit var created: TextView
    private lateinit var originName: TextView
    private lateinit var originUrl: TextView
    private lateinit var locationName: TextView
    private lateinit var locationUrl: TextView

    private var characterID: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {

        _binding = FragmentCharacterDetailsBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(
            this,
            Injection.provideCharacterViewModelFactory(requireContext()))[CharacterViewModel::class.java]

        characterID = arguments?.getInt(CHARACTER_ID) ?: error("Character ID must not be Null")

        bindViews()
        setViews()
        setRecyclerView()

        return binding.root
    }

    private fun bindViews() {
        with(binding) {
            imageView = characterImageView
            imageUrl = characterImageUrl
            name = characterName
            species = characterSpecies
            status = characterStatus
            gender = characterGender
            type = characterType
            url = characterUrl
            created = characterCreated
            originName = characterOriginName
            originUrl = characterOriginUrl
            locationName = characterLocationName
            locationUrl = characterLocationUrl
        }
    }

    private fun setViews() {
        viewModel.requestCharacterDetails(characterID)?.let {
            it.observe(viewLifecycleOwner) { character->
                name.text = character.name
                imageUrl.text = character.image
                species.text = character.species
                status.text = character.status
                gender.text = character.gender
                type.text = character.type.ifBlank { "unknown" }
                url.text = character.url
                created.text = character.created.subSequence(0, 10)
                originName.text = character.originName
                originUrl.text = character.originUrl.ifBlank { "unknown" }
                locationName.text = character.locationName.ifBlank { "unknown" }
                locationUrl.text = character.locationUrl.ifBlank { "unknown" }
                Glide.with(requireContext()).load("${imageUrl.text}").into(imageView)
            }
        }
    }

    private fun setRecyclerView() {
        viewModel.requestCharacterDetails(characterID)?.let {
            it.observe(viewLifecycleOwner) { character ->
                val recyclerView = binding.characterRecyclerview
                val recyclerViewAdapter = CharacterRecyclerViewAdapter(character.episode)
                recyclerView.adapter = recyclerViewAdapter
            }
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
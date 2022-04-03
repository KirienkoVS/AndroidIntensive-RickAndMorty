package com.example.rickandmorty.ui.episodes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.example.rickandmorty.databinding.EpisodeDetailsFragmentBinding
import com.example.rickandmorty.isOnline
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EpisodeDetailsFragment: Fragment() {

    private var _binding: EpisodeDetailsFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EpisodeViewModel by viewModels()

    private lateinit var id: TextView
    private lateinit var name: TextView
    private lateinit var episodeNumber: TextView
    private lateinit var airDate: TextView
    private lateinit var created: TextView

    private var episodeID = 0
    private var isOnline = true
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = EpisodeDetailsFragmentBinding.inflate(inflater, container, false)

        isOnline = isOnline(requireContext())
        episodeID = arguments?.getInt(EPISODE_ID) ?: error("Should provide episode ID")

        bindViews()
        setViews()
        initRecyclerView()

        return binding.root
    }

    private fun bindViews() {
        with(binding) {
            id = episodeId
            name = episodeName
            episodeNumber = episode
            airDate = episodeDate
            created = episodeCreated
            recyclerView = episodeCharactersRecyclerview
        }
    }

    private fun setViews() {
        viewModel.requestEpisodeDetails(episodeID)?.let { episodeLiveData ->
            episodeLiveData.observe(viewLifecycleOwner) { episode ->
                if (episode == null) {
                    Toast.makeText(activity, "Data not available!", Toast.LENGTH_LONG).show()
                } else {
                    id.text = episode.id.toString()
                    name.text = episode.name
                    episodeNumber.text = episode.episodeNumber
                    airDate.text = episode.airDate
                    created.text = episode.created.subSequence(0, 10)
                }
            }
        }
    }

    private fun initRecyclerView() {
        viewModel.requestEpisodeDetails(episodeID)?.let { episodeLiveData ->
            episodeLiveData.observe(viewLifecycleOwner) { episode ->
                if (episode == null) {
                    Toast.makeText(activity, "Data not available!", Toast.LENGTH_LONG).show()
                } else {
                    val characterUrlList = episode.characters
                    viewModel.requestEpisodeCharacters(characterUrlList, isOnline)?.let { characterLiveData ->
                        characterLiveData.observe(viewLifecycleOwner) { characterDataList ->
                            val recyclerViewAdapter = EpisodeDetailsAdapter()
                            recyclerViewAdapter.characterList = characterDataList
                            recyclerView.adapter = recyclerViewAdapter
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        const val EPISODE_ID = "episodeID"
    }
}

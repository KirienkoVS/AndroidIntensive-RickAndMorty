package com.example.rickandmorty.ui.episodes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.rickandmorty.Injection
import com.example.rickandmorty.databinding.EpisodeDetailsFragmentBinding

class EpisodeDetailsFragment: Fragment() {

    private var _binding: EpisodeDetailsFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: EpisodeViewModel

    private lateinit var id: TextView
    private lateinit var name: TextView
    private lateinit var episodeNumber: TextView
    private lateinit var airDate: TextView
    private lateinit var url: TextView
    private lateinit var created: TextView

    private var episodeID = 0
    private var isOnline = true
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = EpisodeDetailsFragmentBinding.inflate(inflater, container, false)

        isOnline = Injection.isOnline(requireContext())
        episodeID = arguments?.getInt(EPISODE_ID) ?: error("Should provide episode ID")

        initializeViewModel()
        bindViews()
        setViews()
        setRecyclerView()

        return binding.root
    }

    private fun initializeViewModel() {
        viewModel = ViewModelProvider(
            this,
            Injection.provideEpisodeViewModelFactory(requireContext()))[EpisodeViewModel::class.java]
    }

    private fun bindViews() {
        with(binding) {
            id = episodeId
            name = episodeName
            episodeNumber = episode
            airDate = episodeDate
            url = episodeUrl
            created = episodeCreated
            recyclerView = episodeCharactersRecyclerview
        }
    }

    private fun setViews() {
        viewModel.requestEpisodeDetails(episodeID)?.let {
            it.observe(viewLifecycleOwner) { episode->
                id.text = episode.id.toString()
                name.text = episode.name
                episodeNumber.text = episode.episodeNumber
                airDate.text = episode.airDate
                url.text = episode.url
                created.text = episode.created.subSequence(0, 10)
            }
        }
    }

    private fun setRecyclerView() {
        viewModel.requestEpisodeDetails(episodeID)?.let { episodeLiveData ->
            episodeLiveData.observe(viewLifecycleOwner) { episodeData ->
                val characterUrlList = episodeData.characters

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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        const val EPISODE_ID = "episodeID"
    }
}
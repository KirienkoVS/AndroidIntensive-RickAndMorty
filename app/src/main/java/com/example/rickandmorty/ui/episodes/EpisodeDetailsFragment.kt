package com.example.rickandmorty.ui.episodes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.example.rickandmorty.R
import com.example.rickandmorty.databinding.EpisodeDetailsFragmentBinding
import com.example.rickandmorty.isOnline
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EpisodeDetailsFragment: Fragment() {

    private var _binding: EpisodeDetailsFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EpisodeViewModel by viewModels()

    private var episodeID = 0
    private var isOnline = true

    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = EpisodeDetailsFragmentBinding.inflate(inflater, container, false)

        isOnline = isOnline(requireContext())
        episodeID = arguments?.getInt(EPISODE_ID) ?: error("Should provide episode ID")

        bindViews()
        setViews()
        initRecyclerView()
        showProgressBar()

        return binding.root
    }

    private fun bindViews() {
        progressBar = binding.charactersProgressBar
        recyclerView = binding.episodeCharactersRecyclerview
    }

    private fun setViews() {
        viewModel.requestEpisodeDetails(episodeID)?.let { episodeLiveData ->
            episodeLiveData.observe(viewLifecycleOwner) { episode ->
                if (episode == null) {
                    Toast.makeText(activity, getString(R.string.data_not_avaliable), Toast.LENGTH_LONG).show()
                } else {
                    binding.episodeId.text = episode.id.toString()
                    binding.episodeName.text = episode.name
                    binding.episode.text = episode.episodeNumber
                    binding.episodeDate.text = episode.airDate
                    binding.episodeCreated.text = episode.created.subSequence(0, 10)
                }
            }
        }
    }

    private fun initRecyclerView() {
        viewModel.requestEpisodeDetails(episodeID)?.let { episodeLiveData ->
            episodeLiveData.observe(viewLifecycleOwner) { episode ->
                if (episode == null) {
                    Toast.makeText(activity, getString(R.string.data_not_avaliable), Toast.LENGTH_LONG).show()
                    viewModel.setProgressBarVisibility(false)
                } else {
                    val characterUrlList = episode.characters
                    viewModel.requestEpisodeCharacters(characterUrlList, isOnline)?.let { characterLiveData ->
                        characterLiveData.observe(viewLifecycleOwner) { characterDataList ->
                            val recyclerViewAdapter = EpisodeDetailsAdapter()
                            recyclerViewAdapter.characterList = characterDataList
                            recyclerView.adapter = recyclerViewAdapter
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        const val EPISODE_ID = "episodeID"
    }
}

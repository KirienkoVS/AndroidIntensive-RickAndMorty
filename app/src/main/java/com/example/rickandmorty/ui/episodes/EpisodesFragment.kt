package com.example.rickandmorty.ui.episodes

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.rickandmorty.Injection
import com.example.rickandmorty.R
import com.example.rickandmorty.databinding.EpisodesFragmentBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest

class EpisodesFragment : Fragment()  {

    private var _binding: EpisodesFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: EpisodeViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var episodePagingAdapter: EpisodePagingAdapter
    private lateinit var episodeFilterMap: MutableMap<String, String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        _binding = EpisodesFragmentBinding.inflate(inflater, container, false)

        episodeFilterMap = mutableMapOf()
        initializeViewModel()
        setUpEpisodePagingAdapter()

        return binding.root
    }

    private fun initializeViewModel() {
        viewModel = ViewModelProvider(
            this,
            Injection.provideEpisodeViewModelFactory(requireContext())
        ).get(EpisodeViewModel::class.java)
    }

    private fun setUpEpisodePagingAdapter() {
        recyclerView = binding.episodeRecyclerview
        episodePagingAdapter = EpisodePagingAdapter()
        recyclerView.adapter = episodePagingAdapter

        viewModel.queries.observe(viewLifecycleOwner) { queries ->
            viewLifecycleOwner.lifecycleScope.launchWhenCreated {
                viewModel.requestEpisodes(queries).collectLatest {
                    episodePagingAdapter.submitData(it)
                }
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun showFilterDialog() {
        val inflater = requireActivity().layoutInflater
        val filterListLayout = inflater.inflate(R.layout.episodes_filter, null)
        val customTitle = inflater.inflate(R.layout.dialog_title, null)
        val nameEditText = filterListLayout.findViewById<EditText>(R.id.episode_name_edit_text)
        val numberEditText = filterListLayout.findViewById<EditText>(R.id.episode_number_edit_text)
        val dialog = MaterialAlertDialogBuilder(requireContext())

        // dialog builder
        with(dialog) {
            setView(filterListLayout)
            setCustomTitle(customTitle)
            setCancelable(false)
            setPositiveButton("Apply") { _, _ ->
                episodeFilterMap.put("name", nameEditText.text.toString())
                episodeFilterMap.put("episode", numberEditText.text.toString())
                viewModel.setFilter(episodeFilterMap)
            }
            setNegativeButton("Cancel") { _, _ -> }
        }.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.filter -> {
                showFilterDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.filter_menu, menu)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
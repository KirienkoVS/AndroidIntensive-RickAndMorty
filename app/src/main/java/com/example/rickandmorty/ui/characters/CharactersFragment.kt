package com.example.rickandmorty.ui.characters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.CheckBox
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.rickandmorty.Injection
import com.example.rickandmorty.R
import com.example.rickandmorty.databinding.CharactersFragmentBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest

class CharactersFragment : Fragment() {

    private var _binding: CharactersFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CharacterViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var characterPagingAdapter: CharacterPagingAdapter
    private lateinit var characterFilterMap: MutableMap<String, String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = CharactersFragmentBinding.inflate(inflater, container, false)

        initializeViewModel()
        setUpCharacterPagingAdapter()

        characterFilterMap = mutableMapOf()

//        binding.retryButton.setOnClickListener { characterPagingAdapter.retry() }

        return binding.root
    }

    private fun initializeViewModel() {
        viewModel = ViewModelProvider(
            this,
            Injection.provideCharacterViewModelFactory(requireContext())
        ).get(CharacterViewModel::class.java)
    }

    private fun setUpCharacterPagingAdapter() {
        recyclerView = binding.characterRecyclerview
        characterPagingAdapter = CharacterPagingAdapter()

        recyclerView.adapter = characterPagingAdapter/*.withLoadStateHeaderAndFooter(
            header = CharacterLoadStateAdapter { characterPagingAdapter.retry() },
            footer = CharacterLoadStateAdapter { characterPagingAdapter.retry() }
        )*/

        viewModel.queries.observe(viewLifecycleOwner) { queries ->
            viewLifecycleOwner.lifecycleScope.launchWhenCreated {
                viewModel.requestCharacters(queries).collectLatest {
                    characterPagingAdapter.submitData(it)
                }
            }
        }


        /*characterPagingAdapter.addLoadStateListener { loadState ->

            // Show empty list
            val isListEmpty = loadState.refresh is LoadState.Error && characterPagingAdapter.itemCount == 0
            binding.emptyList.isVisible = isListEmpty

            // Show a retry header if there was an error refreshing, and items were previously
            // cached OR default to the default prepend state
            header.loadState = loadState.mediator
                ?.refresh
                ?.takeIf { it is LoadState.Error && characterPagingAdapter.itemCount > 0 }
                ?: loadState.prepend

            // Only show the list if refresh succeeds, either from the the local db or the remote
            recyclerView.isVisible = loadState.source.refresh is LoadState.NotLoading || loadState.mediator?.refresh is LoadState.NotLoading
            // Show loading spinner during initial load or refresh
            binding.progressBar.isVisible = loadState.mediator?.refresh is LoadState.Loading
            // Show the retry state if initial load or refresh fails and there are no items
            binding.retryButton.isVisible = loadState.mediator?.refresh is LoadState.Error && characterPagingAdapter.itemCount == 0
            // Toast on any error, regardless of whether it came from RemoteMediator or PagingSource
            val errorState = loadState.source.append as? LoadState.Error
                ?: loadState.source.prepend as? LoadState.Error
                ?: loadState.append as? LoadState.Error
                ?: loadState.prepend as? LoadState.Error

            errorState?.let { Toast.makeText(activity, "${it.error}", Toast.LENGTH_LONG).show() }
        }*/

    }

    @SuppressLint("InflateParams")
    private fun showFilterDialog() {
        val inflater = requireActivity().layoutInflater
        val filterListLayout = inflater.inflate(R.layout.characters_filter, null)
        val customTitle = inflater.inflate(R.layout.dialog_title, null)
        val nameEditText = filterListLayout.findViewById<EditText>(R.id.edit_text)
        val dialog = MaterialAlertDialogBuilder(requireContext())

        // gets checkboxes from characters_filter.xml
        val filterList = mutableListOf<CheckBox>()
        filterListLayout.findViewById<ConstraintLayout>(R.id.constraint_layout).forEach {
            if (it is CheckBox) {
                filterList.add(it)
            }
        }

        // remembers checkboxes flags
        filterList.forEach { checkBox ->
            characterFilterMap.entries.forEach { filter ->
                if (checkBox.text == filter.value && checkBox.transitionName == filter.key) {
                    checkBox.isChecked = true
                }
            }
        }

        // dialog builder
        with(dialog) {
            setView(filterListLayout)
            setCustomTitle(customTitle)
            setCancelable(false)
            setPositiveButton("Apply") { _, _ ->
                filterList.forEach {
                    if (it.isChecked) {
                        characterFilterMap.put(it.transitionName, it.text.toString())
                    } else if (characterFilterMap[it.transitionName].isNullOrBlank()){
                        characterFilterMap.put(it.transitionName, "")
                    }
                }
                characterFilterMap.put("name", nameEditText.text.toString())
                viewModel.setFilter(characterFilterMap)
            }
            setNegativeButton("Cancel") { _, _ -> }
            setNeutralButton("Clear", null)
            create().apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                        characterFilterMap.clear()
                        filterList.forEach { it.isChecked = false }
                    }
                }
            }
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

        val searchItem = menu.findItem(R.id.search_action)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
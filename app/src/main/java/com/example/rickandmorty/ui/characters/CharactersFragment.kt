package com.example.rickandmorty.ui.characters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.rickandmorty.R
import com.example.rickandmorty.databinding.CharactersFragmentBinding
import com.example.rickandmorty.initLoadStateAdapter
import com.example.rickandmorty.isOnline
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CharactersFragment : Fragment() {

    private var _binding: CharactersFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CharacterViewModel by viewModels()

    private lateinit var recyclerView: RecyclerView
    private lateinit var pagingAdapter: CharacterPagingAdapter
    private lateinit var characterFilterMap: MutableMap<String, String>
    private lateinit var emptyTextView: TextView
    private lateinit var swipeRefresh: SwipeRefreshLayout

    private var isOnline = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        characterFilterMap = mutableMapOf()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = CharactersFragmentBinding.inflate(inflater, container, false)

        isOnline = isOnline(requireContext())
        recyclerView = binding.characterRecyclerview
        emptyTextView = binding.emptyTextView
        swipeRefresh = binding.swipeRefresh
        pagingAdapter = CharacterPagingAdapter()

        initPagingAdapter()
        initSwipeToRefresh()

        return binding.root
    }

    private fun initPagingAdapter() {
        displayCharacters()
        initLoadStateAdapter(
            isOnline, emptyTextView, viewLifecycleOwner, recyclerView, activity, swipeRefresh, pagingAdapter
        )
    }

    private fun initSwipeToRefresh() {
        binding.swipeRefresh.setOnRefreshListener { pagingAdapter.refresh() }
    }

    private fun displayCharacters() {
        viewModel.queries.observe(viewLifecycleOwner) { queries ->
            viewLifecycleOwner.lifecycleScope.launchWhenCreated {
                viewModel.requestCharacters(queries).collectLatest {
                    pagingAdapter.submitData(it)
                }
            }
        }
    }

    private fun displayFoundCharacters(search: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchCharacters(search).collectLatest {
                pagingAdapter.submitData(it)
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun showFilterDialog() {
        val inflater = requireActivity().layoutInflater
        val filterLayout = inflater.inflate(R.layout.characters_filter, null)
        val customTitle = inflater.inflate(R.layout.dialog_title, null)
        val nameEditText = filterLayout.findViewById<EditText>(R.id.edit_text)
        val dialog = MaterialAlertDialogBuilder(requireContext())

        // gets checkboxes from characters_filter.xml
        val filterList = mutableListOf<CheckBox>()
        filterLayout.findViewById<ConstraintLayout>(R.id.constraint_layout).forEach {
            if (it is CheckBox) {
                filterList.add(it)
            }
        }

        // restores checkboxes flags
        filterList.forEach { checkBox ->
            characterFilterMap.entries.forEach { filter ->
                if (checkBox.text == filter.value && checkBox.transitionName == filter.key) {
                    checkBox.isChecked = true
                }
            }
        }

        // restores editText text
        characterFilterMap.entries.forEach { filter ->
            if (nameEditText.transitionName == filter.key) {
                nameEditText.setText(filter.value)
            }
        }

        // dialog builder
        with(dialog) {
            setView(filterLayout)
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
                        nameEditText.text.clear()
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
            R.id.menu_refresh -> {
                pagingAdapter.refresh()
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

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.isNotBlank()) {
                    displayFoundCharacters(newText.lowercase())
                } else {
                    displayCharacters()
                }
                return false
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
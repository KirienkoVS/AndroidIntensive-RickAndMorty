package com.example.rickandmorty.ui.characters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.example.rickandmorty.Injection
import com.example.rickandmorty.R
import com.example.rickandmorty.databinding.CharactersFragmentBinding
import com.example.rickandmorty.ui.LoadStateAdapter
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
        characterFilterMap = mutableMapOf()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = CharactersFragmentBinding.inflate(inflater, container, false)

        initViewModel()
        initPagingAdapter()
        initSwipeToRefresh()

        return binding.root
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(
            this,
            Injection.provideCharacterViewModelFactory(requireContext())
        ).get(CharacterViewModel::class.java)
    }

    private fun initPagingAdapter() {
        recyclerView = binding.characterRecyclerview
        characterPagingAdapter = CharacterPagingAdapter()

        viewModel.queries.observe(viewLifecycleOwner) { queries ->
            viewLifecycleOwner.lifecycleScope.launchWhenCreated {
                viewModel.requestCharacters(queries).collectLatest {
                    characterPagingAdapter.submitData(it)
                }
            }
        }

        val header = LoadStateAdapter { characterPagingAdapter.retry() }

        recyclerView.adapter = characterPagingAdapter.withLoadStateHeaderAndFooter(
            header = header,
            footer = LoadStateAdapter { characterPagingAdapter.retry() }
        )
        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            characterPagingAdapter.loadStateFlow.collectLatest { loadStates ->
                binding.swipeRefresh.isRefreshing = loadStates.mediator?.refresh is LoadState.Loading
            }
        }
        characterPagingAdapter.addLoadStateListener { loadState ->
            val isListEmpty = loadState.refresh is LoadState.Error && characterPagingAdapter.itemCount == 0
            binding.emptyTextView.isVisible = isListEmpty
            header.loadState = loadState.mediator
                ?.refresh
                ?.takeIf { it is LoadState.Error && characterPagingAdapter.itemCount > 0 }
                ?: loadState.prepend
            recyclerView.isVisible = loadState.source.refresh is LoadState.NotLoading ||
                    loadState.mediator?.refresh is LoadState.NotLoading
            val errorState = loadState.source.append as? LoadState.Error
                ?: loadState.source.prepend as? LoadState.Error
                ?: loadState.append as? LoadState.Error
                ?: loadState.prepend as? LoadState.Error
            errorState?.let { Toast.makeText(activity, "${it.error}", Toast.LENGTH_LONG).show() }
        }
    }

    private fun initSwipeToRefresh() {
        binding.swipeRefresh.setOnRefreshListener { characterPagingAdapter.refresh() }
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
                characterPagingAdapter.refresh()
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
package com.example.rickandmorty.ui.characters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.CheckBox
import android.widget.EditText
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
    private lateinit var characterAdapter: CharacterAdapter
    private lateinit var filterMap: MutableMap<String, String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        _binding = CharactersFragmentBinding.inflate(inflater, container, false)

        recyclerView = binding.characterRecyclerview
        characterAdapter = CharacterAdapter()
        recyclerView.adapter = characterAdapter
        filterMap = mutableMapOf()

        viewModel = ViewModelProvider(
            this,
            Injection.provideCharacterViewModelFactory(requireContext())
        ).get(CharacterViewModel::class.java)

        viewModel.queries.observe(viewLifecycleOwner) { queries ->
            viewLifecycleOwner.lifecycleScope.launchWhenCreated {
                viewModel.requestCharacters(queries).collectLatest {
                    characterAdapter.submitData(it)
                }
            }
        }

        return binding.root
    }

    @SuppressLint("InflateParams")
    private fun showFilterDialog() {
        val inflater = requireActivity().layoutInflater
        val filterListLayout = inflater.inflate(R.layout.filter_list, null)
        val customTitle = inflater.inflate(R.layout.dialog_title, null)
        val nameEditText = filterListLayout.findViewById<EditText>(R.id.edit_text)
        val builder = MaterialAlertDialogBuilder(requireContext())
        
        val filterList = mutableListOf<CheckBox>()

        filterListLayout.findViewById<ConstraintLayout>(R.id.constraint_layout).forEach {
            if (it is CheckBox) {
                filterList.add(it)
            }
        }

        filterList.forEach { checkBox ->
            filterMap.entries.forEach { filter ->
                if (checkBox.text == filter.value && checkBox.transitionName == filter.key) {
                    checkBox.isChecked = true
                }
            }
        }

        with(builder) {
            setView(filterListLayout)
            setCustomTitle(customTitle)
            setCancelable(false)
            setPositiveButton("Apply") { _, _ ->
                filterList.forEach {
                    if (it.isChecked) {
                        filterMap.put(it.transitionName, it.text.toString())
                    } else if (filterMap[it.transitionName].isNullOrBlank()){
                        filterMap.put(it.transitionName, "")
//                        filterMap.put("name", "")
                    }
                }
                filterMap.put("name", nameEditText.text.toString())
//                Toast.makeText(activity, "${filterMap.entries}", Toast.LENGTH_SHORT).show()
                viewModel.setFilter(filterMap)
            }
            setNegativeButton("Cancel") { _, _ -> }
            setNeutralButton("Clear", null)
            create().apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                        filterMap.clear()
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
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
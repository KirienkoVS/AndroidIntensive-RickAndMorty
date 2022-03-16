package com.example.rickandmorty.ui.characters

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.rickandmorty.Injection
import com.example.rickandmorty.R
import com.example.rickandmorty.databinding.CharactersFragmentBinding
import kotlinx.coroutines.flow.collectLatest

class CharactersFragment : Fragment() {

    private var _binding: CharactersFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CharacterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        _binding = CharactersFragmentBinding.inflate(inflater, container, false)

        val recyclerView = binding.characterRecyclerview
        val characterAdapter = CharacterAdapter()
        recyclerView.adapter = characterAdapter

        viewModel = ViewModelProvider(
            this,
            Injection.provideCharacterViewModelFactory(requireContext())
        ).get(CharacterViewModel::class.java)

        viewModel.filter.observe(viewLifecycleOwner) { filter ->
            Toast.makeText(activity, filter, Toast.LENGTH_SHORT).show()
            viewLifecycleOwner.lifecycleScope.launchWhenCreated {
                viewModel.requestCharacters(filter, "").collectLatest {
                    characterAdapter.submitData(it)
                }
            }
        }

        return binding.root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.groupId) {
            R.id.group_status -> {
                when(item.itemId) {
                    R.id.alive -> {
                        viewModel.setFilter("Alive", "status")
                        item.isChecked = true
                        true
                    }
                    R.id.dead -> {
                        viewModel.setFilter("Dead", "status")
                        item.isChecked = true
                        true
                    }
                    R.id.unknown -> {
                        viewModel.setFilter("unknown", "status")
                        item.isChecked = true
                        true
                    }
                    else -> super.onOptionsItemSelected(item)
                }
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.search_item, menu)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
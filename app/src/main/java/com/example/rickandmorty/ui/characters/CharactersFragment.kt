package com.example.rickandmorty.ui.characters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.rickandmorty.Injection
import com.example.rickandmorty.databinding.CharactersFragmentBinding
import kotlinx.coroutines.flow.collectLatest

class CharactersFragment : Fragment() {

    private var _binding: CharactersFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        _binding = CharactersFragmentBinding.inflate(inflater, container, false)

        val recyclerView = binding.characterRecyclerview
        val recyclerViewAdapter = CharacterAdapter { position, data ->
            Toast.makeText(activity, "$position $data", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = recyclerViewAdapter

        val viewModel = ViewModelProvider(
            this,
            Injection.provideCharacterViewModelFactory(requireContext())
        ).get(CharacterViewModel::class.java)

        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            viewModel.requestCharacters().collectLatest {
                recyclerViewAdapter.submitData(it)
            }
        }

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
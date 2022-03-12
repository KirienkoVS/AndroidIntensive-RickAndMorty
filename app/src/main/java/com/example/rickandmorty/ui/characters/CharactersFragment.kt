package com.example.rickandmorty.ui.characters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.rickandmorty.databinding.CharactersFragmentBinding
import kotlinx.coroutines.flow.collectLatest

class CharactersFragment : Fragment() {

    private var _binding: CharactersFragmentBinding? = null
    private val binding get() = _binding!!

//    private lateinit var characterList: List<CharacterData>

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

        val viewModel = ViewModelProvider(this).get(CharacterViewModel::class.java)

        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            viewModel.requestCharacters().collectLatest {
                recyclerViewAdapter.submitData(it)
            }
        }

        return binding.root
    }

//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        super.onCreateOptionsMenu(menu, inflater)
//        inflater.inflate(R.menu.search_item)
//
//        val item = menu.findItem(R.id.search_action)
//        val searchView = item.actionView as SearchView
//
//        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String?): Boolean {
//                return false
//            }
//
//            override fun onQueryTextChange(newText: String?): Boolean {
//                val searchList: ArrayList<CharacterData> = arrayListOf()
//
//
//            }
//
//        })
//    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
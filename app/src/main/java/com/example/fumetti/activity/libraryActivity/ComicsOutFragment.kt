package com.example.fumetti.activity.libraryActivity

import SearchHandler
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.fumetti.R
import com.example.fumetti.data.ComicSorted
import com.example.fumetti.data.ComicStatus
import com.example.fumetti.database.utility.ComicLoader
import com.example.fumetti.database.utility.ComicsAdapter
import com.google.firebase.auth.FirebaseAuth

class ComicsOutFragment : Fragment() {

    private lateinit var comicLoader: ComicLoader
    private var searchHandler: SearchHandler? = null
    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var sortSpinner: Spinner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_comic_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        comicLoader = ComicLoader(requireContext())
        recyclerView = view.findViewById(R.id.recyclerView)
        searchView = view.findViewById(R.id.searchView)
        sortSpinner = view.findViewById(R.id.sortSpinner)

        searchView.clearFocus()
        searchView.setOnClickListener {
            searchView.isIconified = false
        }

        setupSortSpinner()
        loadComics(ComicSorted.BY_NAME)
    }

    private fun setupSortSpinner() {
        val options = resources.getStringArray(R.array.sort_options)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sortSpinner.adapter = adapter

        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val sort = when (position) {
                    0 -> ComicSorted.BY_SERIES
                    1 -> ComicSorted.BY_NUMBER
                    2 -> ComicSorted.BY_NAME
                    else -> ComicSorted.UNKOWN
                }
                loadComics(sort)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun loadComics(sort: ComicSorted) {
        comicLoader.loadComics(
            recyclerView = recyclerView,
            adapterMode = ComicsAdapter.AdapterMode.PREVIEW,
            ordering = sort,
            status = ComicStatus.OUT,
            filter = { comic ->
                comic.status == ComicStatus.OUT &&
                        comic.userId != FirebaseAuth.getInstance().currentUser?.uid
            },
            onAdapterReady = { adapter ->
                searchHandler = SearchHandler(searchView, adapter)
            }
        )
    }

    override fun onDestroyView() {
        searchHandler = null
        super.onDestroyView()
    }
}

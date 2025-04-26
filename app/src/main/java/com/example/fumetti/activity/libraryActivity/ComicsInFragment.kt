package com.example.fumetti.activity.libraryActivity

import SearchHandler
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fumetti.R
import com.example.fumetti.data.ComicStatus
import com.example.fumetti.database.ComicDatabase
import com.example.fumetti.database.utility.ComicLoader

class ComicsInFragment : Fragment() {

    private lateinit var comicDatabase: ComicDatabase
    private var searchHandler: SearchHandler? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var comicLoader: ComicLoader

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_comic_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        comicDatabase = ComicDatabase()

        comicLoader = ComicLoader(requireContext())
        recyclerView = view.findViewById(R.id.recyclerView)
        searchView = view.findViewById(R.id.searchView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        loadComics()
    }

    private fun loadComics() {
        comicLoader.loadComics(
            recyclerView = recyclerView,
            filter = { comic -> comic.status == ComicStatus.TAKEN },
        )
    }

    override fun onDestroyView() {
        searchHandler = null
        super.onDestroyView()
    }
}

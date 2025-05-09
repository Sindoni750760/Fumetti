package com.example.fumetti.activity.libraryActivity

import SearchHandler
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.fumetti.R
import com.example.fumetti.data.ComicStatus
import com.example.fumetti.database.utility.ComicLoader
import com.example.fumetti.database.utility.ComicsAdapter

class ComicsInFragment : Fragment() {

    private lateinit var comicLoader: ComicLoader
    private var searchHandler: SearchHandler? = null
    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_comic_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        comicLoader = ComicLoader(requireContext())
        recyclerView = view.findViewById(R.id.recyclerView)
        searchView = view.findViewById(R.id.searchView)

        loadComics()
    }

    private fun loadComics() {
        comicLoader.loadComics(
            recyclerView = recyclerView,
            adapterMode = ComicsAdapter.AdapterMode.MY_LIBRARY,
            status = ComicStatus.IN
        )

        // Ritardo per aspettare che l’adapter sia assegnato da ComicLoader
        view?.postDelayed({
            val adapter = recyclerView.adapter as? ComicsAdapter ?: return@postDelayed
            searchHandler = SearchHandler(searchView, adapter)
        }, 500)
    }

    override fun onDestroyView() {
        searchHandler = null
        super.onDestroyView()
    }
}

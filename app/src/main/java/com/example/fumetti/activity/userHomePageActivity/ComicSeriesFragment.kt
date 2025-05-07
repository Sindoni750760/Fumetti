package com.example.fumetti.activity.userHomePageActivity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fumetti.R
import com.example.fumetti.data.ComicSorted
import com.example.fumetti.data.ComicStatus
import com.example.fumetti.database.ComicDatabase
import com.example.fumetti.database.utility.ComicLoader
import com.example.fumetti.database.utility.ComicsAdapter

class ComicSeriesFragment : Fragment() {

    private lateinit var comicDatabase: ComicDatabase
    private lateinit var recyclerView: RecyclerView
    private lateinit var comicLoader: ComicLoader

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_comic_display, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        comicDatabase = ComicDatabase()
        comicLoader = ComicLoader(requireContext())
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        loadData()
    }

    private fun loadData() {
        comicLoader.loadComics(
            recyclerView = recyclerView,
            adapterMode = ComicsAdapter.AdapterMode.PREVIEW,
            ordering = ComicSorted.BY_SERIES,
            status = ComicStatus.UNKOWN,
            filter = { comic -> !(comic.series.isNullOrEmpty()) }
        )
    }
}

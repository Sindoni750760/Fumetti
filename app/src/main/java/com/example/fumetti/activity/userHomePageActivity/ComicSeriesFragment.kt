package com.example.fumetti.activity.userHomePageActivity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fumetti.R
import com.example.fumetti.activity.ComicDetailActivity
import com.example.fumetti.data.Comic
import com.example.fumetti.data.ComicStatus
import com.example.fumetti.database.ComicDatabase
import com.example.fumetti.database.utility.ComicLoader
import com.example.fumetti.database.utility.ComicsAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ComicSeriesFragment : Fragment() {

    private lateinit var comicDatabase: ComicDatabase
    private lateinit var recyclerView: RecyclerView
    private lateinit var loader: ComicLoader

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_comic_display, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        comicDatabase = ComicDatabase()
        loader = ComicLoader(requireContext()) // <<< QUI! Devi inizializzarlo

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        loadData()
    }

    private fun loadData() {
        loader.loadComics(
            recyclerView = recyclerView,
            filter = { comic -> comic.series.isNullOrBlank().not()}, // <<< QUI! `number` non `numero`
            sort = { comics -> comics.sortedBy { it.series } }
        )
    }
}

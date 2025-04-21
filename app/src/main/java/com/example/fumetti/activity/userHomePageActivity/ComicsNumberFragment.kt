package com.example.fumetti.activity.userHomePageActivity

import SearchHandler
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fumetti.R
import com.example.fumetti.activity.ComicDetailActivity
import com.example.fumetti.data.Comic
import com.example.fumetti.data.ComicStatus
import com.example.fumetti.database.ComicDatabase
import com.example.fumetti.database.Utility.ComicsAdapter
import com.google.firebase.firestore.FirebaseFirestore

class ComicsNumberFragment : Fragment() {

    private lateinit var comicDatabase: ComicDatabase
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_comic_display, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        comicDatabase = ComicDatabase()

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        loadData()
    }

    private fun loadData() {
        FirebaseFirestore.getInstance().collection("comic")
            .get()
            .addOnSuccessListener { result ->
                val comics = result.mapNotNull { document ->
                    val id = document.getString("id") ?: document.id
                    val name = document.getString("name") ?: return@mapNotNull null
                    val imageUrl = document.getString("imageUrl") ?: ""
                    val number = document.getLong("number")?.toInt()
                        ?: document.getString("number")?.toIntOrNull() ?: 0
                    val series = document.getString("series") ?: return@mapNotNull null
                    val description = document.getString("description") ?: ""
                    val numericId = document.getLong("numericId")?.toString()
                        ?: document.getString("numericId") ?: ""
                    val userId = document.getString("userId")
                        ?: document.getLong("userId")?.toString() ?: "undefined"
                    val seriesNumber = document.getLong("seriesNumber")?.toInt()
                        ?: document.getString("seriesNumber")?.toIntOrNull() ?: return@mapNotNull null
                    val status = try {
                        ComicStatus.valueOf(document.getString("status") ?: "DISPONIBILE")
                    } catch (e: Exception) {
                        ComicStatus.UNKOWN
                    }

                    Comic(description, id, imageUrl, name, number, numericId, series, seriesNumber, status, userId)
                }

                val sorted = comics.sortedBy { it.seriesNumber}
                val filteredComics = sorted.filter{comic ->
                    !comic.number.toString().isNullOrBlank() || !comic.seriesNumber.toString().isNullOrBlank()
                }

                recyclerView.adapter = ComicsAdapter(
                    requireContext(),
                    filteredComics,
                    ComicsAdapter.AdapterMode.PREVIEW,
                    comicDatabase = comicDatabase,
                    updateStatus = { _, _ ->
                        Toast.makeText(context, "I fumetti non disponibili non possono essere aggiornati", Toast.LENGTH_SHORT).show()
                    },
                    onComicClick = { comic ->
                        startActivity(Intent(requireContext(), ComicDetailActivity::class.java).apply {
                            putExtra("COMIC_ID", comic.id)
                        })
                    }
                )
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Errore di caricamento", exception)
            }
    }
}

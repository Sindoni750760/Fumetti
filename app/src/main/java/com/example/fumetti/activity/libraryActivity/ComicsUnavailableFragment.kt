package com.example.fumetti.activity.libraryActivity

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

class ComicsUnavailableFragment : Fragment() {

    private lateinit var comicDatabase: ComicDatabase
    private var searchHandler: SearchHandler? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_comic_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        comicDatabase = ComicDatabase()

        recyclerView = view.findViewById(R.id.recyclerView)
        searchView = view.findViewById(R.id.searchView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val db = FirebaseFirestore.getInstance()
        db.collection("comic")
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Toast.makeText(context, "Nessun fumetto trovato", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val comics = result.map { document ->
                    val id = document.getString("id") ?: document.id
                    val name = document.getString("name") ?: ""
                    val imageUrl = document.getString("imageUrl") ?: ""
                    val number = document.getLong("number")?.toInt()
                        ?: (document.getString("number")?.toIntOrNull() ?: 0)
                    val series = document.getString("series") ?: " "
                    val description = document.getString("description") ?: " "
                    val numericId = document.getLong("numericId")?.toString()
                        ?: document.getString("numericId") ?: " "
                    val userIdFromDb = document.getString("userId")
                        ?: document.getLong("userId")?.toString() ?: "undefined"
                    val statusStr = document.getString("status")
                    val status = try {
                        ComicStatus.valueOf(document.getString("status") ?: "NON_DISPONIBILE")
                    } catch (e: Exception) {
                        Log.e("ComicsUnavailableFragment", "Stato non valido per il fumetto $id ($statusStr)", e)
                        ComicStatus.UNKOWN
                    }
                    val seriesNumber = document.getLong("seriesNumber")?.toInt()
                        ?: (document.getString("seriesNumber")?.toIntOrNull() ?: 0)

                    Comic(
                        description, id, imageUrl, name, number, numericId,
                        series, seriesNumber, status, userIdFromDb
                    )
                }

                val unavailableComics = comics.filter { it.status == ComicStatus.NON_DISPONIBILE }

                val adapter = ComicsAdapter(
                    requireContext(),
                    unavailableComics,
                    ComicsAdapter.AdapterMode.LIBRARY,
                    comicDatabase = comicDatabase,
                    updateStatus = { _, _ ->
                        Toast.makeText(context, "I fumetti non disponibili non possono essere aggiornati", Toast.LENGTH_SHORT).show()
                    },
                    onComicClick = { comic ->
                        val intent = Intent(requireContext(), ComicDetailActivity::class.java)
                        intent.putExtra("COMIC_ID", comic.id)
                        startActivity(intent)
                    }
                )

                recyclerView.adapter = adapter
                searchHandler = SearchHandler(searchView, adapter).apply {
                    searchView.setQuery(lastQuery, false)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Errore di caricamento", exception)
            }
    }

    override fun onDestroyView() {
        searchHandler = null
        super.onDestroyView()
    }
}

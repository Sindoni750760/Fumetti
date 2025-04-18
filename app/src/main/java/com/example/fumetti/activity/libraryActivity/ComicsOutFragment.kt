package com.example.fumetti.activity.libraryActivity

import SearchHandler
import com.example.fumetti.R
import com.example.fumetti.activity.ComicDetailActivity
import com.example.fumetti.data.ComicStatus
import com.example.fumetti.database.ComicDatabase
import com.example.fumetti.database.Utility.ComicsAdapter
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fumetti.data.Comic
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
class ComicsOutFragment : Fragment() {

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

        loadComics()
    }

    private fun loadComics() {
        val db = FirebaseFirestore.getInstance()
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        db.collection("comic")
            .get()
            .addOnSuccessListener { result ->
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
                    val status = try {
                        ComicStatus.valueOf(document.getString("status") ?: "IN_PRENOTAZIONE")
                    } catch (e: Exception) {
                        ComicStatus.UNKOWN
                    }
                    val seriesNumber = document.getLong("seriesNumber")?.toInt()
                        ?: (document.getString("seriesNumber")?.toIntOrNull() ?: 0)

                    Comic(
                        description, id, imageUrl, name, number, numericId,
                        series, seriesNumber, status, userIdFromDb
                    )
                }

                val userComics = comics.filter {
                    it.status == ComicStatus.IN_PRENOTAZIONE && it.userId == currentUserId
                }

                val adapter = ComicsAdapter(
                    requireContext(),
                    userComics,
                    ComicsAdapter.AdapterMode.LIBRARY,
                    comicDatabase,
                    updateStatus = { comic, newStatus ->
                        if (comic.status == ComicStatus.IN_PRENOTAZIONE && newStatus == ComicStatus.DISPONIBILE) {
                            comicDatabase.updateComicStatus(comic.id, newStatus,
                                onSuccess = {
                                    Toast.makeText(context, "Stato aggiornato con successo", Toast.LENGTH_SHORT).show()
                                },
                                onFailure = { exception ->
                                    Toast.makeText(context, "Errore: ${exception.message}", Toast.LENGTH_SHORT).show()
                                })
                        } else {
                            Toast.makeText(context, "Transizione non valida", Toast.LENGTH_SHORT).show()
                        }
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
            .addOnFailureListener {
                Toast.makeText(context, "Errore nel caricamento dei dati", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        searchHandler = null
        super.onDestroyView()
    }
}

package com.example.fumetti

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fumetti.data.Comic
import com.example.fumetti.data.ComicStatus
import com.example.fumetti.database.ComicDatabase
import com.example.fumetti.database.ComicsAdapter

class OrderedComics : AppCompatActivity() {

    private val comicDatabase = ComicDatabase()
    private lateinit var orderedComicsAdapter: ComicsAdapter
    private lateinit var waitingListAdapter: WaitingListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ordered_comics)

        val recyclerViewOrderedComics = findViewById<RecyclerView>(R.id.recyclerViewOrderedComics)
        recyclerViewOrderedComics.layoutManager = LinearLayoutManager(this)

        val recyclerViewWaitingList = findViewById<RecyclerView>(R.id.recyclerViewWaitingList)
        recyclerViewWaitingList.layoutManager = LinearLayoutManager(this)

        // Caricamento fumetti ordinati nella RecyclerView
        val userId = "USER_ID" // Sostituisci con l'ID utente corretto
        comicDatabase.getAllComicsByUser(userId) { comics: List<Comic> ->
            val orderedComics = comics.filter { it.status == ComicStatus.IN_PRENOTAZIONE }
            if (orderedComics.isNotEmpty()) {
                orderedComicsAdapter = ComicsAdapter(
                    this, orderedComics,
                    ComicsAdapter.AdapterMode.PREVIEW,
                    comicDatabase
                ) { view, status ->
                    updateComicStatus(view, status)
                }
                recyclerViewOrderedComics.adapter = orderedComicsAdapter
            } else {
                Toast.makeText(this, "Nessun fumetto ordinato", Toast.LENGTH_SHORT).show()
            }
        }

        // Caricamento lista di attesa nella RecyclerView
        comicDatabase.getAllComicsByUser { comics: List<Comic> ->
            val waitingList = comics.filter { it.status == ComicStatus.IN_PRENOTAZIONE }
            if (waitingList.isNotEmpty()) {
                waitingListAdapter = WaitingListAdapter(this, waitingList)
                recyclerViewWaitingList.adapter = waitingListAdapter
            } else {
                Toast.makeText(this, "Nessuna persona in attesa", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun updateComicStatus(view: ImageView, status: String) {
        when (status) {
            "disponibile" -> view.setImageResource(R.drawable.ic_circle_green)
            "in prestito" -> view.setImageResource(R.drawable.ic_circle_yellow)
            "occupato" -> view.setImageResource(R.drawable.ic_circle_red)
        }
    }
}

package com.example.fumetti.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fumetti.R
import com.example.fumetti.data.ComicStatus
import com.example.fumetti.database.ComicDatabase
import com.example.fumetti.database.adapter.ComicsAdapter
import com.google.firebase.auth.FirebaseAuth

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

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return showError("Utente non autenticato")

        // Fumetti prenotati dall’utente
        comicDatabase.getAllComicsByUser(userId) { comics ->
            val orderedComics = comics.filter { it.status == ComicStatus.IN_PRENOTAZIONE }
            if (orderedComics.isNotEmpty()) {
                orderedComicsAdapter = ComicsAdapter(
                    context = this,
                    comicsList = orderedComics,
                    mode = ComicsAdapter.AdapterMode.PREVIEW,
                    comicDatabase = comicDatabase,
                    updateStatus = { _, _ -> },
                    onComicClick = { comic ->
                        Toast.makeText(this, "Hai selezionato: ${comic.name}", Toast.LENGTH_SHORT).show()
                    }
                )
                recyclerViewOrderedComics.adapter = orderedComicsAdapter
            } else {
                Toast.makeText(this, "Nessun fumetto ordinato", Toast.LENGTH_SHORT).show()
            }
        }

        // Lista d’attesa generale
        comicDatabase.getAllComics { comics ->
            val waitingList = comics.filter { it.status == ComicStatus.IN_PRENOTAZIONE }
            if (waitingList.isNotEmpty()) {
                waitingListAdapter = WaitingListAdapter(this, waitingList)
                recyclerViewWaitingList.adapter = waitingListAdapter
            } else {
                Toast.makeText(this, "Nessuna persona in attesa", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        finish()
    }
}

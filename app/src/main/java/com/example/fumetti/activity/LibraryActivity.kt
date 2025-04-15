package com.example.fumetti.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fumetti.R
import com.example.fumetti.data.Comic
import com.example.fumetti.data.ComicStatus
import com.example.fumetti.database.ComicDatabase
import com.example.fumetti.database.adapter.ComicsAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class LibraryActivity : AppCompatActivity() {

    private val comicDatabase = ComicDatabase()

    private lateinit var recyclerOut: RecyclerView
    private lateinit var recyclerAvailable: RecyclerView
    private lateinit var recyclerUnavailable: RecyclerView

    private lateinit var adapterOut: ComicsAdapter
    private lateinit var adapterAvailable: ComicsAdapter
    private lateinit var adapterUnavailable: ComicsAdapter

    companion object {
        const val TAG = "LibraryActivity"
        const val EXTRA_USER_ID = "EXTRA_USER_ID"
    }

    private val userId: String? by lazy { intent.getStringExtra(EXTRA_USER_ID) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)

        logStartupMode()
        initViews()
        setupRecyclerViews()
        setupNavigationButtons()
        loadData()
    }

    private fun logStartupMode() {
        Log.d(TAG, "onCreate: Avvio LibraryActivity")
        if (userId != null) {
            Log.d(TAG, "Modalità Libreria Personale: userId = $userId")
        } else {
            Log.d(TAG, "Modalità Libreria Globale")
        }
    }

    private fun initViews() {
        recyclerOut = findViewById(R.id.recyclerViewOut)
        recyclerAvailable = findViewById(R.id.recyclerViewAvailable)
        recyclerUnavailable = findViewById(R.id.recyclerViewUnavailable)
    }

    private fun setupRecyclerViews() {
        recyclerOut.layoutManager = LinearLayoutManager(this)
        recyclerAvailable.layoutManager = LinearLayoutManager(this)
        recyclerUnavailable.layoutManager = LinearLayoutManager(this)

        adapterOut = createAdapter(recyclerOut) { adapterOut }
        adapterAvailable = createAdapter(recyclerAvailable) { adapterAvailable }
        adapterUnavailable = createAdapter(recyclerUnavailable) { adapterUnavailable }
    }

    private fun createAdapter(
        recycler: RecyclerView,
        adapterRef: () -> ComicsAdapter
    ): ComicsAdapter {
        val adapter = ComicsAdapter(
            this, mutableListOf(), ComicsAdapter.AdapterMode.PREVIEW, comicDatabase,
            updateStatus = { comic, status ->
                updateStatusVisual(recycler, adapterRef().getPositionFromComic(comic), status)
            },
            onComicClick = { comic ->
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                if (comic.userId.isNullOrEmpty() || comic.userId == "undefined") {
                    prenotaComic(comic)
                } else if (comic.userId == currentUserId) {
                    rilasciaComic(comic)
                } else {
                    Toast.makeText(this, "Fumetto non disponibile: prenotato da un altro utente", Toast.LENGTH_SHORT).show()
                }
            }
        )
        recycler.adapter = adapter
        return adapter
    }

    private fun setupNavigationButtons() {
        findViewById<Button>(R.id.buttonHomePage).setOnClickListener {
            startActivity(Intent(this, UserHomePageActivity::class.java))
            finish()
        }
    }

    private fun loadData() {
        if (userId != null) {
            loadUserLibrary()
        } else {
            loadGlobalLibrary()
        }
    }

    private fun loadUserLibrary() {
        comicDatabase.getAllComicsByUser(userId!!) { comics ->
            refreshAdapters(comics)
        }
    }

    private fun loadGlobalLibrary() {
        val db = FirebaseFirestore.getInstance()
        db.collection("comic").get()
            .addOnSuccessListener { result ->
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                val comics = result.map { document ->
                    parseComicFromDocument(document, currentUserId)
                }
                refreshAdapters(comics)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Errore nel recupero dei fumetti: ${exception.message}", exception)
            }
    }

    private fun parseComicFromDocument(document: DocumentSnapshot, currentUserId: String?): Comic {
        val id = document.getString("id") ?: document.id
        val name = document.getString("name") ?: ""
        val imageUrl = document.getString("imageUrl") ?: ""
        val number = document.getLong("number")?.toInt()
            ?: document.getString("number")?.toIntOrNull() ?: 0
        val series = document.getString("series") ?: ""
        val description = document.getString("description") ?: ""
        val numericId = document.getLong("numericId")?.toString()
            ?: document.getString("numericId") ?: ""
        val userIdFromDb = document.getString("userId")
            ?: document.getLong("userId")?.toString()
            ?: "undefined"
        val seriesNumber = document.getLong("seriesNumber")?.toInt()
            ?: document.getString("seriesNumber")?.toIntOrNull() ?: 0

        val status = when {
            userIdFromDb.isBlank() || userIdFromDb == "undefined" -> ComicStatus.DISPONIBILE
            userIdFromDb == currentUserId -> ComicStatus.IN_PRENOTAZIONE
            else -> ComicStatus.NON_DISPONIBILE
        }

        return Comic(
            description = description,
            id = id,
            imageUrl = imageUrl,
            name = name,
            number = number,
            numericId = numericId,
            series = series,
            seriesNumber = seriesNumber,
            status = status,
            userId = userIdFromDb
        )
    }

    private fun refreshAdapters(comics: List<Comic>) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        val comicsOut = comics.filter { it.status == ComicStatus.IN_PRENOTAZIONE && it.userId == currentUserId }
        val comicsAvailable = comics.filter { it.status == ComicStatus.DISPONIBILE }
        val comicsUnavailable = comics.filter { it.status == ComicStatus.NON_DISPONIBILE }

        adapterOut = createAndSetAdapter(recyclerOut, comicsOut, { adapterOut = it }, { adapterOut })
        adapterAvailable = createAndSetAdapter(recyclerAvailable, comicsAvailable, { adapterAvailable = it }, { adapterAvailable })
        adapterUnavailable = createAndSetAdapter(recyclerUnavailable, comicsUnavailable, { adapterUnavailable = it }, { adapterUnavailable })
    }

    private fun createAndSetAdapter(
        recycler: RecyclerView,
        comics: List<Comic>,
        adapterSetter: (ComicsAdapter) -> Unit,
        adapterGetter: () -> ComicsAdapter
    ): ComicsAdapter {
        val adapter = ComicsAdapter(
            this, comics.toMutableList(), ComicsAdapter.AdapterMode.PREVIEW, comicDatabase,
            updateStatus = { comic, status ->
                updateStatusVisual(recycler, adapterGetter().getPositionFromComic(comic), status)
            },
            onComicClick = { comic ->
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                if (comic.userId.isNullOrEmpty() || comic.userId == "undefined") {
                    prenotaComic(comic)
                } else if (comic.userId == currentUserId) {
                    rilasciaComic(comic)
                } else {
                    Toast.makeText(this, "Fumetto non disponibile", Toast.LENGTH_SHORT).show()
                }
            }
        )
        recycler.adapter = adapter
        adapterSetter(adapter)
        return adapter
    }

    private fun prenotaComic(comic: Comic) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("comic").document(comic.id)
            .update("userId", currentUserId)
            .addOnSuccessListener {
                comic.userId = currentUserId
                comic.status = ComicStatus.IN_PRENOTAZIONE
                spostaComic(comic)
            }
    }

    private fun rilasciaComic(comic: Comic) {
        FirebaseFirestore.getInstance().collection("comic").document(comic.id)
            .update("userId", null)
            .addOnSuccessListener {
                comic.userId = null
                comic.status = ComicStatus.DISPONIBILE
                spostaComic(comic)
            }
    }

    private fun spostaComic(comic: Comic) {
        adapterAvailable.removeComics(comic)
        adapterOut.removeComics(comic)
        adapterUnavailable.removeComics(comic)

        when (comic.status) {
            ComicStatus.DISPONIBILE -> adapterAvailable.addComics(comic)
            ComicStatus.IN_PRENOTAZIONE -> adapterOut.addComics(comic)
            ComicStatus.NON_DISPONIBILE -> adapterUnavailable.addComics(comic)
            else -> {}
        }
    }

    private fun updateStatusVisual(recyclerView: RecyclerView, position: Int, status: ComicStatus) {
        val viewHolder = recyclerView.findViewHolderForAdapterPosition(position)
        val view = viewHolder?.itemView?.findViewById<ImageView>(R.id.statusIndicator)
        if (view != null) {
            updateComicStatus(view, status.name.lowercase())
        }
    }

    private fun updateComicStatus(view: ImageView, status: String) {
        when (status) {
            "disponibile" -> view.setImageResource(R.drawable.ic_circle_green)
            "in_prestito" -> view.setImageResource(R.drawable.ic_circle_yellow)
            "occupato" -> view.setImageResource(R.drawable.ic_circle_red)
            else -> view.setImageResource(R.drawable.ic_circle_gray)
        }
    }
}
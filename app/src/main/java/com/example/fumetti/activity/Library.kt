package com.example.fumetti.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fumetti.R
import com.example.fumetti.data.Comic
import com.example.fumetti.data.ComicStatus
import com.example.fumetti.database.ComicDatabase
import com.example.fumetti.database.adapter.ComicsAdapter
import com.google.firebase.firestore.FirebaseFirestore

/*class Library : AppCompatActivity() {

    private val comicDatabase = ComicDatabase()

    private lateinit var comicsOutRecycler: RecyclerView
    private lateinit var comicsDisponibiliRecycler: RecyclerView
    private lateinit var comicsNonDisponibiliRecycler: RecyclerView

    private lateinit var comicAdapterOut: ComicsAdapter
    private lateinit var comicAdapterDisponibili: ComicsAdapter
    private lateinit var comicAdapterNonDisponibili: ComicsAdapter

    companion object {
        const val TAG = "LibraryActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)

        Log.d(TAG, "onCreate: Inizializzazione della Library Activity")

        // Associa le RecyclerView
        comicsOutRecycler = findViewById(R.id.comicsOut)
        comicsDisponibiliRecycler = findViewById(R.id.comicsDisponibili)
        comicsNonDisponibiliRecycler = findViewById(R.id.recyclerView3)

        // Imposta i LayoutManager
        comicsOutRecycler.layoutManager = LinearLayoutManager(this)
        comicsDisponibiliRecycler.layoutManager = LinearLayoutManager(this)
        comicsNonDisponibiliRecycler.layoutManager = LinearLayoutManager(this)

        // Inizializza gli adapter
        comicAdapterOut = ComicsAdapter(
            this, listOf(), ComicsAdapter.AdapterMode.LIBRARY, comicDatabase,
            updateStatus = { comic, newStatus ->
                Log.d(TAG, "updateStatus: Aggiornamento status del fumetto ${comic.id} a $newStatus")
            },
            onComicClick = { comic ->
                Log.d(TAG, "onComicClick: Fumetto cliccato ${comic.id}")
                startActivity(Intent(this, ComicDetailActivity::class.java).apply {
                    putExtra("COMIC_ID", comic.id)
                })
            }
        )

        comicAdapterDisponibili = ComicsAdapter(
            this, listOf(), ComicsAdapter.AdapterMode.LIBRARY, comicDatabase,
            updateStatus = { comic, newStatus ->
                Log.d(TAG, "updateStatus: Aggiornamento status del fumetto ${comic.id} a $newStatus")
            },
            onComicClick = { comic ->
                Log.d(TAG, "onComicClick: Fumetto cliccato ${comic.id}")
                startActivity(Intent(this, ComicDetailActivity::class.java).apply {
                    putExtra("COMIC_ID", comic.id)
                })
            }
        )

        comicAdapterNonDisponibili = ComicsAdapter(
            this, listOf(), ComicsAdapter.AdapterMode.LIBRARY, comicDatabase,
            updateStatus = { comic, newStatus ->
                Log.d(TAG, "updateStatus: Aggiornamento status del fumetto ${comic.id} a $newStatus")
            },
            onComicClick = { comic ->
                Log.d(TAG, "onComicClick: Fumetto cliccato ${comic.id}")
                startActivity(Intent(this, ComicDetailActivity::class.java).apply {
                    putExtra("COMIC_ID", comic.id)
                })
            }
        )

        // Assegna gli adapter alle RecyclerView
        comicsOutRecycler.adapter = comicAdapterOut
        comicsDisponibiliRecycler.adapter = comicAdapterDisponibili
        comicsNonDisponibiliRecycler.adapter = comicAdapterNonDisponibili

        // Configura i pulsanti per la navigazione e logga il click
        findViewById<Button>(R.id.buttonHomePage).setOnClickListener {
            Log.d(TAG, "buttonHomePage clicked: Navigazione verso UserHomePageActivity")
            startActivity(Intent(this, UserHomePageActivity::class.java))
            finish()
        }

        findViewById<Button>(R.id.buttonMyLibrary).setOnClickListener {
            Log.d(TAG, "buttonMyLibrary clicked: Navigazione verso MyLibrary")
            startActivity(Intent(this, MyLibrary::class.java))
            finish()
        }

        findViewById<Button>(R.id.buttonOrderedComics).setOnClickListener {
            Log.d(TAG, "buttonOrderedComics clicked: Navigazione verso OrderedComics")
            startActivity(Intent(this, OrderedComics::class.java))
        }

        findViewById<ImageButton>(R.id.profileIcon).setOnClickListener {
            Log.d(TAG, "profileIcon clicked: Navigazione verso UserProfileActivity")
            startActivity(Intent(this, UserProfileActivity::class.java))
        }

        loadData()
    }

    private fun loadData() {
        Log.d(TAG, "loadData: Avvio caricamento dati da Firestore")
        val db = FirebaseFirestore.getInstance()
        db.collection("comic")
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Log.w(TAG, "loadData: Nessun fumetto trovato")
                    Toast.makeText(this, "Nessun fumetto trovato", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val comics = result.map { document ->
                    Log.d(TAG, "loadData: Elaborazione documento ${document.id}")
                    try {
                        // Se "id" non è presente, viene utilizzato l'id del documento
                        val id = document.getString("id") ?: document.id
                        val name = document.getString("name") ?: ""
                        val series = document.getString("series") ?: ""
                        // Prova prima con getLong e poi con una conversione da String
                        val number = document.getLong("number")?.toInt() ?: (document.getString("number")?.toIntOrNull() ?: 0)
                        val description = document.getString("description") ?: ""
                        val imageUrl = document.getString("imageUrl") ?: ""
                        // Conversione di userId in Int
                        val userIdFromDb = document.getLong("userId")?.toInt() ?: (document.getString("userId")?.toIntOrNull() ?: 0)
                        val statusString = document.getString("status")
                        val status = if (statusString != null) {
                            try {
                                ComicStatus.valueOf(statusString)
                            } catch (e: IllegalArgumentException) {
                                Log.e(TAG, "loadData: Stato non valido per il fumetto $id: $statusString", e)
                                ComicStatus.UNKNOWN
                            }
                        } else {
                            ComicStatus.DISPONIBILE
                        }
                        Comic(id, name, imageUrl, number, series, description, status, userIdFromDb)
                    } catch (e: Exception) {
                        Log.e(TAG, "loadData: Errore nella mappatura del documento ${document.id}: ${e.message}", e)
                        // Se occorre, si può decidere di escludere il documento oppure restituire un oggetto fittizio
                        Comic("", "", "", 0, "", "", ComicStatus.UNKNOWN, 0)
                    }
                }

                Log.d(TAG, "loadData: Fumetti totali caricati: ${comics.size}")

                // Filtra in base allo status e aggiorna gli adapter
                val comicsInPreorder = comics.filter { it.status == ComicStatus.IN_PRENOTAZIONE }
                val comicsDisponibili = comics.filter { it.status == ComicStatus.DISPONIBILE }
                val comicsNonDisponibili = comics.filter { it.status == ComicStatus.NON_DISPONIBILE }

                Log.d(TAG, "loadData: Fumetti in pre-ordine: ${comicsInPreorder.size}")
                Log.d(TAG, "loadData: Fumetti disponibili: ${comicsDisponibili.size}")
                Log.d(TAG, "loadData: Fumetti non disponibili: ${comicsNonDisponibili.size}")

                comicAdapterOut.updateList(comicsInPreorder)
                comicAdapterDisponibili.updateList(comicsDisponibili)
                comicAdapterNonDisponibili.updateList(comicsNonDisponibili)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "loadData: Errore di caricamento da Firestore: ${exception.message}", exception)
                Toast.makeText(this, "Errore nel caricamento dei dati", Toast.LENGTH_SHORT).show()
            }
    }
}
*/
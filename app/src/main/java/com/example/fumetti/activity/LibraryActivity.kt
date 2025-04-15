package com.example.fumetti.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
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
import com.google.firebase.firestore.FirebaseFirestore
class LibraryActivity : AppCompatActivity() {

    private val comicDatabase = ComicDatabase()
    companion object {
        const val TAG = "LibraryActivity"
        const val EXTRA_USER_ID = "EXTRA_USER_ID"
    }

    private val userId: String? by lazy { intent.getStringExtra(EXTRA_USER_ID) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)

        Log.d(TAG, "Activity avviata in modalità ${if (userId != null) "utente" else "globale"}")

        // Initialize RecyclerViews
        val recyclerOut = findViewById<RecyclerView>(R.id.recyclerViewOut)
        recyclerOut.layoutManager = LinearLayoutManager(this)
        recyclerOut.adapter = ComicsAdapter(this, emptyList(), ComicsAdapter.AdapterMode.LIBRARY, comicDatabase, { _, _ -> }, { })

        val recyclerAvailable = findViewById<RecyclerView>(R.id.recyclerViewAvailable)
        recyclerAvailable.layoutManager = LinearLayoutManager(this)
        recyclerAvailable.adapter = ComicsAdapter(this, emptyList(), ComicsAdapter.AdapterMode.LIBRARY, comicDatabase, { _, _ -> }, { })

        val recyclerUnavailable = findViewById<RecyclerView>(R.id.recyclerViewUnavailable)
        recyclerUnavailable.layoutManager = LinearLayoutManager(this)
        recyclerUnavailable.adapter = ComicsAdapter(this, emptyList(), ComicsAdapter.AdapterMode.LIBRARY, comicDatabase, { _, _ -> }, { })

        findViewById<Button>(R.id.buttonHomePage).setOnClickListener {
            startActivity(Intent(this, UserHomePageActivity::class.java))
            finish()
        }
        loadData(recyclerOut, recyclerAvailable, recyclerUnavailable)
    }
    private fun loadData(
        recyclerViewOut: RecyclerView,
        recyclerViewAvailable: RecyclerView,
        recyclerViewUnavailable: RecyclerView
    ) {
        val db = FirebaseFirestore.getInstance()
        db.collection("comic")
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Toast.makeText(this, "Nessun fumetto trovato", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

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
                        if (statusStr != null) ComicStatus.valueOf(statusStr)
                        else ComicStatus.DISPONIBILE
                    } catch (e: Exception) {
                        Log.e(TAG, "Stato non valido per il fumetto $id ($statusStr)", e)
                        ComicStatus.UNKOWN
                    }
                    val seriesNumber = document.getLong("seriesNumber")?.toInt()
                        ?: (document.getString("seriesNumber")?.toIntOrNull() ?: 0)

                    Comic(
                        description,
                        id,
                        imageUrl,
                        name,
                        number,
                        numericId,
                        series,
                        seriesNumber,
                        status,
                        userIdFromDb
                    )
                }

                // Filtra i fumetti per stato
                val comicsOut = comics.filter { it.status == ComicStatus.IN_PRENOTAZIONE && it.userId == currentUserId }
                val comicsAvailable = comics.filter { it.status == ComicStatus.DISPONIBILE }
                val comicsUnavailable = comics.filter { it.status == ComicStatus.NON_DISPONIBILE }

                // Popola i RecyclerView con gli adapter aggiornati
                recyclerViewOut.adapter = ComicsAdapter(
                    this, comicsOut, ComicsAdapter.AdapterMode.LIBRARY,
                    comicDatabase = comicDatabase,
                    updateStatus = { comic, newStatus ->
                        if (comic.status == ComicStatus.IN_PRENOTAZIONE && newStatus == ComicStatus.DISPONIBILE) {
                            comicDatabase.updateComicStatus(comic.id, newStatus,
                                onSuccess = {
                                    Toast.makeText(this, "Stato aggiornato con successo", Toast.LENGTH_SHORT).show()
                                },
                                onFailure = { exception ->
                                    Toast.makeText(this, "Errore nell'aggiornamento: ${exception.message}", Toast.LENGTH_SHORT).show()
                                }
                            )
                        } else {
                            Toast.makeText(
                                this,
                                "Transizione non valida per lo stato del fumetto",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    onComicClick = { comic ->
                        val intent = Intent(this, ComicDetailActivity::class.java)
                        intent.putExtra("COMIC_ID", comic.id)
                        startActivity(intent)
                    }
                )

                recyclerViewAvailable.adapter = ComicsAdapter(
                    this, comicsAvailable, ComicsAdapter.AdapterMode.LIBRARY,
                    comicDatabase = comicDatabase,
                    updateStatus = { comic, newStatus ->
                        if (comic.status == ComicStatus.DISPONIBILE && newStatus == ComicStatus.IN_PRENOTAZIONE) {
                            comicDatabase.updateComicStatus(comic.id, newStatus,
                                onSuccess = {
                                    Toast.makeText(this, "Stato aggiornato con successo", Toast.LENGTH_SHORT).show()
                                },
                                onFailure = { exception ->
                                    Toast.makeText(this, "Errore nell'aggiornamento: ${exception.message}", Toast.LENGTH_SHORT).show()
                                }
                            )
                        } else {
                            Toast.makeText(
                                this,
                                "Transizione non valida per lo stato del fumetto",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    onComicClick = { comic ->
                        val intent = Intent(this, ComicDetailActivity::class.java)
                        intent.putExtra("COMIC_ID", comic.id)
                        startActivity(intent)
                    }
                )

                recyclerViewUnavailable.adapter = ComicsAdapter(
                    this, comicsUnavailable, ComicsAdapter.AdapterMode.LIBRARY,
                    comicDatabase = comicDatabase,
                    updateStatus = { _, _ ->
                        Toast.makeText(
                            this,
                            "I fumetti non disponibili non possono essere aggiornati",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onComicClick = { comic ->
                        val intent = Intent(this, ComicDetailActivity::class.java)
                        intent.putExtra("COMIC_ID", comic.id)
                        startActivity(intent)
                    }
                )
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Errore nel recupero dei fumetti: ${exception.message}", exception)
            }
    }
}
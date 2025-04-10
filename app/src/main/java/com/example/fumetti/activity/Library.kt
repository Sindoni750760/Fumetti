package com.example.fumetti.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fumetti.R
import com.example.fumetti.data.Comic
import com.example.fumetti.data.ComicStatus
import com.example.fumetti.database.ComicDatabase
import com.example.fumetti.database.AddComics
import com.example.fumetti.database.OrderedComics
import com.example.fumetti.database.adapter.ComicsAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Library : AppCompatActivity() {
    private val comicDatabase = ComicDatabase()

    private lateinit var comicAdapterOut: ComicsAdapter
    private lateinit var comicAdapterDisponibili: ComicsAdapter
    private lateinit var comicAdapterNonDisponibili: ComicsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)

        val comicsOutRecycler = findViewById<RecyclerView>(R.id.comicsOut)
        val comicsDisponibiliRecycler = findViewById<RecyclerView>(R.id.comicsDisponibili)
        val comicsNonDisponibiliRecycler = findViewById<RecyclerView>(R.id.recyclerView3)

        comicsOutRecycler.layoutManager = LinearLayoutManager(this)
        comicsDisponibiliRecycler.layoutManager = LinearLayoutManager(this)
        comicsNonDisponibiliRecycler.layoutManager = LinearLayoutManager(this)

        findViewById<Button>(R.id.buttonHomePage).setOnClickListener {
            startActivity(Intent(this, UserHomePageActivity::class.java))
            finish()
        }

        findViewById<Button>(R.id.buttonMyLibrary).setOnClickListener {
            startActivity(Intent(this, MyLibrary::class.java))
            finish()
        }

        findViewById<Button>(R.id.buttonOrderedComics).setOnClickListener {
            startActivity(Intent(this, OrderedComics::class.java))
        }

        findViewById<ImageButton>(R.id.profileIcon).setOnClickListener {
            startActivity(Intent(this, UserProfileActivity::class.java))
        }

        loadData()
    }

    private fun loadData() {
        val db = FirebaseFirestore.getInstance()
        db.collection("comic")
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Toast.makeText(this, "Nessun fumetto trovato", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val comics = result.map {
                    val id = it.getString("id") ?: ""
                    val name = it.getString("name") ?: ""
                    val series = it.getString("series") ?: ""
                    val number = it.getString("number")?.toIntOrNull() ?: 0
                    val description = it.getString("description") ?: ""
                    val imageUrl = it.getString("imageUrl") ?: ""
                    val userIdFromDb = it.getLong("userId")?.toInt() ?: 0
                    val status = ComicStatus.valueOf(it.getString("status") ?: "UNKNOWN")

                    Comic(id, name, imageUrl, number, series, description, status, userIdFromDb)
                }

                val comicsOut = comics.filter { it.status == ComicStatus.IN_PRENOTAZIONE }
                val comicsDisponibili = comics.filter { it.status == ComicStatus.DISPONIBILE }
                val comicsNonDisponibili = comics.filter { it.status == ComicStatus.NON_DISPONIBILE }

                comicAdapterOut = ComicsAdapter(
                    this, comicsOut, ComicsAdapter.AdapterMode.LIBRARY, comicDatabase,
                    updateStatus = { _, _ -> },
                    onComicClick = { comic ->
                        val intent = Intent(this, ComicDetailActivity::class.java)
                        intent.putExtra("COMIC_ID", comic.id)
                        startActivity(intent)
                    }
                )

                comicAdapterDisponibili = ComicsAdapter(
                    this, comicsDisponibili, ComicsAdapter.AdapterMode.LIBRARY, comicDatabase,
                    updateStatus = { _, _ -> },
                    onComicClick = { comic ->
                        val intent = Intent(this, ComicDetailActivity::class.java)
                        intent.putExtra("COMIC_ID", comic.id)
                        startActivity(intent)
                    }
                )

                comicAdapterNonDisponibili = ComicsAdapter(
                    this, comicsNonDisponibili, ComicsAdapter.AdapterMode.LIBRARY, comicDatabase,
                    updateStatus = { _, _ -> },
                    onComicClick = { comic ->
                        val intent = Intent(this, ComicDetailActivity::class.java)
                        intent.putExtra("COMIC_ID", comic.id)
                        startActivity(intent)
                    }
                )

                findViewById<RecyclerView>(R.id.comicsOut).adapter = comicAdapterOut
                findViewById<RecyclerView>(R.id.comicsDisponibili).adapter = comicAdapterDisponibili
                findViewById<RecyclerView>(R.id.recyclerView3).adapter = comicAdapterNonDisponibili
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Errore di caricamento", exception)
            }
    }
}

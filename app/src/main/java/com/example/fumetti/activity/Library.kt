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

        // Configura layout manager per i RecyclerView
        comicsOutRecycler.layoutManager = LinearLayoutManager(this)
        comicsDisponibiliRecycler.layoutManager = LinearLayoutManager(this)
        comicsNonDisponibiliRecycler.layoutManager = LinearLayoutManager(this)

        // Inizializza e assegna gli adapter
        comicAdapterOut = ComicsAdapter(this, emptyList(), ComicsAdapter.AdapterMode.LIBRARY, comicDatabase) { _, _ -> }
        comicAdapterDisponibili = ComicsAdapter(this, emptyList(), ComicsAdapter.AdapterMode.LIBRARY, comicDatabase) { _, _ -> }
        comicAdapterNonDisponibili = ComicsAdapter(this, emptyList(), ComicsAdapter.AdapterMode.LIBRARY, comicDatabase) { _, _ -> }

        comicsOutRecycler.adapter = comicAdapterOut
        comicsDisponibiliRecycler.adapter = comicAdapterDisponibili
        comicsNonDisponibiliRecycler.adapter = comicAdapterNonDisponibili

        // Navigazione tra le schermate
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

        // Caricamento dati fumetti dall'utente corrente
        val userId = getUserId()
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
                    val id = it.getLong("id")?.toInt() ?: 0
                    val name = it.getString("name") ?: ""
                    val series = it.getString("series")
                    val number = it.getString("number")
                    val description = it.getString("description") ?: ""
                    val imageUrl = it.getString("imageUrl") ?: ""
                    val userIdFromDb = it.getLong("userId")?.toInt() ?: 0
                    val status = ComicStatus.valueOf(it.getString("status") ?: "UNKNOWN")

                    Comic(id, name, series, number, description, imageUrl, userIdFromDb, status)
                }

                val comicsOut = comics.filter { it.status == ComicStatus.IN_PRENOTAZIONE }
                val comicsDisponibili = comics.filter { it.status == ComicStatus.DISPONIBILE }
                val comicsNonDisponibili = comics.filter { it.status == ComicStatus.NON_DISPONIBILE }

                comicAdapterOut.updateList(comicsOut)
                comicAdapterDisponibili.updateList(comicsDisponibili)
                comicAdapterNonDisponibili.updateList(comicsNonDisponibili)
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Errore di caricamento", exception)
            }
    }

    private fun getUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: "defaultUser"
    }

    // Metodo per aggiornare lo stato grafico del fumetto
    private fun updateComicStatus(view: ImageView, status: String) {
        when (status) {
            "DISPONIBILE" -> view.setImageResource(R.drawable.ic_circle_green)
            "IN_PRENOTAZIONE" -> view.setImageResource(R.drawable.ic_circle_yellow)
            "NON_DISPONIBILE" -> view.setImageResource(R.drawable.ic_circle_red)
            else -> view.setImageResource(R.drawable.ic_circle_gray)
        }
    }
}

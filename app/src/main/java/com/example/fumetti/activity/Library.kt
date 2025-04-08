package com.example.fumetti.activity

import android.content.Intent
import android.os.Bundle
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
import com.example.fumetti.database.ComicsAdapter
import com.example.fumetti.database.OrderedComics

class Library : AppCompatActivity() {
    private val comicDatabase = ComicDatabase()
    private lateinit var comicAdapter: ComicsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Pulsante per tornare alla Home
        val buttonHomePage = findViewById<Button>(R.id.buttonHomePage)
        buttonHomePage.setOnClickListener {
            startActivity(Intent(this, UserHomePageActivity::class.java))
            finish()
        }

        // Pulsante per accedere alla MyLibrary
        val buttonMyLibrary = findViewById<Button>(R.id.buttonMyLibrary)
        buttonMyLibrary.setOnClickListener {
            startActivity(Intent(this, MyLibrary::class.java))
            finish()
        }

        // Icona profilo per accedere al profilo utente
        val profileIcon = findViewById<ImageView>(R.id.profileIcon)
        profileIcon.setOnClickListener {
            startActivity(Intent(this, UserProfileActivity::class.java))
        }

        // Caricamento fumetti disponibili nella RecyclerView
        //TODO: da sistemare
        val userId = "USER_ID" // Sostituisci con l'ID utente corretto
        comicDatabase.getAllComicsByUser(userId){ comics: List<Comic> ->
            val availableComics = comics.filter { it.status == ComicStatus.MANCANTE }
            if (availableComics.isNotEmpty()) {
                comicAdapter = ComicsAdapter(
                    this, availableComics,
                    ComicsAdapter.AdapterMode.PREVIEW,
                    comicDatabase
                ) { view, status ->
                    updateComicStatus(view, status)
                }
                recyclerView.adapter = comicAdapter
            } else {
                Toast.makeText(this, "Nessun fumetto disponibile", Toast.LENGTH_SHORT).show()
            }
        }

        val buttonOrderedComics = findViewById<Button>(R.id.buttonOrderedComics)
        buttonOrderedComics.setOnClickListener {
            startActivity(Intent(this, OrderedComics::class.java))
        }
    }
    fun updateComicStatus(view: ImageView, status: String) {
        when (status) {
            "disponibile" -> view.setImageResource(R.drawable.ic_circle_green)
            "in prestito" -> view.setImageResource(R.drawable.ic_circle_yellow)
            "non disponibili" -> view.setImageResource(R.drawable.ic_circle_red)
        }
    }
}
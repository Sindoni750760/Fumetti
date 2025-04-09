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
        findViewById<Button>(R.id.buttonHomePage).setOnClickListener {
            startActivity(Intent(this, UserHomePageActivity::class.java))
            finish()
        }

        // Pulsante per accedere alla MyLibrary
        findViewById<Button>(R.id.buttonMyLibrary).setOnClickListener {
            startActivity(Intent(this, MyLibrary::class.java))
            finish()
        }

        // Icona profilo per accedere al profilo utente
        findViewById<ImageView>(R.id.profileIcon).setOnClickListener {
            startActivity(Intent(this, UserProfileActivity::class.java))
        }

        // Caricamento fumetti disponibili nella RecyclerView
        val userId = "USER_ID" // Sostituisci con l'ID utente corretto
        comicDatabase.getAllComicsByUser(userId) { comics: List<Comic> ->
            val availableComics = comics.filter { it.status == ComicStatus.NON_DISPONIBILE }
            if (availableComics.isNotEmpty()) {
                comicAdapter = ComicsAdapter(
                    this, availableComics,
                    ComicsAdapter.AdapterMode.PREVIEW,
                    comicDatabase
                ) { comic, status ->
                    val view = recyclerView.findViewHolderForAdapterPosition(availableComics.indexOf(comic))
                        ?.itemView?.findViewById<ImageView>(R.id.statusIndicator)
                    view?.let { updateComicStatus(it, status.toString()) }
                }
                recyclerView.adapter = comicAdapter
            } else {
                Toast.makeText(this, "Nessun fumetto disponibile", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.buttonOrderedComics).setOnClickListener {
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

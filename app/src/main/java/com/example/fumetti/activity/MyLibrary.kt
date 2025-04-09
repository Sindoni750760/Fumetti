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
import com.example.fumetti.database.AddComics
import com.example.fumetti.database.OrderedComics
import com.example.fumetti.R
import com.example.fumetti.database.ComicDatabase
import com.example.fumetti.database.ComicsAdapter
import com.example.fumetti.database.RemoveComics
import com.example.fumetti.data.Comic

class MyLibrary : AppCompatActivity() {

    private val comicDatabase = ComicDatabase()
    private lateinit var comicAdapter: ComicsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_library)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Pulsante per tornare alla Home
        findViewById<Button>(R.id.buttonHomePage).setOnClickListener {
            startActivity(Intent(this, UserHomePageActivity::class.java))
            finish()
        }

        // Pulsante per accedere alla Library
        findViewById<Button>(R.id.buttonMyLibrary).setOnClickListener {
            startActivity(Intent(this, Library::class.java))
            finish()
        }

        // Pulsante per accedere alla schermata di aggiunta fumetti
        findViewById<ImageButton>(R.id.buttonAddComics).setOnClickListener {
            startActivity(Intent(this, AddComics::class.java))
        }

        // Pulsante per accedere alla schermata di prenotazione fumetti
        findViewById<ImageButton>(R.id.buttonRemoveComics).setOnClickListener {
            startActivity(Intent(this, RemoveComics::class.java))
        }

        // Icona profilo per accedere alla schermata profilo
        findViewById<ImageView>(R.id.profileIcon).setOnClickListener {
            startActivity(Intent(this, UserProfileActivity::class.java))
        }

        // Caricamento fumetti prenotati nella RecyclerView
        val userId = "USER_ID" // Sostituisci con l'ID utente corretto
        comicDatabase.getAllComicsByUser(userId) { comics: List<Comic> ->
            if (comics.isNotEmpty()) {
                comicAdapter = ComicsAdapter(
                    this, comics,
                    ComicsAdapter.AdapterMode.PREVIEW,
                    comicDatabase
                ) { comic, status ->
                    val view = recyclerView.findViewHolderForAdapterPosition(comics.indexOf(comic))
                        ?.itemView?.findViewById<ImageView>(R.id.statusIndicator)
                    view?.let { updateComicStatus(it, status.toString()) }
                }
                recyclerView.adapter = comicAdapter
            } else {
                Toast.makeText(this, "Nessun fumetto disponibile", Toast.LENGTH_SHORT).show()
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

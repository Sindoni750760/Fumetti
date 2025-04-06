package com.example.fumetti.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fumetti.R
import com.example.fumetti.database.ComicDatabase
import com.example.fumetti.database.ComicsAdapter

class MyLibrary : AppCompatActivity() {

    private val comicDatabase = ComicDatabase()
    private lateinit var comicAdapter: ComicsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_library)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Pulsante per tornare alla Home
        val buttonHomePage = findViewById<Button>(R.id.buttonToHomePage)
        buttonHomePage.setOnClickListener {
            startActivity(Intent(this, UserHomePageActivity::class.java))
            finish()
        }

        // Pulsante per accedere alla Library
        val buttonLibrary = findViewById<Button>(R.id.buttonToMyLibrary)
        buttonLibrary.setOnClickListener {
            startActivity(Intent(this, Library::class.java))
            finish()
        }

        // Icona profilo per accedere alla schermata profilo
        val profileIcon = findViewById<ImageView>(R.id.profileIcon)
        profileIcon.setOnClickListener {
            startActivity(Intent(this, UserProfileActivity::class.java))
        }

        // Caricamento fumetti prenotati nella RecyclerView
        comicDatabase.getAllComics { comics ->
            if (comics.isNotEmpty()) {
                comicAdapter = ComicsAdapter(
                    this, comics,
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
    }

    fun updateComicStatus(view: ImageView, status: String) {
        when (status) {
            "disponibile" -> view.setImageResource(R.drawable.ic_circle_green)
            "in prestito" -> view.setImageResource(R.drawable.ic_circle_yellow)
            "occupato" -> view.setImageResource(R.drawable.ic_circle_red)
        }
    }
}

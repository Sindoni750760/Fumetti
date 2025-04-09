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
import com.example.fumetti.database.adapter.ComicsAdapter
import com.example.fumetti.database.RemoveComics
import com.example.fumetti.data.Comic
import com.example.fumetti.data.ComicStatus
import com.google.firebase.auth.FirebaseAuth

class MyLibrary : AppCompatActivity() {

    private val comicDatabase = ComicDatabase()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_library)

        val recyclerOut = findViewById<RecyclerView>(R.id.recyclerViewOut)
        val recyclerAvailable = findViewById<RecyclerView>(R.id.recyclerViewAvailable)
        val recyclerUnavailable = findViewById<RecyclerView>(R.id.recyclerViewUnavailable)

        recyclerOut.layoutManager = LinearLayoutManager(this)
        recyclerAvailable.layoutManager = LinearLayoutManager(this)
        recyclerUnavailable.layoutManager = LinearLayoutManager(this)

        // Adapter iniziali vuoti per evitare warning
        recyclerOut.adapter = ComicsAdapter(this, emptyList(), ComicsAdapter.AdapterMode.PREVIEW, comicDatabase) { _, _ -> }
        recyclerAvailable.adapter = ComicsAdapter(this, emptyList(), ComicsAdapter.AdapterMode.PREVIEW, comicDatabase) { _, _ -> }
        recyclerUnavailable.adapter = ComicsAdapter(this, emptyList(), ComicsAdapter.AdapterMode.PREVIEW, comicDatabase) { _, _ -> }

        // Navigazione
        findViewById<Button>(R.id.buttonHomePage).setOnClickListener {
            startActivity(Intent(this, UserHomePageActivity::class.java))
            finish()
        }

        findViewById<Button>(R.id.buttonMyLibrary).setOnClickListener {
            startActivity(Intent(this, Library::class.java))
            finish()
        }

        findViewById<ImageButton>(R.id.buttonAddComics).setOnClickListener {
            startActivity(Intent(this, AddComics::class.java))
        }

        findViewById<ImageButton>(R.id.buttonRemoveComics).setOnClickListener {
            startActivity(Intent(this, RemoveComics::class.java))
        }

        findViewById<ImageView>(R.id.profileIcon).setOnClickListener {
            startActivity(Intent(this, UserProfileActivity::class.java))
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "defaultUser"

        comicDatabase.getAllComicsByUser(userId) { comics: List<Comic> ->
            val comicsOut = comics.filter { it.status == ComicStatus.NON_DISPONIBILE }
            val comicsAvailable = comics.filter { it.status == ComicStatus.DISPONIBILE }
            val comicsUnavailable = comics.filter { it.status == ComicStatus.IN_PRENOTAZIONE }

            recyclerOut.adapter = ComicsAdapter(
                this, comicsOut, ComicsAdapter.AdapterMode.PREVIEW, comicDatabase
            ) { comic, status ->
                updateStatusVisual(recyclerOut, comicsOut.indexOf(comic), status)
            }

            recyclerAvailable.adapter = ComicsAdapter(
                this, comicsAvailable, ComicsAdapter.AdapterMode.PREVIEW, comicDatabase
            ) { comic, status ->
                updateStatusVisual(recyclerAvailable, comicsAvailable.indexOf(comic), status)
            }

            recyclerUnavailable.adapter = ComicsAdapter(
                this, comicsUnavailable, ComicsAdapter.AdapterMode.PREVIEW, comicDatabase
            ) { comic, status ->
                updateStatusVisual(recyclerUnavailable, comicsUnavailable.indexOf(comic), status)
            }
        }
    }

    private fun updateStatusVisual(recyclerView: RecyclerView, position: Int, status: ComicStatus) {
        val view = recyclerView.findViewHolderForAdapterPosition(position)
            ?.itemView?.findViewById<ImageView>(R.id.statusIndicator)
        view?.let { updateComicStatus(it, status.name.lowercase()) }
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
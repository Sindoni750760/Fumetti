package com.example.fumetti.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fumetti.R
import com.example.fumetti.database.ComicDatabase
import com.example.fumetti.database.adapter.ComicsAdapter
import com.example.fumetti.data.ComicStatus
import com.google.firebase.auth.FirebaseAuth

/*class MyLibrary : AppCompatActivity() {

    private val comicDatabase = ComicDatabase()

    private lateinit var recyclerOut: RecyclerView
    private lateinit var recyclerAvailable: RecyclerView
    private lateinit var recyclerUnavailable: RecyclerView

    private lateinit var adapterOut: ComicsAdapter
    private lateinit var adapterAvailable: ComicsAdapter
    private lateinit var adapterUnavailable: ComicsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_library)

        recyclerOut = findViewById(R.id.recyclerViewOut)
        recyclerAvailable = findViewById(R.id.recyclerViewAvailable)
        recyclerUnavailable = findViewById(R.id.recyclerViewUnavailable)

        recyclerOut.layoutManager = LinearLayoutManager(this)
        recyclerAvailable.layoutManager = LinearLayoutManager(this)
        recyclerUnavailable.layoutManager = LinearLayoutManager(this)

        adapterOut = ComicsAdapter(
            this, listOf(), ComicsAdapter.AdapterMode.PREVIEW, comicDatabase,
            updateStatus = { comic, status ->
                updateStatusVisual(recyclerOut, adapterOut.getPositionFromComic(comic), status)
            },
            onComicClick = { comic ->
                startActivity(Intent(this, ComicDetailActivity::class.java).apply {
                    putExtra("COMIC_ID", comic.id)
                })
            }
        )

        adapterAvailable = ComicsAdapter(
            this, listOf(), ComicsAdapter.AdapterMode.PREVIEW, comicDatabase,
            updateStatus = { comic, status ->
                updateStatusVisual(recyclerAvailable, adapterAvailable.getPositionFromComic(comic), status)
            },
            onComicClick = { comic ->
                startActivity(Intent(this, ComicDetailActivity::class.java).apply {
                    putExtra("COMIC_ID", comic.id)
                })
            }
        )

        adapterUnavailable = ComicsAdapter(
            this, listOf(), ComicsAdapter.AdapterMode.PREVIEW, comicDatabase,
            updateStatus = { comic, status ->
                updateStatusVisual(recyclerUnavailable, adapterUnavailable.getPositionFromComic(comic), status)
            },
            onComicClick = { comic ->
                startActivity(Intent(this, ComicDetailActivity::class.java).apply {
                    putExtra("COMIC_ID", comic.id)
                })
            }
        )

        recyclerOut.adapter = adapterOut
        recyclerAvailable.adapter = adapterAvailable
        recyclerUnavailable.adapter = adapterUnavailable

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

        comicDatabase.getAllComicsByUser(userId) { comics ->
            adapterOut.updateList(comics.filter { it.status == ComicStatus.NON_DISPONIBILE })
            adapterAvailable.updateList(comics.filter { it.status == ComicStatus.DISPONIBILE })
            adapterUnavailable.updateList(comics.filter { it.status == ComicStatus.IN_PRENOTAZIONE })
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
*/
package com.example.fumetti.database.utility


import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fumetti.activity.ComicDetailActivity
import com.example.fumetti.data.Comic
import com.example.fumetti.data.ComicStatus
import com.example.fumetti.database.ComicDatabase
import com.google.firebase.firestore.FirebaseFirestore

class ComicLoader(private val context: Context) {

    private val comicDatabase = ComicDatabase()

    fun loadComics(
        recyclerView: RecyclerView,
        adapterMode: ComicsAdapter.AdapterMode = ComicsAdapter.AdapterMode.PREVIEW,
        filter: (Comic) -> Boolean = { true },
        sort: (List<Comic>) -> List<Comic> = { it.sortedBy { comic -> comic.seriesNumber } }
    ) {
        if (recyclerView.layoutManager == null) {
            recyclerView.layoutManager = LinearLayoutManager(context)
        }

        val comicsAdapter = ComicsAdapter(
            context = context,
            comics = emptyList(), // lista iniziale vuota
            mode = adapterMode,
            comicDatabase = comicDatabase,
            updateStatus = { _, _ ->
                Toast.makeText(
                    context,
                    "I fumetti non disponibili non possono essere aggiornati",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onComicClick = { comic ->
                context.startActivity(
                    Intent(context, ComicDetailActivity::class.java).apply {
                        putExtra("COMIC_ID", comic.id)
                    }
                )
            }
        )

        recyclerView.adapter = comicsAdapter // assegno SUBITO l'adapter, anche se vuoto

        FirebaseFirestore.getInstance().collection("comic")
            .get()
            .addOnSuccessListener { result ->
                val comics = result.mapNotNull { document ->
                    val id = document.getString("id") ?: document.id
                    val name = document.getString("name") ?: return@mapNotNull null
                    val imageUrl = document.getString("imageUrl") ?: ""
                    val numberField = document.get("number")
                    val number = when (numberField) {
                        is Number -> numberField.toLong()
                        is String -> numberField.toLongOrNull() ?: 0L
                        else -> 0L
                    }
                    val series = document.getString("series") ?: ""
                    val description = document.getString("description") ?: ""
                    val numericId = document.getLong("numericId")?.toString()
                        ?: document.getString("numericId") ?: ""
                    val userId = document.getString("userId")
                        ?: document.getLong("userId")?.toString() ?: "undefined"
                    val seriesNumberField = document.get("seriesNumber")
                    val seriesNumber = when (seriesNumberField) {
                        is Number -> seriesNumberField.toInt()
                        is String -> seriesNumberField.toIntOrNull() ?: 0
                        else -> 0
                    }

                    val status = try {
                        ComicStatus.valueOf(document.getString("status") ?: "DISPONIBILE")
                    } catch (_: Exception) {
                        ComicStatus.UNKOWN
                    }

                    Comic(description, id, imageUrl, name, number, numericId, series, seriesNumber, status, userId)
                }

                val sortedComics = sort(comics)
                val filteredComics = sortedComics.filter(filter)

                comicsAdapter.updateList(filteredComics) // aggiorno l'adapter esistente
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Errore di caricamento", exception)
            }
    }
}

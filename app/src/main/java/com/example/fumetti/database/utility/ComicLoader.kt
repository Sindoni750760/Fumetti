package com.example.fumetti.database.utility


import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fumetti.activity.ComicDetailActivity
import com.example.fumetti.data.Comic
import com.example.fumetti.data.ComicSorted
import com.example.fumetti.data.ComicStatus
import com.example.fumetti.database.ComicDatabase
import com.google.firebase.firestore.FirebaseFirestore

class ComicLoader(private val context: Context) {

    private val comicDatabase = ComicDatabase()

    fun loadComics(
        recyclerView: RecyclerView,
        adapterMode: ComicsAdapter.AdapterMode = ComicsAdapter.AdapterMode.PREVIEW,
        ordering: ComicSorted = ComicSorted.UNKOWN,
        status: ComicStatus = ComicStatus.UNKOWN,
        filter: (Comic) -> Boolean = { true },
        onAdapterReady: ((ComicsAdapter) -> Unit)? = null
    ) {
        if (recyclerView.layoutManager == null) {
            recyclerView.layoutManager = LinearLayoutManager(context)
        }

        val comicsAdapter = ComicsAdapter(
            context = context,
            comics = emptyList(),
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

        recyclerView.adapter = comicsAdapter

        FirebaseFirestore.getInstance().collection("comic")
            .get()
            .addOnSuccessListener { result ->
                val comics = result.mapNotNull { document ->
                    val id = document.getString("id") ?: document.id
                    val name = document.getString("name") ?: return@mapNotNull null
                    val imageUrl = document.getString("imageUrl") ?: ""
                    val number = when (val nf = document.get("number")) {
                        is Number -> nf.toLong()
                        is String -> nf.toLongOrNull() ?: 0L
                        else -> 0L
                    }
                    val series = document.getString("series") ?: ""
                    val description = document.getString("description") ?: ""
                    val numericId = document.getLong("numericId")?.toString()
                        ?: document.getString("numericId") ?: ""
                    val userId = document.getString("userId")
                        ?: document.getLong("userId")?.toString() ?: "undefined"
                    val seriesNumber = when (val sn = document.get("seriesNumber")) {
                        is Number -> sn.toInt()
                        is String -> sn.toIntOrNull() ?: 0
                        else -> 0
                    }

                    val statusEnum = try {
                        val statusStr = document.getString("status")?.uppercase() ?: "IN"
                        ComicStatus.valueOf(statusStr)
                    } catch (_: Exception) {
                        ComicStatus.UNKOWN
                    }

                    Comic(
                        description = description,
                        id = id,
                        imageUrl = imageUrl,
                        name = name,
                        number = number,
                        numericId = numericId,
                        series = series,
                        seriesNumber = seriesNumber,
                        status = statusEnum,
                        userId = userId
                    )
                }

                val sortedComics = when (ordering) {
                    ComicSorted.BY_NAME -> comics.sortedBy { it.name }
                    ComicSorted.BY_NUMBER -> comics.sortedBy { it.number }
                    ComicSorted.BY_SERIES -> comics.sortedWith(
                        compareBy<Comic> { it.series.toString() }.thenBy { it.number }
                    )
                    else -> comics
                }

                val filteredComics = sortedComics.filter {
                    filter(it) && (status == ComicStatus.UNKOWN || it.status == status)
                }

                comicsAdapter.updateList(filteredComics)
                onAdapterReady?.invoke(comicsAdapter)
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Errore di caricamento", exception)
            }
    }
}

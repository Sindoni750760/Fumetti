package com.example.fumetti.database


import com.example.fumetti.MyApplication
import com.example.fumetti.data.Comic
import com.example.fumetti.data.ComicStatus
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.google.firebase.FirebaseApp

class ComicDatabase(){

    private val firestore: FirebaseFirestore by lazy {
        val context = MyApplication.getContext()
        val appList = FirebaseApp.getApps(context)
        if (appList.isNotEmpty()) {
            FirebaseFirestore.getInstance()
        } else {
            throw IllegalStateException("FirebaseApp non è stato inizializzato correttamente.")
        }
    }

    // Ottieni tutti i fumetti
    fun getAllComics(callback: (List<Comic>) -> Unit) {
        firestore.collection("comics")
            .get()
            .addOnSuccessListener { snapshot ->
                val comics = snapshot.documents.mapNotNull { it.toObject(Comic::class.java) }
                callback(comics)
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }

    // Prenota un fumetto
    suspend fun reserveComic(comicId: String, userId: String): Boolean {
        return try {
            val comicRef = firestore.collection("comics").document(comicId)
            val snapshot = comicRef.get().await()
            val comic = snapshot.toObject(Comic::class.java) ?: return false

            if (comic.status == ComicStatus.MANCANTE) {
                comicRef.update(
                    mapOf(
                        "status" to ComicStatus.IN_PRENOTAZIONE.name,
                        "userId" to userId
                    )
                ).await()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    // Restituisci un fumetto
    suspend fun returnComic(comicId: String): Boolean {
        return try {
            val comicRef = firestore.collection("comics").document(comicId)
            comicRef.update(
                mapOf(
                    "status" to ComicStatus.MANCANTE.name,
                    "userId" to null
                )
            ).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Ottieni un singolo fumetto
    suspend fun getComic(comicId: String): Comic? {
        return try {
            firestore.collection("comics")
                .document(comicId)
                .get()
                .await()
                .toObject(Comic::class.java)
        } catch (e: Exception) {
            null
        }
    }
}

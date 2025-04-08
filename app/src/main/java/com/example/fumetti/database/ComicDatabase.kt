package com.example.fumetti.database

import com.example.fumetti.MyApplication
import com.example.fumetti.data.Comic
import com.example.fumetti.data.ComicStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.FirebaseApp

class ComicDatabase {

    private val firestore: FirebaseFirestore by lazy {
        val context = MyApplication.getContext()
        val appList = FirebaseApp.getApps(context)
        if (appList.isNotEmpty()) {
            FirebaseFirestore.getInstance()
        } else {
            throw IllegalStateException("FirebaseApp non è stato inizializzato correttamente.")
        }
    }

    // Ottieni tutti i fumetti di un utente
    fun getAllComicsByUser(userId: String? = null, callback: (List<Comic>) -> Unit) {
        firestore.collection("comics")
            .whereEqualTo("userId", userId)
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
    fun reserveComic(comicId: String, userId: String, callback: (Boolean) -> Unit) {
        firestore.collection("comics").document(comicId)
            .get()
            .addOnSuccessListener { snapshot ->
                val comic = snapshot.toObject(Comic::class.java)
                if (comic != null && comic.status == ComicStatus.MANCANTE) {
                    firestore.collection("comics").document(comicId)
                        .update(
                            mapOf(
                                "status" to ComicStatus.IN_PRENOTAZIONE.name,
                                "userId" to userId
                            )
                        )
                        .addOnSuccessListener {
                            callback(true)
                        }
                        .addOnFailureListener {
                            callback(false)
                        }
                } else {
                    callback(false)
                }
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    // Restituisci un fumetto
    fun returnComic(comicId: String, callback: (Boolean) -> Unit) {
        firestore.collection("comics").document(comicId)
            .update(
                mapOf(
                    "status" to ComicStatus.MANCANTE.name,
                    "userId" to null
                )
            )
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    // Ottieni un singolo fumetto
    fun getComic(comicId: String, callback: (Comic?) -> Unit) {
        firestore.collection("comics").document(comicId)
            .get()
            .addOnSuccessListener { snapshot ->
                val comic = snapshot.toObject(Comic::class.java)
                callback(comic)
            }
            .addOnFailureListener {
                callback(null)
            }
    }
}
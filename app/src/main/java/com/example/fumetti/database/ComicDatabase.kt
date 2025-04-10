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

    fun getAllComicsByUser(userId: String? = null, callback: (List<Comic>) -> Unit) {
        firestore.collection("comic")
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

    fun reserveComic(comicId: String, callback: (Boolean) -> Unit) {
        firestore.collection("comic")
            .document(comicId)
            .update("status", ComicStatus.IN_PRENOTAZIONE.name)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    fun returnComic(comicId: String, callback: (Boolean) -> Unit) {
        firestore.collection("comic")
            .document(comicId)
            .update(
                mapOf(
                    "status" to ComicStatus.NON_DISPONIBILE.name,
                    "userId" to null
                )
            )
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    fun addToWaitingList(comicId: String, userId: String, callback: (Boolean) -> Unit) {
        firestore.collection("waiting_list")
            .add(mapOf("comicId" to comicId, "userId" to userId))
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    fun getComic(comicId: String, callback: (Comic?) -> Unit) {
        firestore.collection("comic")
            .document(comicId)
            .get()
            .addOnSuccessListener { snapshot ->
                val comic = snapshot.toObject(Comic::class.java)
                callback(comic)
            }
            .addOnFailureListener {
                callback(null)
            }
    }
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
    fun removeComicFromUserLibrary(userId: String, comicTitle: String, onResult: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        db.collection("comic")
            .whereEqualTo("name", comicTitle)
            .limit(1)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val comicDoc = result.documents.first()

                    // 1. Rimuove dalla libreria personale
                    db.collection("users")
                        .document(userId)
                        .collection("user_library")
                        .document(comicDoc.id)
                        .delete()
                        .addOnSuccessListener {
                            // 2. Aggiorna lo status del fumetto globale
                            db.collection("comic")
                                .document(comicDoc.id)
                                .update("status", ComicStatus.DISPONIBILE.name)
                                .addOnSuccessListener { onResult(true) }
                                .addOnFailureListener { onResult(false) }
                        }
                        .addOnFailureListener { onResult(false) }
                } else {
                    onResult(false)
                }
            }
            .addOnFailureListener {
                onResult(false)
            }
    }
}

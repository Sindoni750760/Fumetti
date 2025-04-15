package com.example.fumetti.database

import android.util.Log
import com.example.fumetti.MyApplication
import com.example.fumetti.data.Comic
import com.example.fumetti.data.ComicStatus
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class ComicDatabase {

    // Inizializzazione centralizzata di Firestore utilizzando il contesto globale
    private val firestore: FirebaseFirestore by lazy {
        val context = MyApplication.getContext()
        val appList = FirebaseApp.getApps(context)
        if (appList.isNotEmpty()) {
            FirebaseFirestore.getInstance()
        } else {
            throw IllegalStateException("FirebaseApp non è stato inizializzato correttamente.")
        }
    }

    // Costanti per uniformare l'uso delle collection in Firestore
    private companion object {
        const val COLLECTION_COMIC = "comic" // Utilizziamo "comic" come nome della collection
        const val COLLECTION_WAITING_LIST = "waiting_list"
        const val COLLECTION_USERS = "users"
        const val COLLECTION_USER_LIBRARY = "user_library"
        const val TAG = "ComicDatabase"
    }

    fun getAllComicsByUser(userId: String? = null, callback: (List<Comic>) -> Unit) {
        firestore.collection(COLLECTION_COMIC)
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                val comics = snapshot.documents.mapNotNull { it.toObject(Comic::class.java) }
                callback(comics)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Errore nel recupero dei fumetti per utente (userId: $userId): ${exception.message}", exception)
                callback(emptyList())
            }
    }

    fun reserveComic(comicId: String, callback: (Boolean) -> Unit) {
        firestore.collection(COLLECTION_COMIC)
            .document(comicId)
            .update("status", ComicStatus.IN_PRENOTAZIONE.name)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Errore nella prenotazione del fumetto (comicId: $comicId): ${exception.message}", exception)
                callback(false)
            }
    }

    fun returnComic(comicId: String, callback: (Boolean) -> Unit) {
        firestore.collection(COLLECTION_COMIC)
            .document(comicId)
            .update(
                mapOf(
                    "status" to ComicStatus.NON_DISPONIBILE.name,
                    "userId" to null
                )
            )
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Errore nella restituzione del fumetto (comicId: $comicId): ${exception.message}", exception)
                callback(false)
            }
    }

    fun addToWaitingList(comicId: String, userId: String, callback: (Boolean) -> Unit) {
        firestore.collection(COLLECTION_WAITING_LIST)
            .add(mapOf("comicId" to comicId, "userId" to userId))
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Errore nell'aggiunta alla lista d'attesa (comicId: $comicId, userId: $userId): ${exception.message}", exception)
                callback(false)
            }
    }

    fun getComic(comicId: String, callback: (Comic?) -> Unit) {
        firestore.collection(COLLECTION_COMIC)
            .document(comicId)
            .get()
            .addOnSuccessListener { snapshot ->
                val comic = snapshot.toObject(Comic::class.java)
                callback(comic)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Errore nel recupero del fumetto (comicId: $comicId): ${exception.message}", exception)
                callback(null)
            }
    }

    fun getAllComics(callback: (List<Comic>) -> Unit) {
        firestore.collection(COLLECTION_COMIC)
            .get()
            .addOnSuccessListener { snapshot ->
                val comics = snapshot.documents.mapNotNull { it.toObject(Comic::class.java) }
                callback(comics)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Errore nel recupero di tutti i fumetti: ${exception.message}", exception)
                callback(emptyList())
            }
    }

    fun removeComicFromUserLibrary(userId: String, comicTitle: String, onResult: (Boolean) -> Unit) {
        firestore.collection(COLLECTION_COMIC)
            .whereEqualTo("name", comicTitle)
            .limit(1)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val comicDoc = result.documents.first()

                    // 1. Rimuove dalla libreria personale
                    firestore.collection(COLLECTION_USERS)
                        .document(userId)
                        .collection(COLLECTION_USER_LIBRARY)
                        .document(comicDoc.id)
                        .delete()
                        .addOnSuccessListener {
                            // 2. Aggiorna lo status del fumetto globale
                            firestore.collection(COLLECTION_COMIC)
                                .document(comicDoc.id)
                                .update("status", ComicStatus.DISPONIBILE.name)
                                .addOnSuccessListener { onResult(true) }
                                .addOnFailureListener { exception ->
                                    Log.e(TAG, "Errore nell'aggiornamento dello status del fumetto globale (comicDoc.id: ${comicDoc.id}): ${exception.message}", exception)
                                    onResult(false)
                                }
                        }
                        .addOnFailureListener { exception ->
                            Log.e(TAG, "Errore nella rimozione del fumetto dalla libreria personale (userId: $userId, comicDoc.id: ${comicDoc.id}): ${exception.message}", exception)
                            onResult(false)
                        }
                } else {
                    Log.e(TAG, "Nessun fumetto trovato con il titolo: $comicTitle")
                    onResult(false)
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Errore nella ricerca del fumetto per rimuovere dalla libreria personale (comicTitle: $comicTitle): ${exception.message}", exception)
                onResult(false)
            }
    }
}

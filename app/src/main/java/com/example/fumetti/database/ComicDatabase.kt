package com.example.fumetti.database

import android.util.Log
import com.example.fumetti.MyApplication
import com.example.fumetti.data.ComicStatus
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

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

    private companion object {
        const val COLLECTION_COMIC = "comic" // Utilizziamo "comic" come nome della collection
        const val COLLECTION_WAITING_LIST = "waiting_list"
        const val COLLECTION_USERS = "users"
        const val COLLECTION_USER_LIBRARY = "user_library"
        const val TAG = "ComicDatabase"
    }

    fun reserveComic(comicId: String,userId: String, callback: (Boolean) -> Unit) {
        firestore.collection(COLLECTION_COMIC)
            .document(comicId)
            .update(
                mapOf(
                    "userId" to userId,
                    "status" to ComicStatus.TAKEN.name
                )
            )
            .addOnSuccessListener{callback(true)}
            .addOnFailureListener{exception ->
                Log.e(TAG, "Errore nella prenotazione del fumetto (comicId: $comicId): ${exception.message}", exception)
                callback(false)
            }
    }

    fun returnComic(comicId: String, callback: (Boolean) -> Unit) {
        firestore.collection(COLLECTION_COMIC)
            .document(comicId)
            .update(
                mapOf(
                    "userId" to FieldValue.delete(),
                    "status" to ComicStatus.IN.name
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

    fun updateComicStatus(comicId: String, newStatus: ComicStatus, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val comicRef = db.collection("comic").document(comicId)

        // Update the "status" field in the database
        comicRef.update("status", newStatus.name)
            .addOnSuccessListener {
                Log.d("ComicDatabase", "Status of comic $comicId updated to $newStatus")
                onSuccess() // Invoke the success callback
            }
            .addOnFailureListener { exception ->
                Log.e("ComicDatabase", "Failed to update status of comic $comicId: ${exception.message}", exception)
                onFailure(exception) // Invoke the failure callback
            }
    }
}

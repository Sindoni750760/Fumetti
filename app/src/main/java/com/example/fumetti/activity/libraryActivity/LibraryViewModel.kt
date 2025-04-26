package com.example.fumetti.activity.libraryActivity
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fumetti.data.Comic
import com.example.fumetti.data.ComicStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LibraryViewModel : ViewModel() {

    private val _comicsOut = MutableLiveData<List<Comic>>()

    private val _comicsAvailable = MutableLiveData<List<Comic>>()

    private val _comicsUnavailable = MutableLiveData<List<Comic>>()

    fun loadComics() {
        val db = FirebaseFirestore.getInstance()
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        db.collection("comic")
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
                    } catch (e: Exception) {
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
                        status = status,
                        userId = userId
                    )
                }

                _comicsOut.value = comics.filter {
                    it.status == ComicStatus.TAKEN && it.userId == currentUserId
                }.sortedBy { it.id }

                _comicsAvailable.value = comics.filter {
                    it.status == ComicStatus.IN
                }.sortedBy { it.id }

                _comicsUnavailable.value = comics.filter {
                    it.status == ComicStatus.OUT
                }.sortedBy { it.id }
            }
            .addOnFailureListener { exception ->
                Log.e("LibraryViewModel", "Errore nel caricamento dei fumetti: ${exception.message}", exception)
            }
    }
}


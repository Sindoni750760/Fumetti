package com.example.fumetti.activity.libraryActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fumetti.data.Comic
import com.example.fumetti.data.ComicStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LibraryViewModel : ViewModel() {

    private val _comicsOut = MutableLiveData<List<Comic>>()
    val comicsOut: LiveData<List<Comic>> get() = _comicsOut

    private val _comicsAvailable = MutableLiveData<List<Comic>>()
    val comicsAvailable: LiveData<List<Comic>> get() = _comicsAvailable

    private val _comicsUnavailable = MutableLiveData<List<Comic>>()
    val comicsUnavailable: LiveData<List<Comic>> get() = _comicsUnavailable

    fun loadComics() {
        val db = FirebaseFirestore.getInstance()
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        db.collection("comic")
            .get()
            .addOnSuccessListener { result ->
                val comics = result.map { document ->
                    val id = document.getString("id") ?: document.id
                    val name = document.getString("name") ?: ""
                    val imageUrl = document.getString("imageUrl") ?: ""
                    val number = document.getLong("number")?.toInt()
                        ?: (document.getString("number")?.toIntOrNull() ?: 0)
                    val series = document.getString("series") ?: " "
                    val description = document.getString("description") ?: " "
                    val numericId = document.getLong("numericId")?.toString()
                        ?: document.getString("numericId") ?: " "
                    val userIdFromDb = document.getString("userId")
                        ?: document.getLong("userId")?.toString() ?: "undefined"
                    val status = try {
                        ComicStatus.valueOf(document.getString("status") ?: "DISPONIBILE")
                    } catch (e: Exception) {
                        ComicStatus.UNKOWN
                    }
                    val seriesNumber = document.getLong("seriesNumber")?.toInt()
                        ?: (document.getString("seriesNumber")?.toIntOrNull() ?: 0)

                    Comic(
                        description, id, imageUrl, name, number, numericId,
                        series, seriesNumber, status, userIdFromDb
                    )
                }

                _comicsOut.value = comics.filter {
                    it.status == ComicStatus.IN_PRENOTAZIONE && it.userId == currentUserId
                }
                _comicsAvailable.value = comics.filter { it.status == ComicStatus.DISPONIBILE }
                _comicsUnavailable.value = comics.filter { it.status == ComicStatus.NON_DISPONIBILE }
            }
    }
}


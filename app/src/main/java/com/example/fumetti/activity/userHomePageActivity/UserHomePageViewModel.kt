package com.example.fumetti.activity.userHomePageActivity

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fumetti.data.Comic
import com.example.fumetti.data.ComicStatus
import com.google.firebase.firestore.FirebaseFirestore

class UserHomePageViewModel : ViewModel() {

    private val _allComics = MutableLiveData<List<Comic>>()

    fun loadComics() {
        val db = FirebaseFirestore.getInstance()

        db.collection("comic")
            .get()
            .addOnSuccessListener { result ->
                val comics = result.mapNotNull { document ->
                    val id = document.getString("id") ?: document.id
                    val name = document.getString("name") ?: return@mapNotNull null
                    val imageUrl = document.getString("imageUrl") ?: ""
                    val number = (document.get("number") as? Number)?.toLong()
                        ?: (document.getString("number")?.toLongOrNull() ?: 0L)
                    val series = document.getString("series") ?: ""
                    val description = document.getString("description") ?: ""
                    val numericId = document.getLong("numericId")?.toString()
                        ?: document.getString("numericId") ?: ""
                    val userId = document.getString("userId")
                        ?: document.getLong("userId")?.toString() ?: "undefined"
                    val seriesNumber = (document.get("seriesNumber") as? Number)?.toInt()
                        ?: (document.getString("seriesNumber")?.toIntOrNull() ?: 0)
                    val status = try {
                        ComicStatus.valueOf(document.getString("status") ?: "DISPONIBILE")
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
                        status = status,
                        userId = userId
                    )
                }

                _allComics.value = comics
            }
            .addOnFailureListener {
                Log.e("UserHomePageViewModel", "Errore caricamento fumetti", it)
            }
    }
}

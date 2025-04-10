package com.example.fumetti.database

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fumetti.R
import com.example.fumetti.data.Comic
import com.example.fumetti.database.ComicDatabase
import com.google.firebase.auth.FirebaseAuth

class AddComics : AppCompatActivity() {

    private val comicDatabase = ComicDatabase()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_comics)

        val spinnerComics = findViewById<Spinner>(R.id.spinnerComics)
        val buttonAddComic = findViewById<Button>(R.id.buttonAddComic)

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "defaultUser"

        comicDatabase.getAllComicsByUser(userId) { comics: List<Comic> ->
            if (comics.isEmpty()) {
                Toast.makeText(this, "Nessun fumetto disponibile", Toast.LENGTH_SHORT).show()
                return@getAllComicsByUser
            }

            val comicTitles = comics.map { it.name }
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, comicTitles)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            runOnUiThread {
                spinnerComics.adapter = adapter
            }
        }

        buttonAddComic.setOnClickListener {
            val selectedItem = spinnerComics.selectedItem
            if (selectedItem != null) {
                val selectedComicTitle = selectedItem.toString()
                addComicToLibrary(selectedComicTitle)
            } else {
                Toast.makeText(this, "Seleziona un fumetto!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addComicToLibrary(comicTitle: String) {
        // Qui va la logica vera per aggiungerlo a Firestore se vuoi
        Toast.makeText(this, "$comicTitle aggiunto alla tua libreria", Toast.LENGTH_SHORT).show()
    }
}

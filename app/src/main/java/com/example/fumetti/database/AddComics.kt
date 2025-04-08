package com.example.fumetti.database

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fumetti.R
import com.example.fumetti.data.Comic

class AddComics : AppCompatActivity() {

    private val comicDatabase = ComicDatabase()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_comics)

        val spinnerComics = findViewById<Spinner>(R.id.spinnerComics)
        val buttonAddComic = findViewById<Button>(R.id.buttonAddComic)

        // Carica i fumetti disponibili nel database nel Spinner
        val userId = "USER_ID" // Sostituisci con l'ID utente corretto
        comicDatabase.getAllComicsByUser(userId) { comics: List<Comic> ->
            val comicTitles = comics.map { it.name }
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, comicTitles)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerComics.adapter = adapter
        }

        buttonAddComic.setOnClickListener {
            val selectedComicTitle = spinnerComics.selectedItem as String
            addComicToLibrary(selectedComicTitle)
        }
    }

    private fun addComicToLibrary(comicTitle: String) {
        // Logica per aggiungere il fumetto alla libreria personale
        Toast.makeText(this, "$comicTitle aggiunto alla tua libreria", Toast.LENGTH_SHORT).show()
    }
}
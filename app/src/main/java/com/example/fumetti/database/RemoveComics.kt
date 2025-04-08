package com.example.fumetti.database

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fumetti.R
import com.example.fumetti.data.Comic
import com.example.fumetti.data.ComicStatus

class RemoveComics : AppCompatActivity() {

    private val comicDatabase = ComicDatabase()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remove_comics)

        val spinnerComics = findViewById<Spinner>(R.id.spinnerComics)
        val buttonRemoveComic = findViewById<Button>(R.id.buttonRemoveComic)

        // Carica i fumetti presenti nella libreria personale nel Spinner
        val userId = "USER_ID" // Sostituisci con l'ID utente corretto
        comicDatabase.getAllComicsByUser(userId) { comics: List<Comic> ->
            val comicTitles = comics.filter { it.status == ComicStatus.IN_PRENOTAZIONE }.map { it.name }
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, comicTitles)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerComics.adapter = adapter
        }

        buttonRemoveComic.setOnClickListener {
            val selectedComicTitle = spinnerComics.selectedItem as String
            removeComicFromLibrary(selectedComicTitle)
        }
    }

    private fun removeComicFromLibrary(comicTitle: String) {
        // Logica per rimuovere il fumetto dalla libreria personale e rimetterlo disponibile
        comicDatabase.getAllComicsByUser { comics: List<Comic> ->
            val comic = comics.find { it.name == comicTitle }
            comic?.let {
                comicDatabase.returnComic(it.name) { success ->
                    if (success) {
                        Toast.makeText(this, "$comicTitle rimosso dalla tua libreria e reso disponibile", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Errore durante la rimozione del fumetto", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
package com.example.fumetti.activity

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.fumetti.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Intent
import android.widget.ImageButton

class UserHomePageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_homepage)

        val buttonToSearch = findViewById<ImageButton>(R.id.profileIcon)
        buttonToSearch.setOnClickListener{
            val intent = Intent(this, UserProfileActivity::class.java)
            startActivity(intent)
        }

        val buttonToLibrary = findViewById<Button>(R.id.buttonToLibrary)
        buttonToLibrary.setOnClickListener{
            val intent = Intent(this, Library::class.java)
            startActivity(intent)
            finish()
        }

        val buttonToMyLibrary = findViewById<Button>(R.id.buttonToMyLibrary)
        buttonToMyLibrary.setOnClickListener{
            val intent = Intent(this, MyLibrary::class.java)
            startActivity(intent)
            finish()
        }

        val userCollection = "user_${getUserId()}"
        loadData(userCollection)
    }

    private fun loadData(collectionName: String) {
        if (collectionName.isBlank()) {
            Log.e("FirestoreError", "Il nome della collezione non può essere vuoto.")
            return
        }

        val db = FirebaseFirestore.getInstance()
        db.collection(collectionName)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d("FirestoreData", "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Errore di caricamento", exception)
            }
    }

    private fun saveData(collectionName: String, data: Map<String, Any>) {
        val db = FirebaseFirestore.getInstance()
        db.collection(collectionName)
            .add(data)
            .addOnSuccessListener { documentReference ->
                Log.d("FirestoreSuccess", "Documento aggiunto con ID: ${documentReference.id}")
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Errore di salvataggio", exception)
            }
    }

    private fun getUserId(): String {
        // Simulazione recupero ID utente (da FirebaseAuth, SharedPreferences o altro)
        return FirebaseAuth.getInstance().currentUser?.uid ?: "defaultUser"
    }
}
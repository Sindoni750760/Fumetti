package com.example.fumetti.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fumetti.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserHomePageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_homepage)

        val buttonToSearch = findViewById<ImageButton>(R.id.profileIcon)
        buttonToSearch.setOnClickListener {
            val intent = Intent(this, UserProfileActivity::class.java)
            startActivity(intent)
        }

        val buttonToLibrary = findViewById<Button>(R.id.buttonToLibrary)
        buttonToLibrary.setOnClickListener {
            val intent = Intent(this, Library::class.java)
            startActivity(intent)
            finish()
        }

        val buttonToMyLibrary = findViewById<Button>(R.id.buttonToMyLibrary)
        buttonToMyLibrary.setOnClickListener {
            val intent = Intent(this, MyLibrary::class.java)
            startActivity(intent)
            finish()
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = MyAdapter(emptyList()) // Imposta un adapter vuoto inizialmente

        val recyclerViewNames = findViewById<RecyclerView>(R.id.recyclerViewNames)
        recyclerViewNames.layoutManager = LinearLayoutManager(this)
        recyclerViewNames.adapter = MyAdapter(emptyList()) // Imposta un adapter vuoto inizialmente

        val recyclerViewSeriesNumbers = findViewById<RecyclerView>(R.id.recyclerViewSeriesNumbers)
        recyclerViewSeriesNumbers.layoutManager = LinearLayoutManager(this)
        recyclerViewSeriesNumbers.adapter = MyAdapter(emptyList()) // Imposta un adapter vuoto inizialmente

        val userCollection = "user_${getUserId()}"
        loadData(userCollection, recyclerView, recyclerViewNames, recyclerViewSeriesNumbers)
    }

    private fun loadData(collectionName: String, recyclerView: RecyclerView, recyclerViewNames: RecyclerView, recyclerViewSeriesNumbers: RecyclerView) {
        if (collectionName.isBlank()) {
            Log.e("FirestoreError", "Il nome della collezione non può essere vuoto.")
            return
        }

        val db = FirebaseFirestore.getInstance()
        db.collection(collectionName)
            .get()
            .addOnSuccessListener { result ->
                val data = result.map { document -> document.data }.take(5) // Limita a 5 elementi
                recyclerView.adapter = MyAdapter(data)
                recyclerView.adapter?.notifyDataSetChanged()

                recyclerViewNames.adapter = MyAdapter(data)
                recyclerViewNames.adapter?.notifyDataSetChanged()

                recyclerViewSeriesNumbers.adapter = MyAdapter(data)
                recyclerViewSeriesNumbers.adapter?.notifyDataSetChanged()

                for (document in result) {
                    Log.d("FirestoreData", "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreError", "Errore di caricamento", exception)
            }
    }

    private fun getUserId(): String {
        // Simulazione recupero ID utente (da FirebaseAuth, SharedPreferences o altro)
        return FirebaseAuth.getInstance().currentUser?.uid ?: "defaultUser"
    }

    // Classe MyAdapter incorporata
    inner class MyAdapter(private val dataList: List<Map<String, Any>>) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val textView: TextView = view.findViewById(R.id.textView)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_layout, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = dataList[position]
            holder.textView.text = item["key"].toString() // Sostituisci "key" con la chiave appropriata
        }

        override fun getItemCount() = dataList.size
    }
}
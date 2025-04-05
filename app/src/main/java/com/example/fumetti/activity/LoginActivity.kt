package com.example.fumetti.activity

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.UnderlineSpan
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fumetti.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerText = findViewById<TextView>(R.id.registerText)

        val registerContent = SpannableString("Non hai un account? Registrati qui")
        registerContent.setSpan(UnderlineSpan(), registerContent.length - 13, registerContent.length, 0)
        registerText.text = registerContent
        registerText.movementMethod = LinkMovementMethod.getInstance()

        registerText.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Tutti i campi non sono compilati", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                loginUser(email, password)
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val collectionName = "user_${getUserId()}"
                    val data = mapOf("key" to "value")
                    saveData(collectionName, data)
                    getUserName(email)
                } else {
                    Toast.makeText(this, "Credenziali errate", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun getUserName(email: String) {
        firestore.collection("users").whereEqualTo("email", email).get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val name = documents.documents[0].getString("name")
                    Toast.makeText(this, "Benvenuto $name!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, UserHomePageActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Utente non trovato", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Errore nel recupero del nome utente", Toast.LENGTH_SHORT).show()
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
        return FirebaseAuth.getInstance().currentUser?.uid ?: "defaultUser"
    }
}
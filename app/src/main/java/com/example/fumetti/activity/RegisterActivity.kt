package com.example.fumetti.activity


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fumetti.R
import com.example.fumetti.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val nameEditText = findViewById<EditText>(R.id.nameEditText)
        val surnameEditText = findViewById<EditText>(R.id.surnameEditText)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val registerButton = findViewById<Button>(R.id.button_register)

        // Blocca inserimento newline
        listOf(nameEditText, surnameEditText, emailEditText, passwordEditText).forEach { editText ->
            editText.setOnKeyListener { _, keyCode, _ ->
                keyCode == KeyEvent.KEYCODE_ENTER
            }
        }

        registerButton.setOnClickListener {
            val name = sanitizeInput(nameEditText.text.toString().trim())
            val surname = sanitizeInput(surnameEditText.text.toString().trim())
            val email = sanitizeInput(emailEditText.text.toString().trim())
            val password = sanitizeInput(passwordEditText.text.toString().trim())

            if (name.isEmpty() || surname.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Dati mancanti, inserisci tutti i campi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!checkEmail(email)) {
                Toast.makeText(this, "Email non valida. Inserisci un'email corretta.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!checkPassword(password)) {
                Toast.makeText(
                    this,
                    "Password non sicura. Deve contenere almeno:\n- 1 lettera maiuscola\n- 1 lettera minuscola\n- 1 carattere speciale",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            registerUser(email, password, name, surname)
        }
    }

    private fun registerUser(email: String, password: String, name: String, surname: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val userId = auth.currentUser?.uid ?: return@addOnSuccessListener

                val newUser = User(
                    id = userId,
                    name = name,
                    surname = surname,
                    email = email,
                    password = "" // Non salvare la password in chiaro!
                )
                saveUserToDatabase(newUser)
                startActivity(Intent(this, UserHomePageActivity::class.java))
                finish()
            }
            .addOnFailureListener { exception ->
                Log.e("RegisterActivity", "Registrazione fallita", exception)
                Toast.makeText(this, "Registrazione fallita: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUserToDatabase(user: User) {
        firestore.collection("users").document(user.id)
            .set(user)
            .addOnSuccessListener {
                Toast.makeText(this, "Registrazione completata!", Toast.LENGTH_SHORT).show()
                goToUserHomePage()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Errore nel salvataggio dei dati", Toast.LENGTH_SHORT).show()
            }
    }

    private fun goToUserHomePage() {
        startActivity(Intent(this, UserHomePageActivity::class.java))
        finish()
    }

    private fun sanitizeInput(input: String): String {
        return input.replace("\n", "").replace("\r", "")
    }

    private fun checkEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun checkPassword(password: String): Boolean {
        val hasLowercase = password.any { it.isLowerCase() }
        val hasUppercase = password.any { it.isUpperCase() }
        val hasSpecialChar = password.any { !it.isLetterOrDigit() }
        return hasUppercase && hasLowercase && hasSpecialChar
    }
}

package com.example.fumetti.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.fumetti.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserProfileActivity: AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        val userName = findViewById<TextView>(R.id.userName)
        val userEmail = findViewById<TextView>(R.id.userEmail)
        val logOutButton = findViewById<Button>(R.id.logoutButton)
        findViewById<ImageView>(R.id.profileIcon)
        val mancoList = findViewById<Button>(R.id.mancoListButton)

        val user = FirebaseAuth.getInstance().currentUser

        if(user != null){
            userEmail.text = user.email
            fetchUserName(user.uid, userName)

        }

        logOutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, HomePageActivity::class.java))
            finish()
        }
        mancoList.setOnClickListener{
            startActivity(Intent(this, MissingListActivity::class.java))
            finish()
        }
    }
    private fun fetchUserName(userId: String, userNameTextView: TextView) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    userNameTextView.text = document.getString("name") ?: "Nome non disponibile"
                }
            }
    }
}
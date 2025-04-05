package com.example.fumetti.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.fumetti.R

class HomePageActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homepage)
        //Button che, richiamato, entra nella schermata del login
        val loginButton = findViewById<Button>(R.id.loginButton)
        loginButton.setOnClickListener{
            startActivity(Intent(this,LoginActivity::class.java))
            finish()
        }
        val registerButton = findViewById<Button>(R.id.registerButton)
        registerButton.setOnClickListener{
            startActivity(Intent(this,RegisterActivity::class.java))
            finish()
        }
    }
}
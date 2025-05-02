package com.example.fumetti


import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.fumetti.activity.HomePageActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Start UserHomePageActivity
        startActivity(Intent(this, HomePageActivity::class.java))
        finish()
    }
}
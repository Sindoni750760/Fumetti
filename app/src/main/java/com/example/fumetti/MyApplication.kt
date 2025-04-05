package com.example.fumetti

import android.app.Application
import com.google.firebase.FirebaseApp

class MyApplication: Application(){
    companion object{
        private lateinit var instance: MyApplication
        fun getContext() = instance.applicationContext
    }
    override fun onCreate(){
        super.onCreate()
        instance = this
        FirebaseApp.initializeApp(this)
    }
}
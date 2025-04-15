package com.example.fumetti.data

import androidx.room.PrimaryKey
import androidx.room.Entity


@Entity(
    tableName = "users"
)

data class User(
    @PrimaryKey(autoGenerate = true)
    val email: String,
    val id: String,
    val name: String,
    val password: String,
    val surname: String,
)
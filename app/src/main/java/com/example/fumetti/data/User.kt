package com.example.fumetti.data

import androidx.room.PrimaryKey
import androidx.room.Entity
import androidx.room.ForeignKey
import com.google.firebase.firestore.auth.User


@Entity(
    tableName = "users",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )]
)

data class User(@PrimaryKey(autoGenerate = true)
                val id: String,
                val name: String,
                val surname: String,
                val email: String,
                val password: String
)
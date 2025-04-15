package com.example.fumetti.data

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "comics",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)

data class Comic(
    val description: String,
    val id: String,
    val imageUrl: String,
    val name: String,
    val number: Int? = 0,
    val numericId: String,
    val series: String? = "",
    val seriesNumber: Int = 0,
    var status: ComicStatus,
    var userId: String? = null,
)

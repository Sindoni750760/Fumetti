package com.example.fumetti.data

data class Comic(
    val id: Int = 0,
    val name: String = "",
    val series: String? = "",
    val number: String? = "",
    val description: String = "",
    val imageUrl: String = "",
    val userId: Int,
    var status: ComicStatus
)
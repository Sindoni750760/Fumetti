package com.example.fumetti.data

data class Comic(
    var id: String = "",
    var name: String = "",
    var imageUrl: String = "",
    var number: Int? = null,
    val series: String = "",
    var description: String = "",
    var status: ComicStatus = ComicStatus.DISPONIBILE,
    var userId: Int?= null,
)
